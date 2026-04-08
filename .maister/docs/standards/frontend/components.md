## Components

### Single Responsibility
Each component should do one thing well.

### Reusability
Design components to work across different contexts with configurable props.

### Composability
Build complex UIs by combining smaller components rather than creating monoliths.

### Clear Interface
Define explicit, documented props with sensible defaults.

### Encapsulation
Keep implementation details private; expose only what's necessary.

### Consistent Naming
Use descriptive names that indicate purpose and follow team conventions.

### Local State
Keep state as close to where it's used as possible; lift only when needed.

### Minimal Props
If a component needs many props, consider composition or splitting it.

### Documentation
Document usage, props, and examples to help team adoption.

### Jetpack Compose Only
100% Compose UI — no XML layouts. All UI is built with Jetpack Compose.

### Material 3 Restriction
Material 2 APIs are banned via lint error (`ComposeM2Api`). Material 3 is only used internally by `design-system-internals` — feature modules use the Hedvig design system.

### Composable Naming
Composable functions use PascalCase: `@Composable fun FeatureScreen()`. Regular functions use camelCase.

### Destination Composable Pattern
Screen entry points are `internal @Composable fun {Feature}Destination(viewModel, navigateUp, ...)`.
They collect state via `viewModel.uiState.collectAsStateWithLifecycle()` and delegate to a private `{Feature}Screen` composable.

### Feature Module Directory Structure
```
feature-{name}/src/main/kotlin/.../
  ui/           # Destination, ViewModel, Presenter, Screen composables
  navigation/   # Destination definitions, NavGraph
  di/           # Koin DI module
  data/         # (optional) Use cases, repositories
```

### Accessibility
UI changes must be checked for accessibility (enforced via PR template checklist).
