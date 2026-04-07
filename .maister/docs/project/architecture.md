# System Architecture

## Overview
Hedvig Android is a highly modular Android application with 80+ Gradle modules organized into distinct layers. The architecture follows a feature-based MVI pattern using Molecule for reactive state management, with strict module dependency enforcement.

## Architecture Pattern
**Pattern**: Feature-based Modular MVI with Molecule

The app enforces a layered architecture where feature modules are independent units that communicate through the data and core layers. Feature modules CANNOT depend on other feature modules — this is enforced at build time by the custom Gradle convention plugin.

**Data Flow**: User Action -> Event -> Presenter (Composable) -> UiState -> UI

## System Structure

### Feature Layer (27 modules)
- **Location**: `app/feature/feature-{name}/`
- **Purpose**: Self-contained user-facing features (home, chat, insurances, payments, claims, profile, etc.)
- **Pattern**: Each module follows `ui/ + navigation/ + di/` structure
- **Key constraint**: Features are isolated — no cross-feature dependencies

### Data Layer (13 modules)
- **Location**: `app/data/data-{domain}/`
- **Purpose**: Business logic, repositories, use cases, data access
- **Pattern**: Public/Android split (`-public` for interfaces, `-android` for implementation) or KMP single module with `commonMain`/`androidMain`
- **Key files**: Repository interfaces, Use cases, Apollo GraphQL operations

### Core Layer (14 modules)
- **Location**: `app/core/core-{name}/`
- **Purpose**: Shared utilities, common types, datastore, resources
- **Key modules**: `core-common-public`, `core-datastore-public`, `core-resources`

### Apollo/GraphQL Layer (6 modules)
- **Location**: `app/apollo/`
- **Purpose**: GraphQL client configuration, schema management, normalized caching
- **Backend**: Octopus API with response-based code generation

### Design System
- **Location**: `app/design-system/`
- **Purpose**: Reusable UI components, theming, Material 3 integration
- **Modules**: `design-system-api`, `design-system-hedvig`, `design-system-internals`

### Navigation
- **Location**: `app/navigation/`
- **Purpose**: Type-safe navigation with serializable destinations
- **Top-level graphs**: Home, Insurances, Forever, Payments, Profile

### Infrastructure Modules
- **Auth**: BankID integration, OAuth
- **Database**: Room database modules
- **Language**: Localization (KMP-compatible)
- **Logging/Tracking**: Timber, Datadog, Firebase analytics
- **Network**: Ktor client configuration, interceptors

## Data Flow

```
UI (Compose)
    |
    v (events)
ViewModel (MoleculeViewModel)
    |
    v (delegates)
Presenter (MoleculePresenter - @Composable)
    |
    v (calls)
Use Cases (business logic)
    |
    v (calls)
Repositories (data access)
    |
    v (queries/mutations)
Apollo GraphQL Client --> Octopus Backend
Room Database (local cache)
DataStore (preferences)
```

## External Integrations
| Integration | Technology | Purpose |
|---|---|---|
| Octopus Backend | Apollo GraphQL 4.4.1 | Primary data source |
| Firebase | Crashlytics, Analytics, Messaging | Monitoring, push notifications |
| Datadog | SDK 3.6.0 | RUM, logs, analytics |
| BankID | Custom auth modules | Swedish digital identity authentication |
| Lokalise | Gradle plugin | Translation management |
| Play Store | GitHub Actions workflow | Production distribution |
| Firebase App Distribution | GitHub Actions workflow | Staging/internal distribution |

## Configuration
- **Build types**: Debug (dev), Staging, Release — each with different backend and application ID
- **Gradle convention plugins**: Custom DSL (`hedvig { compose(); apollo("octopus") }`) for consistent module setup
- **Feature flags**: Managed through `featureflags` module
- **Demo mode**: `Provider<T>` pattern with `ProdOrDemoProvider` for conditional implementations

## Module Dependency Rules
1. Feature modules CANNOT depend on other feature modules (enforced by build plugin)
2. Feature modules depend on data, core, navigation, and design-system modules
3. Data modules expose public interfaces in `-public` modules
4. Core modules provide shared utilities available to all layers
5. Type-safe project accessors (`projects.coreCommonPublic`) used for all inter-module dependencies

---
*Based on codebase analysis performed 2026-04-07*
