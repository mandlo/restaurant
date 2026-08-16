# Restaurant Persistence Practice

A small Spring Boot application for learning Java testing progressively. It
now has a full slice through the stack — persistence, service, and a REST API
you can exercise from Swagger — each with its own testing style. It targets
Java 21, uses Maven, PostgreSQL, Spring Data JPA, and Spring MVC, with
springdoc-openapi generating the Swagger UI from the controllers.

## Technology

- Java 21
- Spring Boot 3.3.2
- Maven
- PostgreSQL
- Spring Data JPA and Hibernate
- springdoc-openapi 2.6.0
- JUnit 5, AssertJ, and Testcontainers

## Project structure

```text
src/main/java/com/restaurant/
  RestaurantApplication.java       Spring Boot entry point
  domain/
    MenuItem.java                  menu item entity and availability behavior
    MenuItemCategory.java          menu categories
    DiningTable.java               table entity and reservation behavior
  repository/
    MenuItemRepository.java        Spring Data menu item queries
    DiningTableRepository.java     Spring Data table queries
  service/
    MenuItemService.java           menu item use cases: lookup, edit, retire, restore
    DiningTableService.java        table use cases: lookup, reserve, release, edit
  controller/
    MenuItemController.java        REST endpoints under /api/menu-items
    DiningTableController.java     REST endpoints under /api/dining-tables
    ApiExceptionHandler.java       maps EntityNotFoundException and validation errors to HTTP status codes
    dto/                           request/response records, kept separate from the entities
src/test/java/com/restaurant/
  domain/                          fast unit tests with no Spring or database
  repository/                      database-backed persistence tests
  service/                         fast unit tests with a mocked repository
  controller/                      fast @WebMvcTest slice tests with a mocked service
  AbstractIntegrationTest.java    shared PostgreSQL Testcontainers setup
```

## How the code fits together

`MenuItem` and `DiningTable` are JPA entities. Their fields become database
columns, while `@Id` and `@GeneratedValue` define their primary keys. The
entities also contain small state transitions, such as marking a menu item
unavailable or reserving a table.

`MenuItemRepository` and `DiningTableRepository` are interfaces. Spring Data
JPA generates their implementations from `JpaRepository` and derives the
custom query methods from their names. For example,
`findBySeatsGreaterThanEqualAndAvailableTrue` finds tables that have enough
seats and are not reserved.

The domain tests are true unit tests: they instantiate an entity directly and
verify one behavior at a time. They are fast and do not need Spring, Maven
infrastructure beyond the test dependencies, PostgreSQL, or Docker.

The repository tests use `@SpringBootTest` and Testcontainers because a
repository's real behavior depends on JPA, Hibernate, and PostgreSQL. Each test
starts with a fresh PostgreSQL container and verifies the generated query
against a real database. This is an integration test, not a unit test, and is
the next testing step after the domain tests.

`MenuItemService` and `DiningTableService` sit on top of the repositories.
They hold the use cases the controllers below call: look items up, edit them,
retire or restore them. Each mutating method is `@Transactional` —
that's what makes the entity *managed* for the duration of the call, so
editing it in place (see below) is enough; the change flushes to the database
when the transaction commits. A missing id raises
`jakarta.persistence.EntityNotFoundException` rather than returning `null`,
so callers don't have to remember to null-check.

The service tests are unit tests too, but of a different shape than the
domain tests: they use Mockito (`@ExtendWith(MockitoExtension.class)`,
`@Mock`) to fake the repository instead of hitting a database, so they can
verify the service's own logic — delegation, the not-found exception — in
isolation and without Spring or Docker.

`MenuItemController` and `DiningTableController` translate HTTP to service
calls. Request and response bodies are records in `controller/dto/`, not the
entities themselves — `MenuItemRequest` carries its own Bean Validation
annotations (`@NotBlank`, `@DecimalMin`, ...), and `MenuItemResponse.from(...)`
maps an entity to the shape returned over the wire. Keeping that mapping at
the controller boundary means the entity is free to change internally without
changing the API, and the API is free to omit or reshape fields without
touching the entity.

`ApiExceptionHandler` is a `@RestControllerAdvice`: it catches
`EntityNotFoundException` (thrown by the services) and turns it into a `404`
with a `{"message": "..."}` body, and catches Bean Validation failures on
`@Valid @RequestBody` arguments and turns them into a `400` the same way.
Without it, both would otherwise surface as an unhelpful `500`.

