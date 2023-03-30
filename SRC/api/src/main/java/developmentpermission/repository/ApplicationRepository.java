package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.Application;

/**
 * O_申請Repositoryインタフェース
 */
@Transactional
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Integer> {

	/**
	 * 申請一覧取得
	 * 
	 * @return 申請一覧
	 */
	@Query(value = "SELECT application_id, applicant_id, status, register_status, collation_text, register_datetime, update_datetime FROM o_application WHERE application_id = :applicationId ORDER BY application_id ASC", nativeQuery = true)
	List<Application> getApplicationList(@Param("applicationId") Integer applicationId);
}
