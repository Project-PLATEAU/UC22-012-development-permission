package developmentpermission.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import developmentpermission.entity.CategoryJudgement;
import developmentpermission.entity.Department;

/**
 * 回答DAO
 */
@Transactional
public class AnswerDao extends AbstractDao {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(AnswerDao.class);

	/**
	 * コンストラクタ
	 * 
	 * @param emf Entityマネージャファクトリ
	 */
	public AnswerDao(EntityManagerFactory emf) {
		super(emf);
	}

	/**
	 * M_部署検索
	 * 
	 * @param answerId 回答ID
	 * @return M_部署リスト
	 */
	@SuppressWarnings("unchecked")
	public List<Department> getDepartmentList(Integer answerId) {
		LOGGER.debug("M_部署検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT DISTINCT " + //
					"  c.department_id AS department_id, " + //
					"  c.department_name AS department_name, " + //
					"  c.answer_authority_flag AS answer_authority_flag, " + //
					"  c.mail_address AS mail_address " + //
					"FROM " + //
					"  o_answer AS a " + //
					"INNER JOIN " + //
					"  m_category_judgement AS b " + //
					"ON " + //
					"  a.judgement_id = b.judgement_item_id " + //
					"INNER JOIN " + //
					"  m_department AS c " + //
					"ON " + //
					"  b.department_id = c.department_id " + //
					"WHERE " + //
					"  a.answer_id = :answerId " + //
					"ORDER BY " + //
					"  department_id ASC";
			Query query = em.createNativeQuery(sql, Department.class);
			query = query.setParameter("answerId", answerId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("M_部署検索 終了");
		}
	}
	
	/**
	 * M_区分判定検索
	 * 
	 * @param answerId 回答ID
	 * @return M_区分判定リスト
	 */
	@SuppressWarnings("unchecked")
	public List<CategoryJudgement> getCategoryJudgementList(int answerId) {
		LOGGER.debug("M_区分判定検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT DISTINCT " + //
					"  b.judgement_item_id AS judgement_item_id, " + //
					"  b.department_id AS department_id, " + //
					"  b.category_1 AS category_1, " + //
					"  b.category_2 AS category_2, " + //
					"  b.category_3 AS category_3, " + //
					"  b.category_4 AS category_4, " + //
					"  b.category_5 AS category_5, " + //
					"  b.category_6 AS category_6, " + //
					"  b.category_7 AS category_7, " + //
					"  b.category_8 AS category_8, " + //
					"  b.category_9 AS category_9, " + //
					"  b.category_10 AS category_10, " + //
					"  b.gis_judgement AS gis_judgement, " + //
					"  b.buffer AS buffer, " + //
					"  b.judgement_layer AS judgement_layer, " + //
					"  b.title AS title, " + //
					"  b.applicable_summary AS applicable_summary, " + //
					"  b.applicable_description AS applicable_description, " + //
					"  b.non_applicable_display_flag AS non_applicable_display_flag, " + //
					"  b.non_applicable_summary AS non_applicable_summary, " + //
					"  b.non_applicable_description AS non_applicable_description, " + //
					"  b.table_name AS table_name, " + //
					"  b.field_name AS field_name, " + //
					"  b.non_applicable_layer_display_flag AS non_applicable_layer_display_flag, " + //
					"  b.simultaneous_display_layer AS simultaneous_display_layer, " + //
					"  b.simultaneous_display_layer_flag AS simultaneous_display_layer_flag, " + //
					"  b.display_attribute_flag AS display_attribute_flag, " + //
					"  b.answer_require_flag AS answer_require_flag, " + //
					"  b.default_answer AS default_answer, " + //
					"  b.answer_editable_flag AS answer_editable_flag " + //
					"FROM " + //
					"  o_answer AS a " + //
					"INNER JOIN " + //
					"  m_category_judgement AS b " + //
					"ON " + //
					"  a.judgement_id = b.judgement_item_id " + //
					"WHERE " + //
					"  a.answer_id = :answerId " + //
					"ORDER BY " + //
					"  b.judgement_item_id ASC";
			Query query = em.createNativeQuery(sql, CategoryJudgement.class);
			query = query.setParameter("answerId", answerId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("M_区分判定検索 終了");
		}
	}
}
