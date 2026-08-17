# Expense & Budgeting Backend

A backend-focused personal finance API built with Java and Spring Boot to practice non-trivial business logic, relational modeling, scheduled processing, reporting, database performance analysis, and containerized deployment.

The project models users, financial accounts, income/expense/transfer transactions, categories, monthly budgets, recurring transactions, derived account balances, analytical reports, and CSV exports. It also includes a reproducible 100,000-row PostgreSQL benchmark suite used to measure and improve reporting performance.

## Highlights

- Income, expense, and account-transfer workflows
- CHEQUING, SAVINGS, CREDIT_CARD, and CASH accounts
- Monthly category budgets and overspending status
- Derived current balances rather than persisted mutable balances
- Weekly and monthly recurring transactions
- Scheduled recurring-transaction processing with catch-up handling
- Idempotent occurrence tracking and concurrency protection
- Monthly summaries, arbitrary date-range reports, category breakdowns, and budget status reports
- Transaction CSV export with proper CSV escaping and downloadable HTTP responses
- PostgreSQL schema managed with Flyway migrations
- Testcontainers-backed integration testing against real PostgreSQL
- Dockerized Spring Boot application and PostgreSQL database
- k6 performance benchmarks over 100,000 synthetic transactions
- PostgreSQL `EXPLAIN (ANALYZE, BUFFERS)` analysis and targeted composite indexing

## Tech Stack

- Java 21
- Spring Boot 4.1
- Spring Web MVC
- Spring Data JPA / Hibernate
- PostgreSQL 17
- Flyway
- Maven Wrapper
- Bean Validation
- springdoc OpenAPI / Swagger UI
- JUnit 5
- Testcontainers
- Docker / Docker Compose
- k6

## Domain Model

### User

Owns accounts, categories, budgets, and recurring transactions.

### Account

Supported account types:

- `CHEQUING`
- `SAVINGS`
- `CREDIT_CARD`
- `CASH`

Each account stores an opening balance. The current balance is derived from its transaction ledger rather than stored as a second mutable balance.

### Transaction

Supported transaction types:

- `INCOME`
- `EXPENSE`
- `TRANSFER`

Income and expenses reference one account and one category. Transfers use a source and destination account and deliberately do not use a category.

### Category

Categories are typed as either `INCOME` or `EXPENSE`, allowing transaction creation rules to reject mismatched category types.

### Budget

A budget represents a monthly spending limit for an expense category. Values such as spent amount, remaining amount, percentage used, and overspent status are derived from transaction data instead of persisted separately.

### Recurring Transaction

Recurring income, expense, and transfer definitions support:

- `WEEKLY`
- `MONTHLY`

The recurring engine tracks scheduled occurrences separately from generated transactions so processing is idempotent and auditable.

## Derived State

A central design goal of the project is distinguishing persistent state from derived state.

For example, account balances are computed as:

```text
current balance
= opening balance
+ income
- expenses
+ transfers in
- transfers out
```

Budget status is similarly derived:

```text
remaining amount = budget limit - category spending
percentage used  = category spending / budget limit * 100
```

This avoids maintaining duplicate mutable values that can drift out of sync with the underlying transaction ledger.

## API Overview

The application exposes user-scoped REST resources.

### Users

```http
POST /api/users
GET  /api/users/{id}
```

### Accounts

```http
POST /api/users/{userId}/accounts
GET  /api/users/{userId}/accounts
GET  /api/users/{userId}/accounts/{accountId}
```

### Categories

```http
POST /api/users/{userId}/categories
GET  /api/users/{userId}/categories
GET  /api/users/{userId}/categories/{categoryId}
```

### Transactions

```http
POST /api/users/{userId}/transactions/income
POST /api/users/{userId}/transactions/expense
POST /api/users/{userId}/transactions/transfer
GET  /api/users/{userId}/transactions
GET  /api/users/{userId}/transactions/{transactionId}
```

### Budgets

```http
POST  /api/users/{userId}/budgets
GET   /api/users/{userId}/budgets
GET   /api/users/{userId}/budgets/{budgetId}
PATCH /api/users/{userId}/budgets/{budgetId}/limit
```

### Recurring Transactions

```http
POST  /api/users/{userId}/recurring-transactions/income
POST  /api/users/{userId}/recurring-transactions/expense
POST  /api/users/{userId}/recurring-transactions/transfer
GET   /api/users/{userId}/recurring-transactions
GET   /api/users/{userId}/recurring-transactions/{recurringTransactionId}
PATCH /api/users/{userId}/recurring-transactions/{recurringTransactionId}/active
```

### Reports

```http
GET /api/users/{userId}/reports/monthly?month=2026-08
GET /api/users/{userId}/reports/categories?month=2026-08
GET /api/users/{userId}/reports/budgets?month=2026-08
GET /api/users/{userId}/reports/summary?from=2026-08-01&to=2026-08-31
GET /api/users/{userId}/reports/accounts/{accountId}/balance
```

The date-range API treats `from` and `to` as inclusive dates while repository queries internally use half-open ranges (`[start, end)`).

### CSV Export

```http
GET /api/users/{userId}/exports/transactions?from=2026-08-01&to=2026-08-31
```

The response uses `text/csv`, UTF-8 encoding, and `Content-Disposition: attachment` with a generated filename.

