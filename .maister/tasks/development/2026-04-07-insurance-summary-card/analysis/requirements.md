# Requirements: Insurance Summary Card

## Initial Description
Implement an Insurance Summary Card on the Home screen showing active policies count, monthly cost, and next payment date. Extend HomeQuery GraphQL, add InsuranceSummaryData domain model, update UseCase/Presenter/UiState, create InsuranceSummaryCard composable. Full TDD approach.

## Q&A Summary

### From Research Phase
- **Data source**: Extend existing HomeQuery (ADR-001)
- **Module placement**: In feature-home (ADR-002)
- **UI complexity**: Static card (ADR-003)
- **Testing**: TDD red-green-refactor (ADR-004)

### From Gap Analysis (Phase 2)
- **Policy name field**: Use BOTH `currentAgreement.productVariant.displayName` (title) + `exposureDisplayName` (subtitle)
- **Monthly cost source**: `futureCharge.net` (actual next charge amount)
- **Navigation**: New `navigateToInsurances` callback parameter
- **Demo mode**: Return mock data (card visible)

### From Requirements Gathering (Phase 5)
- **Discoverability**: Card visible immediately on Home load (center group, no scrolling required)
- **UI pattern**: Follow ClaimStatusCards pattern as closest template
- **Visual assets**: Use ASCII mockups from Phase 4

## Similar Features Identified
- **ClaimStatusCards**: Closest analogy - card in center group of HomeLayout with state-driven data
- **MemberReminderCards**: Secondary reference - conditional card rendering with HedvigNotificationCard
- **ImportantMessages**: Reference for message display pattern

## Functional Requirements

### FR-1: Display Insurance Summary
- Show number of active insurance contracts
- Show per-policy info: product name (productVariant.displayName) + insured object (exposureDisplayName)
- Show total monthly cost from futureCharge.net formatted with currency
- Show next payment date from futureCharge.date formatted as readable date

### FR-2: Navigation
- "View Details" button navigates to Insurances tab
- New `navigateToInsurances` callback threaded through composable hierarchy

### FR-3: Conditional Visibility
- Card visible only when member has active contracts (ContractStatus.Active)
- Card hidden (renders nothing) when no active contracts
- Card hidden during loading/error states (follows HomeUiState pattern)

### FR-4: Layout Integration
- Card placed in center group of HomeLayout, after ClaimStatusCards, before bottom group
- Uses HedvigCard from design system
- Follows 16dp horizontal padding + safe area insets convention
- 24dp spacing from ClaimStatusCards above

### FR-5: TDD Implementation
- Write failing tests first for each layer (domain model, use case, presenter)
- Implement minimum code to pass
- Refactor

### FR-6: Demo Mode
- GetHomeDataUseCaseDemo returns mock InsuranceSummaryData
- Card visible and functional in demo mode

## Reusability Opportunities
- UiMoney.fromMoneyFragment() for money formatting (from core-ui-data)
- MoneyFragment for GraphQL money fields (from apollo-octopus-public)
- HedvigCard for card container (from design-system-hedvig)
- ClaimStatusCards layout/test patterns for replication

## Scope Boundaries
- **In scope**: GraphQL extension, domain model, use case mapping, presenter, UI state, composable, layout integration, presenter tests, use case tests, composable previews, demo mode
- **Out of scope**: Animated transitions, expandable card, per-contract drill-down, analytics tracking, deep linking, tablet layout, localization (use existing string patterns)

## Technical Considerations
- Must add `projects.coreUiData` dependency to feature-home build.gradle.kts
- Adding field to HomeData will break all test constructors (mechanical update)
- HomeLayout custom Layout needs new slot + measurement/placement logic update
- Apollo codegen will auto-generate types from extended query
