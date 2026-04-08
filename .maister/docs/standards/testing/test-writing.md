## Test Writing

### Test Behavior
Focus on what code does, not how it does it, to allow safe refactoring.

### Clear Names
Use descriptive names explaining what's tested and expected (`shouldReturnErrorWhenUserNotFound`).

### Mock External Dependencies
Isolate tests by mocking databases, APIs, and external services.

### Fast Execution
Keep unit tests fast (milliseconds) so developers run them frequently.

### Risk-Based Testing
Prioritize testing based on business criticality and likelihood of bugs.

### Balance Coverage and Velocity
Adjust test coverage based on project needs and team workflow.

### Critical Path Focus
Ensure core user workflows and critical business logic are well-tested.

### Appropriate Depth
Match edge case testing to the risk profile of the code.

### PR Quality Gates
Every PR must pass 4 parallel CI jobs: unit tests (`./gradlew test`), Android lint, ktlint formatting, debug build. New pushes cancel in-progress runs.

### Molecule Presenter Testing
Use `presenter.test(initialState) { }` from molecule-test:
```kotlin
@Test
fun `descriptive name in backticks`() = runTest {
  val useCase = FakeUseCase()
  val presenter = FeaturePresenter(useCase)
  presenter.test(FeatureUiState.Loading) {
    skipItems(1)
    useCase.turbine.add(someData.right())
    assertThat(awaitItem()).isInstanceOf(FeatureUiState.Success::class)
  }
}
```

### Assertion Library: AssertK
Use AssertK exclusively for assertions — never JUnit assertEquals or Google Truth:
- `assertThat(x).isEqualTo(y)`
- `assertThat(x).isInstanceOf(Type::class)`
- `assertThat(x).prop(Type::field).isEqualTo(y)`

### Turbine for Fakes
Test fakes use `Turbine<Either<ErrorMessage, T>>`:
```kotlin
class FakeUseCase : UseCaseInterface {
  val turbine = Turbine<Either<ErrorMessage, ResultType>>()
  override suspend fun invoke() = turbine.awaitItem()
}
```

### Test Framework
JUnit 4 (`@Test`) with `kotlinx.coroutines.test.runTest`. Use `TestLogcatLoggingRule` as `@get:Rule`.

### Test Method Names
Use backtick-quoted descriptive names: `` `if receive error show error screen` ``

### Test File Locations
Unit tests: `src/test/kotlin/`. Android tests: `src/androidTest/kotlin/`. Shared test utilities in `-test` modules.
