package microservice.base_source.data_access.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import microservice.base_source.data_access.entity.SaleProduct;

@Repository
public interface SaleProductRepository extends JpaRepository<SaleProduct, Long> {
	
}
