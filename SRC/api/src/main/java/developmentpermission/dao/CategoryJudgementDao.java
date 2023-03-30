package developmentpermission.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import developmentpermission.entity.CategoryJudgement;

/**
 * M_区分判定DAO
 */
public class CategoryJudgementDao extends AbstractDao {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(CategoryJudgementDao.class);

	/**
	 * コンストラクタ
	 * 
	 * @param emf Entityマネージャファクトリ
	 */
	public CategoryJudgementDao(EntityManagerFactory emf) {
		super(emf);
	}

	/**
	 * 区分判定一覧取得
	 * 
	 * @return 区分判定一覧
	 */
	@SuppressWarnings("unchecked")
	public List<CategoryJudgement> getCategoryJudgementList() {
		LOGGER.debug("区分判定一覧取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			String sql = "" + //
					"SELECT " + //
					"  judgement_item_id, " + //
					"  department_id, " + //
					"  category_1, " + //
					"  category_2, " + //
					"  category_3, " + //
					"  category_4, " + //
					"  category_5, " + //
					"  category_6, " + //
					"  category_7, " + //
					"  category_8, " + //
					"  category_9, " + //
					"  category_10, " + //
					"  gis_judgement, " + //
					"  buffer, " + //
					"  display_attribute_flag, " + //
					"  judgement_layer, " + //
					"  title, " + //
					"  applicable_summary, " + //
					"  applicable_description, " + //
					"  non_applicable_display_flag, " + //
					"  non_applicable_summary, " + //
					"  non_applicable_description, " + //
					"  table_name, " + //
					"  field_name, " + //
					"  non_applicable_layer_display_flag, " + //
					"  simultaneous_display_layer, " + //
					"  simultaneous_display_layer_flag, " + //
					"  answer_require_flag, " + //
					"  default_answer, " + //
					"  answer_editable_flag " + //
					"FROM " + //
					"  m_category_judgement " + //
					"ORDER BY " + //
					"  disp_order ASC";

			return em.createNativeQuery(sql, CategoryJudgement.class).getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("区分判定一覧取得 終了");
		}
	}

	/**
	 * 区分判定一覧取得
	 * 
	 * @param judgementItemId 判定項目ID
	 * @return 区分判定一覧
	 */
	@SuppressWarnings("unchecked")
	public List<CategoryJudgement> getCategoryJudgementListById(String judgementItemId) {
		LOGGER.debug("区分判定一覧取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			String sql = "" + //
					"SELECT " + //
					"  judgement_item_id, " + //
					"  department_id, " + //
					"  category_1, " + //
					"  category_2, " + //
					"  category_3, " + //
					"  category_4, " + //
					"  category_5, " + //
					"  category_6, " + //
					"  category_7, " + //
					"  category_8, " + //
					"  category_9, " + //
					"  category_10, " + //
					"  gis_judgement, " + //
					"  buffer, " + //
					"  display_attribute_flag, " + //
					"  judgement_layer, " + //
					"  title, " + //
					"  applicable_summary, " + //
					"  applicable_description, " + //
					"  non_applicable_display_flag, " + //
					"  non_applicable_summary, " + //
					"  non_applicable_description, " + //
					"  table_name, " + //
					"  field_name, " + //
					"  non_applicable_layer_display_flag, " + //
					"  simultaneous_display_layer, " + //
					"  simultaneous_display_layer_flag, " + //
					"  answer_require_flag, " + //
					"  default_answer, " + //
					"  answer_editable_flag " + //
					"FROM " + //
					"  m_category_judgement " + //
					"WHERE " + //
					"  judgement_item_id = :judgementItemId " + //
					"ORDER BY " + //
					"  disp_order ASC";

			return em.createNativeQuery(sql, CategoryJudgement.class).setParameter("judgementItemId", judgementItemId)
					.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("区分判定一覧取得 終了");
		}
	}
}
