package microservice.base_source.presentation.response.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import microservice.base_source.domain.entity.Address;
import microservice.base_source.domain.entity.Order;
import microservice.base_source.domain.entity.OrderItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailResponse {

    // Order information
    private Long orderId;
    private String buyerId;
    private Long addressId;
    private String status;
    private String paymentMethod;
    private String note;
    private String type;
    private Long shippingFee;
    private Long totalPrice;
    private Long couponId;
    private String transactionQrUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Address information
    private String receiverName;
    private String receiverPNum;
    private String province;
    private String district;
    private String commune;
    private String detail;

    // Order items
    private List<OrderItemResponse> orderItems;

    public static OrderDetailResponse fromEntity(Order order, List<OrderItem> orderItems) {
        return fromEntityWithProductNamesAndAddress(order, orderItems, Map.of(), null, null);
    }

    public static OrderDetailResponse fromEntityWithProductNames(
            Order order, List<OrderItem> orderItems, Map<Long, String> productNameByOrderItemId) {
        return fromEntityWithProductNamesAndAddress(order, orderItems, productNameByOrderItemId, null, null);
    }

    public static OrderDetailResponse fromEntityWithProductNamesAndAddress(
            Order order, List<OrderItem> orderItems, Map<Long, String> productNameByOrderItemId,
            Map<Long, String> productImgByOrderItemId, Address address) {
        return OrderDetailResponse.builder()
                .orderId(order.getOrderId())
                .buyerId(order.getBuyerId())
                .addressId(order.getAddressId())
                .status(order.getStatus() != null ? order.getStatus().toString() : null)
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().toString() : null)
                .note(order.getNote())
//                .type(order.getType() != null ? order.getType().toString() : null)
                .shippingFee(order.getShippingFee())
                .totalPrice(order.getTotalPrice())
                .couponId(order.getCouponId())
                .transactionQrUrl(order.getTransactionQrUrl())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .receiverName(address != null ? address.getReceiverName() : null)
                .receiverPNum(address != null ? address.getReceiverPNum() : null)
                .province(address != null ? address.getProvince() : null)
                .district(address != null ? address.getDistrict() : null)
                .commune(address != null ? address.getCommune() : null)
                .detail(address != null ? address.getDetail() : null)
                .orderItems(orderItems.stream()
                        .map(item -> OrderItemResponse.fromEntityWithProductInfo(
                                item,
                                productNameByOrderItemId.getOrDefault(item.getOrderItemId(), ""),
                                productImgByOrderItemId != null
                                        ? productImgByOrderItemId.getOrDefault(item.getOrderItemId(), "")
                                        : ""))
                        .collect(Collectors.toList()))
                .build();
    }
}