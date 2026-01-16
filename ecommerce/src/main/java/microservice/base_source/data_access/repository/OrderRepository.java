package microservice.base_source.data_access.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import microservice.base_source.data_access.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
	
}
