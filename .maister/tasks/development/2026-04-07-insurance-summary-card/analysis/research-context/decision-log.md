# Decision Log

## ADR-001: Data Source Strategy -- Extend HomeQuery vs Separate Query

### Status
Accepted

### Context
The Insurance Summary Card needs data about active contracts (count, display names, premiums) and upcoming charges (amount, date). This data could come from extending the existing `HomeQuery` GraphQL query or from a new, dedicated query. The Home screen already executes `HomeQuery` on every load, and the Octopus backend serves both sets of data from the same `currentMember` root.

### Decision Drivers
- Minimizing network calls on the Home screen (already combines 7+ flows in `GetHomeDataUseCaseImpl`)
- Keeping data loading atomic so the summary card appears at the same time as other home content
- Leveraging Apollo normalized cache coherence (one query = one cache update)
- Simplicity of implementation for a demo-focused feature

### Considered Options
1. **Extend HomeQuery** -- add `displayName`, `premium`, and `futureCharge` fields to the existing `QueryHome.graphql`
2. **New dedicated query** -- create `QueryInsuranceSummary.graphql` with only the fields needed for the card
3. **Reuse data from Insurances tab** -- read from an existing query used by the `feature-insurances` module

### Decision Outcome
Chosen option: **Option 1 (Extend HomeQuery)**, because it avoids an additional network round-trip, keeps the summary card data lifecycle identical to other home content, and requires no new `combine()` flow in the already-complex use case. The additional fields are lightweight (a string, a money fragment, and a date) and do not materially increase response size.

### Consequences

#### Good
- Single network call serves all Home screen data
- Apollo cache stays coherent -- one query invalidation refreshes everything
- No new flow orchestration needed in `GetHomeDataUseCaseImpl`
- Summary card appears simultaneously with other home content (no loading flicker)

#### Bad
- HomeQuery grows slightly larger; every home load now fetches summary fields even if the card is hidden
- Tighter coupling between the summary card feature and the HomeQuery schema -- removing the feature later requires a query change

---

## ADR-002: Module Placement -- feature-home vs New Module

### Status
Accepted

### Context
The Hedvig Android app enforces strict module boundaries: feature modules cannot depend on other feature modules. The Insurance Summary Card displays on the Home screen and uses data from the Home query. It could live in the existing `feature-home` module or in a new `feature-insurance-summary` module.

### Decision Drivers
- Architectural rule: feature modules cannot depend on each other (enforced by `hedvig.gradle.plugin`)
- The card's data comes entirely from `HomeQuery` and flows through `HomePresenter`
- Creating a new module for a single card adds build overhead (new Gradle module, DI module, navigation wiring)
- Demo clarity -- fewer files to navigate means a cleaner presentation

### Considered Options
1. **Existing feature-home module** -- add new files alongside existing home UI and data code
2. **New feature-insurance-summary module** -- dedicated module with its own presenter, use case, and DI
3. **Shared UI module** -- place the card composable in a `ui-` module and consume from feature-home

### Decision Outcome
Chosen option: **Option 1 (Existing feature-home module)**, because the card is intrinsically a Home screen component with no independent lifecycle. Its data comes from `HomeQuery`, its state lives in `HomeUiState`, and it renders inside `HomeDestination`. A separate module would create artificial boundaries and require exposing `HomeData` fields across module boundaries.

### Consequences

#### Good
- Zero new modules to configure (no `build.gradle.kts`, no DI module, no navigation graph)
- Natural data flow -- `HomeData` to `HomeUiState` to `HomeDestination` with no cross-module interface
- Faster build times (one fewer module to compile)
- Easier to follow during a demo

#### Bad
- `feature-home` grows slightly; if insurance summary becomes a complex feature later, it may warrant extraction
- Cannot reuse the summary card in other screens without refactoring to a shared module

---

## ADR-003: UI Complexity -- Static Card vs Interactive Widget

### Status
Accepted

### Context
The Insurance Summary Card could range from a simple static display (three metrics + a button) to an interactive widget (expandable policy list, inline editing, animated transitions). The feature serves as a demo of AI-driven TDD workflow, so the implementation must be clear and followable.

### Decision Drivers
- Demo audience needs to follow each TDD step without getting lost in UI complexity
- Static cards match existing Home screen patterns (claim status cards, important messages)
- Interactive widgets require additional state management (expand/collapse events, animations)
- Time constraint -- feature should be implementable in a single demo session

### Considered Options
1. **Static card** -- displays 3 metrics (policy count, monthly cost, next payment date) + "View Details" navigation button
2. **Expandable card** -- same metrics but tappable to reveal per-policy breakdown
3. **Interactive dashboard** -- card with charts, historical cost trends, and inline actions

### Decision Outcome
Chosen option: **Option 1 (Static card)**, because it demonstrates the full MVI data flow (GraphQL -> UseCase -> Presenter -> UiState -> Composable) without adding event-handling complexity. The "View Details" button navigates to the existing Insurances tab, keeping navigation simple. Each TDD step remains focused and easy to explain.

### Consequences

#### Good
- Clear, linear TDD progression: model -> use case -> presenter -> UI
- No new `HomeEvent` variants needed (the button triggers navigation, not a presenter event)
- Matches the visual style of existing cards on the Home screen
- Each step is self-contained and demonstrable

#### Bad
- Limited utility for real users (they see numbers but cannot drill into details inline)
- "View Details" navigates away from Home, which is a context switch
- Future interactive features will require additional events and state

---

## ADR-004: Testing Approach -- TDD vs Test-After

### Status
Accepted

### Context
This feature is being developed as a demonstration of AI-driven development workflow. The testing approach (write tests first vs write tests after implementation) significantly affects the narrative and structure of the demo. The existing codebase has established test patterns using Turbine, AssertK, and `presenter.test()`.

### Decision Drivers
- Demo narrative requires visible "red -> green -> refactor" progression
- Existing test infrastructure (`TestGetHomeDataUseCase`, `FakeCrossSellHomeNotificationService`, `TestApolloClientRule`) supports TDD well
- Test-first forces clear interface design before implementation
- Audience should see failing tests become passing as code is written

### Considered Options
1. **Strict TDD** -- write each test before the corresponding implementation, following red-green-refactor
2. **Test-after** -- implement the feature first, then add tests
3. **Hybrid** -- write data layer tests first (TDD), then implement UI without tests
4. **No tests** -- skip tests entirely for speed

### Decision Outcome
Chosen option: **Option 1 (Strict TDD)**, because the demo's primary purpose is showing how AI follows a disciplined development workflow. Each TDD cycle creates a natural "chapter" in the demo narrative:

1. Write failing domain model test -> create data class
2. Write failing use case test -> implement GraphQL mapping
3. Write failing presenter test -> wire state through
4. Implement UI composable (tests via preview/snapshot)

### Consequences

#### Good
- Clear demo narrative with visible progression
- Tests serve as living documentation of expected behavior
- Forces interface-first design (write the assertion before the implementation)
- High confidence in correctness from the start
- Existing test utilities (`Turbine`, `presenter.test()`, `TestApolloClientRule`) make TDD practical

#### Bad
- Slower initial velocity (tests before code)
- Some refactoring of tests may be needed as the design firms up
- UI composable testing is less natural in TDD (preview-based rather than assertion-based)
