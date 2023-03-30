package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.ApplicationSearchResult;

/**
 * M_申請情報検索結果Repositoryインタフェース
 */
@Transactional
@Repository
public interface ApplicationSearchResultRepository extends JpaRepository<ApplicationSearchResult, String> {

	/**
	 * 申請情報検索結果一覧取得
	 * 
	 * @return 申請情報検索結果一覧
	 */
	@Query(value = "SELECT application_search_result_id, reference_type, display_column_name, display_order, table_name, table_column_name, response_key, table_width FROM m_application_search_result ORDER BY display_order ASC, application_search_result_id ASC", nativeQuery = true)
	List<ApplicationSearchResult> getApplicationSearchResultList();

}
