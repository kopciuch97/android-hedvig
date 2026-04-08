# Solution Exploration: Insurance Summary Card on Home Screen

## Problem Reframing

### Research Question
What feature can be implemented in the Hedvig Android codebase to best demonstrate AI development flow advantages for a sales pitch?

### How Might We Questions
1. **HMW display insurance summary data on the Home screen** without duplicating data-fetching logic or violating the module dependency rules?
2. **HMW choose the right UI complexity** that looks impressive in a demo without adding unreasonable implementation scope?
3. **HMW structure the code** so the demo covers the full stack (GraphQL, data, presenter, UI, DI, tests) while staying implementable in a short session?
4. **HMW integrate with the existing Home screen layout** (custom `HomeLayout` composable) without breaking the carefully designed centering/scroll behavior?
5. **HMW source payment and contract data** given that the Home module currently has no access to premium or payment date information?

---

## Explored Alternatives

### Decision Area 1: Data Source Strategy

#### Alternative 1A: Extend the Existing HomeQuery GraphQL Query

**Description**: Add `premium`, `productVariant`, and cost-related fields to the existing `QueryHome.graphql` `activeContracts` selection set. Add `futureCharge { date }` to get the next payment date. All data flows through the existing `GetHomeDataUseCase` and `HomeData` model.

**Strengths**:
- Single network request -- no additional latency or cache coordination
- Follows the existing pattern exactly: the `HomeQuery` already fetches `activeContracts { masterInceptionDate }`, so adding fields is a natural extension
- The entire data pipeline (Apollo query -> UseCase -> Presenter -> UiState) is already wired; we just expand it
- Minimizes new files -- most changes are additions to existing classes

**Weaknesses**:
- Makes the already-large `HomeQuery` even larger (it currently combines with 7 flows)
- The `activeContracts` selection set in `QueryHome.graphql` is minimal today (only `masterInceptionDate`); adding premium/cost fields changes its character
- If the schema fields for `premium` or `futureCharge` require additional backend computation, it could slow the overall Home query

**Best when**: The demo prioritizes showing changes that ripple through every layer (GraphQL -> data model -> presenter -> UI) with minimal new files, which is ideal for demonstrating AI workflow.

**Evidence links**: The existing `QueryHome.graphql` already queries `activeContracts` (line 59-61). The `InsuranceContracts` query in feature-insurances shows the exact GraphQL fields available: `premium { ...MoneyFragment }`, `productVariant`, and `cost { ...MonthlyCostFragment }`. The `UpcomingPayment` query shows `futureCharge { date, net { ...MoneyFragment } }` is available on `currentMember`.

---

#### Alternative 1B: Create a Separate Dedicated GraphQL Query

**Description**: Create a new `QueryInsuranceSummary.graphql` file in feature-home that fetches only the fields needed for the summary card: active contract count, premiums, and next charge date. Wire it through a new `GetInsuranceSummaryUseCase` that runs in parallel with `GetHomeDataUseCase`.

**Strengths**:
- Clean separation of concerns -- the summary card data is independent of other home data
- Can be cached independently, so card failures do not break the rest of the home screen
- The new query is self-documenting about what the feature needs

**Weaknesses**:
- Adds an extra network call on every Home screen load
- Requires a new `combine` flow in the Presenter (the 7-flow combine in `GetHomeDataUseCase` is already at the custom-combine limit)
- More files to create: new query, new use case, new use case interface, new DI registration
- The Presenter already manages complex loading state; adding a second async data source increases complexity

**Best when**: The feature were going into production and long-term maintainability matters more than demo speed.

**Evidence links**: The project already has separate queries per concern (e.g., `QueryUnreadMessageCount.graphql` runs independently). The custom 7-arity `combine` function in `GetHomeDataUseCase.kt` (lines 326-346) shows the pattern but also shows the strain of adding more flows.

---

#### Alternative 1C: Reuse Data from Existing Data Modules via Use Cases

