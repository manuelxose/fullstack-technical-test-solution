# Backend Architecture Notes

## Flow

```text
SimilarProductsController
  -> GetSimilarProductsUseCase
    -> ProductCatalogPort
      -> ProductApiClient
        -> External mock API on port 3001
```

## Responsibilities

- Controller: owns the HTTP route and maps domain products to response DTOs.
- DTO package: contains REST response shapes without domain or mapping logic.
- Use case: obtains similar ids, removes duplicates, fetches details concurrently, and preserves similarity order.
- Port: defines what the application needs from a product catalog.
- Adapter: owns WebClient, URLs, status-code policy, timeout policy, and short-lived cache.

## Failure Policy

| Scenario | API behavior |
| --- | --- |
| `/similarids` returns `404` | Return `404` |
| `/similarids` returns `5xx` or times out | Return `502` |
| Product detail returns `404` | Skip that product |
| Product detail returns `5xx` | Skip that product |
| Product detail times out | Skip that product |

This policy favors useful partial responses while still reporting that the requested source product does not exist.

## Operational Notes

- Timeouts, connection limits, cache TTL, and detail concurrency are externalized in `application.yml`.
- The REST DTO package is deliberately separate from the domain model so API representation can evolve without leaking infrastructure concerns into the use case.
- The adapter owns all downstream HTTP behavior, which keeps the application use case testable without WebClient or network setup.
- The k6 scenario stresses normal, missing, error, slow, and very slow paths; the implementation is tuned to return quickly under those conditions.
