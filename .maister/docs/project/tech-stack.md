# Technology Stack

## Overview
This document describes the technology choices for Hedvig Android — a production mobile app built with Kotlin, Jetpack Compose, and Apollo GraphQL on a highly modular architecture (80+ Gradle modules).

## Languages

### Kotlin (2.3.10)
- **Usage**: 100% of codebase (1,189 Kotlin files)
- **Rationale**: Official Android development language with excellent Compose and coroutines support
- **Key Features Used**: Coroutines, Flow, Serialization, Multiplatform (KMP in 43 modules)

## Frameworks

### UI & Presentation
| Technology | Version | Usage |
|---|---|---|
| Jetpack Compose | 2026.01.01 BOM | All UI — 100% Compose, no XML layouts |
| Material 3 | Latest via BOM | Design system theming (internal to design-system-internals) |
| Coil | 3.3.0 | Image loading (SVG, GIF, PDF support) |
| Accompanist | 0.37.3 | Permissions handling |
| ExoPlayer (Media3) | 1.9.2 | Video playback |

### State Management & Architecture
| Technology | Version | Usage |
|---|---|---|
| Molecule | 2.2.0 | Reactive MVI pattern (Composable presenters) |
| Kotlin Coroutines | 1.10.2 | Async operations |
| Arrow | 2.2.1.1 | Functional programming (Either, Option) |

### Data & Networking
| Technology | Version | Usage |
|---|---|---|
| Apollo GraphQL | 4.4.1 | Primary data source (Octopus backend), normalized caching |
| Ktor Client | 3.4.0 | HTTP client with custom interceptors |
| OkHttp | 5.3.2 | Network layer |
| kotlinx.serialization | 1.10.0 | JSON serialization |

### Dependency Injection
| Technology | Version | Usage |
|---|---|---|
| Koin | 4.1.1 BOM | Service locator/DI with modular configuration |

## Database

### Room (2.8.4)
- **Type**: SQLite-based relational database
- **Usage**: Local persistence for caching and offline data
- **Modules**: Dedicated `database/` modules

### DataStore (1.2.0)
- **Type**: Key-value preferences
- **Usage**: Encrypted preferences storage

## Build Tools & Package Management

### Gradle (8.11+)
- **Android Gradle Plugin**: 9.0.0
- **Convention Plugins**: Custom `hedvig.gradle.plugin` with DSL for Compose, Apollo, Room, Serialization
- **Module Discovery**: Automatic — any directory with `build.gradle.kts` under `/app` is auto-discovered
- **Dependency Versions**: Centralized in `gradle/libs.versions.toml` (150+ entries)
- **BOM Strategy**: Compose BOM, Firebase BOM, OkHttp BOM, Koin BOM

### Build Optimization
| Feature | Status |
|---|---|
| Gradle Configuration Cache | Enabled |
| Parallel Builds | Enabled |
| Build Cache | Enabled (Gradle Develocity 4.3.2) |
| JVM Memory | 8GB heap |

## Infrastructure

### CI/CD — GitHub Actions
| Workflow | Purpose |
|---|---|
| pr.yml | PR checks (lint, test, build) |
| staging.yml | Staging builds via Firebase App Distribution |
| upload-to-play-store.yml | Production releases to Play Store |
| graphql-schema.yml | Apollo schema sync |
| strings.yml | Lokalise translation sync |
| unused-resources.yml | Resource cleanup checks |
| umbrella.yml | Comprehensive validation |

### Monitoring & Analytics
| Technology | Version | Usage |
|---|---|---|
| Datadog | 3.6.0 | RUM, logs, analytics |
| Firebase Crashlytics | Latest via BOM | Crash reporting |
| Firebase Analytics | Latest via BOM | Usage analytics |
| Firebase Messaging | Latest via BOM | Push notifications |

## Development Tools

### Linting & Formatting
| Tool | Configuration |
|---|---|
| ktlint | `ktlint_official` style, 2-space indent, 120 char max, trailing commas |
| Android Lint | Custom rules in `hedvig-lint` module |
| Sort Dependencies | Plugin for consistent dependency ordering |

### Code Generation
| Tool | Usage |
|---|---|
| Apollo Codegen | GraphQL type-safe Kotlin from `.graphql` files |
| KSP (2.3.2) | Annotation processing for Room, etc. |

## Testing
| Technology | Version | Usage |
|---|---|---|
| JUnit 4 | 4.13.2 | Unit test framework |
| Turbine | 1.2.1 | Flow testing |
| AssertK | 0.28.1 | Fluent assertions |
| Robolectric | 4.16.1 | Android test support |
| TestParameterInjector | 1.21 | Parameterized testing |
| Molecule Test Utils | 2.2.0 | Presenter testing |

## Localization
| Technology | Usage |
|---|---|
| Lokalise | Translation management platform |
| lokalise-gradle-plugin | Automated translation download |

## Version Management
- All dependency versions centralized in `gradle/libs.versions.toml`
- BOMs used for framework version alignment (Compose, Firebase, OkHttp, Koin)
- Type-safe project accessors for inter-module dependencies
- `dependencyAnalysis` plugin (3.5.1) for dependency health monitoring

---
*Last Updated*: 2026-04-07
*Auto-detected*: All technologies and versions detected from `gradle/libs.versions.toml`, `build.gradle.kts` files, and source code analysis