**Description**: Instead of querying GraphQL directly, inject existing use cases or repositories from `data-contract` or the payments module to get contract and payment data. Compose the summary from data that is already being fetched elsewhere.

**Strengths**:
- No new GraphQL queries at all
- Leverages existing, tested data layer code

**Weaknesses**:
- Feature modules cannot depend on other feature modules (enforced at build time). The payment data lives in `feature-payments`, not in a shared `data-*` module
- The `data-contract` public module exposes models but may not expose the specific use cases needed (premium, next payment date)
- Would require creating new shared data modules to expose payment data, which is far more work than extending a query
- Couples the summary card to the loading state and cache timing of other modules

**Best when**: The project had a shared `data-payments-public` module with the needed interfaces already exposed. It does not currently.

**Evidence links**: The `data-contract` module exists but its public API (`CrossSell`, `ImageAsset`) does not include premium or payment data. The `feature-payments` module owns the `UpcomingPayment` query. The build plugin enforces feature-to-feature dependency prohibition (`CLAUDE.md`: "Feature modules CANNOT depend on other feature modules").

---

### Decision Area 2: Module Placement

#### Alternative 2A: Directly in feature-home (Recommended)

**Description**: All new code (summary card composable, expanded data model, presenter changes, tests) lives within the existing `feature-home` module. The card is a composable function in the `ui/` package, the data model extensions are in the `data/` package.

**Strengths**:
- Zero new modules to create -- fastest path to a working demo
- The summary card is semantically a home screen concern: it shows at-a-glance insurance status
- Follows the existing pattern: other home-screen-specific UI (claim status cards, member reminders, VIM cards) all live in feature-home
- The HomeLayout already has a custom layout system for placing content sections

**Weaknesses**:
- If the summary card were needed on other screens later, it would need to be extracted
- Makes the feature-home module slightly larger

**Best when**: The card is exclusively a Home screen feature and the goal is a fast, convention-following demo.

**Evidence links**: All current Home screen sections (claims cards, VIM, member reminders, cross-sells) are implemented directly in `feature-home/home/ui/`. The `HomeLayout` composable (lines 44-55) explicitly defines slots for each section. The `HomeDestination` wires them together.

---

#### Alternative 2B: New Shared UI Component Module

**Description**: Create a new `ui-insurance-summary` module containing the summary card composable and its data model. Feature-home would depend on this module.

**Strengths**:
- Reusable if other screens (e.g., profile, payments) want to show a similar card
- Clean module boundary

**Weaknesses**:
- Adds module creation overhead (build.gradle.kts, package structure, DI wiring)
- Over-engineering for a demo feature that lives on one screen
- The card needs home-specific data (from HomeQuery), so the "shared" module would still need data to be passed in from feature-home
- No existing precedent for single-card UI modules in this codebase

**Best when**: The card is planned for multiple screens across the app.

**Evidence links**: Existing shared UI modules (`ui-emergency`, `claim-status`) contain components used by multiple features. The insurance summary card has no current multi-screen requirement.

---

#### Alternative 2C: Split Between New data-insurance-summary and feature-home

**Description**: Create a `data-insurance-summary-public` module with the data model and use case interface, then implement it in feature-home or a `data-insurance-summary` module.

**Strengths**:
- Clean data layer separation
- The use case could be tested independently

**Weaknesses**:
- Two new modules for what amounts to 3-4 fields added to an existing query
- Significantly more demo time spent on module scaffolding vs. actual feature code
- The data is a subset of what `HomeQuery` already fetches (contract count) plus a few new fields

**Best when**: This were a production feature with complex business logic warranting its own data module.

**Evidence links**: The existing `data-addons`, `data-contract`, `data-conversations` modules exist because they serve multiple features. The insurance summary data is consumed only by the Home screen.

---

### Decision Area 3: UI Complexity Level

#### Alternative 3A: Well-Designed Static Card with Key Metrics

**Description**: A single `HedvigCard` (or `Surface`) containing three key data points in a clean layout: (1) number of active policies with a label, (2) total monthly cost formatted with currency, (3) next payment date. Uses the project's design system typography, colors, and spacing. Includes a "View Details" text button that navigates to the Insurances tab.

