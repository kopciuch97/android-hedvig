# UI Mockups: Insurance Summary Card on Home Screen

**Generated**: 2026-04-07
**Task Path**: /Users/szymonkopa/work/android/.maister/tasks/development/2026-04-07-insurance-summary-card
**Feature Type**: Enhancement (new card slot in existing HomeLayout)

## Overview

### UI Requirements
- Insurance Summary Card showing active policy count, per-policy details, total monthly cost, next payment date, and a "View Details" action
- Card placed between the center group (WelcomeMessage + ClaimStatusCards) and the bottom group (VIM + MemberReminders + buttons) in the HomeLayout
- Card hidden when no active contracts exist

### Integration Strategy
**Decision**: Add a new `insuranceSummaryCard` slot to `HomeLayout`, positioned after `claimStatusCards` in the center group.
**Rationale**: The center group is the user's primary focus area. Placing insurance summary here gives it high visibility without competing with actionable notifications in the bottom group. It logically follows claim status (both are "state of your insurance" information). The bottom group remains reserved for alerts, reminders, and CTAs.

## Existing Layout Analysis

### Application Structure

The Home screen uses a custom `Layout` composable (`HomeLayout`) with named slots identified by `HomeLayoutContent` enum values. Content is divided into two placement groups:

- **Center group** (vertically centered when space allows): WelcomeMessage, ClaimStatusCards
- **Bottom-attached group** (anchored to bottom): VeryImportantMessages, MemberReminderCards, StartClaimButton, HelpCenterButton

When total content exceeds screen height, everything falls back to simple column layout (scrollable).

**Key Components**:
- Layout: `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/ui/HomeLayout.kt`
- Screen: `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/ui/HomeDestination.kt`
- State: `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/ui/HomePresenter.kt`
- Card container: `app/design-system/design-system-hedvig/src/commonMain/kotlin/com/hedvig/android/design/system/hedvig/HedvigCard.kt`
- Buttons: `app/design-system/design-system-hedvig/src/commonMain/kotlin/com/hedvig/android/design/system/hedvig/Button.kt`
- Notification cards: `HedvigNotificationCard` (used in MemberReminderCards and elsewhere)
- Typography: `HedvigTheme.typography` (headlineMedium, bodyMedium, labelSmall, etc.)
- Colors: `HedvigTheme.colorScheme` (surfacePrimary, textPrimary, textSecondary)
- Shapes: `HedvigTheme.shapes.cornerXLarge` (default HedvigCard shape)

### Identified Patterns
- **Slot-based custom Layout**: Each content area is a composable lambda passed to `HomeLayout`, identified by `layoutId`
- **Conditional rendering**: Cards check for null/empty data before rendering (e.g., `if (uiState.claimStatusCardsData != null)`)
- **Horizontal padding convention**: All cards use `Modifier.padding(horizontal = 16.dp)` plus safe-area insets
- **HedvigCard usage**: Rounded card with `surfacePrimary` background, `cornerXLarge` shape, optional `onClick`
- **Text hierarchy**: `headlineMedium` for titles, `bodyMedium` for content, `labelSmall` for metadata

## Mockups

### Mockup 1: InsuranceSummaryCard Composable (Isolation)

**Context**: The card's internal layout showing all content elements.

```
 16dp horizontal padding
 |                                                    |
 v                                                    v
 ┌────────────────────────────────────────────────────┐
 │  HedvigCard (surfacePrimary, cornerXLarge)         │
 │                                                    │
 │  ┌──────────────────────────────────────────────┐  │
 │  │ 16dp padding all sides                       │  │
 │  │                                              │  │
 │  │  "Your Insurance"           "3 Active"       │  │
 │  │  headlineMedium             labelSmall        │  │
 │  │  textPrimary               textSecondary      │  │
 │  │                                              │  │
 │  │  ────────────────────────── (HorizontalDivider)  │
 │  │  8dp vertical spacing                        │  │
 │  │                                              │  │
 │  │  "Home Insurance"                            │  │
 │  │  bodyMedium, textPrimary                     │  │
 │  │  "Bellmansgatan 5"                           │  │
 │  │  bodySmall, textSecondary                    │  │
 │  │                                              │  │
 │  │  "Car Insurance"                             │  │
 │  │  bodyMedium, textPrimary                     │  │
 │  │  "ABC 123"                                   │  │
 │  │  bodySmall, textSecondary                    │  │
 │  │                                              │  │
 │  │  "Pet Insurance"                             │  │
 │  │  bodyMedium, textPrimary                     │  │
 │  │  "Fido"                                      │  │
 │  │  bodySmall, textSecondary                    │  │
 │  │                                              │  │
 │  │  ────────────────────────── (HorizontalDivider)  │
 │  │  8dp vertical spacing                        │  │
 │  │                                              │  │
 │  │  Row:                                        │  │
 │  │  "499 kr/mo"              "Next payment: 1 May" │
 │  │  bodyMedium, textPrimary  bodySmall, textSecondary│
 │  │                                              │  │
 │  │  ────────────────────────── (HorizontalDivider)  │
 │  │  8dp vertical spacing                        │  │
 │  │                                              │  │
 │  │           [ View Details ]                    │  │
 │  │           HedvigTextButton                    │  │
 │  │           centered, Large size                │  │
 │  │                                              │  │
 │  └──────────────────────────────────────────────┘  │
 └────────────────────────────────────────────────────┘
```

