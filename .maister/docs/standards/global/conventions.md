## Development Conventions

### Predictable Structure
Organize files and directories in a logical, navigable layout.

### Up-to-Date Documentation
Keep README files current with setup steps, architecture overview, and contribution guidelines.

### Clean Version Control
Write clear commit messages, use feature branches, and add meaningful descriptions to pull requests.

### Environment Variables
Store configuration in environment variables; never commit secrets or API keys.

### Minimal Dependencies
Keep dependencies lean and up-to-date; document why major ones are included.

### Consistent Reviews
Follow a defined code review process with clear expectations for reviewers and authors.

### Testing Standards
Define required test coverage (unit, integration, etc.) before merging.

### Feature Flags
Use flags for incomplete features instead of long-lived branches.

### Changelog Updates
Maintain a changelog or release notes for significant changes.

### Build What's Needed
Avoid speculative code and "just in case" additions (see minimal-implementation.md).

### MVI with Molecule Pattern
ViewModels extend `MoleculeViewModel<Event, UiState>` delegating to a `MoleculePresenter<Event, UiState>`. Flow: User Action -> Event -> Presenter -> UiState -> UI.

Presenter uses `@Composable present()` with `CollectEvents { event -> when(event) { } }` for event handling.

Events and UiState are `internal sealed interface` with:
- Events: `data object` for parameterless, `data class` for parameterized
- UiState: typically Loading, Success (data class), Failure variants

### LoadIteration Retry Pattern
```kotlin
var loadIteration by remember { mutableIntStateOf(0) }
LaunchedEffect(loadIteration) { /* load data */ }
CollectEvents { event -> when (event) { Event.Reload -> loadIteration++ } }
```

### Feature Module Isolation
Feature modules CANNOT depend on other feature modules (build-time enforced). Shared code must go in data-*, core-*, or library modules.

### Internal Visibility
Feature module classes (Presenters, ViewModels, Events, UiState) are `internal`. Only DI modules (`val xModule = module { }`) and navigation graph extension functions are public.

### Module Organization
- Module suffixes: `-public` (API/interfaces), `-android` (implementation), `-test` (test utils)
- KMP modules skip suffixes, use `commonMain`/`androidMain` directories
- Auto-discovery: any dir with `build.gradle.kts` under `app/` is auto-included
- Repository mode strict: no per-module repository declarations

### Convention Plugin DSL
Module build files use:
```kotlin
hedvig {
  compose()           // Enable Jetpack Compose
  apollo("octopus")   // Enable Apollo codegen
  serialization()     // Enable kotlinx.serialization
  androidResources()  // Enable Android resources (disabled by default)
}
```

### JVM & Kotlin Version
JVM target 21 (Zulu JDK in CI). Kotlin language/API version 2.2 with experimental features: context parameters, expect-actual classes, when guards.

### Koin DI Pattern
Each module declares `val xModule = module { }` in `di/` package:
```kotlin
val featureModule = module {
  single<UseCaseInterface> { UseCaseImpl(get<ApolloClient>()) }
  viewModel<FeatureViewModel> { FeatureViewModel(get<UseCaseInterface>()) }
}
```
Use `Provider<T>` via `ProdOrDemoProvider` only when demo mode needs different behavior.
