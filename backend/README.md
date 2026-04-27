# Similar Products Backend

Spring Boot implementation for the backend technical test.

## API

```http
GET /product/{productId}/similar
```

Runs on port `5000` and returns:

```json
[
  {
    "id": "2",
    "name": "Dress",
    "price": 19.99,
    "availability": true
  }
]
```

The response is ordered by the similarity order returned by the external mock API.

## Architecture

The application uses a lightweight Clean Architecture split:

```text
interfaces/rest              Controller and exception mapping
interfaces/rest/dto          Response DTOs
application/usecase          Similar-products orchestration
domain/model                 Product model
domain/port                  ProductCatalogPort abstraction
infrastructure/http/client   WebClient adapter for the external API
```

The use case depends only on the domain port. WebClient, external URLs, timeouts, status-code mapping, and cache policy live in the infrastructure adapter.

## Resilience and Performance

- Similar product details are fetched concurrently with bounded concurrency.
- Similarity order is preserved after concurrent detail resolution.
- Duplicate similar ids are removed while keeping first-seen order.
- If `/similarids` returns `404`, this API returns `404`.
- If `/similarids` fails with any other downstream error or timeout, this API returns `502`.
- If a detail request returns `404`, `5xx`, or times out, that single product is skipped.
- Successful similar-id responses and detail responses are cached for a short TTL to reduce repeated downstream calls during load tests.

## Trade-offs

- WebFlux is used to keep downstream calls non-blocking and simple to compose under high concurrency.
- Bounded concurrency protects the external mock and the application event loop from unbounded fan-out.
- Detail failures are skipped because the contract asks for similar product details, and partial useful data is better than failing the whole request for one unavailable similar product.
- `/similarids` failures are not hidden, because that call determines whether the requested source product exists.
- The cache TTL is intentionally short: it improves repeated-load behavior without making stale catalog data a long-lived concern.
- Java 17 is selected as the runtime target because it is the Spring Boot 3 baseline and reduces evaluator setup friction.

## Configuration

```yaml
server:
  port: 5000

external-products-api:
  base-url: http://localhost:3001
  connect-timeout-ms: 500
  response-timeout-ms: 1400
  max-connections: 512
  pending-acquire-max-count: 2048
  cache-ttl: PT1M
  detail-concurrency: 8
```

The response timeout is intentionally lower than the slow mock responses used in the k6 test.

## Run

Requirements:

- Java 17+
- Docker, only for the original mock/k6 evaluation

Run tests:

```bash
./mvnw test
```

Run the API:

```bash
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

If `JAVA_HOME` points to Java 16 or older, set it to Java 17+ first.

## Docker Evaluation

The repository root contains the Docker Compose stack used by the original backend test. From the root folder, run:

```bash
docker-compose up -d simulado influxdb grafana similar-products
docker-compose run --rm k6 run scripts/test.js
```

## Tests

The test suite covers:

- Use-case ordering, duplicate removal, empty lists, and partial results.
- Controller response mapping for `200` and `404`.
- WebClient adapter behavior for similar ids, product details, downstream failures, timeouts, and cache reuse.

## CI

GitHub Actions runs the backend suite with Java 17 on every push and pull request.