**Strengths**:
- Fastest to implement -- pure data display with design system components
- Easy to test (presenter test verifies data mapping; UI is stateless)
- Visually clean and professional -- the Hedvig design system handles the polish
- Demonstrates the full stack without UI complexity dominating the demo
- The static nature means fewer edge cases (loading states, animation timing)

**Weaknesses**:
- Less "wow factor" than animated alternatives
- Might look too simple in isolation (though the design system cards look polished)

**Best when**: The demo emphasis is on AI workflow speed and code quality rather than UI animation prowess.

**Evidence links**: The existing Home screen uses `HedvigNotificationCard` and similar design system components for its cards. The `HedvigTheme` provides consistent spacing (16dp padding pattern), typography, and color tokens.

---

#### Alternative 3B: Interactive Card with Expandable Contract List

**Description**: The card shows summary metrics in collapsed state. Tapping expands it to show individual contract names with their premiums. Uses `AnimatedVisibility` or `AnimatedContent` for the expand/collapse transition.

**Strengths**:
- More visually engaging -- the expand animation adds interactivity
- Shows more data without cluttering the collapsed view
- Demonstrates Compose animation capabilities

**Weaknesses**:
- Requires additional state management (expanded/collapsed) in the presenter
- Needs per-contract data (name, premium) which means more GraphQL fields
- The expand/collapse interaction needs to work well within the `HomeLayout` custom layout, which uses fixed-size placeables and custom centering logic -- animated height changes could cause layout issues
- More test surface area (expanded state, collapsed state, transition)
- Risk of the demo going over time

**Best when**: The demo audience is specifically interested in UI/animation capabilities.

**Evidence links**: The `HomeLayout` (lines 56-156) uses a custom `Layout` composable with pre-measured placeables. Dynamically changing heights (from expand/collapse) would need careful integration with the centering algorithm (lines 129-155).

---

#### Alternative 3C: Animated Card with Progress Ring and Transitions

**Description**: The card features a circular progress indicator showing "coverage level" or payment progress, with number-counting animations on the cost display, and a subtle shimmer loading state. Entry animation slides the card in from below.

**Strengths**:
- Maximum visual impact for a demo
- Shows Compose's animation capabilities (Canvas drawing, animated values, transitions)

**Weaknesses**:
- Substantially more implementation time (custom Canvas drawing, animation orchestration)
- The "coverage level" metric would need to be invented -- there is no real backend concept for this
- Custom animations bypass the design system, risking visual inconsistency
- Much harder to test (animation timing, visual verification)
- High risk of not finishing in demo timeframe
- Could distract from the core story of "AI development workflow" by becoming about "can AI write complex animations"

**Best when**: The demo is specifically about UI capabilities and the timeframe is generous.

**Evidence links**: The existing Home screen has no custom animations beyond standard Compose transitions. The design system (`HedvigTheme`) does not include progress ring components. Adding one would be inconsistent with the existing visual language.

---

### Decision Area 4: Testing Strategy

#### Alternative 4A: Presenter Tests Only (Follows Existing Pattern)

**Description**: Write tests for the `HomePresenter` that verify the insurance summary data flows correctly from the use case through to the `HomeUiState.Success`. Use the existing `TestGetHomeDataUseCase` pattern with `Turbine` and `molecule-test`. Verify: loading state, success state with correct data mapping, error state, refresh behavior.

**Strengths**:
- Directly follows the existing `HomePresenterTest.kt` pattern (TestParameterInjector, Turbine, molecule test)
- Tests the most important layer: data transformation and state management
- Fast to write and fast to run
- Demonstrates that AI can follow existing test conventions perfectly
- The existing test file provides exact patterns to match (lines 57-80 of HomePresenterTest.kt)

**Weaknesses**:
- Does not verify UI rendering
- Does not catch composable-level bugs

**Best when**: The demo prioritizes showing convention adherence and full-stack coverage within a tight timeframe.

