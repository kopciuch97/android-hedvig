# Development Roadmap

## Current State
- **Version**: 14.0.10
- **Key Features**: Insurance management, claims filing, chat support, payments, referral program (Forever), profile management
- **Architecture**: 80+ modules (27 feature, 14 core, 13 data, 6 Apollo, design system, navigation, auth, database)

## Planned Enhancements

### High Priority
- [ ] **Feature Development** — Continue expanding insurance product offerings and user-facing capabilities
- [ ] **KMP Migration** — Migrate remaining modules to Kotlin Multiplatform (currently 54% coverage)

### Medium Priority
- [ ] **Code Quality** — Improve test coverage reporting in CI/CD pipeline
- [ ] **Documentation** — Add Architecture Decision Records (ADRs) for key decisions
- [ ] **Performance** — Establish performance testing workflows for critical user journeys

### Technical Debt
- [ ] **KDoc Coverage** — Add documentation comments to public API modules (data layer, use cases)
- [ ] **Resource Cleanup** — Automate unused resource detection (currently manual workflow)

## Future Considerations
- **Full KMP**: Complete migration of all eligible modules to Kotlin Multiplatform
- **Architecture Documentation**: Visual module dependency diagrams in main documentation
- **Scalability**: Performance profiling and optimization for growing feature set

---
*Assessment based on project analysis performed 2026-04-07*