**Internal Structure**:
```
Column(Modifier.padding(16.dp)) {
  Row {                                    // Header row
    "Your Insurance" (headlineMedium)      // Left-aligned
    Spacer(weight)
    "3 Active" (labelSmall)                // Right-aligned badge/label
  }
  HorizontalDivider()
  // Per-policy items (forEach loop)
  Column(verticalArrangement = spacedBy(8.dp)) {
    PolicyRow(displayName, exposureDisplayName)  // Repeated per contract
  }
  HorizontalDivider()
  Row {                                    // Cost summary row
    "499 kr/mo" (bodyMedium)               // Left-aligned
    Spacer(weight)
    "Next payment: 1 May" (bodySmall)      // Right-aligned
  }
  HorizontalDivider()
  HedvigTextButton("View Details")         // Centered, navigates to Insurances tab
}
```

**Component Reuse**:
- `HedvigCard` (`HedvigCard.kt`) for the container
- `HedvigTextButton` (`Button.kt`) for "View Details"
- `HedvigText` with `HedvigTheme.typography.*` for all text
- `HorizontalDivider` from Compose Material for separators

---

### Mockup 2: Full Home Screen - Standard View (with Insurance Summary Card)

**Context**: Active member with no claims, showing the new card integrated into the layout.

```
 ┌──────────────────────────────────────────────┐
 │         [Logo]              [Chat]           │  TopAppBarLayoutForActions
 ├──────────────────────────────────────────────┤
 │                                              │
 │                 TopSpacer                    │  WindowInsets + 64dp toolbar
 │                                              │
 │                                              │
 │                                              │
 │             "Hi, Szymon!"                    │  WelcomeMessage (EXISTING)
 │              centered text                   │  headlineLarge
 │                                              │
 │                 24dp gap                     │
 │                                              │
 │  ┌──────────────────────────────────────┐    │
 │  │ Your Insurance             3 Active  │    │  NEW: InsuranceSummaryCard
 │  │ ──────────────────────────────────── │    │  (insuranceSummaryCard slot)
 │  │ Home Insurance                       │    │
 │  │ Bellmansgatan 5                      │    │
 │  │ Car Insurance                        │    │
 │  │ ABC 123                              │    │
 │  │ ──────────────────────────────────── │    │
 │  │ 499 kr/mo       Next payment: 1 May │    │
 │  │ ──────────────────────────────────── │    │
 │  │          [ View Details ]            │    │
 │  └──────────────────────────────────────┘    │
 │                                              │
 │                 16dp gap                     │
 │                                              │  --- bottom-attached group ---
 │  ┌──────────────────────────────────────┐    │
 │  │ [========= Start a Claim =========] │    │  StartClaimButton (EXISTING)
 │  └──────────────────────────────────────┘    │
 │                  8dp gap                    │
 │  ┌──────────────────────────────────────┐    │
 │  │ [         Other services          ]  │    │  HelpCenterButton (EXISTING)
 │  └──────────────────────────────────────┘    │
 │                                              │
 │                BottomSpacer                  │
 └──────────────────────────────────────────────┘
```

**Integration Points**:
- NEW slot: `insuranceSummaryCard` added to `HomeLayout` signature
- NEW enum value: `HomeLayoutContent.InsuranceSummaryCard`
- Card placed in `centerPlaceables` list, after `claimStatusCards` (with 24dp spacing)
- When no claims exist, card appears directly below WelcomeMessage
- Card uses same horizontal padding convention as other cards (16dp + safeDrawing insets)

---

### Mockup 3: Home Screen - No Active Contracts (Card Hidden)

**Context**: Member with no active insurance contracts. The card is not rendered.

