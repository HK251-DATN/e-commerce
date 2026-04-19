package microservice.base_source.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class QrPaymentService {
    
    private static final String SEPAY_QR_BASE_URL = "https://qr.sepay.vn/img";
    private static final String ORDER_PREFIX = "DH";
    
    @Value("${sepay.account}")
    private String sepayAccount;
    
    @Value("${sepay.bank}")
    private String sepayBank;
    
    public String createOrderTransactionQrUrl(String orderId, Long totalPrice) {
        Assert.hasText(orderId, "orderId must not be blank");
        Assert.notNull(totalPrice, "totalPrice must not be null");
        Assert.isTrue(totalPrice > 0, "totalPrice must be positive");
        
        // Format: DH00012345 (10 chars total, zero-padded to 8 digits)
        String description = "DH%08d".formatted(Integer.parseInt(orderId));
        String encodedDescription = URLEncoder.encode(description, StandardCharsets.UTF_8);
        
        // long để tránh "10000.00" — ngân hàng không nhận số thập phân
        long amount = totalPrice;
        
        return UriComponentsBuilder.fromHttpUrl(SEPAY_QR_BASE_URL)
                .queryParam("acc", sepayAccount)
                .queryParam("bank", sepayBank)
                .queryParam("amount", amount)
                .queryParam("des", encodedDescription)
                .toUriString();
    }
}
