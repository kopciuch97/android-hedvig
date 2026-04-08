# Specification: Insurance Summary Card

## Goal
Add an Insurance Summary Card to the Home screen that displays active policies, monthly cost, and next payment date, giving members immediate at-a-glance visibility into their insurance portfolio without navigating away from Home.

## User Stories
- As a Hedvig member, I want to see a summary of my active insurance policies on the Home screen so that I can quickly understand my coverage at a glance.
- As a Hedvig member, I want to see my total monthly cost and next payment date so that I stay informed about upcoming charges.
- As a Hedvig member, I want to tap "View Details" to navigate to the Insurances tab so that I can explore my policies in more detail.

## Core Requirements

1. **Display insurance summary card** in the center group of HomeLayout, positioned after ClaimStatusCards with 24dp spacing
2. **Show per-policy information**: product name (`productVariant.displayName` as title) and insured object (`exposureDisplayName` as subtitle) for each active contract
3. **Show active count**: display "N Active" label in the card header alongside "Your Insurance" title
4. **Show monthly cost**: display `futureCharge.net` formatted via UiMoney (e.g., "499 kr")
5. **Show next payment date**: display `futureCharge.date` as a readable date (e.g., "Next payment: 1 May")
6. **Conditional visibility**: card renders nothing when no active contracts exist; card not shown during loading/error states
7. **Navigation**: "View Details" button navigates to the Insurances tab via a `navigateToInsurances: () -> Unit` callback
8. **Demo mode**: `GetHomeDataUseCaseDemo` returns mock `InsuranceSummaryData` so the card is visible in demo mode
9. **TDD approach**: write failing tests first for domain model, use case mapping, and presenter propagation

## Visual Design

Reference: `/Users/szymonkopa/work/android/.maister/tasks/development/2026-04-07-insurance-summary-card/analysis/ui-mockups.md`

Key design elements:
- **Container**: `HedvigCard` with default `surfacePrimary` background and `cornerXLarge` shape
- **Internal padding**: 16dp all sides
- **Header row**: "Your Insurance" (`headlineMedium`, `textPrimary`) left-aligned, "N Active" (`labelSmall`, `textSecondary`) right-aligned
- **Dividers**: `HorizontalDivider` between header, policy list, cost summary, and button sections
- **Policy rows**: `bodyMedium` + `textPrimary` for display name, `bodySmall` + `textSecondary` for exposure name, 8dp vertical spacing between policies
- **Cost row**: monthly cost (`bodyMedium`, `textPrimary`) left, next payment date (`bodySmall`, `textSecondary`) right, using `Arrangement.SpaceBetween`
- **Button**: `HedvigTextButton` with `buttonSize = Large`, centered
- **Outer padding**: 16dp horizontal + safe-area insets (matching all other Home content)
- **Fidelity**: approximate -- follow design system components, not pixel-perfect

4 mockup variants provided: isolation view, standard home screen, empty state (hidden), busy screen with claims + reminders.

## Reusable Components

### Existing Code to Leverage