The controller tests use `@WebMvcTest`, a slice test that boots only the web
layer — no database, no service implementation, no Docker. `@MockBean`
replaces the real service with a Mockito mock, and `MockMvc` sends real HTTP
requests through real Spring MVC request handling (routing, JSON
(de)serialization, validation, the exception handler) so what's being tested
is the wiring, not just a Java method call.

### Editing entities in place

`MenuItem.updateDetails(...)` and `DiningTable.updateSeats(...)` mutate an
entity's fields directly, the same way `markReserved()` and
`markUnavailable()` do. As long as the entity is *managed* — loaded from the
database within an open persistence context, not detached — Hibernate
tracks the change and writes it back on the next flush without needing an
explicit `save()` call. This is called dirty checking, and
`MenuItemRepositoryTest.editingManagedEntity_flushesChangesWithoutExplicitSave`
demonstrates it: it loads a `MenuItem`, edits it in place, calls
`entityManager.flush()`, then re-reads it to confirm the change reached the
database.

## Data model

Two entities, no relationship between them yet — see the full
[entity-relationship diagram](docs/erd.md) for fields, constraints, and
repository queries.

## Run unit tests only

```bash
mvn -Dtest='com.restaurant.domain.*Test,com.restaurant.service.*Test,com.restaurant.controller.*Test' test
```

## Run persistence tests

Docker must be running because Testcontainers starts PostgreSQL automatically:

```bash
mvn test
```

## Run the application

For local application startup, create a PostgreSQL database and user matching
`src/main/resources/application.yml`:

```bash
createdb restaurant
mvn spring-boot:run
```

Swagger UI is at http://localhost:8080/swagger-ui.html (raw OpenAPI JSON at
http://localhost:8080/v3/api-docs). The database starts empty, so create a
table and a menu item first and reuse the `id` each response returns for the
rest of the walkthrough below.

## Try the API with Swagger

Every endpoint is grouped in Swagger UI under its tag ("Menu items" /
"Dining tables"). Expand an operation, click **Try it out**, fill in the body
or parameters, then **Execute**.

### Menu items — `/api/menu-items`

| Method | Path | Body | Does |
|---|---|---|---|
| POST | `/api/menu-items` | `MenuItemRequest` | add a menu item |
| GET | `/api/menu-items/{id}` | — | look up one item |
| GET | `/api/menu-items?category=MAIN` | — | list available items in a category |
| PUT | `/api/menu-items/{id}` | `MenuItemRequest` | edit name, price, prep time |
| PATCH | `/api/menu-items/{id}/unavailable` | — | take off the menu |
| PATCH | `/api/menu-items/{id}/available` | — | put back on the menu |

Example `MenuItemRequest` — POST this first, then reuse the returned `id`:

```json
{
  "name": "Truffle Pasta",
  "category": "MAIN",
  "price": 18.00,
  "preparationMinutes": 25
}
```

`category` must be one of `STARTER`, `MAIN`, `DESSERT`, `DRINK`. Try `POST`
again with `"name": ""` or `"price": 0` — you'll get a `400` with a
`message` field naming what's wrong, instead of a `500`.

### Dining tables — `/api/dining-tables`

| Method | Path | Body | Does |
|---|---|---|---|
| POST | `/api/dining-tables` | `DiningTableRequest` | add a table |
| GET | `/api/dining-tables/{id}` | — | look up one table |
| GET | `/api/dining-tables?minSeats=4` | — | list available tables with enough seats |
| PUT | `/api/dining-tables/{id}/seats` | `UpdateSeatsRequest` | change seat count |
| PATCH | `/api/dining-tables/{id}/reserve` | — | reserve a table |
| PATCH | `/api/dining-tables/{id}/release` | — | release a reserved table |

Example `DiningTableRequest`:

```json
{ "tableNumber": 5, "seats": 4 }
```

Example `UpdateSeatsRequest`:

```json
{ "seats": 6 }
```

A full walkthrough: `POST` a table with `seats: 4`, `PATCH .../reserve` it and
watch `available` flip to `false` in the response, then `PATCH .../release`
and watch it flip back. Looking up, reserving, releasing, or editing an `id`
that doesn't exist returns `404` with `{"message": "Dining table 99 not
found"}` rather than an unhandled exception — that's `ApiExceptionHandler`
again, reused across both controllers.
