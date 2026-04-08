# High-Level Design: Insurance Summary Card

## Design Overview

**Business context**: Hedvig users currently land on the Home screen with no at-a-glance view of their insurance portfolio. Adding a summary card gives members immediate visibility into their active policies, monthly cost, and next payment date -- reinforcing trust and reducing navigation to find basic information. This feature also serves as a demonstration of AI-driven TDD development workflow.

**Chosen approach**: Extend the existing `HomeQuery.graphql` to fetch additional contract and billing fields (`displayName`, `premium`, `futureCharge`), parse them in `GetHomeDataUseCaseImpl`, surface them through the existing **MVI + Molecule** pipeline (`HomePresenter` -> `HomeUiState.Success`), and render a new **InsuranceSummaryCard** composable within the `HomeDestination` screen. All changes stay within the existing `feature-home` module. The card is **static** (display-only with a navigation-only "View Details" button) to minimize scope and maximize demo clarity.

**Key decisions:**
- Extend `HomeQuery` rather than creating a separate GraphQL query to avoid an extra network call and keep data loading atomic (ADR-001)
- Place all code in existing `feature-home` module since the card is tightly coupled to home screen data and lifecycle (ADR-002)
- Build a static, read-only card rather than an interactive widget to keep scope small and demo-friendly (ADR-003)
- Follow strict TDD (red-green-refactor) to demonstrate the workflow clearly to an audience (ADR-004)

---

## Architecture

### System Context (C4 Level 1)

```
                          +------------------+
                          |   Hedvig Member  |
                          |    (Android)     |
                          +--------+---------+
                                   |
                                   | Views home screen
                                   v
                          +------------------+
                          |   Hedvig Android |
                          |       App        |
                          +--------+---------+
                                   |
                                   | GraphQL (HTTPS)
                                   v
                          +------------------+
                          | Octopus Backend  |
                          |   (GraphQL API)  |
                          +------------------+
```

The Insurance Summary Card is entirely within the Hedvig Android App boundary. It reads data from the same Octopus GraphQL backend that already serves the Home screen, using the extended `HomeQuery`. No new external systems or integration points are introduced.

### Container Overview (C4 Level 2)

```
+-----------------------------------------------------------------------+
|                        feature-home module                            |
|                                                                       |
|  +------------------+    +---------------------+    +--------------+  |
|  | QueryHome.graphql|    | GetHomeDataUseCase  |    | HomePresenter|  |
|  | (extended)       +--->| Impl                +--->|              |  |
|  |                  |    |                     |    | (Molecule)   |  |
|  +------------------+    +----------+----------+    +------+-------+  |
|                                     |                      |          |
|                                     v                      v          |
|                          +----------+----------+    +------+-------+  |
|                          | HomeData            |    | HomeUiState  |  |
|                          | (+ InsuranceSummary)|    | .Success     |  |
|                          +---------------------+    | (+ summary)  |  |
|                                                     +------+-------+  |
|                                                            |          |
|                                                            v          |
|                                                     +------+-------+  |
|                                                     | HomeDestin-  |  |
|                                                     | ation.kt     |  |
|                                                     | (renders     |  |
|                                                     |  Summary     |  |
|                                                     |  Card)       |  |
|                                                     +--------------+  |
+-----------------------------------------------------------------------+
         |
         | Apollo GraphQL (HTTPS)
         v
+------------------+
| Octopus Backend  |
+------------------+
```

**Container responsibilities:**

| Container | Responsibility |
|-----------|---------------|
| `QueryHome.graphql` | Declares the GraphQL query fields; extended with `displayName`, `premium`, and `futureCharge` |
| `GetHomeDataUseCaseImpl` | Executes the HomeQuery via Apollo, maps response to `HomeData` domain model |
| `HomePresenter` | Molecule-based presenter; collects `HomeData` flow, maps to `HomeUiState` |
| `HomeUiState` | Immutable UI state sealed interface consumed by Compose UI |
| `HomeDestination` | Compose screen that reads `HomeUiState` and renders all cards including the new summary card |

---

## Key Components