| Component | File Path | How to Leverage |
|-----------|-----------|-----------------|
| `HedvigCard` | `app/design-system/design-system-hedvig/src/commonMain/kotlin/.../HedvigCard.kt` | Card container with `surfacePrimary` background, `cornerXLarge` shape |
| `HedvigTextButton` | `app/design-system/design-system-hedvig/src/commonMain/kotlin/.../Button.kt` | "View Details" button, `buttonSize = Large` |
| `HedvigText` | Design system | All text rendering with `HedvigTheme.typography.*` |
| `UiMoney` + `UiMoney.fromMoneyFragment()` | `app/core/core-ui-data/src/commonMain/kotlin/com/hedvig/android/core/uidata/UiMoney.kt` | Format monetary amounts from GraphQL `MoneyFragment` |
| `MoneyFragment` | `app/apollo/apollo-octopus-public/src/commonMain/graphql/.../FragmentMoneyFragment.graphql` | GraphQL fragment for `Money` type fields (`net`, `gross`, etc.) |
| `HomeLayout` | `app/feature/feature-home/src/main/kotlin/.../HomeLayout.kt` | Custom Layout with slot-based placement; extend with new `insuranceSummaryCard` slot |
| `HomeLayoutContent` enum | Same file as `HomeLayout` | Add `InsuranceSummaryCard` entry |
| `FixedSizePlaceable` | Same file as `HomeLayout` | Add 24dp spacing placeable in `centerPlaceables` |
| `HomeData` data class | `app/feature/feature-home/src/main/kotlin/.../GetHomeDataUseCase.kt` | Extend with `insuranceSummary: InsuranceSummaryData?` field |
| `HomeUiState.Success` | `app/feature/feature-home/src/main/kotlin/.../HomePresenter.kt` | Add `insuranceSummaryData: InsuranceSummaryData?` field |
| `SuccessData` | Same file as `HomePresenter` | Add field + mapping in `fromHomeData()` and `fromLastState()` |
| `GetHomeDataUseCaseDemo` | `app/feature/feature-home/src/main/kotlin/.../GetHomeDataUseCaseDemo.kt` | Add mock `InsuranceSummaryData` to demo response |
| `HomeDestination` | `app/feature/feature-home/src/main/kotlin/.../HomeDestination.kt` | Render new card in success state, pass to `HomeLayout` |
| `HomeGraph` | `app/feature/feature-home/src/main/kotlin/.../HomeGraph.kt` | Thread `navigateToInsurances` callback |
| `ClaimStatusCards` pattern | `HomeDestination.kt` lines 466-473 | Conditional rendering pattern: `if (uiState.x != null) { ... }` |
| Existing test patterns | `HomePresenterTest.kt`, `GetHomeUseCaseTest.kt` | Molecule presenter testing, Apollo test response patterns |

### New Components Required

| Component | Justification |
|-----------|--------------|
| `InsuranceSummaryData` (data class) | New domain model -- no existing model combines active contract display info with charge data. Nested inside or alongside `HomeData`. |
| `InsuranceSummaryCard` (composable) | New UI component -- no existing composable renders this specific combination of policy list + cost summary. Composed entirely from existing design system primitives. |
| Extended `QueryHome.graphql` fields | Current query only fetches `masterInceptionDate` on `activeContracts`. Need `exposureDisplayName`, `currentAgreement { productVariant { displayName } }`, and `futureCharge { date, net { ...MoneyFragment } }` on `currentMember`. |

## Technical Approach

### Data Flow
1. **GraphQL**: Extend `QueryHome.graphql` to fetch `exposureDisplayName` and `currentAgreement.productVariant.displayName` on `activeContracts`, plus `futureCharge { date, net { ...MoneyFragment } }` on `currentMember`
2. **Domain**: Create `InsuranceSummaryData` data class with: `policies: List<PolicyInfo>` (each having `displayName: String` and `exposureDisplayName: String`), `monthlyCost: UiMoney?`, `nextPaymentDate: LocalDate?`
3. **Use Case**: In `GetHomeDataUseCaseImpl`, map new GraphQL fields to `InsuranceSummaryData`. When `activeContracts` is empty, set to `null`. Add `insuranceSummary` field to `HomeData`
4. **Presenter**: `SuccessData.fromHomeData()` passes `InsuranceSummaryData` through. `HomeUiState.Success` gains `insuranceSummaryData: InsuranceSummaryData?`
5. **UI**: `HomeDestination` renders `InsuranceSummaryCard` when data is non-null, passing to `HomeLayout`'s new `insuranceSummaryCard` slot

