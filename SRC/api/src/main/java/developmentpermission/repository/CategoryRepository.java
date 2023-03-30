package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.ApplicationCategoryMaster;

/**
 * M_申請区分Repositoryインタフェース
 */
@Transactional
@Repository
public interface CategoryRepository extends JpaRepository<ApplicationCategoryMaster, String> {

	/**
	 * 申請区分リスト取得
	 * 
	 * @param viewId 画面ID
	 * @return 申請区分リスト
	 */
	@Query(value = "SELECT category_id, view_id, \"order\", label_name FROM m_application_category WHERE view_id = :viewId ORDER BY \"order\" ASC", nativeQuery = true)
	List<ApplicationCategoryMaster> getApplicationCategoryList(@Param("viewId") String viewId);
}
