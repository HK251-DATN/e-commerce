package microservice.base_source.domain.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microservice.base_source.domain.entity.Order;
import microservice.base_source.domain.entity.ProcessedTransaction;
import microservice.base_source.domain.use_case.PaymentPollingUseCase;
import microservice.base_source.persistence.repository.OrderRepository;
import microservice.base_source.persistence.repository.ProcessedTransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentPollingService implements PaymentPollingUseCase {
    
    private final Sheets sheetsService;
    private final OrderRepository orderRepository;
    private final ProcessedTransactionRepository processedTransactionRepository;
    
    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;
    
    private int lastProcessedRow = 2;
    
    
    @Override
    @Scheduled(fixedDelayString = "${payment.polling.interval}")
    public void pollPayments () {
        try {
            // Chỉ đọc từ row mới nhất, không đọc lại toàn bộ
            String range = String.format("sepay!A%d:I", lastProcessedRow);
            
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            
            List<List<Object>> rows = response.getValues();
            if (rows == null || rows.isEmpty()) return;
            
            log.info("Polling: found {} new row(s)", rows.size());
            
            for (List<Object> row : rows) {
                processRow(row);
            }
            
            lastProcessedRow += rows.size();
            
        } catch (Exception e) {
            log.error("Error polling Google Sheet: {}", e.getMessage());
        }
    }
    
    @Override
    public void processRow (List<Object> row) {
        // Tuỳ cột Sepay — thường là:
        // [0] Thời gian, [1] Số tiền, [2] Nội dung, [3] Mã GD, [4] Tài khoản
        if (row.size() < 9) return;
        
        String description  = row.get(5).toString().trim();
        String transactionId = row.get(8).toString().trim();
        
        // Tránh xử lý 2 lần
        if (processedTransactionRepository.existsByTransactionId(transactionId)) return;
        
        // Tách order code khỏi nội dung — VD: "Thanh toan DH-12345"
        String orderCode = extractOrderCode(description);
        if (orderCode == null) {
            log.warn("No order code found in: {}", description);
            return;
        }
        
        // Tìm order
        orderRepository.findByOrderId(Long.valueOf(orderCode)).ifPresentOrElse(
                order -> {
                    order.setStatus(Order.OrderStatus.PAID);
                    order.setTransactionId(transactionId);
                    orderRepository.save(order);
                    
                    // Lưu lại tx đã xử lý
                    ProcessedTransaction tx = new ProcessedTransaction();
                    tx.setTransactionId(transactionId);
                    processedTransactionRepository.save(tx);
                    
                    log.info("Order {} updated to PAID", orderCode);
                },
                () -> log.warn("Order not found for code: {}", orderCode)
        );
    }
    
    @Override
    public String extractOrderCode (String description) {
        // Regex tìm pattern Format: DH00012345 (10 chars total, zero-padded to 8 digits)
        Pattern pattern = Pattern.compile("DH\\d{8}");
        Matcher matcher = pattern.matcher(description.toUpperCase());
        String result = null;
        
        if (matcher.find()) {
            result = String.valueOf(Integer.parseInt(matcher.group().substring(2))); // remove "DH";
        }
        
        return result;
    }
    
    
}
