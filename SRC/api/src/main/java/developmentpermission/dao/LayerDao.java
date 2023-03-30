package developmentpermission.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import developmentpermission.entity.Layer;

/**
 * M_レイヤDAO
 */
@Transactional
public class LayerDao extends AbstractDao {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(LayerDao.class);

	/**
	 * コンストラクタ
	 * 
	 * @param emf Entityマネージャファクトリ
	 */
	public LayerDao(EntityManagerFactory emf) {
		super(emf);
	}

	/**
	 * レイヤ一覧取得
	 * 
	 * @param layerIdList レイヤIDリスト
	 * @return レイヤ一覧
	 */
	@SuppressWarnings("unchecked")
	public List<Layer> getLayers(List<String> layerIdList) {
		LOGGER.debug("レイヤ一覧取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			String sql = "" + //
					"SELECT " + //
					"  layer_id, " + //
					"  layer_type, " + //
					"  layer_name, " + //
					"  table_name, " + //
					"  layer_code, " + //
					"  layer_query, " + //
					"  query_require_flag " + //
					"FROM " + //
					"  m_layer " + //
					"WHERE " + //
					"  layer_id IN (:layerId) " + //
					"ORDER BY layer_id ASC";

			return em.createNativeQuery(sql, Layer.class).setParameter("layerId", layerIdList).getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("レイヤ一覧取得 終了");
		}
	}
}
