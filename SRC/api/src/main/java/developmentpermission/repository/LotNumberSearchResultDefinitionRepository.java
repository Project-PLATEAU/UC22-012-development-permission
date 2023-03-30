package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.LotNumberSearchResultDefinition;

/**
 * M_地番検索結果定義Repositoryインタフェース
 */
@Transactional
@Repository
public interface LotNumberSearchResultDefinitionRepository
		extends JpaRepository<LotNumberSearchResultDefinition, String> {

	/**
	 * 地番検索結果定義一覧取得
	 * 
	 * @return 地番検索結果定義一覧
	 */
	@Query(value = "SELECT lot_number_search_definition_id, display_order, table_type, display_column_name, table_column_name, table_width, response_key FROM m_lot_number_search_result_definition ORDER BY display_order ASC", nativeQuery = true)
	List<LotNumberSearchResultDefinition> getLotNumberSearchResultDefinitionList();

}