| Component | Purpose | Responsibilities | Key Interfaces | Dependencies |
|-----------|---------|-----------------|----------------|-------------|
| **QueryHome.graphql** (extended) | Declare data needs for insurance summary | - Request `displayName` and `premium { ...MoneyFragment }` on `activeContracts` - Request `futureCharge { date, net { ...MoneyFragment } }` on `currentMember` | Apollo code generation produces `HomeQuery.Data` types | Octopus GraphQL schema |
| **InsuranceSummaryData** (new data class) | Domain model for summary card data | - Hold active policy count, list of policy display names, monthly cost, next payment date - Nullable to represent "no active contracts" state | Nested inside or alongside `HomeData` | None (pure data) |
| **GetHomeDataUseCaseImpl** (modified) | Map GraphQL response to domain model | - Parse new `activeContracts` fields into `InsuranceSummaryData` - Compute monthly cost from `futureCharge.net` - Extract next payment date from `futureCharge.date` | `Flow<Either<ApolloOperationError, HomeData>>` | `ApolloClient`, `HomeQuery` |
| **HomePresenter** (modified) | Thread summary data through to UI state | - Pass `InsuranceSummaryData` from `HomeData` into `HomeUiState.Success` via `SuccessData` | `MoleculePresenter<HomeEvent, HomeUiState>` | `GetHomeDataUseCase` |
| **HomeUiState.Success** (modified) | Carry summary data to UI layer | - New `insuranceSummary: InsuranceSummaryUiState?` property | Read by `HomeDestination` composable | None (pure data) |
| **InsuranceSummaryCard** (new composable) | Render the summary card UI | - Display active policies count - Display monthly cost formatted with currency - Display next payment date - "View Details" button (navigates to Insurances tab) | `@Composable fun InsuranceSummaryCard(state, onViewDetails, modifier)` | Design system components (`HedvigCard`, `HedvigText`, `HedvigButton`) |
| **HomeDestination** (modified) | Integrate card into home screen | - Render `InsuranceSummaryCard` in the scrollable content area, positioned below welcome message and above claim status cards | Existing composable, new slot content | `InsuranceSummaryCard` |

---

## Data Flow

### Primary Data Flow

```
Octopus Backend
      |
      | HomeQuery response (JSON over HTTPS)
      v
ApolloClient (normalized cache)
      |
      | HomeQuery.Data (generated Kotlin)
      v
GetHomeDataUseCaseImpl
      |
      | Parses activeContracts[].displayName, activeContracts[].premium,
      | currentMember.futureCharge.date, currentMember.futureCharge.net
      |
      | Maps to InsuranceSummaryData(
      |   activePoliciesCount: Int,
      |   policyNames: List<String>,
      |   monthlyCost: UiMoney?,
      |   nextPaymentDate: LocalDate?
      | )
      v
HomeData (domain model, now includes insuranceSummary field)
      |
      | Flow<Either<ApolloOperationError, HomeData>>
      v
HomePresenter (@Composable present())
      |
      | Maps HomeData.insuranceSummary -> InsuranceSummaryUiState
      | Wraps in HomeUiState.Success
      v
HomeUiState.Success (includes insuranceSummary: InsuranceSummaryUiState?)
      |
      | Collected via collectAsStateWithLifecycle()
      v
HomeDestination composable
      |
      | Passes state to InsuranceSummaryCard()
      v
UI rendered on screen
```

### Data Mapping Details

**GraphQL to Domain:**
- `activeContracts.size` -> `activePoliciesCount`
- `activeContracts[].displayName` -> `policyNames`
- `futureCharge.net.amount` + `futureCharge.net.currencyCode` -> `monthlyCost` (as `UiMoney`)
- `futureCharge.date` -> `nextPaymentDate` (as `LocalDate`)

**Domain to UI State:**
- `InsuranceSummaryData` -> `InsuranceSummaryUiState` (essentially 1:1 mapping with formatted strings)
- When `activePoliciesCount == 0`, the entire summary card is hidden (null state)

---

## Integration Points

| Integration Point | Direction | Protocol | Details |
|-------------------|-----------|----------|---------|
| Octopus GraphQL API | Outbound | HTTPS/GraphQL | Extended `HomeQuery` -- same endpoint, same auth, additional fields only |
| Apollo Normalized Cache | Internal | In-memory | New fields automatically cached by Apollo's normalized cache |
| Home Screen Navigation | Internal | Compose Navigation | "View Details" button triggers navigation to Insurances tab via existing `Navigator` |
| Design System | Internal | Compose API | Uses existing `HedvigCard`, `HedvigText`, `HedvigButton` components |
| MoneyFragment | Internal | GraphQL Fragment | Reuses existing `MoneyFragment` for currency-safe money representation |

**No new external dependencies are introduced.** All integration uses existing patterns and libraries already in the codebase.

---

## Design Decisions

| ADR | Title | Decision | Rationale |
|-----|-------|----------|-----------|
| ADR-001 | Data source strategy | Extend HomeQuery | Single network call, atomic data loading, Apollo cache coherence |
| ADR-002 | Module placement | In feature-home | Card is home-screen-specific, avoids cross-module dependency |
| ADR-003 | UI complexity | Static card | Minimal scope, clear demo narrative, no new state management |
| ADR-004 | Testing approach | TDD (red-green-refactor) | Demonstrates AI development workflow, ensures test coverage from start |

Full decision records are in [decision-log.md](./decision-log.md).

---

## TDD Implementation Sequence