**Evidence links**: `HomePresenterTest.kt` uses `TestParameterInjector`, `Turbine` for async, `assertk` for assertions, and `molecule.test.test` for presenter testing. The test creates a `TestGetHomeDataUseCase` with controllable turbines. This is the established pattern across the codebase.

---

#### Alternative 4B: Presenter Tests + Composable Preview Tests

**Description**: In addition to presenter tests, add `@Preview` composable functions for the summary card in various states (loading, populated, error). These serve as visual regression baselines and documentation.

**Strengths**:
- Previews are useful for demo -- can show the card in Android Studio preview pane
- Previews already exist extensively in `HomeLayout.kt` (lines 203-367) and `HomeDestination.kt`
- Provides visual verification without a full UI test framework
- Fast to write -- just composable functions with hardcoded state

**Weaknesses**:
- Previews are not automated tests -- they do not catch regressions unless paired with screenshot testing (which this project does not appear to use)
- Slightly more code to write

**Best when**: The demo wants to show both test-driven correctness and visual output in the IDE.

**Evidence links**: The existing `HomeLayout.kt` has 4 `@Preview` functions (lines 204-298) showing different content configurations. `HomeDestination.kt` likely has similar previews. The project uses `HedvigPreview` and `CollectionPreviewParameterProvider` for systematic previews.

---

#### Alternative 4C: Full TDD Approach (Red-Green-Refactor)

**Description**: Write failing tests first, then implement the minimum code to make them pass, then refactor. Start with presenter tests, then move to data layer, then UI.

**Strengths**:
- Demonstrates disciplined engineering practice
- The narrative of "watch AI do TDD" is compelling for a technical audience
- Ensures high test coverage

**Weaknesses**:
- Significantly slower in a demo context -- each red-green cycle requires explanation
- The existing codebase does not appear to follow strict TDD (tests exist but are written alongside or after implementation)
- Risk of the demo feeling slow or getting bogged down in test setup
- The audience may lose interest watching test failures before seeing any UI

**Best when**: The audience is engineering leadership who values process rigor over speed.

**Evidence links**: The existing test file (`HomePresenterTest.kt`) tests specific behaviors but does not show evidence of TDD methodology (no commit history of red-then-green). The test patterns are more "verify after implementation."

---

## Trade-Off Analysis

### Data Source Strategy

| Perspective | 1A: Extend HomeQuery | 1B: Separate Query | 1C: Reuse Data Modules |
|---|---|---|---|
| **Technical Feasibility** | HIGH - Fields exist in schema, pattern established | MEDIUM - New query + use case + combine flow | LOW - Required modules do not exist |
| **User Impact** | HIGH - Single fast load | MEDIUM - Extra network call possible | LOW - Blocked by missing infrastructure |
| **Simplicity** | HIGH - Extends existing pipeline | MEDIUM - New parallel data flow | LOW - New modules needed |
| **Risk** | LOW - Proven pattern, minimal new code | MEDIUM - Cache coordination, loading state | HIGH - Scope explosion into module creation |
| **Scalability** | MEDIUM - HomeQuery grows larger | HIGH - Independent query lifecycle | HIGH - Clean module boundaries |

### Module Placement

| Perspective | 2A: In feature-home | 2B: New UI Module | 2C: Split data + feature |
|---|---|---|---|
| **Technical Feasibility** | HIGH - No new modules | MEDIUM - Module scaffolding | MEDIUM - Two new modules |
| **User Impact** | HIGH - Same load behavior | HIGH - Same load behavior | HIGH - Same load behavior |
| **Simplicity** | HIGH - All code in one place | LOW - Unnecessary abstraction | LOW - Over-engineered |
| **Risk** | LOW - No new build config | MEDIUM - Build config could have issues | MEDIUM - More surface area |
| **Scalability** | MEDIUM - Extraction needed later | HIGH - Reusable from day one | HIGH - Clean separation |

### UI Complexity

