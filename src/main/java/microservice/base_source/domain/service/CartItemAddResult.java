package microservice.base_source.domain.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import microservice.base_source.domain.entity.CartItem;

@Getter
@AllArgsConstructor
public class CartItemAddResult {
    private final CartItem cartItem;
    /** Non-null when part of the requested quantity overflowed the sale limit and was added at original price. */
    private final CartItem overflowItem;
    /** Non-null when quantity was capped or split due to a sale limit. */
    private final String note;

    public CartItemAddResult(CartItem cartItem, String note) {
        this(cartItem, null, note);
    }

    public boolean wasCapped() {
        return note != null;
    }

    public boolean hasOverflow() {
        return overflowItem != null;
    }
}
