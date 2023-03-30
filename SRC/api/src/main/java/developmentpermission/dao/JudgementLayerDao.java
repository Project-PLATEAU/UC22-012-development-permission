package developmentpermission.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import developmentpermission.entity.ColumnValue;
import developmentpermission.entity.Distance;
import developmentpermission.entity.Oid;

/**
 * F_レイヤDAO
 */
@Transactional
public class JudgementLayerDao extends AbstractDao {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(JudgementLayerDao.class);

	/**
	 * コンストラクタ
	 * 
	 * @param emf Entityマネージャファクトリ
	 */
	public JudgementLayerDao(EntityManagerFactory emf) {
		super(emf);
	}

	/**
	 * 指定レイヤと指定地番図形が重なるか判定
	 * 
	 * @param lotNumberIdList 地番IDリスト
	 * @param targetTableName 比較テーブル名
	 * @return 比較テーブル内図形で地番と重なった図形のOID1件
	 */
	@SuppressWarnings("unchecked")
	public List<Oid> getIntersectsOid(List<Integer> lotNumberIdList, String targetTableName) {
		LOGGER.debug("指定レイヤと指定地番図形が重なるか判定 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"WITH lon_number_geom AS ( " + // 地番の特定
					"  SELECT " + //
					"    geom " + //
					"  FROM " + //
					"    f_lot_number " + //
					"  WHERE " + //
					"    chiban_id IN (:lotNumbers) " + //
					") " + //
					"SELECT DISTINCT " + //
					"  a.ogc_fid AS oid " + //
					"FROM " + //
					"  " + targetTableName + " AS a " + //
					"INNER JOIN " + //
					"  lon_number_geom AS b " + // 接触確認
					"ON " + //
					"  ST_Intersects(a.wkb_geometry, b.geom) " + //
					"ORDER BY a.ogc_fid ASC";

			return em.createNativeQuery(sql, Oid.class).setParameter("lotNumbers", lotNumberIdList).getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("指定レイヤと指定地番図形が重なるか判定 終了");
		}
	}

	/**
	 * 指定レイヤと指定地番図形が重なるか判定
	 * 
	 * @param lotNumberIdList 地番IDリスト
	 * @param targetTableName 比較テーブル名
	 * @return 比較テーブル内図形で地番と重なった図形のOID1件
	 */
	@SuppressWarnings("unchecked")
	public List<Oid> getBufferIntersectsOid(List<Integer> lotNumberIdList, String targetTableName, int epsg,
			double buffer) {
		LOGGER.debug("指定レイヤと指定地番図形(バッファ)が重なるか判定 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"WITH lon_number_geom AS ( " + // 地番を特定し、バッファを発生
					"  SELECT " + //
					"    ST_Transform(" + //
					"      CAST(ST_Buffer(" + // 型変換を「::」で実施すると、フレームワークでエラーとなるので、cast関数を使用する。
					"        CAST(ST_Transform(geom, 4612) AS geography), :buffer" + // 距離指定でバッファを発生させる場合は、一度地理座標系に変換し、geographyに変換すると精度の高いバッファができる
					"      ) AS geometry), :epsg) AS geom " + // geographyは元のgeometryに戻し、さらに元の座標系に変換する
					"  FROM " + //
					"    f_lot_number " + //
					"  WHERE " + //
					"    chiban_id IN (:lotNumbers) " + //
					") " + //
					"SELECT DISTINCT " + //
					"  a.ogc_fid AS oid " + //
					"FROM " + //
					"  " + targetTableName + " AS a " + //
					"INNER JOIN " + //
					"  lon_number_geom AS b " + // 接触確認
					"ON " + //
					"  ST_Intersects(a.wkb_geometry, b.geom) " + //
					"ORDER BY a.ogc_fid ASC";

			return em.createNativeQuery(sql, Oid.class).setParameter("lotNumbers", lotNumberIdList)
					.setParameter("epsg", epsg).setParameter("buffer", buffer).getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("指定レイヤと指定地番図形(バッファ)が重なるか判定 終了");
		}
	}

	/**
	 * 指定レイヤと指定地番図形が重ならない場合の距離取得
	 * 
	 * @param lotNumberIdList 地番IDリスト
	 * @param targetTableName 比較テーブル名
	 * @return 距離
	 */
	@SuppressWarnings("unchecked")
	public List<Distance> getDistance(List<Integer> lotNumberIdList, String targetTableName, int epsg) {
		LOGGER.debug("指定レイヤと指定地番図形が重ならない場合の距離取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					" WITH lot_number_geom AS" + 
					" (" +
					" SELECT" + 
					" ST_Union(geom) AS geom" +
					" FROM f_lot_number" +
					" WHERE chiban_id IN (:lotNumbers))," +
					" judgement_layer_geom AS" +
					" (" +
					" SELECT" + 
					" ST_Union(wkb_geometry) AS geom" +
					" FROM " + targetTableName +
					" )" +
					" SELECT" + 
					" ST_Distance(" +
					" CAST(ST_Transform(a.geom, :epsg) AS geography)," +
					" CAST(ST_Transform(b.geom, :epsg) AS geography))" +
					" AS distance" +
					" FROM lot_number_geom AS a" + 
					" CROSS JOIN judgement_layer_geom AS b";

			return em.createNativeQuery(sql, Distance.class).setParameter("lotNumbers", lotNumberIdList)
					.setParameter("epsg", epsg).getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("指定レイヤと指定地番図形が重ならない場合の距離取得 終了");
		}
	}
	
	/**
	 * 指定テーブルのと指定カラム値取得
	 * 
	 * @param tableName  テーブル名
	 * @param columnName カラム名
	 * @param oid        OID
	 * @return カラム値
	 */
	@SuppressWarnings("unchecked")
	public List<ColumnValue> getColumnValue(String tableName, String columnName, int oid) {
		LOGGER.debug("指定テーブルの指定カラム値取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT CAST(" + columnName + " AS TEXT) AS val " + //
					"FROM " + tableName + " " + //
					"WHERE ogc_fid = :oid";

			return em.createNativeQuery(sql, ColumnValue.class).setParameter("oid", oid).getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("指定テーブルの指定カラム値取得 終了");
		}
	}
}