| Perspective | 3A: Static Card | 3B: Expandable Card | 3C: Animated Card |
|---|---|---|---|
| **Technical Feasibility** | HIGH - Design system components | MEDIUM - HomeLayout integration risk | LOW - Custom Canvas + animations |
| **User Impact** | HIGH - Clear, fast, informative | HIGH - More data accessible | MEDIUM - Flashy but possibly confusing |
| **Simplicity** | HIGH - Stateless composable | MEDIUM - Expand/collapse state | LOW - Complex animation code |
| **Risk** | LOW - No moving parts | MEDIUM - Layout interaction issues | HIGH - Time overrun, visual inconsistency |
| **Scalability** | HIGH - Easy to add fields later | MEDIUM - Expansion logic couples to data | LOW - Animation code is brittle |

### Testing Strategy

| Perspective | 4A: Presenter Tests | 4B: Presenter + Previews | 4C: Full TDD |
|---|---|---|---|
| **Technical Feasibility** | HIGH - Established pattern | HIGH - Previews are easy | HIGH - Same tools |
| **User Impact** | N/A | MEDIUM - Visual documentation | N/A |
| **Simplicity** | HIGH - One test file | MEDIUM - Tests + previews | LOW - Process overhead |
| **Risk** | LOW - Known patterns | LOW - Additive | MEDIUM - Demo pacing risk |
| **Scalability** | MEDIUM - Tests catch logic bugs | HIGH - Visual + logic coverage | HIGH - Full coverage |

---

## Recommended Approach

### Selected Combination

**1A + 2A + 3A + 4B**: Extend the existing HomeQuery, implement directly in feature-home, build a clean static card with design system components, and write presenter tests plus composable previews.

### Primary Rationale

This combination maximizes the "AI development flow" story by touching every architectural layer (GraphQL schema extension, data model, use case, presenter, UI state, composable, DI, tests) while minimizing risk of time overruns or convention violations. The changes ripple naturally through the existing pipeline -- exactly the kind of cross-cutting work that demonstrates AI's ability to understand and modify a complex codebase holistically.

### Key Trade-Offs Accepted

- **HomeQuery grows larger**: We accept a slightly larger query in exchange for zero additional network calls and zero new data flow infrastructure. The query currently fetches `activeContracts { masterInceptionDate }` -- adding `premium` and `displayName` fields is a modest expansion.
- **No module-level reusability**: The summary card code lives in feature-home only. If another screen needs it later, extraction is straightforward but not free.
- **Static over animated UI**: We trade visual "wow factor" for reliability and demo speed. The Hedvig design system makes even static cards look professional.

### Key Assumptions

1. **The GraphQL schema exposes `premium` on `activeContracts` and `futureCharge` on `currentMember`**: Evidence from `QueryInsuranceContracts.graphql` and `QueryUpcomingPayment.graphql` confirms these fields exist. If the schema has changed, the query extension would fail at build time.
2. **The `HomeLayout` custom layout can accommodate a new content slot**: The layout currently has 8 slots (enum `HomeLayoutContent`). Adding a 9th is straightforward but requires modifying the custom layout logic.
3. **The demo environment has network access to the staging backend**: The summary card displays real data, which requires a working GraphQL connection.
4. **The Hedvig design system has sufficient card/surface components**: Evidence from imports in `HomeDestination.kt` confirms `Surface`, `HedvigCard`, `HedvigNotificationCard`, and related components are available.

### Confidence Level

**High** -- Every component of this approach has direct precedent in the existing codebase. The data fields exist in the schema, the architectural patterns are established, and the testing tools are proven.

---

## Why Not Others

### Why Not 1B (Separate Query)?
Adds unnecessary complexity for a demo. A second parallel data flow means coordinating two loading states, handling partial success/failure, and expanding the already-strained combine in the Presenter. The benefit (independent caching) is irrelevant for a demo.

### Why Not 1C (Reuse Data Modules)?
Blocked by missing infrastructure. The premium and payment data is locked inside `feature-payments` with no shared data module exposing it. Creating shared modules would dominate the demo time and shift the story from "build a feature" to "refactor module boundaries."

