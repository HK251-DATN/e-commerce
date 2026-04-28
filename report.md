# Session Report — Ecommerce Service

## 1. Cart + Sale Event Integration

### Problem
Previously a buyer who had exhausted the `maxBuy` limit for a sale product was completely blocked from adding more of that product to the cart, even though buying more at the original price should be allowed.

### Changes

**`CartItem`** — added `saleEventId` column. Cart item identity is now `(cartId, batchDetailId, saleEventId)`, not just `(cartId, batchDetailId)`. A buyer can hold one sale-price item and one normal-price item for the same batch at the same time.

**`CartItemAddResult`** (new) — return type for `addToCart`. Carries the primary `cartItem`, an optional `overflowItem` (the normal-price item created when the sale limit is exceeded mid-request), and a human-readable `note`.

**`CartItemService.addToCart`** — replaced the hard block with a three-way split:
- `remaining > quantity` → all units added at sale price, no overflow.
- `0 < remaining < quantity` → `remaining` units at sale price + `(quantity - remaining)` units at original price as a separate cart item.
- `remaining <= 0` → all units added at original price, no error thrown.

The per-buyer quota check now uses `findByCartIdAndBatchDetailIdAndSaleEventId` so it only counts the sale item's quantity toward the limit, not the overflow normal-price item.

**`CartItemRepository`** — two new Spring Data methods:
- `findByCartIdAndBatchDetailIdAndSaleEventId` — looks up a sale item by its full three-part key.
- `findByCartIdAndBatchDetailIdAndSaleEventIdIsNull` — looks up the normal-price item for the same batch.

Both native queries (`findCartItemsWithBatchDetailByCartId`, `findSelectedCartItemsWithBatchDetailByCartId`) extended with:
- `bd.quantity AS batchQuantity` — for stock status.
- `ci.sale_event_id`, `sp.sale_price`, `sp.dis_val` — for sale pricing display.

**`CartItemWithBatchDetailDTO`** — added `getBatchQuantity()`, `getSaleEventId()`, `getSalePrice()`, `getDisVal()`.

**`CartItemResponse`** — added `saleEventId`, `salePrice`, `disVal`, `cartItemStatus` (`AVAILABLE` / `OUT_OF_STOCK`).

`CartItemStatus` enum lives inside `CartItem` (inner enum, accessed as `CartItem.CartItemStatus`).

---

## 2. Stale Sale Reconciliation

### Problem
A buyer could add a product to the cart during a sale, then return later after the sale expired or sold out. The cart would still show the stale sale price.

### Changes

**`CartItemService.reconcileStaleSaleItems`** (new private method) — called at the start of both `getAllWithBatchDetailByCartId` and `getSelectedItemsWithBatchDetail`. For each cart item tagged with a `saleEventId`:
1. Calls `findActiveSaleProductByBatchId` — if the sale is still active, skip.
2. If stale: look for an existing normal-price item for the same batch.
   - Found → merge quantities into the normal item, delete the stale sale item.
   - Not found → strip `saleEventId` from the item (converts it to a normal-price item in place).

---

## 3. `OUT_OF_STOCK` Status

### Problem
When `BatchDetail.quantity` reaches 0 the cart had no way to signal this to the UI.

### Changes

**`CartItemService.convertToResponse`** — computes `cartItemStatus` from `dto.getBatchQuantity()`: `OUT_OF_STOCK` when `<= 0`, `AVAILABLE` otherwise. No data mutation — purely a read-time computed field.

---

## 4. Dedicated Product Detail Endpoint

### Problem
The front-end was calling `GET /api/product-search?productGeneralId=X` to view a single product's detail page — a search endpoint used as a point-lookup.

### New endpoint: `GET /api/products/{batchDetailId}`

**`ProductGeneralRepository.findByBatchDetailId`** (new query) — joins `BATCH_DETAIL`, `PRODUCT_GENERAL`, and an active-sale-only subquery of `SALE_PRODUCT` × `SALE_EVENT`. Only returns sale pricing when the event is currently active (`active_yn = 'Y'`, `enabled_yn = 'Y'`, within date range, `cur_qty > 0`). Returns `null` for `salePrice`/`disVal`/`saleEventId` when no active sale applies (no `COALESCE(..., 0)` masking).

**`ProductInfoService`** (new) — assembles the response from three sources:
1. `findByBatchDetailId` → core pricing + general info.
2. `ProductGeneral` entity (PK lookup) → `tags`, `unit`, `unitQuantity`.
3. `BatchDetail` entity (PK lookup) → `detailContent`.
4. `GET {product-storage-url}/api/product-batch/{batchId}/proof-images` → parses the `detail` array from the response body. Fails gracefully: if the product-storage service is unreachable the endpoint still returns with `proofImages: []`.

**`ProductDetailResponse`** (new) — flat response object combining all four sources.

**`ProductController`** (new) — `GET /api/products/{batchDetailId}`.

**`application.yaml`** — added `services.product-storage.url` (defaults to `localhost:9200`). Also added `services.identity.url` and `services.back-office.url` for future use.

---

## Files Changed

| File | Change |
|------|--------|
| `domain/entity/CartItem.java` | + `saleEventId` field, + `CartItemStatus` inner enum |
| `domain/service/CartItemService.java` | Rewrote `addToCart`; added `reconcileStaleSaleItems`, `persistSaleItem`, `persistNormalItem`; updated `convertToResponse` |
| `domain/service/CartItemAddResult.java` | New — wraps primary item, overflow item, and note |
| `domain/service/ProductInfoService.java` | New — assembles product detail from DB + product-storage HTTP call |
| `persistence/dto/CartItemWithBatchDetailDTO.java` | + `getBatchQuantity`, `getSaleEventId`, `getSalePrice`, `getDisVal` |
| `persistence/repository/CartItemRepository.java` | + 2 saleEventId-aware lookup methods; extended both native queries |
| `persistence/repository/ProductGeneralRepository.java` | + `findByBatchDetailId` with active-sale subquery |
| `presentation/response/cartitem/CartItemResponse.java` | + `saleEventId`, `salePrice`, `disVal`, `cartItemStatus` |
| `presentation/response/product/ProductDetailResponse.java` | New |
| `presentation/rest/ProductController.java` | New — `GET /api/products/{batchDetailId}` |
| `resources/application.yaml` | + `services.*.url` config block |
