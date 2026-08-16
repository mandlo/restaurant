# Running the tests, and trying the API in Swagger

## Run unit tests only

Domain, service, and controller tests are all fast: no Spring context beyond
what `@WebMvcTest` boots for the controller slice, no database, no Docker.

```bash
mvn -Dtest='com.restaurant.domain.*Test,com.restaurant.service.*Test,com.restaurant.controller.*Test' test
```

## Run persistence tests

The repository tests are integration tests: `@SpringBootTest` plus
Testcontainers, which starts a real PostgreSQL container per test class.
Docker must be running.

```bash
mvn test
```

This also re-runs the fast tests above — `mvn test` runs the whole suite.

## Run the application

Swagger UI talks to a running instance of the app, which needs a real
PostgreSQL database. Create one matching
[`src/main/resources/application.yml`](../src/main/resources/application.yml):

```bash
createdb restaurant
mvn spring-boot:run
```

Then open Swagger UI at **http://localhost:8080/swagger-ui.html** (raw
OpenAPI JSON at http://localhost:8080/v3/api-docs). The database starts
empty, so create a table and a menu item first and reuse the `id` each
response returns for the rest of the walkthrough below.

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