## Recurring Transaction Processing

Recurring transaction processing goes beyond a simple CRUD resource.

The system includes:

- A scheduler configured through `app.recurring-transactions.cron`
- Catch-up processing for missed occurrences after downtime
- Separate occurrence records for scheduled execution history
- A uniqueness constraint on recurring transaction + scheduled date
- Idempotent processing to prevent duplicate generated transactions
- Database locking/concurrency protection around due recurring work

The default schedule is configured as:

```yaml
app:
  recurring-transactions:
    cron: 0 0 1 * * *
```

## Reporting and Aggregation

Reporting is intentionally performed with database-side aggregation rather than loading complete transaction histories into Java.

The repository layer uses aggregate JPQL and projection interfaces for operations such as:

- Conditional `SUM` for income and expenses
- `GROUP BY` category spending
- Date-range filtering
- Account activity totals

Application code then performs lightweight business calculations such as net cash flow and budget percentage usage.

## Database Migrations

Flyway owns the schema. Current migrations create:

1. Users
2. Accounts
3. Categories
4. Transactions
5. Budgets
6. Recurring transactions and occurrence tracking
7. Reporting performance index

Applied migrations are treated as immutable; subsequent schema changes should be introduced through new migration files.

## Performance Engineering

The repository contains a dedicated `performance/` workspace with:

```text
performance/
├── README.md
├── k6/
├── results/
│   ├── baseline/
│   └── indexed/
└── seed/
```

A reproducible PostgreSQL seed script generates 100,000 synthetic transactions with an approximately:

- 75% expense
- 15% income
- 10% transfer

distribution across multiple accounts and categories.

### Benchmark Methodology

Each measured API workload used:

- 100,000 synthetic transactions
- k6
- 10 virtual users
- 30-second duration
- 1 discarded warm-up run
- 3 measured runs
- mean metrics across measured runs

### Measured Results

| Endpoint | Baseline Avg | Indexed Avg | Avg Reduction | Baseline p95 | Indexed p95 | p95 Reduction | Baseline req/s | Indexed req/s |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| Monthly summary | 41.94 ms | 3.36 ms | 91.98% | 54.66 ms | 4.17 ms | 92.37% | 238.23 | 2903.41 |
| Category spending | 43.92 ms | 9.93 ms | 77.40% | 58.32 ms | 14.01 ms | 75.98% | 227.92 | 998.46 |
| Date-range summary | 77.72 ms | 35.87 ms | 53.85% | 100.85 ms | 43.71 ms | 56.66% | 130.13 | 277.70 |

The optimization was driven by execution-plan analysis rather than adding indexes speculatively. PostgreSQL plans showed repeated sequential scans over the 100,000-row transaction table for account-scoped monthly reports.

The resulting Flyway migration adds:

```sql
CREATE INDEX idx_transactions_account_date
    ON transactions (account_id, transaction_date);
```

For selective monthly queries PostgreSQL switched to bitmap index/heap scans and substantially reduced unnecessary row scanning. Broad queries covering most of the dataset remain less naturally index-selective, which is documented separately in `performance/README.md` along with raw k6 result artifacts and captured execution plans.

## Running with Docker

### 1. Create environment configuration

Copy the example file:

```bash
cp .env.example .env
```

Example values:

```env
POSTGRES_DB=expense_budgeting
POSTGRES_USER=user
POSTGRES_PASSWORD=password
```

### 2. Start the complete stack

```bash
docker compose up --build
```

Docker Compose runs:

- Spring Boot on host port `8080`
- PostgreSQL 17 on host port `5434`

Inside the Compose network, the application connects to PostgreSQL using the `postgres` service name on port `5432`.

### 3. Swagger UI

After startup, use the application's springdoc Swagger UI to explore and execute the REST API.

## Running Tests

The project contains domain/unit tests plus PostgreSQL-backed integration tests using Testcontainers.

With Docker available:

```bash
./mvnw test
```

The Testcontainers integration tests use isolated temporary PostgreSQL instances rather than the Docker Compose development/benchmark database.

## Project Structure

```text
src/main/java/com/spsk1313/expensebudgeting/
├── account/
├── budget/
├── category/
├── common/
├── export/
├── recurringtransaction/
├── report/
├── transaction/
└── user/
```

The codebase is organized by domain/feature slice, with controllers, services, repositories, DTOs, exceptions, and projections colocated with the relevant feature.

## Key Engineering Lessons

This project was built specifically to move beyond simple CRUD and practice:

- Encoding domain invariants in both Java and PostgreSQL constraints
- Designing transfers without double-counting income or expenses
- Distinguishing persisted state from derived state
- Transactional service boundaries and rollback behavior
- Idempotent scheduled processing
- Locking and concurrency reasoning
- Aggregate SQL/JPQL and projections
- Avoiding unnecessary entity loading for analytical workloads
- Half-open internal date ranges
- CSV serialization and HTTP file downloads
- Testcontainers integration testing
- Docker networking and environment configuration
- Reading PostgreSQL query plans
- Index selectivity and composite index ordering
- Measuring p95 latency and throughput with repeatable load tests

## Notes

This is a backend learning/portfolio project. The benchmark figures above come from a controlled synthetic dataset on a local Dockerized environment and are documented as such rather than presented as production traffic results.
