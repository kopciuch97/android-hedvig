# Scope Clarifications

## Decisions Made

### Critical: Policy Display Name Field
- **Decision**: Use BOTH `currentAgreement { productVariant { displayName } }` as title AND `exposureDisplayName` as subtitle
- **GraphQL fields needed**: `currentAgreement { productVariant { displayName } }` + `exposureDisplayName` on `activeContracts`

### Important: Monthly Cost Source
- **Decision**: Use `futureCharge.net` (actual next charge amount including discounts/adjustments)
- **Rationale**: Shows real charge amount users will actually pay

### Important: Navigation Callback
- **Decision**: Add explicit `navigateToInsurances: () -> Unit` parameter
- **Rationale**: Consistent with existing navigation patterns in HomeDestination

### Important: Demo Mode
- **Decision**: Return mock `InsuranceSummaryData` with sample values (card visible in demo mode)
- **Rationale**: Demo mode should showcase all features

## Scope Boundaries
- No scope expansion needed
- All changes contained within feature-home module + coreUiData dependency addition
- Build dependency: must add `projects.coreUiData` to feature-home's build.gradle.kts