```
 ┌──────────────────────────────────────────────┐
 │         [Logo]              [Chat]           │  TopAppBarLayoutForActions
 ├──────────────────────────────────────────────┤
 │                                              │
 │                 TopSpacer                    │
 │                                              │
 │                                              │
 │                                              │
 │                                              │
 │                                              │
 │             "Hi, Szymon!"                    │  WelcomeMessage (EXISTING)
 │              centered text                   │  (vertically centered)
 │                                              │
 │                                              │
 │           (no InsuranceSummaryCard)           │  HIDDEN: empty contracts list
 │           (no ClaimStatusCards)               │  HIDDEN: no active claims
 │                                              │
 │                                              │
 │                 16dp gap                     │
 │                                              │
 │  ┌──────────────────────────────────────┐    │
 │  │ [========= Start a Claim =========] │    │  StartClaimButton
 │  └──────────────────────────────────────┘    │
 │                  8dp gap                    │
 │  ┌──────────────────────────────────────┐    │
 │  │ [         Other services          ]  │    │  HelpCenterButton
 │  └──────────────────────────────────────┘    │
 │                                              │
 │                BottomSpacer                  │
 └──────────────────────────────────────────────┘
```

**Integration Notes**:
- Card renders nothing when contract list is empty (height = 0)
- The `centerPlaceables` conditional check mirrors the existing `claimStatusCards` pattern: `if (claimStatusCardsPlaceable.height > 0)`
- Layout behavior unchanged: WelcomeMessage stays centered, bottom group stays anchored

---

### Mockup 4: Home Screen - Claims + Insurance Summary Card + Reminders

**Context**: Busy home screen with active claims, insurance summary, and member reminders. Tests scroll behavior.

```
 ┌──────────────────────────────────────────────┐
 │         [Logo]              [Chat]           │  TopAppBarLayoutForActions
 ├──────────────────────────────────────────────┤
 │                 TopSpacer                    │
 │                                              │
 │             "Hi, Szymon!"                    │  WelcomeMessage
 │                                              │
 │                 24dp gap                     │
 │                                              │
 │  ┌──────────────────────────────────────┐    │
 │  │ Claim: Water damage                  │    │  ClaimStatusCards (EXISTING)
 │  │ [====|====|    |    ] Submitted       │    │  HorizontalPager
 │  └──────────────────────────────────────┘    │
 │                                              │
 │                 24dp gap                     │
 │                                              │
 │  ┌──────────────────────────────────────┐    │  NEW: InsuranceSummaryCard
 │  │ Your Insurance             2 Active  │    │
 │  │ ──────────────────────────────────── │    │
 │  │ Home Insurance                       │    │
 │  │ Bellmansgatan 5                      │    │
 │  │ Car Insurance                        │    │
 │  │ ABC 123                              │    │
 │  │ ──────────────────────────────────── │    │
 │  │ 349 kr/mo       Next payment: 1 May │    │
 │  │ ──────────────────────────────────── │    │
 │  │          [ View Details ]            │    │
 │  └──────────────────────────────────────┘    │
 │                                              │
 │                 16dp gap                     │
 │  ┌──────────────────────────────────────┐    │
 │  │ ! Connect your payment method        │    │  MemberReminderCards (EXISTING)
 │  │   [Connect Payment ->]               │    │  HedvigNotificationCard
 │  └──────────────────────────────────────┘    │
 │                 16dp gap                     │
 │  ┌──────────────────────────────────────┐    │
 │  │ [========= Start a Claim =========] │    │  StartClaimButton
 │  └──────────────────────────────────────┘    │
 │                  8dp gap                    │
 │  ┌──────────────────────────────────────┐    │
 │  │ [         Other services          ]  │    │  HelpCenterButton
 │  └──────────────────────────────────────┘    │
 │                                              │
 │                BottomSpacer                  │
 └──────────────────────────────────────────────┘
             (scrollable when exceeding screen)
```

**Integration Notes**:
- When content exceeds `fullScreenSize.height`, the layout falls back to column mode (all items stacked, vertically scrollable)
- The `centerPlaceables` now contains: WelcomeMessage + 24dp + ClaimStatusCards + 24dp + InsuranceSummaryCard
- Z-index ordering: WelcomeMessage renders above cards when overlapping during scroll/animation (existing `reverseOrderOfZIndex` behavior)

---

## Reusable Components

### Layout
- **HomeLayout**: `app/feature/feature-home/.../HomeLayout.kt` - Custom Layout composable with named slots. Needs new `insuranceSummaryCard` slot parameter.
- **HomeLayoutContent enum**: Same file. Needs new `InsuranceSummaryCard` entry.

### UI Components
- **HedvigCard**: `app/design-system/design-system-hedvig/src/commonMain/kotlin/.../HedvigCard.kt`
  - Use with default `shape = cornerXLarge`, `color = surfacePrimary`
  - No `onClick` needed on the card itself (action is via the button inside)

- **HedvigTextButton**: `app/design-system/design-system-hedvig/src/commonMain/kotlin/.../Button.kt`
  - Use `buttonSize = Large` for the "View Details" action
  - Variant: default text button style

