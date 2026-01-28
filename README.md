[![CI](https://github.com/Antycide/ecommerce-springboot-api/actions/workflows/ci.yml/badge.svg)](https://github.com/Antycide/ecommerce-springboot-api/actions/workflows/ci.yml)

# E-Commerce (Spring Boot)

Production-style e-commerce REST API built with Spring Boot 3, JWT auth, Spring Security, JPA/Hibernate, and MapStruct.
Includes unit + integration tests with Testcontainers and a CI workflow that runs on every push.

## Features
- JWT authentication with role-based access (ADMIN/CUSTOMER)
- Product, category, cart, order, checkout, wishlist, and review flows
- DTO-based API with validation and global error handling
- Postgres persistence via Spring Data JPA
- OpenAPI/Swagger documentation

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

Swagger UI:
- `http://localhost:8080/swagger-ui/index.html`

## Auth flow (quick start)

1) Register:
```http
POST /api/v1/auth/registration
```

2) Login:
```http
POST /api/v1/auth/login
```

3) Use the JWT:
```
Authorization: Bearer <token>
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

Products (ADMIN):
- `POST /api/admin/products`
- `GET /api/admin/products`
- `GET /api/admin/products/{id}`
- `PUT /api/admin/products/{id}`
- `DELETE /api/admin/products/{id}`

Categories (admin only):
- `POST /api/categories`
- `GET /api/categories`
- `GET /api/categories/{id}`
- `PUT /api/categories/{id}`
- `DELETE /api/categories/{id}`

Products (CUSTOMER):
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