### Integration Points
- **Build dependency**: Add `implementation(projects.coreUiData)` to `app/feature/feature-home/build.gradle.kts` for `UiMoney`
- **Navigation**: Add `navigateToInsurances: () -> Unit` parameter to `HomeGraph.homeGraph()`, `HomeDestination()`, and `HomeScreen()`. The caller (app module) provides tab navigation
- **Layout**: Add `InsuranceSummaryCard` to `centerPlaceables` in `HomeLayout` after `ClaimStatusCards` with 24dp spacing, using the same conditional height > 0 pattern
- **Test fixtures**: All test files constructing `HomeData` or `HomeUiState.Success` need mechanical update to include the new field (default to `null`)

### Architecture Decisions (from research phase)
- ADR-001: Extend HomeQuery (not separate query) -- single network call, atomic data loading
- ADR-002: Keep all code in feature-home -- card is tightly coupled to home screen lifecycle
- ADR-003: Static card -- minimal scope, display-only with navigation button
- ADR-004: TDD approach -- red-green-refactor for each layer

## Implementation Guidance

### Testing Approach
- 2-8 focused tests per implementation step group
- Test verification runs only new tests, not entire suite
- **Domain model tests**: construct `InsuranceSummaryData`, verify field access
- **Use case tests**: Apollo test response with active contracts + futureCharge, assert correct mapping. Edge cases: zero contracts (null summary), missing futureCharge (null cost/date)
- **Presenter tests**: provide `HomeData` with summary data through fake use case, assert `HomeUiState.Success` contains it
- **Composable previews**: `@HedvigPreview` for card with multiple policies, single policy, and maximum content
- Use AssertK exclusively for assertions
- Use Turbine for test fakes
- Backtick-quoted test method names
- JUnit 4 with `runTest`

### Standards Compliance
- **Coding Style** (`standards/global/coding-style.md`): ktlint_official, 2-space indent, 120 char lines, trailing commas, PascalCase composables
- **Conventions** (`standards/global/conventions.md`): MVI with Molecule pattern, `internal` visibility for feature classes, feature module isolation, LoadIteration pattern
- **Components** (`standards/frontend/components.md`): Jetpack Compose only, M3 restricted to design-system-internals, composable PascalCase naming, Destination composable pattern
- **Error Handling** (`standards/global/error-handling.md`): Arrow Either for error handling, `safeFlow` for Apollo
- **Test Writing** (`standards/testing/test-writing.md`): AssertK assertions, Turbine fakes, molecule `presenter.test`, backtick names, `TestLogcatLoggingRule`
- **Minimal Implementation** (`standards/global/minimal-implementation.md`): No speculative features, build only what is needed

## Out of Scope
- Animated transitions or expand/collapse behavior
- Per-contract drill-down or inline detail views
- Analytics/tracking events for the card
- Deep linking to the summary card
- Tablet or large-screen-specific layouts (uses standard Compose responsive behavior)
- Localization string key creation (use existing `core-resources` patterns)
- Dark mode testing (handled by design system automatically)
- Per-policy premium breakdown (only total `futureCharge.net` is shown)

## Success Criteria
1. **Data accuracy**: Card displays correct active policy count, per-policy names, total monthly cost, and next payment date matching GraphQL response
2. **Null safety**: Card is not rendered when member has no active contracts (no crash, no empty card)
3. **Test coverage**: At least 6 unit tests pass -- domain model, use case happy path, use case zero-contracts edge case, use case missing-futureCharge edge case, presenter propagation, presenter null-summary
4. **Visual consistency**: Card uses HedvigCard, HedvigTextButton, HedvigText with standard theme values; horizontal padding and spacing match other Home screen content
5. **No regression**: All existing `HomePresenterTest` and `GetHomeUseCaseTest` tests pass after mechanical updates
6. **Build health**: `./gradlew :feature-home:test` and `./gradlew ktlintCheck` pass cleanly
7. **Demo mode**: Card visible with mock data in demo mode
8. **Navigation**: "View Details" button triggers navigation to Insurances tab
