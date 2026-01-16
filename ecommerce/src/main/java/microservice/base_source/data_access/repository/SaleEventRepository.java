package microservice.base_source.data_access.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import microservice.base_source.data_access.entity.SaleEvent;

@Repository
public interface SaleEventRepository extends JpaRepository<SaleEvent, Long> {
	
}