### Why Not 2B (New UI Module)?
Over-engineering for a single-screen card. The codebase creates shared UI modules (like `ui-emergency`) only when multiple features consume the component. The insurance summary card has no multi-screen requirement.

### Why Not 2C (Split data + feature)?
Same over-engineering concern as 2B, but worse -- two new modules with build configuration, package structure, and DI wiring. The data model is 3-4 fields that naturally extend `HomeData`.

### Why Not 3B (Expandable Card)?
The `HomeLayout` uses a custom `Layout` composable with pre-measured placeables and a centering algorithm. Dynamically changing card height from expand/collapse interactions risks breaking the layout math. The risk-to-reward ratio is unfavorable for a demo.

### Why Not 3C (Animated Card)?
Too much implementation risk. Custom Canvas animations, number-counting effects, and shimmer states are time-consuming, hard to test, and visually inconsistent with the existing design system. The demo story should be "AI builds a real feature fast" not "AI writes complex animations."

### Why Not 4A Alone (Presenter Tests Only)?
Presenter tests are the minimum, but adding composable previews is low-cost (a few extra functions) and high-value for the demo -- the audience can see the card rendered in Android Studio without running the app. The marginal effort is worth it.

### Why Not 4C (Full TDD)?
The demo audience benefits more from seeing a feature materialize quickly than from watching red-green-refactor cycles. The existing codebase does not follow strict TDD, so doing it would actually be inconsistent with project conventions.

---

## Deferred Ideas

1. **Payment progress indicator**: A visual indicator showing days until next payment. Interesting but requires inventing UI patterns not in the design system. Defer to post-demo product discussion.

2. **Per-contract breakdown view**: Tapping the card could navigate to a detailed breakdown of each contract's cost. This is essentially what the Insurances tab already does. Defer as it duplicates existing functionality.

3. **Shared `data-insurance-summary` module**: If the card proves valuable in production, extracting the data model and use case into a shared module would enable other features to consume it. Defer until a second consumer exists.

4. **Animated entry transition**: A subtle slide-up or fade-in when the card first appears. Could be added as a polish pass after the core feature works. Low risk, but not needed for demo impact.

5. **Empty state for zero active contracts**: The card should handle the case where a member has no active contracts (terminated, pending). The presenter already tracks `ContractStatus` -- the card can simply not render when status is `Terminated` or `Pending`. This is a detail for implementation, not a separate feature.

---

## Implementation Sketch (For Solution Designer Reference)

The recommended approach touches these files:

| Layer | File(s) | Change |
|-------|---------|--------|
| GraphQL | `QueryHome.graphql` | Add `premium { ...MoneyFragment }`, `displayName` to `activeContracts`; add `futureCharge { date, net { ...MoneyFragment } }` to `currentMember` |
| Data Model | `HomeData.kt` | Add `insuranceSummary: InsuranceSummary?` field with `activeCount`, `totalMonthlyCost`, `nextPaymentDate` |
| Use Case | `GetHomeDataUseCase.kt` | Map new query fields to `InsuranceSummary` in `either` block |
| Presenter | `HomePresenter.kt` | Pass `insuranceSummary` through `SuccessData` to `HomeUiState.Success` |
| UI State | `HomePresenter.kt` | Add `insuranceSummary` field to `HomeUiState.Success` |
| UI | New `InsuranceSummaryCard.kt` | Composable using design system `Surface`, `HedvigText`, `HedvigTextButton` |
| Layout | `HomeLayout.kt` | Add `InsuranceSummaryCard` slot to `HomeLayoutContent` enum and layout logic |
| Destination | `HomeDestination.kt` | Wire the card composable into the layout |
| DI | `HomeModule.kt` | No changes needed (data flows through existing use case) |
| Tests | `HomePresenterTest.kt` | Add test cases for summary data presence/absence |
| Previews | `InsuranceSummaryCard.kt` | Add `@Preview` functions for populated and empty states |
