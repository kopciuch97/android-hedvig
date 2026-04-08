# Implementation Plan: Insurance Summary Card

## Overview
Total Steps: 27
Task Groups: 5
Expected Tests: 16-26

## Implementation Steps

### Task Group 1: GraphQL & Domain Model Layer
**Dependencies:** None
**Estimated Steps:** 5

This group extends the GraphQL query, creates the `InsuranceSummaryData` domain model, and adds the build dependency.

- [x] 1.0 Complete GraphQL & Domain Model layer
  - [x] 1.1 Write 3 focused tests for InsuranceSummaryData domain model and GraphQL mapping
    - Test 1: Construct `InsuranceSummaryData` with multiple policies, verify field access (displayName, exposureDisplayName per policy, monthlyCost, nextPaymentDate)
    - Test 2: Verify `InsuranceSummaryData` with null monthlyCost and null nextPaymentDate is valid (futureCharge missing case)
    - Test 3: Verify `InsuranceSummaryData` with empty policies list is constructable (edge case validation)
    - Place tests in: `app/feature/feature-home/src/test/kotlin/com/hedvig/android/feature/home/home/data/InsuranceSummaryDataTest.kt`
    - Use AssertK assertions, JUnit 4, backtick-quoted method names
  - [x] 1.2 Add `coreUiData` build dependency to feature-home
    - In `app/feature/feature-home/build.gradle.kts`, add `implementation(projects.coreUiData)` to dependencies block
    - This provides `UiMoney` and `UiMoney.fromMoneyFragment()` for monetary amount formatting
  - [x] 1.3 Create `InsuranceSummaryData` data class
    - Create in `GetHomeDataUseCase.kt` alongside `HomeData` (or nested within `HomeData`)
    - Fields:
      ```kotlin
      internal data class InsuranceSummaryData(
        val policies: List<PolicyInfo>,
        val monthlyCost: UiMoney?,
        val nextPaymentDate: LocalDate?,
      )

      internal data class PolicyInfo(
        val displayName: String,
        val exposureDisplayName: String,
      )
      ```
    - Add `insuranceSummary: InsuranceSummaryData?` field to `HomeData` data class
  - [x] 1.4 Extend `QueryHome.graphql` with new fields
    - Add to `activeContracts` block:
      ```graphql
      activeContracts {
        masterInceptionDate
        exposureDisplayName
        currentAgreement {
          productVariant {
            displayName
          }
        }
      }
      ```
    - Add to `currentMember` block (top level):
      ```graphql
      futureCharge {
        date
        net {
          ...MoneyFragment
        }
      }
      ```
    - The `MoneyFragment` is already available from `apollo-octopus-public` module
  - [x] 1.5 Ensure domain model tests pass
    - Run: `./gradlew :feature-home:testDebugUnitTest --tests "*.InsuranceSummaryDataTest"`
    - All 3 tests should pass

**Acceptance Criteria:**
- 3 domain model tests pass
- `QueryHome.graphql` includes `exposureDisplayName`, `currentAgreement.productVariant.displayName`, and `futureCharge { date, net { ...MoneyFragment } }`
- `InsuranceSummaryData` and `PolicyInfo` data classes exist
- `HomeData` has `insuranceSummary: InsuranceSummaryData?` field
- `build.gradle.kts` includes `coreUiData` dependency

---

### Task Group 2: Use Case & Data Mapping Layer
**Dependencies:** Group 1
**Estimated Steps:** 6

This group implements the mapping logic in `GetHomeDataUseCaseImpl` and updates the demo use case.

