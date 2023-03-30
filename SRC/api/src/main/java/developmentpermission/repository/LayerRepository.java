package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.Layer;

/**
 * M_レイヤRepositoryインタフェース
 */
@Transactional
@Repository
public interface LayerRepository extends JpaRepository<Layer, String> {

	/**
	 * レイヤ一覧取得
	 * 
	 * @param layerIdList レイヤIDリスト
	 * @return レイヤ一覧
	 */
	@Query(value = "SELECT layer_id, layer_type, layer_name, table_name, layer_code, layer_query, query_require_flag FROM m_layer WHERE layer_id IN (:layerId) ORDER BY layer_id ASC", nativeQuery = true)
	List<Layer> getLayers(@Param("layerId") List<String> layerIdList);
}