- **HedvigText**: Standard text composable
  - `headlineMedium` for "Your Insurance" header
  - `bodyMedium` for policy display names and cost
  - `bodySmall` for exposure names, next payment date
  - `labelSmall` for "3 Active" count badge

- **HedvigNotificationCard**: Used as pattern reference (already used in MemberReminderCards)

### State
- **HomeUiState.Success**: `app/feature/feature-home/.../HomePresenter.kt` - Needs new field for insurance summary data
- **HomePresenter**: Same file. Needs to fetch contract and payment data.

### Typography & Colors
- `HedvigTheme.typography.headlineMedium` - Card title
- `HedvigTheme.typography.bodyMedium` - Policy names, cost amount
- `HedvigTheme.typography.bodySmall` - Exposure names, payment date
- `HedvigTheme.typography.labelSmall` - Active count label
- `HedvigTheme.colorScheme.textPrimary` - Primary text
- `HedvigTheme.colorScheme.textSecondary` - Secondary text (subtitles, metadata)
- `HedvigTheme.colorScheme.surfacePrimary` - Card background

## Implementation Notes

### Changes Required in HomeLayout.kt

1. Add `insuranceSummaryCard` lambda parameter to `HomeLayout` signature
2. Add `HomeLayoutContent.InsuranceSummaryCard` to the enum
3. Add `Box(Modifier.layoutId(HomeLayoutContent.InsuranceSummaryCard)) { insuranceSummaryCard() }` in content
4. Measure the new placeable
5. Add to `centerPlaceables` after claimStatusCards:
   ```
   if (insuranceSummaryCardPlaceable.height > 0) {
     add(FixedSizePlaceable(0, 24.dp.roundToPx()))
     add(insuranceSummaryCardPlaceable)
   }
   ```

### Changes Required in HomeDestination.kt

1. Pass the new `insuranceSummaryCard` slot to `HomeLayout`:
   ```
   insuranceSummaryCard = {
     if (uiState.insuranceSummaryData != null) {
       InsuranceSummaryCard(
         data = uiState.insuranceSummaryData,
         onViewDetailsClick = navigateToInsurancesTab,
         modifier = Modifier
           .fillMaxWidth()
           .padding(horizontal = 16.dp)
           .padding(horizontalInsets),
       )
     }
   },
   ```

### Consistency Checklist
- Uses `HedvigCard` with default theme values (matches all other cards in the app)
- Horizontal padding 16.dp + safe-area insets (matches ClaimStatusCards, MemberReminderCards, buttons)
- Conditional rendering pattern (check for null/empty) matches existing `claimStatusCardsData` pattern
- Text hierarchy follows existing conventions (headlineMedium title, bodyMedium content, bodySmall metadata)
- "View Details" uses `HedvigTextButton` consistent with "Other services" button pattern

### Accessibility Considerations
- Card title "Your Insurance" provides section context for screen readers
- Policy count ("3 Active") should be read as part of the header row
- Each policy row should be individually focusable with content description combining display name and exposure
- "View Details" button has clear action label
- Cost and payment date should be read together as a summary

### Responsive Behavior
- Card stretches to `fillMaxWidth` with 16dp horizontal padding (same as all other Home content)
- Policy list is vertical Column, handles any number of policies naturally
- On narrow screens, the cost/date Row wraps if needed (or use `Arrangement.SpaceBetween`)
- When total content exceeds screen height, entire HomeLayout becomes scrollable (existing behavior)

## Alternatives Considered

### Option A: Place card in the bottom-attached group (before VIM) - Rejected
**Why rejected**: The bottom group contains actionable alerts and CTAs. Insurance summary is informational, not urgency-driven. Placing it there would push action items further down and reduce their visibility.

### Option B: Make the entire card clickable (no separate button) - Rejected
**Why rejected**: The existing `HedvigCard` supports `onClick`, but having a dedicated "View Details" text button is more explicit and discoverable. It also aligns with accessibility best practices (clear action affordance). However, this could be reconsidered if design prefers a cleaner look.

### Option C: Use HedvigNotificationCard instead of HedvigCard - Rejected
**Why rejected**: `HedvigNotificationCard` is designed for alerts/reminders with priority levels. Insurance summary is persistent informational content, not a notification. Using `HedvigCard` is semantically correct and avoids confusion with actual notifications.

### Option D: Place card as first item in center group (before WelcomeMessage) - Rejected
**Why rejected**: WelcomeMessage is the anchor of the centered content group and provides personal greeting context. Insurance info should follow the greeting, not precede it.

### Selected: Option E - New slot in center group, after ClaimStatusCards
**Why selected**: Logically groups "state of your insurance" information together (claims + policies). Maintains WelcomeMessage as the primary centered element. Does not interfere with bottom-attached actionable content. Follows the existing pattern of conditional slot rendering.

---

*Generated by ui-mockup-generator subagent*
