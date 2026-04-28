package microservice.base_source.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import microservice.base_source.domain.entity.BatchDetail;
import microservice.base_source.domain.entity.SaleProduct;
import microservice.base_source.domain.exception.type.NotFoundException;
import microservice.base_source.domain.exception.type.WarnException;
import microservice.base_source.domain.use_case.SaleProductUseCase;
import microservice.base_source.persistence.repository.BatchDetailRepository;
import microservice.base_source.persistence.repository.SaleProductRepository;

@Service
@RequiredArgsConstructor
public class SaleProductService implements SaleProductUseCase {

    private final SaleProductRepository saleProductRepository;
    private final BatchDetailRepository batchDetailRepository;

    @Override
    public List<SaleProduct> getAll(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<SaleProduct> saleProductList = saleProductRepository.findAll(pageable);
        return saleProductList.getContent();
    }

    @Override
    public SaleProduct get(Long saleEventId, String batchId) {
        return saleProductRepository.findOneByEventAndBatch(saleEventId, batchId)
                .orElseThrow(() -> new NotFoundException("SaleProduct not found"));
    }

    @Override
    public SaleProduct get(String batchId) {
        return saleProductRepository.findOneByBatch(batchId)
                .orElseThrow(() -> new NotFoundException("SaleProduct not found"));
    }

    @Override
    public SaleProduct create(SaleProduct saleProduct) {
        if (saleProductRepository.existsByEventAndBatch(saleProduct.getSaleEventId(), saleProduct.getBatchId())) {
            throw new WarnException("Product is already in this sale event");
        }

        BatchDetail batchDetail = batchDetailRepository.findById(saleProduct.getBatchId())
                .orElseThrow(() -> new NotFoundException("BatchDetail not found: " + saleProduct.getBatchId()));

        if (saleProduct.getMaxQty() != null && saleProduct.getMaxQty() > batchDetail.getQuantity()) {
            throw new WarnException(
                    "maxQty (" + saleProduct.getMaxQty() + ") cannot exceed available batch quantity (" + batchDetail.getQuantity() + ")");
        }

        saleProduct.setSalePrice(calculateSalePrice(batchDetail.getPrice(), saleProduct.getDisVal()));

        return saleProductRepository.save(saleProduct);
    }

    @Override
    public SaleProduct update(Long saleEventId, String batchId, SaleProduct saleProduct) {
        SaleProduct existing = saleProductRepository.findOneByEventAndBatch(saleEventId, batchId)
                .orElseThrow(() -> new NotFoundException("SaleProduct not found"));

        if (saleProduct.getMaxQty() != null) {
            BatchDetail batchDetail = batchDetailRepository.findById(batchId)
                    .orElseThrow(() -> new NotFoundException("BatchDetail not found: " + batchId));
            if (saleProduct.getMaxQty() > batchDetail.getQuantity()) {
                throw new WarnException(
                        "maxQty (" + saleProduct.getMaxQty() + ") cannot exceed available batch quantity (" + batchDetail.getQuantity() + ")");
            }
            existing.setMaxQty(saleProduct.getMaxQty());

            if (saleProduct.getDisVal() != null) {
                existing.setDisVal(saleProduct.getDisVal());
                existing.setSalePrice(calculateSalePrice(batchDetail.getPrice(), saleProduct.getDisVal()));
            }
        } else if (saleProduct.getDisVal() != null) {
            BatchDetail batchDetail = batchDetailRepository.findById(batchId)
                    .orElseThrow(() -> new NotFoundException("BatchDetail not found: " + batchId));
            existing.setDisVal(saleProduct.getDisVal());
            existing.setSalePrice(calculateSalePrice(batchDetail.getPrice(), saleProduct.getDisVal()));
        }

        if (saleProduct.getMaxBuy() != null) {
            existing.setMaxBuy(saleProduct.getMaxBuy());
        }

        return saleProductRepository.save(existing);
    }

    @Override
    public void delete(Long saleEventId, String batchId) {
        Optional<SaleProduct> existing = saleProductRepository.findOneByEventAndBatch(saleEventId, batchId);
        if (existing.isEmpty()) {
            return;
        }
        saleProductRepository.delete(existing.get());
    }

    /**
     * Rounds to nearest 1000 VND for easy display and payment.
     * e.g. price=85000, disVal=20 → 68000 VND
     */
    private int calculateSalePrice(BigDecimal originalPrice, Integer disVal) {
        if (originalPrice == null || disVal == null) return 0;
        long raw = originalPrice
                .multiply(BigDecimal.valueOf(100 - disVal))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .longValue();
        return (int) (Math.round(raw / 1000.0) * 1000);
    }
}
