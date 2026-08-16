# Exceptions in this codebase

Two exception types actually appear in the code. A third can happen but is
never explicitly thrown or caught — it's worth knowing about anyway. This
covers all three, where each comes from, and how it's turned into an HTTP
response.

## `jakarta.persistence.EntityNotFoundException`

**What it is**: a standard exception from the JPA spec itself (not
Spring-specific), meaning "a lookup for a specific entity found nothing."

**Where it's thrown**: `MenuItemService` and `DiningTableService` each have a
private `findOrThrow(Long id)` that wraps `repository.findById(id)`:

```java
private MenuItem findOrThrow(Long id) {
    return menuItemRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Menu item " + id + " not found"));
}
```

`findById`, `updateDetails`, `markUnavailable`, `markAvailable` (and the
`DiningTableService` equivalents `findById`, `reserve`, `release`,
`updateSeats`) all route through this one method, so every id-lookup in the
service layer fails the same way instead of each method reinventing its own
"not found" logic.

**Why throw instead of returning `null` or `Optional`**: a thrown exception
can't be silently ignored the way a `null` can. A caller that forgets to
check `if (result == null)` gets a `NullPointerException` two calls later,
somewhere that has no idea what was actually missing. A caller that forgets
to check `Optional.isPresent()` gets a clear crash at `.get()`, but still has
to remember to write the check at all. Throwing at the point where the id
lookup fails puts the informative part — *which* id, *which* entity type —
right there in the exception message, one call away from the actual mistake.

**How it's caught**: `ApiExceptionHandler.handleNotFound` maps it to an HTTP
`404` with a JSON body `{"message": "Menu item 5 not found"}`. Without this
handler, an uncaught `EntityNotFoundException` would surface to the client as
an unhelpful `500 Internal Server Error`.

**How it's tested**:
- Service tests (`MenuItemServiceTest`, `DiningTableServiceTest`) mock the
  repository to return `Optional.empty()` and assert the service method
  throws `EntityNotFoundException` — e.g. `updateDetails_throwsWhenMenuItemDoesNotExist`.
- Controller tests (`MenuItemControllerTest`, `DiningTableControllerTest`)
  mock the *service* to throw it directly and assert the HTTP response is a
  `404` with the expected `message` — e.g. `findById_returns404WhenMenuItemDoesNotExist`.

## `org.springframework.web.bind.MethodArgumentNotValidException`

**What it is**: Spring MVC's exception for "a `@RequestBody` argument
annotated `@Valid` failed Bean Validation." Nothing in this codebase throws
it explicitly — Spring's argument-resolving machinery throws it for you, as
soon as it tries to bind an invalid request body to a controller parameter.

**Where it comes from**: every controller method that takes a body declares
it with `@Valid`, e.g.

```java
public MenuItemResponse create(@Valid @RequestBody MenuItemRequest request) { ... }
```

and the constraints live on the request record itself:

```java
public record MenuItemRequest(
        @NotBlank String name,
        @NotNull MenuItemCategory category,
        @NotNull @DecimalMin("0.01") BigDecimal price,
        @Positive int preparationMinutes) {
}
```

If an incoming JSON body fails any of those (blank `name`, `price` of `0`,
missing `category`, ...), Spring throws this exception *before* the
controller method body ever runs — the service is never called with bad
data.

**How it's caught**: `ApiExceptionHandler.handleInvalid` maps it to an HTTP
`400`. It walks `ex.getBindingResult().getFieldErrors()` and joins each
`field + " " + defaultMessage` into one string, so a client posting
`{"name": "", "price": 0, ...}` gets back something like
`{"message": "name must not be blank, price must be greater than or equal to 0.01"}`
— which field, and what was wrong with it, in one response.

**How it's tested**: controller tests post a deliberately invalid body and
assert `400` — e.g. `create_returns400WhenNameIsBlank`,
`create_returns400WhenSeatsIsNotPositive`.

## `jakarta.validation.ConstraintViolationException` (not currently handled)

**What it is**: the entities themselves — `MenuItem`, `DiningTable` — carry
the same kind of Bean Validation annotations as the request DTOs
(`@NotBlank`, `@Positive`, `@DecimalMin`, `@NotNull`), directly on their
fields. Hibernate re-validates an entity against those constraints whenever
it's about to write it to the database (on `persist`/`flush`, not just on
construction). If an invalid entity ever reaches that point, Hibernate throws
`jakarta.validation.ConstraintViolationException` — a different class from
Spring's `MethodArgumentNotValidException`, despite the similar name.

**Why it exists alongside the DTO validation**: in normal use, this can't
actually fire — every write goes through a controller, which validates the
`MenuItemRequest`/`DiningTableRequest` *before* the service ever constructs
or mutates an entity. The entity-level annotations are a second line of
defense against any *other* path that skips the controller: a future batch
job, a data migration script, or (today) a service method called directly
from a test or from code that isn't the HTTP layer.

**The gap**: `ApiExceptionHandler` has no `@ExceptionHandler` for this type.
If it were ever thrown during a real request, it would fall through to
Spring Boot's default error handling and come back as a `500`, not a clean
`400` — even though, conceptually, it's the same kind of problem
(`MethodArgumentNotValidException` is validation-failed-on-the-way-in, this
is validation-failed-on-the-way-to-the-database). Worth adding a handler for
if this project grows a second entry point into the entities that isn't the
REST layer.

## Not a domain exception: `throws Exception` in controller tests

`MenuItemControllerTest` and `DiningTableControllerTest` declare their
`@Test` methods `throws Exception` because `MockMvc.perform(...)` — the call
that sends a fake HTTP request through the controller — is declared to throw
the checked `java.lang.Exception`. That's a MockMvc API detail, not a
domain exception; it has nothing to do with `EntityNotFoundException` or
validation.

## Not an exception: `ApiError`

`ApiError` (`controller/dto/ApiError.java`) is worth naming here only to
rule it out — it's a plain record, `record ApiError(String message)`, used
as the *response body* for both error paths above. It doesn't extend
`Exception` and is never thrown; it's what gets serialized to JSON after an
exception has already been caught.
