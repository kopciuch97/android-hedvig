# Insurance Summary Card

## What

A card on the Home screen showing a member's active insurance policies at a glance:
- Per-policy info: product name + insured object
- Active policy count
- Total monthly cost (from next scheduled charge)
- Next payment date
- "View Details" CTA navigating to the Insurances tab

The card is hidden when the member has no active contracts.

## Why

Members had no way to see their insurance portfolio status without navigating away from the Home screen. This card gives immediate visibility into coverage and upcoming costs, reinforcing trust and reducing friction.

## How it works

The card extends the existing `HomeQuery` GraphQL query with contract and charge fields, maps them through the standard MVI pipeline (UseCase -> Presenter -> UiState -> Composable), and renders as a new slot in `HomeLayout`.

Protected by `Feature.INSURANCE_SUMMARY_CARD` feature flag for remote toggling.

## Architecture Decisions

### ADR-001: Extend HomeQuery (not separate query)
Single network call, atomic data loading, Apollo cache coherence. Adding fields to the existing query avoids a second round-trip and keeps the summary card's loading state in sync with other Home content.

### ADR-002: Code in feature-home (not new module)
The card is a Home screen concern with no independent lifecycle. Its data comes from `HomeQuery`, its state lives in `HomeUiState`, and it renders inside `HomeDestination`. A separate module would create artificial boundaries.

### ADR-003: Static card (not interactive)
Display-only card with a navigation button. No expand/collapse, no inline editing, no animations. Keeps scope minimal and the TDD progression clean.

### ADR-004: TDD red-green-refactor
Tests written before implementation at each layer (use case mapping, presenter propagation). 10 dedicated tests covering happy paths, edge cases (zero contracts, missing payment data), and state preservation.

## Data flow

```
QueryHome.graphql
  activeContracts { exposureDisplayName, currentAgreement.productVariant.displayName }
  futureCharge { date, net { ...MoneyFragment } }
    -> GetHomeDataUseCaseImpl.toInsuranceSummary()
      -> HomeData.insuranceSummary: InsuranceSummaryData?
        -> HomePresenter -> SuccessData -> HomeUiState.Success.insuranceSummaryData
          -> HomeDestination -> HomeLayout (center group, after ClaimStatusCards)
            -> InsuranceSummaryCard composable
```

## Acceptance Criteria

### Card visibility
- [ ] AC1: Card is visible on Home screen when member has at least one active contract
- [ ] AC2: Card is NOT visible when member has zero active contracts (terminated, pending, or none)
- [ ] AC3: Card is NOT visible during loading or error states
- [ ] AC4: Card can be remotely disabled via `INSURANCE_SUMMARY_CARD` feature flag

### Card content
- [ ] AC5: Card header shows "Your Insurance" title and "N Active" count matching number of active contracts
- [ ] AC6: Each active contract is listed with product name (e.g. "Home Insurance") and insured object (e.g. "Bellmansgatan 5")
- [ ] AC7: Total monthly cost is displayed from `futureCharge.net` with currency (e.g. "349 kr/mo")
- [ ] AC8: Next payment date is displayed from `futureCharge.date` (e.g. "Next payment: 1 May")
- [ ] AC9: When `futureCharge` is null (no upcoming payment), cost and date rows are hidden — policy list still shows
- [ ] AC10: Card uses HedvigCard from design system, visually consistent with other Home screen cards

### Navigation
- [ ] AC11: Tapping "View Details" navigates to the Insurances tab
- [ ] AC12: After navigating to Insurances, member can return to Home via bottom navigation (tab-style switch, not back stack push)

### Demo mode
- [ ] AC13: Card is visible with mock data (2 policies, 499 SEK, May 2026) in demo mode

## Known limitations

- Strings are hardcoded in English — needs Lokalise integration before production
- Date formatting (`formatPaymentDate`) is locale-unaware — uses English month names
- No cap on displayed policies — very tall card if member has 10+ contracts

## Test coverage

- 5 use case tests: multi-contract mapping, zero contracts, missing futureCharge, single contract, 5+ contracts
- 5 presenter tests: data propagation, null handling, error recovery, state preservation, reload cycle
- 2 composable previews: full card (3 policies + cost), minimal card (1 policy, no cost)
