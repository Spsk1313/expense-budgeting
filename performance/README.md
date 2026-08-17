## Benchmark Results

Benchmarks were performed against a synthetic dataset containing
100,000 transactions.

Each endpoint was tested using:

- k6
- 10 virtual users
- 30-second duration
- 1 warm-up run
- 3 measured runs
- mean metrics reported across measured runs

### API Performance

| Endpoint | Baseline Avg | Indexed Avg | Avg Reduction | Baseline p95 | Indexed p95 | p95 Reduction | Baseline req/s | Indexed req/s |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| Monthly summary | 41.94 ms | 3.36 ms | 91.98% | 54.66 ms | 4.17 ms | 92.37% | 238.23 | 2903.41 |
| Category spending | 43.92 ms | 9.93 ms | 77.40% | 58.32 ms | 14.01 ms | 75.98% | 227.92 | 998.46 |
| Date-range summary | 77.72 ms | 35.87 ms | 53.85% | 100.85 ms | 43.71 ms | 56.66% | 130.13 | 277.70 |

### Optimization

Added the following composite B-tree index:

```sql
CREATE INDEX idx_transactions_account_date
    ON transactions (account_id, transaction_date);