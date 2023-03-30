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
public interface ApplicationCategoryMasterRepository extends JpaRepository<ApplicationCategoryMaster, String> {

	/**
	 * 申請区分情報取得
	 * 
	 * @param categoryId  申請区分ID
	 * @param viewId 画面ID
	 * @return ラベル一覧
	 */
	@Query(value = "SELECT category_id, view_id, \"order\", label_name FROM m_application_category WHERE category_id = :categoryId AND view_id = :viewId ORDER BY view_id ASC", nativeQuery = true)
	List<ApplicationCategoryMaster> findByCategoryId(@Param("categoryId") String categoryId, @Param("viewId") String viewId);
}
