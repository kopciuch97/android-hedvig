## Coding Style

### Naming Consistency
Follow established naming patterns for variables, functions, classes, and files throughout the project.

### Automatic Formatting
Use automated tools to enforce consistent indentation, spacing, and line breaks.

### Descriptive Names
Choose names that clearly communicate intent; avoid cryptic abbreviations or single-letter identifiers outside tight loops.

### Focused Functions
Write functions that do one thing well; smaller functions are easier to read, test, and maintain.

### Uniform Indentation
Standardize on spaces or tabs and enforce with editor/linter settings.

### No Dead Code
Remove unused imports, commented-out blocks, and orphaned functions instead of leaving them behind.

### No Backward Compatibility Unless Required
Avoid extra code paths for backward compatibility unless explicitly needed.

### DRY (Don't Repeat Yourself)
Extract repeated logic into reusable functions or modules.

### Ktlint Code Formatting
The project uses `ktlint_official` code style enforced via .editorconfig:
- 2-space indentation (including continuation indent)
- 120 character max line length
- Unix LF line endings
- Trailing commas required on both declaration and call sites
- No wildcard imports — all imports must be explicit
- Final newline required

Always run `./gradlew ktlintFormat` before committing.

Disabled ktlint rules (deliberate): annotation, filename, property-naming, parameter-wrapping, property-wrapping, multiline-expression-wrapping, string-template-indent, function-expression-body, class-signature, chain-method-continuation.

### File & Class Naming
- Kotlin files: PascalCase matching primary class
- Module directories: kebab-case with hyphens only (no dots or underscores — build enforced)
- ViewModels: `{Feature}ViewModel`
- Presenters: `{Feature}Presenter`
- Destinations: `{Feature}Destination`
- Use cases: `{Action}{Domain}UseCase` (e.g., `GetHomeDataUseCase`)
- Composable functions: PascalCase (enforced via ktlint exemption)
- Regular functions: camelCase

### Sorted Dependencies
Dependencies in `build.gradle.kts` files must be sorted alphabetically (enforced by square/sort-dependencies plugin).

### Centralized Version Catalog
All dependency versions in `gradle/libs.versions.toml`. Use type-safe accessors: `libs.*` for libraries, `projects.*` for modules. Never hardcode versions in build files.
