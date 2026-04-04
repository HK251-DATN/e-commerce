package microservice.base_source.persistence.repository;

import microservice.base_source.domain.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartIdAndIsSelected(Long cartId, Boolean isSelected);
    void deleteByCartIdAndIsSelected(Long cartId, Boolean isSelected);
}