- [x] 2.0 Complete Use Case mapping layer
  - [x] 2.1 Write 4 focused tests for use case mapping in GetHomeUseCaseTest.kt
    - Test 1: Happy path - Apollo response with 2 active contracts + futureCharge, assert `homeData.insuranceSummary` has 2 policies with correct displayName/exposureDisplayName, correct monthlyCost (UiMoney from net), correct nextPaymentDate
    - Test 2: Zero active contracts - assert `homeData.insuranceSummary` is null
    - Test 3: Active contracts exist but `futureCharge` is null - assert `insuranceSummary` has policies but `monthlyCost` is null and `nextPaymentDate` is null
    - Test 4: Single active contract with futureCharge - assert correct single-policy mapping
    - Use existing `TestApolloClientRule`, `OctopusFakeResolver`, `registerTestResponse` patterns from existing `GetHomeUseCaseTest.kt`
    - Use `buildMember { }` / `buildContract { }` test builders for Apollo test data, adding `exposureDisplayName`, `currentAgreement`, `futureCharge` fields
  - [x] 2.2 Map GraphQL response to InsuranceSummaryData in GetHomeDataUseCaseImpl
    - In the `either { }` block (around line 168 in `GetHomeDataUseCase.kt`), before the `HomeData(...)` constructor:
      ```kotlin
      val insuranceSummary = if (homeQueryData.currentMember.activeContracts.isNotEmpty()) {
        val policies = homeQueryData.currentMember.activeContracts.map { contract ->
          PolicyInfo(
            displayName = contract.currentAgreement.productVariant.displayName,
            exposureDisplayName = contract.exposureDisplayName,
          )
        }
        val futureCharge = homeQueryData.currentMember.futureCharge
        InsuranceSummaryData(
          policies = policies,
          monthlyCost = futureCharge?.net?.let { UiMoney.fromMoneyFragment(it) },
          nextPaymentDate = futureCharge?.date,
        )
      } else {
        null
      }
      ```
    - Add `insuranceSummary = insuranceSummary` to the `HomeData(...)` constructor call
  - [x] 2.3 Update GetHomeDataUseCaseDemo with mock InsuranceSummaryData
    - In `GetHomeDataUseCaseDemo.kt`, add to the `HomeData(...)` constructor:
      ```kotlin
      insuranceSummary = InsuranceSummaryData(
        policies = listOf(
          PolicyInfo("Home Insurance", "Bellmansgatan 5"),
          PolicyInfo("Car Insurance", "ABC 123"),
        ),
        monthlyCost = UiMoney(499.0, UiCurrencyCode.SEK),
        nextPaymentDate = LocalDate(2026, 5, 1),
      ),
      ```
  - [x] 2.4 Mechanically update all existing test fixtures constructing HomeData
    - **9 sites in `HomePresenterTest.kt`**: Add `insuranceSummary = null,` to each `HomeData(...)` constructor
    - Existing tests in `GetHomeUseCaseTest.kt` use Apollo `registerTestResponse` so they get the default null from the schema (no changes needed for existing tests, only the new field on `HomeData`)
    - Note: If any test constructs `HomeData` directly, add `insuranceSummary = null`
  - [x] 2.5 Ensure use case tests pass
    - Run: `./gradlew :feature-home:testDebugUnitTest --tests "*.GetHomeUseCaseTest"`
    - All existing + 4 new tests should pass

**Acceptance Criteria:**
- 4 new use case tests pass
- All 9+ existing `HomePresenterTest` HomeData constructions compile with new field
- All existing `GetHomeUseCaseTest` tests still pass (no regression)
- Demo use case returns mock insurance summary data
- Mapping correctly handles: multiple contracts, zero contracts (null), missing futureCharge (null cost/date)

---

### Task Group 3: Presenter Layer
**Dependencies:** Group 2
**Estimated Steps:** 5

This group threads `InsuranceSummaryData` through the presenter to `HomeUiState.Success`.

- [x] 3.0 Complete Presenter layer
  - [x] 3.1 Write 3 focused tests for presenter propagation in HomePresenterTest.kt
    - Test 1: `HomeData` with non-null `insuranceSummary` results in `HomeUiState.Success` with matching `insuranceSummaryData`
    - Test 2: `HomeData` with null `insuranceSummary` results in `HomeUiState.Success` with null `insuranceSummaryData`
    - Test 3: After error then success refresh, `insuranceSummaryData` is correctly populated from new data
    - Use existing `TestGetHomeDataUseCase` + `FakeCrossSellHomeNotificationService` patterns
  - [x] 3.2 Add insuranceSummaryData field to HomeUiState.Success
    - In `HomePresenter.kt`, add to `HomeUiState.Success`:
      ```kotlin
      val insuranceSummaryData: InsuranceSummaryData?,
      ```
  - [x] 3.3 Thread InsuranceSummaryData through SuccessData
    - Add `insuranceSummaryData: InsuranceSummaryData?` field to `SuccessData`
    - In `SuccessData.fromHomeData()`: add `insuranceSummaryData = homeData.insuranceSummary`
    - In `SuccessData.fromLastState()`: add `insuranceSummaryData = lastState.insuranceSummaryData`
    - In the `HomeUiState.Success(...)` construction in `present()`: add `insuranceSummaryData = successData.insuranceSummaryData`
  - [x] 3.4 Mechanically update all HomeUiState.Success constructions in tests and previews
    - **7 sites in `HomePresenterTest.kt`** that construct `HomeUiState.Success` directly (assertions): no change needed since they use `isInstanceOf<>` + `prop {}`, not direct construction
    - **4 preview composables in `HomeDestination.kt`**: Add `insuranceSummaryData = null,` (or with sample data for the main preview)
  - [x] 3.5 Ensure presenter tests pass
    - Run: `./gradlew :feature-home:testDebugUnitTest --tests "*.HomePresenterTest"`
    - All existing + 3 new tests should pass

