package microservice.base_source.data_access.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import microservice.base_source.data_access.entity.BatchDetail;

@Repository
public interface BatchDetailRepository extends JpaRepository<BatchDetail, Long> {
	
}
