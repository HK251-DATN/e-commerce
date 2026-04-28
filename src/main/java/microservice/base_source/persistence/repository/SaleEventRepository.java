package microservice.base_source.persistence.repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import microservice.base_source.domain.entity.SaleEvent;

@Repository
public interface SaleEventRepository extends JpaRepository<SaleEvent, Long> {

	@Query(value = """
			SELECT * FROM SALE_EVENT SE
			WHERE (CAST(:searchString AS TEXT) = '' OR
				LOWER(SE.NAME)        LIKE LOWER(CONCAT('%', CAST(:searchString AS TEXT), '%')) OR
				LOWER(SE.DESCRIPTION) LIKE LOWER(CONCAT('%', CAST(:searchString AS TEXT), '%')))
				AND (CAST(:activeYn AS TEXT) = '' OR SE.ACTIVE_YN  = CAST(:activeYn AS TEXT))
				AND (CAST(:enableYn AS TEXT) = '' OR SE.ENABLED_YN = CAST(:enableYn AS TEXT))
				AND (CAST(:beginTime AS TIME)      IS NULL OR SE.BEGIN_TIME >= CAST(:beginTime AS TIME))
				AND (CAST(:endTime   AS TIME)      IS NULL OR SE.END_TIME   <= CAST(:endTime   AS TIME))
				AND (CAST(:beginDate AS TIMESTAMP) IS NULL OR SE.BEGIN_DATE >= CAST(:beginDate AS TIMESTAMP))
				AND (CAST(:endDate   AS TIMESTAMP) IS NULL OR SE.END_DATE   <= CAST(:endDate   AS TIMESTAMP))
			LIMIT :size
			OFFSET (:page - 1) * :size
			""",
			nativeQuery = true)
	List<SaleEvent> search(
		@Param("searchString") String searchString,
		@Param("activeYn")  String activeYn,
		@Param("enableYn")  String enableYn,
		@Param("beginTime") LocalTime beginTime,
		@Param("endTime") 	LocalTime endTime,
		@Param("beginDate") LocalDateTime beginDate,
		@Param("endDate") 	LocalDateTime endDate,
		@Param("page") int page,
		@Param("size") int size
	);

	@Query(value = """
			SELECT * FROM SALE_EVENT
			WHERE ACTIVE_YN = 'Y'
			AND ENABLED_YN = 'Y'
			AND (BEGIN_DATE IS NULL OR BEGIN_DATE <= NOW())
			AND (END_DATE   IS NULL OR END_DATE   >= NOW())
			ORDER BY DISPLAY_PRIORITY ASC NULLS LAST
			LIMIT :size OFFSET (:page - 1) * :size
			""", nativeQuery = true)
	List<SaleEvent> findActiveEvents(@Param("page") int page, @Param("size") int size);
}