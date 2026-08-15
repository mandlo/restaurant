# Restaurant Persistence Practice

A small Spring Boot application for learning Java testing progressively. This
stage contains the persistence and service layers and their tests, but no
controllers yet. It targets Java 21, uses Maven, PostgreSQL, Spring Data JPA,
and includes Swagger/OpenAPI for the later HTTP layer.

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
    MenuItemService.java           menu item use cases: lookup, edit, retire
    DiningTableService.java        table use cases: lookup, reserve, release
src/test/java/com/restaurant/
  domain/                          fast unit tests with no Spring or database
  repository/                      database-backed persistence tests
  service/                         fast unit tests with a mocked repository
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
They hold the use cases a future controller layer will call: look items up,
edit them, retire or restore them. Each mutating method is `@Transactional` —
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

There is intentionally no controller layer yet. That can be added as a later
lesson once the service tests are comfortable.

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
mvn -Dtest='com.restaurant.domain.*Test,com.restaurant.service.*Test' test
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

Swagger UI is configured at:

- http://localhost:8080/swagger-ui.html
- http://localhost:8080/v3/api-docs

This persistence-only stage has no controllers, so Swagger will not list API
operations until the controller lesson is added.