**Acceptance Criteria:**
- 3 new presenter tests pass
- All existing presenter tests pass (no regression)
- `HomeUiState.Success` has `insuranceSummaryData: InsuranceSummaryData?` field
- `SuccessData` correctly maps from `HomeData` and from `lastState`
- All preview composables compile with the new field

---

### Task Group 4: UI & Navigation Layer
**Dependencies:** Group 3
**Estimated Steps:** 7

This group creates the `InsuranceSummaryCard` composable, integrates it into `HomeLayout`, wires up navigation, and adds preview composables.

- [x] 4.0 Complete UI & Navigation layer
  - [x] 4.1 Write 2 focused tests (preview-based smoke tests)
    - These are composable `@HedvigPreview` functions that serve as visual regression anchors:
      - Preview 1: `InsuranceSummaryCard` with 3 policies, cost, and next payment date
      - Preview 2: `InsuranceSummaryCard` with 1 policy, no cost, no payment date
    - Place in the new `InsuranceSummaryCard.kt` file
    - Note: No automated composable unit tests; previews serve as the verification mechanism per project convention
  - [x] 4.2 Create InsuranceSummaryCard composable
    - Create new file: `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/ui/InsuranceSummaryCard.kt`
    - Internal composable following the mockup structure:
      ```kotlin
      @Composable
      internal fun InsuranceSummaryCard(
        data: InsuranceSummaryData,
        onViewDetailsClick: () -> Unit,
        modifier: Modifier = Modifier,
      )
      ```
    - Structure: `HedvigCard` > `Column(padding=16.dp)` > header row > `HorizontalDivider` > policy list > `HorizontalDivider` > cost row > `HorizontalDivider` > `HedvigTextButton("View Details")`
    - Header row: "Your Insurance" (`headlineMedium`, `textPrimary`) + "N Active" (`labelSmall`, `textSecondary`)
    - Policy rows: `bodyMedium` + `textPrimary` for displayName, `bodySmall` + `textSecondary` for exposureDisplayName, 8dp spacing
    - Cost row: monthlyCost formatted as `"$amount/mo"` left, next payment date as `"Next payment: D MMM"` right, `Arrangement.SpaceBetween`
    - Only show cost row if monthlyCost is not null
    - Button: `HedvigTextButton`, `buttonSize = Large`, centered
    - Use hardcoded English strings for first pass (localization out of scope per spec)
  - [x] 4.3 Add InsuranceSummaryCard slot to HomeLayout
    - In `HomeLayout.kt`:
      1. Add parameter: `insuranceSummaryCard: @Composable @UiComposable () -> Unit,`
      2. Add enum value: `HomeLayoutContent.InsuranceSummaryCard`
      3. Add Box in content: `Box(Modifier.layoutId(HomeLayoutContent.InsuranceSummaryCard)) { insuranceSummaryCard() }`
      4. Measure: `val insuranceSummaryCardPlaceable: Placeable = measurables.fastFirstOrNull { it.layoutId == HomeLayoutContent.InsuranceSummaryCard }!!.measure(constraints)`
      5. Add to `centerPlaceables` after claimStatusCards block:
         ```kotlin
         if (insuranceSummaryCardPlaceable.height > 0) {
           add(FixedSizePlaceable(0, 24.dp.roundToPx()))
           add(insuranceSummaryCardPlaceable)
         }
         ```
  - [x] 4.4 Update all HomeLayout call sites
    - **HomeDestination.kt `HomeScreenSuccess`** (line ~452): Add `insuranceSummaryCard` slot:
      ```kotlin
      insuranceSummaryCard = {
        val summaryData = uiState.insuranceSummaryData
        if (summaryData != null) {
          InsuranceSummaryCard(
            data = summaryData,
            onViewDetailsClick = navigateToInsurances,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp)
              .padding(horizontalInsets),
          )
        }
      },
      ```
    - **HomeScreenSuccess composable**: Add `navigateToInsurances: () -> Unit` parameter
    - **HomeScreen composable**: Add `navigateToInsurances: () -> Unit` parameter, pass through to `HomeScreenSuccess`
    - **HomeDestination composable**: Add `navigateToInsurances: () -> Unit` parameter, pass through to `HomeScreen`
    - **4 preview HomeLayout calls** in `HomeLayout.kt` (`PreviewHomeLayout` helper): Add `insuranceSummaryCard = {}` parameter with default `{}`
    - **3 preview HomeScreen calls** in `HomeDestination.kt`: Add `navigateToInsurances = {}`
  - [x] 4.5 Thread navigateToInsurances through HomeGraph
    - In `HomeGraph.kt`:
      1. Add parameter: `navigateToInsurances: () -> Unit`
      2. Pass to `HomeDestination(...)`: `navigateToInsurances = dropUnlessResumed { navigateToInsurances() }`
    - Find the caller of `homeGraph()` in the app module and add the parameter there (likely in `HedvigNavHost` or similar)
  - [x] 4.6 Add InsuranceSummaryCard previews to HomeLayout previews
    - In `HomeLayout.kt`, update `PreviewHomeLayout` helper to accept `insuranceSummaryCard` parameter with default `{}`
    - Add one new preview showing the card in context:
      ```kotlin
      @Preview(showSystemUi = true)
      @Composable
      private fun PreviewHomeLayoutWithInsuranceSummaryCard() { ... }
      ```
  - [x] 4.7 Verify UI compiles and previews render
    - Run: `./gradlew :feature-home:compileDebugKotlin`
    - Verify no compilation errors
    - Run: `./gradlew ktlintFormat` to auto-format

