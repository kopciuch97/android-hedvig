# Documentation Index

**IMPORTANT**: Read this file at the beginning of any development task to understand available documentation and standards.

## Quick Reference

### Project Documentation
Project-level documentation covering vision, goals, architecture, and technology choices.

### Technical Standards
Coding standards, conventions, and best practices organized by domain.

---

## Project Documentation

Located in `.maister/docs/project/`

### Vision (`project/vision.md`)
Hedvig Android project vision and purpose: insurance management app for Nordic customers, current state (v14.0.10, 7 years, 13,655 commits), goals for KMP migration, quality improvements, and feature expansion over the next 6-12 months.

### Roadmap (`project/roadmap.md`)
Development roadmap with prioritized enhancements: feature development and KMP migration (high), test coverage and ADRs (medium), KDoc coverage and resource cleanup (tech debt), and future considerations for full KMP and architecture documentation.

### Tech Stack (`project/tech-stack.md`)
Complete technology inventory: Kotlin 2.3.10, Jetpack Compose (100% Compose, no XML), Apollo GraphQL 4.4.1, Molecule 2.2.0 for MVI, Koin 4.1.1 for DI, Room 2.8.4, Ktor 3.4.0, Arrow 2.2.1.1, AGP 9.0.0, GitHub Actions CI/CD, Datadog and Firebase monitoring, ktlint formatting, and all dependency versions.

### Architecture (`project/architecture.md`)
System architecture: feature-based modular MVI with Molecule, 80+ modules across feature (27), data (13), core (14), Apollo (6), design system, and navigation layers. Strict module dependency rules (no cross-feature deps), data flow from Compose UI through ViewModel/Presenter to repositories and Apollo GraphQL, external integrations, and build configuration.

---

## Technical Standards

### Global Standards

Located in `.maister/docs/standards/global/`

#### Coding Style (`standards/global/coding-style.md`)
Naming consistency, automatic formatting, descriptive names, focused functions, uniform indentation, dead code removal, DRY principle, avoiding unnecessary backward compatibility code. Project-specific: ktlint_official code style (2-space indent, 120 char lines, trailing commas, no wildcard imports, disabled rules), file and class naming conventions (PascalCase files, kebab-case modules, ViewModel/Presenter/Destination/UseCase naming), sorted dependencies via square/sort-dependencies plugin, centralized version catalog in libs.versions.toml.

#### Commenting (`standards/global/commenting.md`)
Self-documenting code practices, sparing comment usage, and avoiding changelog-style comments in code.

#### Development Conventions (`standards/global/conventions.md`)
Predictable file structure, up-to-date documentation, clean version control, environment variables, minimal dependencies, consistent reviews, testing standards, feature flags, and changelog maintenance. Project-specific: MVI with Molecule pattern (MoleculeViewModel/MoleculePresenter, CollectEvents, sealed interface Events/UiState), LoadIteration retry pattern, feature module isolation (no cross-feature deps, build-time enforced), internal visibility for feature module classes, module organization (suffixes, KMP layout, auto-discovery), convention plugin DSL (compose, apollo, serialization, androidResources), JVM 21 with Kotlin 2.2 (context parameters, expect-actual, when guards), Koin DI pattern with ProdOrDemoProvider.

#### Error Handling (`standards/global/error-handling.md`)
Clear user messages, fail-fast validation, typed exceptions, centralized handling, graceful degradation, retry with backoff, and resource cleanup. Project-specific: Arrow Either for error handling (Either<ErrorMessage, T>, either builder, safeExecute/safeFlow), Apollo GraphQL extensions and .graphql file naming, navigation destinations (@Serializable with Destination interface, navgraph/navdestination), use case pattern (interface with invoke + Impl class returning Either).

#### Minimal Implementation (`standards/global/minimal-implementation.md`)
Building only what is needed, deleting exploration artifacts, avoiding future stubs and speculative abstractions, reviewing before commit, and treating unused code as debt.

#### Validation (`standards/global/validation.md`)
Server-side validation, client-side feedback, early input checking, specific error messages, allowlists over blocklists, type/format checks, input sanitization, business rule validation, and consistent enforcement across entry points.

### Frontend Standards

Located in `.maister/docs/standards/frontend/`

#### Accessibility (`standards/frontend/accessibility.md`)
Semantic HTML, keyboard navigation, color contrast, alt text and labels, screen reader testing, ARIA usage, heading structure, and focus management.

#### Components (`standards/frontend/components.md`)
Single responsibility, reusability, composability, clear interfaces, encapsulation, consistent naming, local state management, minimal props, and component documentation. Project-specific: Jetpack Compose only (no XML), Material 3 restriction (M2 banned via lint, M3 internal to design-system-internals), composable PascalCase naming, Destination composable pattern (internal @Composable with collectAsStateWithLifecycle delegating to private Screen), feature module directory structure (ui/navigation/di/data), accessibility checklist enforcement via PR template.

#### CSS (`standards/frontend/css.md`)
Consistent methodology (Tailwind/BEM/modules), working with the framework, design tokens, minimizing custom CSS, and production optimization.

#### Responsive Design (`standards/frontend/responsive.md`)
Mobile-first approach, standard breakpoints, fluid layouts, relative units, cross-device testing, touch-friendly targets, mobile performance, readable typography, and content priority.

### Backend Standards

*Not initialized for this project. If you need backend standards, you can:*
- *Add them manually using the docs-manager skill*
- *Run `/maister:standards-discover --scope=backend` to auto-discover*

### Testing Standards

Located in `.maister/docs/standards/testing/`

#### Test Writing (`standards/testing/test-writing.md`)
Testing behavior over implementation, clear test names, mocking external dependencies, fast execution, risk-based testing, balancing coverage and velocity, critical path focus, and appropriate test depth. Project-specific: PR quality gates (4 parallel CI jobs: unit tests, Android lint, ktlint, debug build), Molecule presenter testing (presenter.test with molecule-test), AssertK assertion library (exclusively, no JUnit assertEquals or Google Truth), Turbine for test fakes (Turbine<Either<ErrorMessage, T>>), JUnit 4 with kotlinx.coroutines.test.runTest and TestLogcatLoggingRule, backtick-quoted test method names, test file locations (src/test/kotlin, src/androidTest/kotlin, -test modules).

---

## How to Use This Documentation

1. **Start Here**: Always read this INDEX.md first to understand what documentation exists
2. **Project Context**: Read relevant project documentation before starting work
3. **Standards**: Reference appropriate standards when writing code
4. **Keep Updated**: Update documentation when making significant changes
5. **Customize**: Adapt all documentation to your project's specific needs

## Updating Documentation

- Project documentation should be updated when goals, tech stack, or architecture changes
- Technical standards should be updated when team conventions evolve
- Always update INDEX.md when adding, removing, or significantly changing documentation
