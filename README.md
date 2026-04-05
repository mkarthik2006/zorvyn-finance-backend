# Finance Data Processing & Access Control Backend

A comprehensive backend system for a finance dashboard, built with **Spring Boot 3**, **PostgreSQL**, **JWT Authentication**, and **Role-Based Access Control (RBAC)**.

> **Zorvyn FinTech Pvt. Ltd. — Backend Developer Intern Assignment**

---

## Table of Contents

- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Features](#features)
- [Role-Based Access Control](#role-based-access-control)
- [API Documentation](#api-documentation)
- [Setup Instructions](#setup-instructions)
- [Running Tests](#running-tests)
- [Assumptions](#assumptions)
- [Design Decisions](#design-decisions)
- [Trade-offs](#trade-offs)
- [Data Model](#data-model)
- [Project Structure](#project-structure)
- [Assignment Compliance Summary](#assignment-compliance-summary)

---

## Architecture

```
Controller Layer (REST APIs)
       |
Service Layer (Business Logic)
       |
Repository Layer (Data Access - JPA)
       |
Database (PostgreSQL)

Cross-cutting:
+-- Security (JWT + Spring Security + RBAC)
+-- Exception Handling (@ControllerAdvice)
+-- DTO Validation (Jakarta Bean Validation)
+-- Standardized API Response Wrapper
```

**Separation of Concerns:**

- **Controllers** handle HTTP requests and responses only
- **Services** contain all business logic
- **Repositories** provide data access via Spring Data JPA
- **DTOs** decouple API contracts from entities, preventing data leakage
- **Security** provides JWT-based stateless authentication with role enforcement

---

## Tech Stack

| Component        | Technology                          |
|------------------|-------------------------------------|
| Language         | Java 17                             |
| Framework        | Spring Boot 3.2.4                   |
| Security         | Spring Security + JWT (jjwt 0.12.5) |
| Database         | PostgreSQL 16                       |
| ORM              | Spring Data JPA / Hibernate 6       |
| Validation       | Jakarta Bean Validation             |
| API Docs         | SpringDoc OpenAPI (Swagger UI)      |
| Build Tool       | Maven                               |
| Containerization | Docker + Docker Compose             |
| Testing          | JUnit 5 + Mockito + MockMvc         |

---

## Features

### Core

- **User Management** — Full CRUD with role assignment and active/inactive status management
- **Financial Records** — Full CRUD with filtering, pagination, and sorting
- **Dashboard Analytics** — Summary, category-wise breakdown, monthly trends, recent activity
- **Role-Based Access Control** — JWT + `@PreAuthorize` enforcement at every endpoint
- **Validation** — DTO-level (`@NotNull`, `@Positive`, `@PastOrPresent`, `@Email`) + business rules
- **Global Exception Handling** — `@ControllerAdvice` with proper HTTP status codes (400, 401, 403, 404, 409, 500)
- **Standardized Response Format** — Every endpoint returns `{success, message, data, timestamp}`

### Enhancements

- **Security Hardening** — Public registration defaults to restricted `VIEWER` role; privilege escalation prevented
- **Global Soft Delete** — Automated filtering via Hibernate `@SQLRestriction` ensures deleted data never leaks into any query, join, or aggregation
- **JWT Authentication** — Stateless, token-based auth using modern JJWT 0.12.x API
- **Pagination & Sorting** — Configurable page size, sort field, and direction on all list endpoints
- **Swagger UI** — Interactive API documentation at `/swagger-ui.html` with Bearer token support
- **Docker Support** — Full `docker-compose.yml` with health checks for one-command setup
- **Unit Tests** — Comprehensive service layer + controller layer tests with Mockito and MockMvc (26 tests, 0 failures)

---

## Role-Based Access Control

### Authorization Strategy

- **JWT-based authentication** — Stateless, no server-side sessions
- **Role-based authorization** — Enforced via Spring Security `@PreAuthorize` annotations and URL-level security rules
- **Zero-Trust Registration** — All new registrations default to `VIEWER`. Elevated roles (`ADMIN`, `ANALYST`) can only be assigned by an existing Admin via the `/api/users` management endpoint. This prevents privilege escalation attacks.

### Role Permissions

| Endpoint                       | ADMIN | ANALYST | VIEWER |
|--------------------------------|:-----:|:-------:|:------:|
| `POST /api/auth/*`             |  Yes  |   Yes   |  Yes   |
| `GET /api/users`               |  Yes  |   No    |   No   |
| `POST /api/users`              |  Yes  |   No    |   No   |
| `PUT /api/users/{id}`          |  Yes  |   No    |   No   |
| `PATCH /api/users/{id}/status` |  Yes  |   No    |   No   |
| `DELETE /api/users/{id}`       |  Yes  |   No    |   No   |
| `POST /api/records`            |  Yes  |   No    |   No   |
| `PUT /api/records/{id}`        |  Yes  |   No    |   No   |
| `DELETE /api/records/{id}`     |  Yes  |   No    |   No   |
| `GET /api/records`             |  Yes  |   Yes   |   No   |
| `GET /api/records/{id}`        |  Yes  |   Yes   |   No   |
| `GET /api/dashboard/*`         |  Yes  |   Yes   |  Yes   |

### Role Behavior Summary

| Role    | Capabilities                                                             |
|---------|--------------------------------------------------------------------------|
| Admin   | Full access — manage users, create/update/delete records, view dashboard |
| Analyst | Read financial records + access all dashboard analytics                  |
| Viewer  | Dashboard view only (summary, trends, categories, recent activity)       |

---

## API Documentation

> **Swagger UI:** After starting the application, visit [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) for interactive API exploration with Bearer token authentication.

### Authentication

| Method | Endpoint             | Description                              | Auth Required |
|--------|----------------------|------------------------------------------|:---:|
| POST   | `/api/auth/register` | Register a new user (defaults to VIEWER) | No  |
| POST   | `/api/auth/login`    | Login and receive JWT token              | No  |

### User Management (ADMIN only)

| Method | Endpoint                  | Description                |
|--------|---------------------------|----------------------------|
| GET    | `/api/users`              | List all active users      |
| GET    | `/api/users/{id}`         | Get user by ID             |
| POST   | `/api/users`              | Create a user with role    |
| PUT    | `/api/users/{id}`         | Update a user              |
| PATCH  | `/api/users/{id}/status`  | Activate/deactivate a user |
| DELETE | `/api/users/{id}`         | Soft delete a user         |

### Financial Records

| Method | Endpoint             | Description                       | Roles          |
|--------|----------------------|-----------------------------------|----------------|
| GET    | `/api/records`       | List records (filter, page, sort) | ADMIN, ANALYST |
| GET    | `/api/records/{id}`  | Get record by ID                  | ADMIN, ANALYST |
| POST   | `/api/records`       | Create a record                   | ADMIN          |
| PUT    | `/api/records/{id}`  | Update a record                   | ADMIN          |
| DELETE | `/api/records/{id}`  | Soft delete a record              | ADMIN          |

**Filter Parameters:**

```
GET /api/records?type=INCOME&category=Salary&startDate=2026-01-01&endDate=2026-03-31&page=0&size=10&sortBy=date&sortDir=desc
```

All filter parameters are optional and can be combined freely.

### Dashboard Analytics

| Method | Endpoint                         | Description                        | Roles |
|--------|----------------------------------|------------------------------------|-------|
| GET    | `/api/dashboard/summary`         | Total income, expense, net balance | ALL   |
| GET    | `/api/dashboard/category-wise`   | Category-wise breakdown            | ALL   |
| GET    | `/api/dashboard/monthly-trends`  | Monthly income/expense trends      | ALL   |
| GET    | `/api/dashboard/recent-activity` | Recent financial records           | ALL   |

### Sample Request/Response

**Register (defaults to VIEWER for security):**

```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "Karthik M",
  "email": "karthik@zorvyn.com",
  "password": "securePass123"
}
```

**Response (201 Created):**

```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "userId": 1,
    "name": "Karthik M",
    "email": "karthik@zorvyn.com",
    "role": "VIEWER"
  },
  "timestamp": "2026-04-02T10:00:00"
}
```

**Login:**

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "karthik@zorvyn.com",
  "password": "securePass123"
}
```

**Create Financial Record (ADMIN only):**

```http
POST /api/records
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "amount": 5000.00,
  "type": "INCOME",
  "category": "Salary",
  "date": "2026-03-15",
  "notes": "Monthly salary"
}
```

**Response (201 Created):**

```json
{
  "success": true,
  "message": "Record created successfully",
  "data": {
    "id": 1,
    "amount": 5000.00,
    "type": "INCOME",
    "category": "Salary",
    "date": "2026-03-15",
    "notes": "Monthly salary",
    "createdById": 1,
    "createdByName": "Karthik M",
    "createdAt": "2026-03-15T10:30:00",
    "updatedAt": "2026-03-15T10:30:00"
  },
  "timestamp": "2026-03-15T10:30:00"
}
```

**Dashboard Summary:**

```http
GET /api/dashboard/summary
Authorization: Bearer <jwt-token>
```

```json
{
  "success": true,
  "message": "Summary retrieved successfully",
  "data": {
    "totalIncome": 50000.00,
    "totalExpense": 18500.00,
    "netBalance": 31500.00,
    "totalRecords": 25,
    "incomeCount": 10,
    "expenseCount": 15
  },
  "timestamp": "2026-04-02T10:00:00"
}
```

**Error Responses:**

```json
// 400 Bad Request — Validation
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "amount": "Amount must be greater than zero",
    "date": "Date cannot be in the future"
  },
  "timestamp": "2026-04-02T10:00:00"
}

// 401 Unauthorized
{
  "success": false,
  "message": "Unauthorized: Authentication is required to access this resource",
  "timestamp": "2026-04-02T10:00:00"
}

// 403 Forbidden
{
  "success": false,
  "message": "You do not have permission to perform this action",
  "timestamp": "2026-04-02T10:00:00"
}

// 404 Not Found
{
  "success": false,
  "message": "Financial Record not found with id: '99'",
  "timestamp": "2026-04-02T10:00:00"
}

// 409 Conflict — Duplicate
{
  "success": false,
  "message": "User with email 'karthik@zorvyn.com' already exists",
  "timestamp": "2026-04-02T10:00:00"
}
```

---

## Setup Instructions

### Option 1: Docker (Recommended)

```bash
# Clone the repository
git clone https://github.com/mkarthik2006/zorvyn-finance-backend.git
cd zorvyn-finance-backend

# Start everything with Docker Compose
docker-compose up --build
```

The app will be available at [http://localhost:8080](http://localhost:8080)

Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Option 2: Local Development

**Prerequisites:**

- Java 17+
- Maven 3.8+
- PostgreSQL 14+

```bash
# 1. Create PostgreSQL database and user
psql -U postgres
CREATE USER finance_user WITH PASSWORD 'finance_pass';
CREATE DATABASE finance_db;
GRANT ALL PRIVILEGES ON DATABASE finance_db TO finance_user;
ALTER DATABASE finance_db OWNER TO finance_user;
\c finance_db
GRANT ALL ON SCHEMA public TO finance_user;

# 2. Build and run
mvn clean install
mvn spring-boot:run
```

### Bootstrap Admin User

Since public registration defaults to `VIEWER` for security, to create the first `ADMIN`:

```bash
# 1. Register via API
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Admin User","email":"admin@zorvyn.com","password":"adminPass123"}'

# 2. Promote to ADMIN via database
docker exec -it finance-db psql -U finance_user -d finance_db \
  -c "UPDATE users SET role = 'ADMIN' WHERE email = 'admin@zorvyn.com';"

# 3. Login to get fresh ADMIN token (role is encoded in JWT at login time)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@zorvyn.com","password":"adminPass123"}'
```

---

## Running Tests

```bash
# Run all tests
mvn test
```

**Test Results: 26 tests, 0 failures**

| Test Suite                      | Tests | Status |
|---------------------------------|:-----:|:------:|
| `FinancialRecordControllerTest` |   5   |  Pass  |
| `AuthControllerTest`            |   4   |  Pass  |
| `UserServiceTest`               |   7   |  Pass  |
| `DashboardServiceTest`          |   4   |  Pass  |
| `FinancialRecordServiceTest`    |   5   |  Pass  |
| `FinanceApplicationTests`       |   1   |  Pass  |

Tests use an in-memory H2 database (`application-test.yml`) so no external database is needed.

---

## Assumptions

1. **Single-tenant system** — The system is designed for a single organization.
2. **Bootstrap Admin** — The first Admin account is provisioned via direct database update. All public registrations default to `VIEWER` for security.
3. **Each financial record belongs to one user** — The creator is tracked via the `createdBy` foreign key relationship.
4. **Categories are free text** — Users can enter any category string rather than selecting from a predefined list, providing flexibility.
5. **Roles are fixed** — `ADMIN`, `ANALYST`, `VIEWER` are the only roles, defined as a Java enum for type safety.
6. **Dates cannot be in the future** — Financial records must have past or present dates, enforced via `@PastOrPresent` validation.
7. **Soft delete is used** — No data is permanently deleted. Records are marked with `deleted = true` and globally filtered via Hibernate `@SQLRestriction`.

---

## Design Decisions

1. **Layered Architecture** — `Controller -> Service -> Repository` pattern ensures clear separation of concerns and testability. Each layer has a single responsibility.

2. **Role Enforcement Strategy** — Public registration is hardcoded to `VIEWER` role. Elevated roles can only be assigned by an authenticated Admin via `/api/users`. This prevents privilege escalation attacks at the registration level.

3. **Global Soft Delete via `@SQLRestriction`** — Instead of manually adding `WHERE deleted = false` to every query, Hibernate `@SQLRestriction("deleted = false")` is applied at the entity level. This ensures soft-deleted records are automatically excluded from all queries, joins, and dashboard aggregations.

4. **Stateless JWT Authentication** — Eliminates server-side session management, making the API horizontally scalable. Uses the modern JJWT 0.12.x API with auto-detected signing algorithms.

5. **Standardized API Response** — Every endpoint returns `{success, message, data, timestamp}` for consistent frontend integration and predictable API behavior.

6. **BigDecimal for Amounts** — Avoids floating-point precision issues that are critical in financial calculations. The column is defined as `NUMERIC(15,2)` in PostgreSQL.

7. **Database Indexes** — Strategic indexes on `date`, `category`, `type`, and `created_by_id` columns for optimized query performance on filtering and dashboard aggregation queries.

8. **DTO Pattern** — Request/Response DTOs decouple API contracts from JPA entities, preventing internal data leakage (e.g., password hashes never appear in API responses).

9. **Global Exception Handler** — Centralized `@ControllerAdvice` with distinct exception handlers ensures consistent error responses with appropriate HTTP status codes across all endpoints.

10. **Database-Level Aggregation** — Dashboard analytics use `SUM`, `COUNT`, `GROUP BY`, and `EXTRACT` directly in JPQL queries, executing at the database level for efficiency rather than fetching all records into memory.

---

## Trade-offs

| Decision                           | Benefit                                 | Trade-off                                       |
|------------------------------------|-----------------------------------------|-------------------------------------------------|
| Free-text categories               | Flexibility for users                   | No category validation or normalization         |
| Single JWT secret                  | Simple configuration                    | Rotating secret requires redeployment           |
| Soft delete with `@SQLRestriction` | Data preservation + leak-proof queries  | DB storage grows over time; needs archival plan |
| Enum-based roles                   | Type safety, simple implementation      | Adding new roles requires code change           |
| PostgreSQL                         | Relational consistency, ACID compliance | Requires running a database server              |
| Monolithic structure               | Simplicity for assignment scope         | Not microservice-ready out of the box           |
| H2 for tests                       | Fast, in-memory test execution          | Minor behavioral differences from PostgreSQL    |
| Force VIEWER on registration       | Prevents privilege escalation           | Requires Admin to manually elevate users        |

---

## Data Model

### User Entity

| Field     | Type          | Constraints                                              |
|-----------|---------------|----------------------------------------------------------|
| id        | Long          | PK, Auto-generated                                       |
| name      | String(100)   | Not null                                                 |
| email     | String(150)   | Not null, Unique                                         |
| password  | String        | Not null, BCrypt encoded                                 |
| role      | Enum          | ADMIN / ANALYST / VIEWER                                 |
| status    | Enum          | ACTIVE / INACTIVE                                        |
| deleted   | Boolean       | Default: false, Filtered globally via `@SQLRestriction`  |
| createdAt | LocalDateTime | Auto-generated on creation                               |
| updatedAt | LocalDateTime | Auto-updated on modification                             |

### Financial Record Entity

| Field     | Type             | Constraints                                              |
|-----------|------------------|----------------------------------------------------------|
| id        | Long             | PK, Auto-generated                                       |
| amount    | BigDecimal(15,2) | Not null, Must be positive                               |
| type      | Enum             | INCOME / EXPENSE                                         |
| category  | String(100)      | Not null, Indexed for search performance                 |
| date      | LocalDate        | Not null, Indexed, Must be past or present               |
| notes     | String(500)      | Optional                                                 |
| createdBy | User (FK)        | ManyToOne, Indexed, Tracks record creator                |
| deleted   | Boolean          | Default: false, Filtered globally via `@SQLRestriction`  |
| createdAt | LocalDateTime    | Auto-generated on creation                               |
| updatedAt | LocalDateTime    | Auto-updated on modification                             |

### Entity Relationship Diagram

```
+----------------+         +----------------------+
|     User       |         |  FinancialRecord     |
+----------------+         +----------------------+
| id (PK)        |<--------| created_by_id (FK)   |
| name           |   1:N   | id (PK)              |
| email (UQ)     |         | amount               |
| password       |         | type                 |
| role           |         | category             |
| status         |         | date                 |
| deleted        |         | notes                |
| createdAt      |         | deleted              |
| updatedAt      |         | createdAt            |
+----------------+         | updatedAt            |
                           +----------------------+
```

---

## Project Structure

```
zorvyn-finance-backend/
├── docker-compose.yml
├── Dockerfile
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/zorvyn/finance/
    │   │   ├── FinanceApplication.java
    │   │   ├── config/
    │   │   │   ├── OpenApiConfig.java
    │   │   │   └── SecurityConfig.java
    │   │   ├── security/
    │   │   │   ├── CustomUserDetailsService.java
    │   │   │   ├── JwtAccessDeniedHandler.java
    │   │   │   ├── JwtAuthenticationEntryPoint.java
    │   │   │   ├── JwtAuthenticationFilter.java
    │   │   │   └── JwtTokenProvider.java
    │   │   ├── dto/
    │   │   │   ├── ApiResponse.java
    │   │   │   ├── auth/
    │   │   │   │   ├── LoginRequest.java
    │   │   │   │   ├── LoginResponse.java
    │   │   │   │   └── RegisterRequest.java
    │   │   │   ├── user/
    │   │   │   │   ├── UserRequest.java
    │   │   │   │   ├── UserResponse.java
    │   │   │   │   └── UserStatusRequest.java
    │   │   │   ├── record/
    │   │   │   │   ├── FinancialRecordRequest.java
    │   │   │   │   └── FinancialRecordResponse.java
    │   │   │   └── dashboard/
    │   │   │       ├── SummaryResponse.java
    │   │   │       ├── CategorySummary.java
    │   │   │       └── MonthlyTrend.java
    │   │   ├── entity/
    │   │   │   ├── User.java
    │   │   │   ├── FinancialRecord.java
    │   │   │   ├── Role.java
    │   │   │   ├── RecordType.java
    │   │   │   └── UserStatus.java
    │   │   ├── repository/
    │   │   │   ├── UserRepository.java
    │   │   │   └── FinancialRecordRepository.java
    │   │   ├── service/
    │   │   │   ├── AuthService.java
    │   │   │   ├── UserService.java
    │   │   │   ├── FinancialRecordService.java
    │   │   │   └── DashboardService.java
    │   │   ├── controller/
    │   │   │   ├── AuthController.java
    │   │   │   ├── UserController.java
    │   │   │   ├── FinancialRecordController.java
    │   │   │   └── DashboardController.java
    │   │   └── exception/
    │   │       ├── GlobalExceptionHandler.java
    │   │       ├── ResourceNotFoundException.java
    │   │       ├── DuplicateResourceException.java
    │   │       └── AccessDeniedException.java
    │   └── resources/
    │       └── application.yml
    └── test/
        ├── java/com/zorvyn/finance/
        │   ├── FinanceApplicationTests.java
        │   ├── service/
        │   │   ├── UserServiceTest.java
        │   │   ├── FinancialRecordServiceTest.java
        │   │   └── DashboardServiceTest.java
        │   └── controller/
        │       ├── AuthControllerTest.java
        │       └── FinancialRecordControllerTest.java
        └── resources/
            └── application-test.yml
```

---

## Assignment Compliance Summary

| Requirement                          | Implementation                                          | Status    |
|--------------------------------------|---------------------------------------------------------|:---------:|
| User and Role Management             | Full CRUD + role assignment + status management         | Completed |
| Financial Records Management         | CRUD + filtering by type/category/date + pagination     | Completed |
| Dashboard Summary APIs               | Summary, category-wise, monthly trends, recent activity | Completed |
| Access Control Logic                 | JWT + `@PreAuthorize` RBAC on every endpoint            | Completed |
| Validation and Error Handling        | DTO validation + `@ControllerAdvice` + proper HTTP codes| Completed |
| Data Persistence                     | PostgreSQL + JPA + indexed entities                     | Completed |
| Authentication (Optional)            | JWT stateless auth with JJWT 0.12.5                    | Completed |
| Pagination (Optional)               | Configurable page/size/sortBy/sortDir                   | Completed |
| Search Support (Optional)            | Case-insensitive category partial match                 | Completed |
| Soft Delete (Optional)              | Global `@SQLRestriction` on both entities               | Completed |
| Unit Tests (Optional)               | 26 tests, all passing                                   | Completed |
| API Documentation (Optional)        | Swagger UI at `/swagger-ui.html`                        | Completed |
| Docker (Optional)                   | Multi-stage Dockerfile + docker-compose                 | Completed |

---

## License

This project is built as part of the Zorvyn FinTech Backend Developer Intern assignment.

&copy; 2026 Karthik M. All rights reserved.