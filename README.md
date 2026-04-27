# Fullstack Technical Test Solution

This repository contains two independent technical-test solutions:

```text
backend/   Spring Boot API for similar products
frontend/  React SPA for the ITX mobile store test
```

The remaining root folders are backend-test infrastructure, matching the original evaluation flow:

```text
shared/simulado/  Simulated external product API configuration
shared/k6/        k6 performance test script
shared/grafana/   Grafana dashboard provisioning for k6 results
```

They are not application code; they are included so the documented backend validation commands can run from this repository root.

## Backend

The backend exposes the agreed API on port `5000`:

```http
GET /product/{productId}/similar
```

It consumes the provided mock API on port `3001`, reads the ordered similar product ids, fetches product details concurrently, preserves the similarity order, and skips unavailable detail products so slow or broken downstream items do not fail the whole response.

Run and validate:

```bash
cd backend
./mvnw test
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
cd backend
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

If `JAVA_HOME` points to an older JDK, set it to Java 17+ before running Maven.

## Frontend

The frontend is a React + TypeScript SPA with:

- Product list page with real-time search by brand or model.
- Product detail page with description and purchasable options.
- Client-side routing, breadcrumbs, cart counter, and localStorage persistence.
- One-hour API response cache.
- Exact dependency versions and `package-lock.json` for reproducible installs.

Run and validate:

```bash
cd frontend
npm install
npm run lint
npm test
npm run build
npm start
```

The default API base URL is `https://itx-frontend-test.onrender.com`. Override it with:

```bash
VITE_API_BASE_URL=https://example.com npm start
```

## Docker Backend Evaluation

This repository includes the mock API and the k6/Grafana/InfluxDB stack expected by the backend test. From this repository root, start the mocks and observability tools:

```bash
docker-compose up -d simulado influxdb grafana
```

Check the mock:

```bash
curl http://localhost:3001/product/1/similarids
```

Then start the backend on port `5000` in one of two ways.

Option 1, run it locally:

```bash
cd backend
./mvnw spring-boot:run
```

Option 2, run it with Docker Compose:

```bash
docker-compose up -d similar-products
```

Run the k6 test. The command path is `scripts/test.js` because Docker Compose mounts `shared/k6` inside the k6 container as `/scripts`, matching the original backend test repository.

```bash
docker-compose run --rm k6 run scripts/test.js
```

Performance dashboard:

```text
http://localhost:3000/d/Le2Ku9NMk/k6-performance-test
```

Docker Desktop must be running before executing the k6 validation.

## Quality Gates

The repository includes GitHub Actions CI with three independent checks:

- Backend: Java 17 setup and `./mvnw test`.
- Frontend: Node 22 setup, `npm ci`, lint, tests, and production build.
- Docker Compose: syntax validation for the evaluation stack.

## Delivery Decisions

- Java 17 is the backend target because it is the Spring Boot 3 baseline and is broadly available in CI and evaluator machines.
- The backend keeps Clean Architecture boundaries small: controller, use case, port, adapter, and DTO packages.
- The backend degrades only similar-product details; the source product still returns `404` when `/similarids` reports it missing.
- The frontend centralizes API access and cache behavior so pages stay focused on rendering and user interaction.
- The original mock, k6, InfluxDB, and Grafana files are kept under `shared/` because they are part of the backend evaluation, not application code.
