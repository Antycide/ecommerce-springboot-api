# E-Commerce (Spring Boot)

## Local setup

Prereqs:
- Java 22
- Docker (for Testcontainers)

Configuration (defaults are safe for local dev):
- DB: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- JWT: `JWT_SECRET_BASE64`, `JWT_EXPIRATION_MS`

Run the app:
```bash
./gradlew bootRun
```

## How to run tests

Run all tests (unit + integration):
```bash
./gradlew test
```

Generate coverage report:
```bash
./gradlew test jacocoTestReport
```

Report output:
- `build/reports/tests/test/index.html`
- `build/reports/jacoco/test/html/index.html`

## API endpoints

Auth:
- `POST /api/v1/auth/registration`
- `POST /api/v1/auth/login`

Admin products:
- `POST /api/admin/products`
- `GET /api/admin/products`
- `GET /api/admin/products/{id}`
- `PUT /api/admin/products/{id}`
- `DELETE /api/admin/products/{id}`

Admin Categories:
- `POST /api/categories`
- `GET /api/categories`
- `GET /api/categories/{id}`
- `PUT /api/categories/{id}`
- `DELETE /api/categories/{id}`

Products:
- `GET /api/products`
- `GET /api/products/{productId}`

OpenAPI:
- `GET /swagger-ui/index.html`
- `GET /v3/api-docs`

Cart:
- `POST /api/cart/{id}`
- `GET /api/cart`
- `PATCH /api/cart/increase/{id}`
- `PATCH /api/cart/decrease/{id}`
- `DELETE /api/cart/{id}`
- `DELETE /api/cart`

Addresses:
- `POST /api/addresses`
- `GET /api/addresses`
- `GET /api/addresses/{id}`
- `DELETE /api/addresses/{id}`

Orders & checkout:
- `POST /api/orders`
- `GET /api/orders`
- `POST /api/checkout/{id}`

Reviews:
- `POST /api/products/{productId}/reviews`
- `GET /api/products/{productId}/reviews`

Wishlist:
- `POST /api/wishlist/{productId}`
- `GET /api/wishlist`
- `GET /api/wishlist/{userId}`
- `DELETE /api/wishlist/{productId}`