**Acceptance Criteria:**
- `InsuranceSummaryCard` composable renders correctly in previews (2 preview variants)
- `HomeLayout` has new `insuranceSummaryCard` slot positioned after `claimStatusCards` with 24dp spacing
- Navigation callback threaded from `HomeGraph` through `HomeDestination` to `HomeScreenSuccess` to `InsuranceSummaryCard`
- Card conditionally rendered only when `insuranceSummaryData` is non-null
- All preview composables compile and include the new parameters
- Code passes `ktlintFormat`

---

### Task Group 5: Test Review & Gap Analysis
**Dependencies:** Groups 1, 2, 3, 4
**Estimated Steps:** 4

- [x] 5.0 Review and fill critical test gaps
  - [x] 5.1 Review tests from previous groups (10 tests: 3 domain + 4 use case + 3 presenter)
    - Verify all tests follow project conventions: AssertK assertions, backtick names, `runTest`, `TestLogcatLoggingRule` where applicable
  - [x] 5.2 Analyze test gaps for the insurance summary card feature
    - Check: edge case where all contracts have missing `productVariant.displayName` (should not happen per schema, String! is non-null)
    - Check: date formatting edge cases (different locales)
    - Check: presenter correctly clears `insuranceSummaryData` on error -> success cycle
    - Check: `SuccessData.fromLastState()` preserves `insuranceSummaryData` across reloads
  - [x] 5.3 Write up to 6 additional strategic tests
    - Test: Use case with 5+ active contracts (verify all are mapped)
    - Test: Presenter emits Loading without `insuranceSummaryData` initially, then Success with data
    - Test: `fromLastState` preserves `insuranceSummaryData` when transitioning from Success to reloading Success
    - Additional tests as needed based on gap analysis (max 6 total additional)
  - [x] 5.4 Run feature-specific tests and verify build health
    - Run: `./gradlew :feature-home:testDebugUnitTest` (all feature-home tests)
    - Run: `./gradlew :feature-home:ktlintCheck`
    - Expect: ~16-20 total tests for this feature pass, plus all existing tests pass

**Acceptance Criteria:**
- All feature-home tests pass (existing + new, approximately 16-20 new tests total)
- No more than 6 additional tests added in this group
- `ktlintCheck` passes cleanly
- No regressions in existing test suite

