package microservice.base_source.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microservice.base_source.domain.entity.CartItem;
import microservice.base_source.domain.entity.SaleProduct;
import microservice.base_source.domain.exception.type.NotFoundException;
import microservice.base_source.domain.use_case.CartItemUseCase;
import microservice.base_source.persistence.dto.CartItemWithBatchDetailDTO;
import microservice.base_source.persistence.repository.CartItemRepository;
import microservice.base_source.persistence.repository.OrderItemRepository;
import microservice.base_source.persistence.repository.SaleProductRepository;
import microservice.base_source.presentation.response.cartitem.CartItemResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartItemService implements CartItemUseCase {

    private final CartItemRepository cartItemRepository;
    private final SaleProductRepository saleProductRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    @Transactional
    public CartItemAddResult addToCart(Long cartId, String buyerId, String batchDetailId, Long quantity, Boolean isSelected) {
        Optional<SaleProduct> activeSale = saleProductRepository.findActiveSaleProductByBatchId(batchDetailId);

        if (activeSale.isPresent()) {
            SaleProduct saleProduct = activeSale.get();
            Long maxBuy = saleProduct.getMaxBuy();

            if (maxBuy != null) {
                long currentCartQty = cartItemRepository
                        .findByCartIdAndBatchDetailIdAndSaleEventId(cartId, batchDetailId, saleProduct.getSaleEventId())
                        .map(CartItem::getQuantity)
                        .orElse(0L);

                long pastOrderQty = orderItemRepository.sumCommittedSaleQuantity(
                        buyerId, batchDetailId, saleProduct.getSaleEventId());

                long alreadyCommitted = currentCartQty + pastOrderQty;
                long remaining = maxBuy - alreadyCommitted;

                if (remaining <= 0) {
                    // Sale limit exhausted — add everything at original price
                    CartItem normalItem = persistNormalItem(cartId, batchDetailId, quantity, isSelected);
                    return new CartItemAddResult(normalItem, null,
                            "Sale limit reached. " + quantity + " item(s) added at original price.");
                }

                if (quantity <= remaining) {
                    // Entire quantity fits within the sale limit
                    CartItem saleItem = persistSaleItem(cartId, batchDetailId, quantity, isSelected, saleProduct.getSaleEventId());
                    return new CartItemAddResult(saleItem, null);
                }

                // Split: remaining at sale price, overflow at original price
                CartItem saleItem = persistSaleItem(cartId, batchDetailId, remaining, isSelected, saleProduct.getSaleEventId());
                long overflow = quantity - remaining;
                CartItem normalItem = persistNormalItem(cartId, batchDetailId, overflow, isSelected);
                String note = remaining + " item(s) added at sale price, " + overflow + " item(s) added at original price (sale limit: " + maxBuy + ").";
                return new CartItemAddResult(saleItem, normalItem, note);
            }

            // Sale exists but no maxBuy limit — tag with saleEventId
            CartItem cartItem = persistSaleItem(cartId, batchDetailId, quantity, isSelected, saleProduct.getSaleEventId());
            return new CartItemAddResult(cartItem, null);
        }

        // Not a sale product — normal flow
        CartItem cartItem = persistNormalItem(cartId, batchDetailId, quantity, isSelected);
        return new CartItemAddResult(cartItem, null);
    }

    @Override
    public CartItem get(Long cartItemId) {
        return cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NotFoundException("Cart item not found with id: " + cartItemId));
    }

    @Override
    public List<CartItem> getAllByCartId(Long cartId) {
        return cartItemRepository.findByCartId(cartId);
    }

    @Override
    public List<CartItemResponse> getAllWithBatchDetailByCartId(Long cartId) {
        reconcileStaleSaleItems(cartId);
        List<CartItemWithBatchDetailDTO> dtos = cartItemRepository
                .findCartItemsWithBatchDetailByCartId(cartId);
        return dtos.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public List<CartItem> getSelectedItems(Long cartId) {
        return cartItemRepository.findByCartIdAndIsSelected(cartId, true);
    }

    @Override
    @Transactional
    public List<CartItemResponse> getSelectedItemsWithBatchDetail(Long cartId) {
        reconcileStaleSaleItems(cartId);
        List<CartItemWithBatchDetailDTO> dtos = cartItemRepository
                .findSelectedCartItemsWithBatchDetailByCartId(cartId);
        return dtos.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CartItem updateQuantity(Long cartItemId, Long quantity) {
        CartItem cartItem = get(cartItemId);
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be greater than 0");
        cartItem.setQuantity(quantity);
        return cartItemRepository.save(cartItem);
    }

    @Override
    @Transactional
    public CartItem toggleSelection(Long cartItemId) {
        CartItem cartItem = get(cartItemId);
        cartItem.setIsSelected(!cartItem.getIsSelected());
        return cartItemRepository.save(cartItem);
    }

    @Override
    @Transactional
    public CartItem update(Long cartItemId, CartItem cartItem) {
        CartItem existingItem = get(cartItemId);
        if (cartItem.getQuantity() != null) {
            if (cartItem.getQuantity() <= 0) throw new IllegalArgumentException("Quantity must be greater than 0");
            existingItem.setQuantity(cartItem.getQuantity());
        }
        if (cartItem.getIsSelected() != null) {
            existingItem.setIsSelected(cartItem.getIsSelected());
        }
        return cartItemRepository.save(existingItem);
    }

    @Override
    @Transactional
    public void delete(Long cartItemId) {
        CartItem cartItem = get(cartItemId);
        cartItemRepository.delete(cartItem);
    }

    @Override
    @Transactional
    public void deleteAllByCartId(Long cartId) {
        cartItemRepository.deleteByCartId(cartId);
    }

    @Override
    @Transactional
    public void deleteSelectedItems(Long cartId) {
        cartItemRepository.deleteByCartIdAndIsSelected(cartId, true);
    }

    @Override
    @Transactional
    public void selectAll(Long cartId) {
        List<CartItem> items = cartItemRepository.findByCartId(cartId);
        items.forEach(item -> item.setIsSelected(true));
        cartItemRepository.saveAll(items);
    }

    @Override
    @Transactional
    public void deselectAll(Long cartId) {
        List<CartItem> items = cartItemRepository.findByCartId(cartId);
        items.forEach(item -> item.setIsSelected(false));
        cartItemRepository.saveAll(items);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    /**
     * For each cart item that references a sale event, check whether that sale is still active.
     * If the sale expired or ran out of stock, either merge the quantity into an existing
     * normal-price item for the same batch, or strip the saleEventId so the item stays in the
     * cart at the original price.
     */
    private void reconcileStaleSaleItems(Long cartId) {
        log.info("reconcileStaleSaleItems(cartId: " + cartId.toString() + ")");
        List<CartItem> items = cartItemRepository.findByCartId(cartId);

        for (CartItem item : items) {
            log.info("cart item id = " + item.getCartItemId().toString());
            if (item.getSaleEventId() == null) continue;

            boolean stillActive = saleProductRepository
                    .findActiveSaleProductByBatchId(item.getBatchDetailId())
                    .isPresent();
            log.info("Event status: " + stillActive);
            if (stillActive) continue;

            // Sale is no longer available — find an existing normal-price item for the same batch
            Optional<CartItem> normalItem = cartItemRepository
                    .findByCartIdAndBatchDetailIdAndSaleEventIdIsNull(cartId, item.getBatchDetailId());

            if (normalItem.isPresent()) {
                CartItem normal = normalItem.get();
                normal.setQuantity(normal.getQuantity() + item.getQuantity());
                cartItemRepository.save(normal);
                cartItemRepository.delete(item);
            } else {
                item.setSaleEventId(null);
                cartItemRepository.save(item);
            }
        }
    }

    private CartItem persistSaleItem(Long cartId, String batchDetailId, long quantity,
                                     Boolean isSelected, Long saleEventId) {
        Optional<CartItem> existing = cartItemRepository
                .findByCartIdAndBatchDetailIdAndSaleEventId(cartId, batchDetailId, saleEventId);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + quantity);
            if (isSelected != null) item.setIsSelected(isSelected);
            return cartItemRepository.save(item);
        }
        CartItem newItem = new CartItem();
        newItem.setCartId(cartId);
        newItem.setBatchDetailId(batchDetailId);
        newItem.setQuantity(quantity);
        newItem.setIsSelected(isSelected != null ? isSelected : true);
        newItem.setSaleEventId(saleEventId);
        return cartItemRepository.save(newItem);
    }

    private CartItem persistNormalItem(Long cartId, String batchDetailId, long quantity, Boolean isSelected) {
        Optional<CartItem> existing = cartItemRepository
                .findByCartIdAndBatchDetailIdAndSaleEventIdIsNull(cartId, batchDetailId);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + quantity);
            if (isSelected != null) item.setIsSelected(isSelected);
            return cartItemRepository.save(item);
        }
        CartItem newItem = new CartItem();
        newItem.setCartId(cartId);
        newItem.setBatchDetailId(batchDetailId);
        newItem.setQuantity(quantity);
        newItem.setIsSelected(isSelected != null ? isSelected : true);
        newItem.setSaleEventId(null);
        return cartItemRepository.save(newItem);
    }

    private CartItemResponse convertToResponse(CartItemWithBatchDetailDTO dto) {
        BigDecimal originPrice = dto.getUnitPrice() != null ? dto.getUnitPrice() : BigDecimal.ZERO;
        BigDecimal unitPrice = (dto.getSalePrice() != null && dto.getSalePrice() > 0)
                ? BigDecimal.valueOf(dto.getSalePrice())
                : originPrice;
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(dto.getQuantity()));

        Integer batchQuantity = dto.getBatchQuantity();
        CartItem.CartItemStatus status = (batchQuantity != null && batchQuantity <= 0)
                ? CartItem.CartItemStatus.OUT_OF_STOCK
                : CartItem.CartItemStatus.AVAILABLE;

        return CartItemResponse.builder()
                .cartItemId(dto.getCartItemId())
                .cartId(dto.getCartId())
                .batchDetailId(dto.getBatchDetailId())
                .quantity(dto.getQuantity())
                .isSelected(dto.getIsSelected())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .productName(dto.getProductName())
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .saleEventId(dto.getSaleEventId())
                .salePrice(dto.getSalePrice())
                .disVal(dto.getDisVal())
                .cartItemStatus(status)
                .build();
    }
}
