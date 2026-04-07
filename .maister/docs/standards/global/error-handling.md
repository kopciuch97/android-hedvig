## Error Handling

### Clear User Messages
Show helpful, actionable messages without exposing internal details or security-sensitive information.

### Fail Fast
Validate inputs and check preconditions early; reject invalid data before it causes deeper issues.

### Typed Exceptions
Use specific exception types instead of generic ones to enable precise error handling.

### Centralized Handling
Catch and process errors at appropriate boundaries (controllers, API layers) rather than scattering try-catch throughout.

### Graceful Degradation
When non-critical services fail, continue operating with reduced functionality rather than crashing entirely.

### Retry with Backoff
Use exponential backoff for transient failures when calling external services.

### Resource Cleanup
Always release resources (file handles, connections) in finally blocks or equivalent cleanup mechanisms.

### Arrow Either for Error Handling
Use `Either<ErrorMessage, T>` from Arrow for all error-returning operations:
```kotlin
suspend fun invoke(): Either<ErrorMessage, Data> = either {
  apolloClient.query(MyQuery()).safeExecute().bind()
}
```
Consuming: `result.fold(ifLeft = { /* error */ }, ifRight = { /* success */ })`

### Apollo GraphQL Extensions
- `safeExecute()` for one-shot queries/mutations -> returns `Either`
- `safeFlow()` for watched/streaming queries
- Place `.graphql` files in `src/main/graphql/`
- Naming: `Query*.graphql`, `Mutation*.graphql`, `Fragment*.graphql`

### Navigation Destinations
Destinations are `@Serializable` data objects/classes implementing `Destination`:
```kotlin
@Serializable data object FeatureDestination : Destination
```
Navigation graphs use `navgraph<>` and `navdestination<>` extension functions with `koinViewModel()`.

### Use Case Pattern
Interface with `invoke()` + separate `Impl` class. Return `Either<ErrorMessage, T>` for error cases.
