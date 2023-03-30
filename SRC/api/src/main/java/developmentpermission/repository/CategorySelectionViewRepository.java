package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.ApplicationCategorySelectionView;

/**
 * M_申請区分選択画面Repositoryインタフェース
 */
@Transactional
@Repository
public interface CategorySelectionViewRepository extends JpaRepository<ApplicationCategorySelectionView, String> {

	/**
	 * 申請区分選択画面リスト取得
	 * 
	 * @return 申請区分選択画面リスト
	 */
	@Query(value = "SELECT view_id, view_flag, multiple_flag, title, require_flag, description FROM m_application_category_selection_view WHERE view_flag = '1' ORDER BY view_id ASC", nativeQuery = true)
	List<ApplicationCategorySelectionView> getCategorySelectionViewList();

}
