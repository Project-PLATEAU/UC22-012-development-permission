package developmentpermission.dao;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import developmentpermission.entity.Answer;
import developmentpermission.entity.ApplicantInformation;
import developmentpermission.entity.Application;
import developmentpermission.entity.ApplicationCategory;
import developmentpermission.entity.ApplicationCategoryMaster;
import developmentpermission.entity.ApplicationCategorySelectionView;
import developmentpermission.entity.ApplicationFileMaster;
import developmentpermission.entity.CategoryJudgement;
import developmentpermission.entity.Department;
import developmentpermission.entity.LotNumberAndDistrict;
import developmentpermission.form.ApplicantInformationItemForm;
import developmentpermission.form.ApplicationCategoryForm;
import developmentpermission.form.ApplicationCategorySelectionViewForm;
import developmentpermission.form.ApplicationSearchConditionForm;
import developmentpermission.form.DepartmentForm;
import developmentpermission.form.StatusForm;

/**
 * O_申請DAO
 */
@Transactional
public class ApplicationDao extends AbstractDao {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationDao.class);

	/** 項目1の申請者情報項目ID */
	public static final String ITEM_1_ID = "1001";
	/** 項目2の申請者情報項目ID */
	public static final String ITEM_2_ID = "1002";
	/** 項目3の申請者情報項目ID */
	public static final String ITEM_3_ID = "1003";
	/** 項目4の申請者情報項目ID */
	public static final String ITEM_4_ID = "1004";
	/** 項目5の申請者情報項目ID */
	public static final String ITEM_5_ID = "1005";
	/** 項目6の申請者情報項目ID */
	public static final String ITEM_6_ID = "1006";
	/** 項目7の申請者情報項目ID */
	public static final String ITEM_7_ID = "1007";
	/** 項目8の申請者情報項目ID */
	public static final String ITEM_8_ID = "1008";
	/** 項目9の申請者情報項目ID */
	public static final String ITEM_9_ID = "1009";
	/** 項目10の申請者情報項目ID */
	public static final String ITEM_10_ID = "1010";

	/**
	 * コンストラクタ
	 * 
	 * @param emf Entityマネージャファクトリ
	 */
	public ApplicationDao(EntityManagerFactory emf) {
		super(emf);
	}

	/**
	 * 申請情報検索
	 * 
	 * @param paramForm 検索条件
	 * @return 検索結果
	 */
	@SuppressWarnings("unchecked")
	public List<Application> searchApplication(ApplicationSearchConditionForm paramForm) {
		LOGGER.debug("申請情報検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			// 申請者情報一覧(複数可)
			List<ApplicantInformationItemForm> applicantList = paramForm.getApplicantInformationItemForm();
			// 申請区分選択一覧(複数可)
			List<ApplicationCategorySelectionViewForm> categorySelectionViewList = paramForm.getApplicationCategories();
			// ステータス(複数不可)
			List<StatusForm> statusList = paramForm.getStatus();
			// 部署(複数不可)
			List<DepartmentForm> departmentList = paramForm.getDepartment();

			// WHERE句構築
			StringBuffer where = new StringBuffer();

			// 登録ステータス(申請済のデータのみを許可)
			appendWhereText(where);
			where.append("oa.register_status='1' ");

			// 申請者情報検索パラメータ
			Map<Integer, String> applicantConditionParam = new LinkedHashMap<Integer, String>();
			if (applicantList != null) {
				// 申請者情報条件はvalueの部分一致検索
				for (ApplicantInformationItemForm applicant : applicantList) {
					String applicantInfoId = applicant.getId();
					String value = applicant.getValue();
					Integer applicantNum = null;
					if (applicantInfoId != null && value != null && !"".equals(value)) {
						switch (applicantInfoId) {
						case ITEM_1_ID:
							applicantNum = 1;
							break;
						case ITEM_2_ID:
							applicantNum = 2;
							break;
						case ITEM_3_ID:
							applicantNum = 3;
							break;
						case ITEM_4_ID:
							applicantNum = 4;
							break;
						case ITEM_5_ID:
							applicantNum = 5;
							break;
						case ITEM_6_ID:
							applicantNum = 6;
							break;
						case ITEM_7_ID:
							applicantNum = 7;
							break;
						case ITEM_8_ID:
							applicantNum = 8;
							break;
						case ITEM_9_ID:
							applicantNum = 9;
							break;
						case ITEM_10_ID:
							applicantNum = 10;
							break;
						default:
							// 想定外のIDなので条件に設定しない
							LOGGER.warn("未対応の申請者情報項目ID: " + applicantInfoId);
							break;
						}
					}

					if (applicantNum != null) {
						if (!applicantConditionParam.containsKey(applicantNum)) {
							// パラメータをセットできるのは各申請者情報項目ID毎に1つのみ
							appendWhereText(where);
							where.append("oai.item_" + applicantNum + " LIKE CONCAT('%', :applicantId_" + applicantNum
									+ ", '%') ");
							applicantConditionParam.put(applicantNum, value);
						}
					}
				}
			}

			// 申請区分検索パラメータ
			Map<String, List<String>> categoryConditionParam = new LinkedHashMap<String, List<String>>();
			if (categorySelectionViewList != null) {
				// 画面ID別に申請区分IDを集約
				for (ApplicationCategorySelectionViewForm categorySelectionView : categorySelectionViewList) {
					List<ApplicationCategoryForm> categoryList = categorySelectionView.getApplicationCategory();
					if (categoryList != null) {
						for (ApplicationCategoryForm category : categoryList) {
							String categoryId = category.getId();
							String screenId = category.getScreenId();
							if (categoryId != null && !"".equals(categoryId) && screenId != null
									&& !"".equals(screenId)) {
								if (!categoryConditionParam.containsKey(screenId)) {
									categoryConditionParam.put(screenId, new ArrayList<String>());
								}

								List<String> conditionList = categoryConditionParam.get(screenId);
								if (!conditionList.contains(categoryId)) {
									conditionList.add(categoryId);
								}
							}
						}
					}
				}

				// 申請区分はscreenIdが同じもの同士についてはOR条件、違うもの同士についてはAND条件でセット
				int idx = 0;
				for (Map.Entry<String, List<String>> entry : categoryConditionParam.entrySet()) {
					if (idx == 0) {
						appendWhereText(where);
					} else {
						where.append("OR ");
					}
					where.append("(oac.view_id = :viewId_" + entry.getKey() + " AND oac.category_id IN (:categoryId_"
							+ entry.getKey() + ") ) ");
					idx++;
				}
			}

			// ステータス
			String statusParam = null;
			if (statusList != null && statusList.size() > 0) {
				// ステータスは1件のみ
				StatusForm statusForm = statusList.get(0);
				String status = statusForm.getValue();
				if (status != null && !"".equals(status)) {
					appendWhereText(where);
					where.append("oa.status = :status ");
					statusParam = status;
				}
			}

			// 部署
			String departmentParam = null;
			if (departmentList != null && departmentList.size() > 0) {
				// 部署は1件のみ
				DepartmentForm departmentForm = departmentList.get(0);
				String departmentId = departmentForm.getDepartmentId();
				if (departmentId != null && !"".equals(departmentId)) {
					appendWhereText(where);
					where.append("mcj.department_id = :departmentId ");
					departmentParam = departmentId;
				}
			}

			String sql = "" + //
					"SELECT DISTINCT " + //
					"  oa.application_id AS application_id, " + //
					"  oa.applicant_id AS applicant_id, " + //
					"  oa.status AS status, " + //
					"  oa.register_status AS register_status, " + //
					"  oa.collation_text AS collation_text, " + //
					"  oa.register_datetime AS register_datetime, " + //
					"  oa.update_datetime AS update_datetime " + //
					"FROM " + // O_申請
					"  o_application AS oa " + //
					"LEFT OUTER JOIN " + // O_申請区分
					"  o_application_category AS oac " + //
					"ON " + //
					"  oa.application_id = oac.application_id " + //
					"LEFT OUTER JOIN " + // O_申請者情報
					"  o_applicant_information AS oai " + //
					"ON " + //
					"  oa.application_id = oai.application_id " + //
					"LEFT OUTER JOIN " + // O_回答
					"  o_answer AS ans " + //
					"ON " + //
					"  oa.application_id = ans.application_id " + //
					"LEFT OUTER JOIN " + // M_区分判定
					"  m_category_judgement AS mcj " + //
					"ON " + //
					"  ans.judgement_id = mcj.judgement_item_id " + //
					where + // where句
					"ORDER BY " + //
					"  oa.application_id ASC ";

			Query query = em.createNativeQuery(sql, Application.class);
			// 申請者情報パラメータ設定
			for (Map.Entry<Integer, String> entry : applicantConditionParam.entrySet()) {
				query = query.setParameter("applicantId_" + entry.getKey(), entry.getValue());
			}
			// 申請区分パラメータ設定
			for (Map.Entry<String, List<String>> entry : categoryConditionParam.entrySet()) {
				query = query.setParameter("viewId_" + entry.getKey(), entry.getKey());
				query = query.setParameter("categoryId_" + entry.getKey(), entry.getValue());
			}
			// ステータスパラメータ設定
			if (statusParam != null) {
				query = query.setParameter("status", statusParam);
			}
			// 部署パラメータ設定
			if (departmentParam != null) {
				query = query.setParameter("departmentId", departmentParam);
			}
			return query.getResultList();
		} finally {
			LOGGER.debug("申請情報検索 終了");
			if (em != null) {
				em.close();
			}
		}
	}

	/**
	 * O_申請区分検索
	 * 
	 * @param applicationId 申請ID
	 * @return O_申請区分リスト
	 */
	@SuppressWarnings("unchecked")
	public List<ApplicationCategory> getApplicationCategoryList(int applicationId) {
		LOGGER.debug("O_申請区分検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT DISTINCT " + //
					"  application_id, " + //
					"  view_id, " + //
					"  category_id " + //
					"FROM " + //
					"  o_application_category " + //
					"WHERE " + //
					"  application_id = :applicationId " + //
					"ORDER BY " + //
					"  application_id ASC ";

			Query query = em.createNativeQuery(sql, ApplicationCategory.class);
			query = query.setParameter("applicationId", applicationId);
			return query.getResultList();
		} finally {
			LOGGER.debug("O_申請区分検索 終了");
			if (em != null) {
				em.close();
			}
		}
	}

	/**
	 * M_申請区分検索
	 * 
	 * @param applicationId 申請ID
	 * @return M_申請区分リスト
	 */
	@SuppressWarnings("unchecked")
	public List<ApplicationCategoryMaster> getApplicationCategoryMasterList(int applicationId) {
		LOGGER.debug("M_申請区分検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT DISTINCT " + //
					"  b.category_id AS category_id, " + //
					"  b.view_id AS view_id, " + //
					"  b.order AS \"order\", " + //
					"  b.label_name AS label_name " + //
					"FROM " + //
					"  o_application_category AS a " + //
					"INNER JOIN " + //
					"  m_application_category AS b " + //
					"ON " + //
					"  a.view_id = b.view_id " + //
					"  AND a.category_id = b.category_id " + //
					"WHERE " + //
					"  application_id = :applicationId " + //
					"ORDER BY " + //
					"  b.order ASC ";

			Query query = em.createNativeQuery(sql, ApplicationCategoryMaster.class);
			query = query.setParameter("applicationId", applicationId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("M_申請区分検索 終了");
		}
	}

	/**
	 * M_申請区分検索
	 * 
	 * @param applicationId 申請ID
	 * @param viewId        画面ID
	 * @return M_申請区分リスト
	 */
	@SuppressWarnings("unchecked")
	public List<ApplicationCategoryMaster> getApplicationCategoryMasterList(int applicationId, String viewId) {
		LOGGER.debug("M_申請区分検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT DISTINCT " + //
					"  b.category_id AS category_id, " + //
					"  b.view_id AS view_id, " + //
					"  b.order AS \"order\", " + //
					"  b.label_name AS label_name " + //
					"FROM " + //
					"  o_application_category AS a " + //
					"INNER JOIN " + //
					"  m_application_category AS b " + //
					"ON " + //
					"  a.view_id = b.view_id " + //
					"  AND a.category_id = b.category_id " + //
					"WHERE " + //
					"  a.application_id = :applicationId " + //
					"  AND a.view_id = :viewId " + //
					"ORDER BY " + //
					"  b.order ASC ";

			Query query = em.createNativeQuery(sql, ApplicationCategoryMaster.class);
			query = query.setParameter("applicationId", applicationId);
			query = query.setParameter("viewId", viewId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("M_申請区分検索 終了");
		}
	}

	/**
	 * O_申請者情報検索
	 * 
	 * @param applicationId 申請ID
	 * @return O_申請者情報リスト
	 */
	@SuppressWarnings("unchecked")
	public List<ApplicantInformation> getApplicantInformationList(int applicationId) {
		LOGGER.debug("O_申請者情報検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT DISTINCT " + //
					"  application_id, " + //
					"  applicant_id, " + //
					"  item_1, " + //
					"  item_2, " + //
					"  item_3, " + //
					"  item_4, " + //
					"  item_5, " + //
					"  item_6, " + //
					"  item_7, " + //
					"  item_8, " + //
					"  item_9, " + //
					"  item_10, " + //
					"  mail_address, " + //
					"  collation_id, " + //
					"  password " + //
					"FROM " + //
					"  o_applicant_information " + //
					"WHERE " + //
					"  application_id = :applicationId " + //
					"ORDER BY " + //
					"  applicant_id ASC";

			Query query = em.createNativeQuery(sql, ApplicantInformation.class);
			query = query.setParameter("applicationId", applicationId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("O_申請者情報検索 終了");
		}
	}

	/**
	 * O_回答検索
	 * 
	 * @param applicationId 申請ID
	 * @param isGoverment   行政かどうか
	 * @return O_回答リスト
	 */
	@SuppressWarnings("unchecked")
	public List<Answer> getAnswerList(int applicationId, boolean isGoverment) {
		LOGGER.debug("O_回答検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT DISTINCT " + //
					"  answer_id, " + //
					"  application_id, " + //
					"  judgement_id, " + //
					"  judgement_result, " + //
					"  answer_content, " + //
					"  notified_text, " + //
					"  register_datetime, " + //
					"  update_datetime, " + //
					"  complete_flag, " + //
					"  notified_flag " + //
					"FROM " + //
					"  o_answer " + //
					"WHERE " + //
					"  application_id = :applicationId ";
			if (!isGoverment) {
				// 事業者の場合は通知フラグがtrueのデータのみ取得
				sql += "  AND notified_flag = '1' ";
			}
			sql += "ORDER BY " + //
					"  answer_id ASC";

			Query query = em.createNativeQuery(sql, Answer.class);
			query = query.setParameter("applicationId", applicationId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("O_回答検索 終了");
		}
	}

	/**
	 * M_区分判定検索
	 * 
	 * @param applicationId 申請ID
	 * @return M_区分判定リスト
	 */
	@SuppressWarnings("unchecked")
	public List<CategoryJudgement> getCategoryJudgementList(int applicationId) {
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
					"  a.application_id = :applicationId " + //
					"ORDER BY " + //
					"  b.judgement_item_id ASC";
			Query query = em.createNativeQuery(sql, CategoryJudgement.class);
			query = query.setParameter("applicationId", applicationId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("M_区分判定検索 終了");
		}
	}

	/**
	 * M_部署検索
	 * 
	 * @param applicationId 申請ID
	 * @return M_部署リスト
	 */
	@SuppressWarnings("unchecked")
	public List<Department> getDepartmentList(int applicationId) {
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
					"  a.application_id = :applicationId " + //
					"ORDER BY " + //
					"  department_id ASC";
			Query query = em.createNativeQuery(sql, Department.class);
			query = query.setParameter("applicationId", applicationId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("M_部署検索 終了");
		}
	}

	/**
	 * M_申請区分選択画面検索
	 * 
	 * @param applicationId 申請ID
	 * @return M_申請区分選択画面リスト
	 */
	@SuppressWarnings("unchecked")
	public List<ApplicationCategorySelectionView> getApplicationCategorySelectionViewList(int applicationId) {
		LOGGER.debug("M_申請区分選択画面検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT DISTINCT " + //
					"  c.view_id AS view_id, " + //
					"  c.view_flag AS view_flag, " + //
					"  c.multiple_flag AS multiple_flag, " + //
					"  c.title AS title, " + //
					"  c.require_flag AS require_flag, " + //
					"  c.description AS description " + //
					"FROM " + //
					"  o_application_category AS a " + //
					"INNER JOIN " + //
					"  m_application_category AS b " + //
					"ON " + //
					"  a.category_id=b.category_id " + //
					"INNER JOIN " + //
					"  m_application_category_selection_view AS c " + //
					"ON " + //
					"  b.view_id = c.view_id " + //
					"WHERE " + //
					"  a.application_id = :applicationId " + //
					"ORDER BY " + //
					"  c.view_id ASC";
			Query query = em.createNativeQuery(sql, ApplicationCategorySelectionView.class);
			query = query.setParameter("applicationId", applicationId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("M_申請区分選択画面検索 終了");
		}
	}

	/**
	 * 申請地番一覧検索
	 * 
	 * @param applicationId 申請ID
	 * @return 申請地番一覧
	 */
	@SuppressWarnings("unchecked")
	public List<LotNumberAndDistrict> getLotNumberList(int applicationId, int epsg) {
		LOGGER.debug("申請地番一覧検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT DISTINCT " + //
					"  a.lot_number_id AS chiban_id, " + //
					"  b.district_id AS district_id, " + //
					"  b.result_column1 AS result_column1, " + //
					"  b.result_column2 AS result_column2, " + //
					"  b.result_column3 AS result_column3, " + //
					"  b.result_column4 AS result_column4, " + //
					"  b.result_column5 AS result_column5, " + //
					"  ST_X(ST_Centroid(ST_Envelope(ST_Transform(b.geom, " + epsg + ")))) AS lon, " + //
					"  ST_Y(ST_Centroid(ST_Envelope(ST_Transform(b.geom, " + epsg + ")))) AS lat, " + //
					"  ST_XMin(ST_Transform(b.geom, " + epsg + ")) AS minlon, " + //
					"  ST_YMin(ST_Transform(b.geom, " + epsg + ")) AS minlat, " + //
					"  ST_XMax(ST_Transform(b.geom, " + epsg + ")) AS maxlon, " + //
					"  ST_YMax(ST_Transform(b.geom, " + epsg + ")) AS maxlat, " + //
					"  b.chiban AS chiban, " + //
					"  c.district_name AS ooaza_district_name, " + //
					"  c.district_kana AS ooaza_district_kana, " + //
					"  c.result_column1 AS ooaza_result_column1, " + //
					"  c.result_column2 AS ooaza_result_column2, " + //
					"  c.result_column3 AS ooaza_result_column3, " + //
					"  c.result_column4 AS ooaza_result_column4, " + //
					"  c.result_column5 AS ooaza_result_column5, " + //
					"  o.status AS status, " + //
					"  a.application_id AS application_id " + //
					"FROM " + //
					"  o_application_lot_number AS a " + //
					"LEFT OUTER JOIN " + // TODO LEFT OUTER JOIN ? INNER JOIN ?
					"  o_application AS o " + //
					"ON " + //
					"  a.application_id = o.application_id " + //
					"LEFT OUTER JOIN " + // TODO LEFT OUTER JOIN ? INNER JOIN ?
					"  f_lot_number AS b " + //
					"ON " + //
					"  a.lot_number_id = b.chiban_id " + //
					"LEFT OUTER JOIN " + // TODO LEFT OUTER JOIN ? INNER JOIN ?
					"  f_district AS c " + //
					"ON " + //
					"  b.district_id = c.district_id " + //
					"WHERE " + //
					"  a.application_id = :applicationId " + //
					"ORDER BY " + //
					"  c.district_kana ASC, " + //
					"  a.lot_number_id ASC";
			Query query = em.createNativeQuery(sql, LotNumberAndDistrict.class);
			query = query.setParameter("applicationId", applicationId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("申請地番一覧検索 終了");
		}
	}

	/**
	 * M_申請ファイル一覧検索
	 * 
	 * @param applicationId 申請ID
	 * @return M_申請ファイル一覧
	 */
	@SuppressWarnings("unchecked")
	public List<ApplicationFileMaster> getApplicationFileMasterList(int applicationId) {
		LOGGER.debug("M_申請ファイル一覧検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT DISTINCT " + //
					"  b.application_file_id, " + //
					"  '' as judgement_item_id , " + //
					"  b.require_flag, " + //
					"  b.upload_file_name " + //
					"FROM " + //
					"  o_answer AS a " + //
					"INNER JOIN " + //
					"  m_application_file AS b " + //
					"ON " + //
					"  a.judgement_id = b.judgement_item_id " + //
					"WHERE " + //
					"  a.application_id = :applicationId " + //
					"  GROUP BY b.application_file_id, b.require_flag, b.upload_file_name " + //
					"ORDER BY " + //
					"  b.application_file_id ASC";
			Query query = em.createNativeQuery(sql, ApplicationFileMaster.class);
			query = query.setParameter("applicationId", applicationId);
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("M_申請ファイル一覧検索 終了");
		}
	}
}