---

## Execution Order

1. **Group 1: GraphQL & Domain Model** (5 steps) - foundation layer, no dependencies
2. **Group 2: Use Case & Data Mapping** (6 steps, depends on 1) - maps GraphQL to domain
3. **Group 3: Presenter Layer** (5 steps, depends on 2) - threads data to UI state
4. **Group 4: UI & Navigation Layer** (7 steps, depends on 3) - composable + layout integration
5. **Group 5: Test Review & Gap Analysis** (4 steps, depends on 1-4) - final verification

## Standards Compliance

Follow standards from `.maister/docs/standards/`:

### Global
- `coding-style.md`: ktlint_official (2-space indent, 120 char lines, trailing commas), PascalCase composables, sorted dependencies
- `conventions.md`: MVI with Molecule pattern, `internal` visibility for feature classes, feature module isolation
- `error-handling.md`: Arrow Either for error handling, `safeFlow` for Apollo queries
- `minimal-implementation.md`: No speculative features, hardcoded strings acceptable for first pass

### Frontend
- `components.md`: Jetpack Compose only, M3 restricted to design-system-internals, HedvigCard/HedvigTextButton from design system
- `accessibility.md`: Meaningful content descriptions, clear action labels

### Testing
- `test-writing.md`: AssertK exclusively, Turbine fakes, molecule `presenter.test`, backtick names, `TestLogcatLoggingRule`, JUnit 4 with `runTest`

## Key Technical Details

### Files to Create
| File | Purpose |
|------|---------|
| `InsuranceSummaryCard.kt` | New composable for the card UI |
| `InsuranceSummaryDataTest.kt` | Domain model unit tests |

### Files to Modify
| File | Changes |
|------|---------|
| `QueryHome.graphql` | Add `exposureDisplayName`, `currentAgreement.productVariant.displayName`, `futureCharge { date, net { ...MoneyFragment } }` |
| `build.gradle.kts` (feature-home) | Add `implementation(projects.coreUiData)` |
| `GetHomeDataUseCase.kt` | Add `InsuranceSummaryData`, `PolicyInfo` data classes; add `insuranceSummary` to `HomeData`; add mapping in `GetHomeDataUseCaseImpl` |
| `GetHomeDataUseCaseDemo.kt` | Add mock `InsuranceSummaryData` |
| `HomePresenter.kt` | Add `insuranceSummaryData` to `HomeUiState.Success` and `SuccessData`; thread through `fromHomeData` and `fromLastState` |
| `HomeLayout.kt` | Add `insuranceSummaryCard` slot, enum value, measurement, placement |
| `HomeDestination.kt` | Render card in `HomeScreenSuccess`, thread `navigateToInsurances` callback, update previews |
| `HomeGraph.kt` | Add `navigateToInsurances` parameter, pass to `HomeDestination` |
| `HomePresenterTest.kt` | Add 3 new tests + mechanical `insuranceSummary = null` on 9 `HomeData` constructions + `insuranceSummaryData = null` on `HomeUiState.Success` constructions in previews |
| `GetHomeUseCaseTest.kt` | Add 4 new tests |

### Mechanical Update Count
- `HomeData(...)` constructions in tests: **9** (add `insuranceSummary = null`)
- `HomeUiState.Success(...)` in previews: **4** (add `insuranceSummaryData = null`)
- `HomeLayout(...)` calls: **5** (add `insuranceSummaryCard = {}`)
- `HomeScreen(...)` calls in previews: **3** (add `navigateToInsurances = {}`)

## Notes

- **Test-Driven**: Each group starts with failing tests (RED), implements minimum code (GREEN), then verifies
- **Run Incrementally**: Only run new/affected tests after each group, full suite in Group 5
- **Mark Progress**: Check off steps as completed in this file
- **Reuse First**: Prioritize `HedvigCard`, `HedvigTextButton`, `UiMoney.fromMoneyFragment()`, existing test patterns
- **Hardcoded Strings**: "Your Insurance", "Active", "View Details", "Next payment:", "/mo" are hardcoded per spec (localization out of scope)
- **Navigation Threading**: The `navigateToInsurances` callback must be threaded through 4 layers: HomeGraph -> HomeDestination -> HomeScreen -> HomeScreenSuccess -> InsuranceSummaryCard