This section defines the order of test-first development steps. Each step follows red (write failing test) -> green (minimal code to pass) -> refactor.

### Step 1: Domain Model

**Test (red):** Write a unit test that constructs `InsuranceSummaryData` with known values and asserts field access works correctly.

**Code (green):** Create the `InsuranceSummaryData` data class inside `GetHomeDataUseCase.kt` alongside `HomeData`. Add `insuranceSummary: InsuranceSummaryData?` field to `HomeData`.

### Step 2: UseCase Mapping

**Test (red):** In `GetHomeUseCaseTest.kt`, register a test Apollo response with `activeContracts` including `displayName` and `premium`, plus `futureCharge` on `currentMember`. Assert that the emitted `HomeData` contains a correctly populated `InsuranceSummaryData`.

**Code (green):** In `GetHomeDataUseCaseImpl`, after mapping existing fields, parse the new GraphQL fields into `InsuranceSummaryData` and set it on `HomeData`.

### Step 3: UseCase Edge Cases

**Test (red):** Test with zero active contracts -- assert `insuranceSummary` is null. Test with missing `futureCharge` -- assert `monthlyCost` and `nextPaymentDate` are null while `activePoliciesCount` is still correct.

**Code (green):** Add null-safety handling in the mapping code.

### Step 4: Presenter Propagation

**Test (red):** In `HomePresenterTest.kt`, provide `HomeData` with an `InsuranceSummaryData` through the fake use case. Assert `HomeUiState.Success` contains the corresponding `InsuranceSummaryUiState`.

**Code (green):** Update `HomeUiState.Success` with `insuranceSummary: InsuranceSummaryUiState?`. Update `SuccessData` to carry it through. Update `SuccessData.fromHomeData()` and `fromLastState()`.

### Step 5: GraphQL Query Extension

**Change:** Extend `QueryHome.graphql` with the new fields. This is not TDD-testable in isolation but is validated by Steps 2-3 which use Apollo test responses against the real query shape.

### Step 6: UI Composable

**Test (red):** Write a `@HedvigPreview` and a screenshot/snapshot test (if the project uses Paparazzi or similar) for `InsuranceSummaryCard` showing the three metrics.

**Code (green):** Implement `InsuranceSummaryCard` composable using design system components.

### Step 7: Integration into HomeDestination

**Code:** Add the `InsuranceSummaryCard` call into `HomeDestination` within the success state rendering, positioned after welcome message and before claim status cards. Wire the "View Details" click to the navigator.

---

## Concrete Examples

### Example 1: Member with Two Active Policies

**Given** a member with 2 active contracts ("Home Insurance" and "Car Insurance"), a future charge of 349.00 SEK on 2026-05-01

**When** the Home screen loads successfully

**Then** the Insurance Summary Card displays:
- "2 Active Policies"
- "349 kr/mo" (formatted monthly cost)
- "Next payment: 1 May" (formatted date)
- A "View Details" button is visible

### Example 2: Member with No Active Contracts (Terminated)

**Given** a member with 0 active contracts and 1 terminated contract

**When** the Home screen loads successfully

**Then** the Insurance Summary Card is not displayed at all (null state, no card rendered)

### Example 3: Network Error then Retry

**Given** the GraphQL request fails with a network error

**When** the Home screen shows the error state and the user pulls to refresh

**Then** after a successful retry, the Insurance Summary Card appears with correct data (the card is part of the normal `HomeUiState.Success` flow, so error recovery works identically to existing behavior)

---

## Out of Scope

- **Interactive card features**: Expandable details, inline policy list, animations -- deferred to future iteration
- **Separate "Insurance Detail" screen**: The "View Details" button navigates to the existing Insurances tab, no new destination
- **Demo mode support**: `GetHomeDataUseCaseDemo` will need updating but is not part of the TDD demonstration scope
- **Localization**: String resources should use existing `core-resources` patterns but exact string keys are an implementation detail
- **Deep linking**: No deep link to the summary card
- **Analytics/tracking**: No new tracking events for the summary card in this iteration
- **Dark mode / theme testing**: Handled automatically by the design system; no special work needed
- **Tablet / large screen layouts**: Uses standard Compose responsive patterns from existing cards

---

## Success Criteria

1. **Data accuracy**: The summary card displays the correct count of active policies, monthly cost, and next payment date matching the GraphQL response
2. **Null safety**: When a member has no active contracts, the card is not rendered (no crash, no empty card)
3. **Test coverage**: At least 4 unit tests pass -- domain model, use case happy path, use case edge case, presenter propagation
4. **Visual consistency**: The card uses existing design system components and visually matches other cards on the Home screen
5. **No regression**: All existing `HomePresenterTest` and `GetHomeUseCaseTest` tests continue to pass
6. **Build health**: `./gradlew :feature-home:test` and `./gradlew ktlintCheck` pass cleanly
