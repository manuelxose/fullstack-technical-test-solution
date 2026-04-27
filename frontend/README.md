# ITX Mobile Store Frontend

React + TypeScript SPA for the ITX frontend technical test.

## Features

- Product list page populated from `GET /api/product`.
- Real-time search by brand or model.
- Responsive product grid with up to four items per row on desktop.
- Product detail page populated from `GET /api/product/:id`.
- Detail page with product image, description, storage selector, color selector, and add-to-cart action.
- Default selection of the first available storage and color option.
- Cart submission through `POST /api/cart` with `{ id, colorCode, storageCode }`.
- Header with home link, breadcrumbs, and persisted cart counter.
- Client-side cache in `localStorage` with a one-hour TTL.

## API Configuration

Default behavior:

```text
Development uses the Vite proxy: /api -> https://itx-frontend-test.onrender.com
Production uses: https://itx-frontend-test.onrender.com
```

Override it with:

```bash
VITE_API_BASE_URL=https://example.com npm start
```

## Scripts

```bash
npm install
npm start
npm run lint
npm test
npm run build
```

## Technical Decisions

- Vite is used for a small SPA with fast local feedback.
- React Router owns client-side navigation between list and detail views.
- API access is centralized in `src/services`.
- Cache expiration is isolated in `src/shared/cache.ts`.
- Cart count is the only global UI state and is persisted in `localStorage`.
- Product data is fetched through services instead of global state because it is remote cacheable data, not application UI state.

## Trade-offs

- The development server proxies `/api` to the real ITX endpoint to avoid browser CORS issues while keeping production URLs explicit.
- The client cache is one hour because the catalog is read-heavy and the technical test values repeated navigation performance.
- Product list and detail pages use local loading/error state instead of a global data library because the app has a small number of screens and simple invalidation rules.
- Add-to-cart keeps only the persisted count in global state, avoiding unnecessary app-wide state for catalog data.

## Validation

The test suite covers product filtering, list/detail API cache behavior, default product options, add-to-cart payloads, and persisted cart count updates.

GitHub Actions runs lint, tests, and production build with Node 22 on every push and pull request.
