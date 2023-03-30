package developmentpermission.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import developmentpermission.dao.AnswerDao;
import developmentpermission.dao.ApplicationDao;
import developmentpermission.entity.Answer;
import developmentpermission.entity.AnswerFile;
import developmentpermission.entity.ApplicantInformation;
import developmentpermission.entity.ApplicantInformationItem;
import developmentpermission.entity.Application;
import developmentpermission.entity.ApplicationCategory;
import developmentpermission.entity.ApplicationCategoryMaster;
import developmentpermission.entity.ApplicationCategorySelectionView;
import developmentpermission.entity.ApplicationFile;
import developmentpermission.entity.ApplicationFileMaster;
import developmentpermission.entity.ApplicationSearchResult;
import developmentpermission.entity.CategoryJudgement;
import developmentpermission.entity.Department;
import developmentpermission.entity.LotNumber;
import developmentpermission.entity.LotNumberAndDistrict;
import developmentpermission.entity.LotNumberSearchResultDefinition;
import developmentpermission.form.AnswerConfirmLoginForm;
import developmentpermission.form.AnswerFileForm;
import developmentpermission.form.AnswerForm;
import developmentpermission.form.AnswerJudgementForm;
import developmentpermission.form.ApplicantInformationItemForm;
import developmentpermission.form.ApplicationCategoryForm;
import developmentpermission.form.ApplicationCategorySelectionViewForm;
import developmentpermission.form.ApplicationFileForm;
import developmentpermission.form.ApplicationInformationSearchResultHeaderForm;
import developmentpermission.form.ApplicationRegisterForm;
import developmentpermission.form.ApplicationRegisterResultForm;
import developmentpermission.form.ApplicationSearchConditionForm;
import developmentpermission.form.ApplicationSearchResultForm;
import developmentpermission.form.ApplyAnswerForm;
import developmentpermission.form.GeneralConditionDiagnosisReportRequestForm;
import developmentpermission.form.GeneralConditionDiagnosisResultForm;
import developmentpermission.form.LotNumberForm;
import developmentpermission.form.UploadApplicationFileForm;
import developmentpermission.repository.AnswerFileRepository;
import developmentpermission.repository.ApplicantInformationItemRepository;
import developmentpermission.repository.ApplicantInformationRepository;
import developmentpermission.repository.ApplicationCategoryMasterRepository;
import developmentpermission.repository.ApplicationFileMasterRepository;
import developmentpermission.repository.ApplicationFileRepository;
import developmentpermission.repository.ApplicationRepository;
import developmentpermission.repository.ApplicationSearchResultRepository;
import developmentpermission.repository.LotNumberRepository;
import developmentpermission.repository.LotNumberSearchResultDefinitionRepository;
import developmentpermission.repository.jdbc.AnswerJdbc;
import developmentpermission.repository.jdbc.ApplicantJdbc;
import developmentpermission.repository.jdbc.ApplicationCategoryJdbc;
import developmentpermission.repository.jdbc.ApplicationFileJdbc;
import developmentpermission.repository.jdbc.ApplicationJdbc;
import developmentpermission.repository.jdbc.ApplicationLotNumberJdbc;
import developmentpermission.util.AuthUtil;
import developmentpermission.util.ExportJudgeForm;
import developmentpermission.util.MailMessageUtil;
import developmentpermission.util.model.MailItem;
import developmentpermission.util.model.MailResultItem;

/**
 * 申請Serviceクラス
 */
@Service
@Transactional
public class ApplicationService extends AbstractJudgementService {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationService.class);

	/** 照合IDの文字数 */
	@Value("${app.applicant.id.length:20}")
	protected int applicantIdLength;
	/** パスワード発行時の文字数 */
	@Value("${app.password.length:10}")
	protected int passwordLength;
	/** パスワードに使用する文字種 */
	@Value("${app.password.character}")
	protected String passwordCharacter;

	/** 参照タイプ: 申請区分 */
	@Value("${app.application.result.type.category:0}")
	protected String typeCategory;
	/** 参照タイプ: 申請者情報 */
	@Value("${app.application.result.type.applicant:1}")
	protected String typeApplicant;
	/** 参照タイプ: その他 */
	@Value("${app.application.result.type.other:2}")
	protected String typeOther;

	/** テーブル名: O_申請区分 */
	@Value("${app.application.table.application.category:o_application_category}")
	protected String tableNameApplicationCategory;
	/** テーブル名: M_申請区分 */
	@Value("${app.application.table.application.category.master:m_application_category}")
	protected String tableNameApplicationCategoryMaster;
	/** テーブル名: O_申請者情報 */
	@Value("${app.application.table.applicant.information:o_applicant_information}")
	protected String tableNameApplicantInformation;
	/** テーブル名: O_回答 */
	@Value("${app.application.table.answer:o_answer}")
	protected String tableNameAnswer;
	/** テーブル名: M_区分設定 */
	@Value("${app.application.table.category.judgement:m_category_judgement}")
	protected String tableNameCategoryJudgement;
	/** テーブル名: M_部署 */
	@Value("${app.application.table.department:m_department}")
	protected String tableNameDepartment;
	/** テーブル名： O_申請 */
	@Value("${app.application.table.application}")
	protected String tableNameApplication;
	/** カラム名: O_申請区分 申請ID */
	@Value("${app.application.category.column.applicationid}")
	protected String columnNameApplicationId;
	/** カラム名: O_申請区分 画面ID */
	@Value("${app.application.category.column.viewid}")
	protected String columnNameApplicationViewId;
	/** カラム名: O_申請区分 申請区分ID */
	@Value("${app.application.category.column.categoryid}")
	protected String columnNameApplicationCategoryId;

	/** カラム名: M_申請区分 申請区分ID */
	@Value("${app.application.category.master.column.categoryid}")
	protected String columnNameApplicationCategoryMasterId;
	/** カラム名: M_申請区分 画面ID */
	@Value("${app.application.category.master.column.viewid}")
	protected String columnNameApplicationCategoryMasterViewId;
	/** カラム名: M_申請区分 昇順 */
	@Value("${app.application.category.master.column.order}")
	protected String columnNameApplicationCategoryMasterOrder;
	/** カラム名: M_申請区分 選択肢名 */
	@Value("${app.application.category.master.column.labelname}")
	protected String columnNameApplicationCategoryMasterLabelName;

	/** カラム名: O_申請者情報 申請ID */
	@Value("${app.applicant.information.column.applicationid}")
	protected String columnNameApplicantInformationApplicationId;
	/** カラム名: O_申請者情報 申請者情報ID */
	@Value("${app.applicant.information.column.applicantid}")
	protected String columnNameApplicantInformationApplicantId;
	/** カラム名: O_申請者情報 項目1 */
	@Value("${app.applicant.information.column.item1}")
	protected String columnNameApplicantInformationItem1;
	/** カラム名: O_申請者情報 項目2 */
	@Value("${app.applicant.information.column.item2}")
	protected String columnNameApplicantInformationItem2;
	/** カラム名: O_申請者情報 項目3 */
	@Value("${app.applicant.information.column.item3}")
	protected String columnNameApplicantInformationItem3;
	/** カラム名: O_申請者情報 項目4 */
	@Value("${app.applicant.information.column.item4}")
	protected String columnNameApplicantInformationItem4;
	/** カラム名: O_申請者情報 項目5 */
	@Value("${app.applicant.information.column.item5}")
	protected String columnNameApplicantInformationItem5;
	/** カラム名: O_申請者情報 項目6 */
	@Value("${app.applicant.information.column.item6}")
	protected String columnNameApplicantInformationItem6;
	/** カラム名: O_申請者情報 項目7 */
	@Value("${app.applicant.information.column.item7}")
	protected String columnNameApplicantInformationItem7;
	/** カラム名: O_申請者情報 項目8 */
	@Value("${app.applicant.information.column.item8}")
	protected String columnNameApplicantInformationItem8;
	/** カラム名: O_申請者情報 項目9 */
	@Value("${app.applicant.information.column.item9}")
	protected String columnNameApplicantInformationItem9;
	/** カラム名: O_申請者情報 項目10 */
	@Value("${app.applicant.information.column.item10}")
	protected String columnNameApplicantInformationItem10;
	/** カラム名: O_申請者情報 メールアドレス */
	@Value("${app.applicant.information.column.mailaddress}")
	protected String columnNameApplicantInformationMailAddress;
	/** カラム名: O_申請者情報 照合ID */
	@Value("${app.applicant.information.column.collationid}")
	protected String columnNameApplicantInformationCollationId;
	/** カラム名: O_申請者情報 パスワード */
	@Value("${app.applicant.information.column.password}")
	protected String columnNameApplicantInformationPassword;

	/** カラム名: O_回答 回答ID */
	@Value("${app.answer.column.answerid}")
	protected String columnNameAnswerId;
	/** カラム名: O_回答 申請ID */
	@Value("${app.answer.column.applicationid}")
	protected String columnNameAnswerApplicationId;
	/** カラム名: O_回答 判定項目ID */
	@Value("${app.answer.column.judgementid}")
	protected String columnNameAnswerJudgementId;
	/** カラム名: O_回答 判定結果 */
	@Value("${app.answer.column.judgementresult}")
	protected String columnNameAnswerJudgementResult;
	/** カラム名: O_回答 回答内容 */
	@Value("${app.answer.column.answercontent}")
	protected String columnNameAnswerContent;
	/** カラム名: O_回答 回答内容 */
	@Value("${app.answer.column.notifiedtext}")
	protected String columnNameAnswerNotifiedText;
	/** カラム名: O_回答 登録日時 */
	@Value("${app.answer.column.registerdatetime}")
	protected String columnNameAnswerRegisterDatetime;
	/** カラム名: O_回答 更新日時 */
	@Value("${app.answer.column.updatedatetime}")
	protected String columnNameAnswerUpdateDatetime;
	/** カラム名: O_回答 完了フラグ */
	@Value("${app.answer.column.completeflag}")
	protected String columnNameAnswerCompleteFlag;
	/** カラム名: O_回答 通知フラグ */
	@Value("${app.answer.column.notifiedflag}")
	protected String columnNameAnswerNotifiedFlag;

	/** カラム名: M_区分判定 判定項目ID */
	@Value("${app.category.judgement.column.judgementitemid}")
	protected String columnNameCategoryJudgementId;
	/** カラム名: M_区分判定 担当部署ID */
	@Value("${app.category.judgement.column.departmentid}")
	protected String columnNameCategoryJudgementDepartmentId;
	/** カラム名: M_区分判定 区分1 */
	@Value("${app.category.judgement.column.category1}")
	protected String columnNameCategoryJudgementCategory1;
	/** カラム名: M_区分判定 区分2 */
	@Value("${app.category.judgement.column.category2}")
	protected String columnNameCategoryJudgementCategory2;
	/** カラム名: M_区分判定 区分3 */
	@Value("${app.category.judgement.column.category3}")
	protected String columnNameCategoryJudgementCategory3;
	/** カラム名: M_区分判定 区分4 */
	@Value("${app.category.judgement.column.category4}")
	protected String columnNameCategoryJudgementCategory4;
	/** カラム名: M_区分判定 区分5 */
	@Value("${app.category.judgement.column.category5}")
	protected String columnNameCategoryJudgementCategory5;
	/** カラム名: M_区分判定 区分6 */
	@Value("${app.category.judgement.column.category6}")
	protected String columnNameCategoryJudgementCategory6;
	/** カラム名: M_区分判定 区分7 */
	@Value("${app.category.judgement.column.category7}")
	protected String columnNameCategoryJudgementCategory7;
	/** カラム名: M_区分判定 区分8 */
	@Value("${app.category.judgement.column.category8}")
	protected String columnNameCategoryJudgementCategory8;
	/** カラム名: M_区分判定 区分9 */
	@Value("${app.category.judgement.column.category9}")
	protected String columnNameCategoryJudgementCategory9;
	/** カラム名: M_区分判定 区分10 */
	@Value("${app.category.judgement.column.category10}")
	protected String columnNameCategoryJudgementCategory10;
	/** カラム名: M_区分判定 GIS判定 */
	@Value("${app.category.judgement.column.gisjudgement}")
	protected String columnNameCategoryJudgementGisJudgement;
	/** カラム名: M_区分判定 バッファ */
	@Value("${app.category.judgement.column.buffer}")
	protected String columnNameCategoryJudgementBuffer;
	/** カラム名: M_区分判定 判定対象レイヤ */
	@Value("${app.category.judgement.column.judgementlayer}")
	protected String columnNameCategoryJudgementLayer;
	/** カラム名: M_区分判定 タイトル */
	@Value("${app.category.judgement.column.title}")
	protected String columnNameCategoryJudgementTitle;
	/** カラム名: M_区分判定 該当表示概要 */
	@Value("${app.category.judgement.column.applicablesummary}")
	protected String columnNameCategoryJudgementApplicableSummary;
	/** カラム名: M_区分判定 該当表示文言 */
	@Value("${app.category.judgement.column.applicabledescription}")
	protected String columnNameCategoryJudgementApplicableDescription;
	/** カラム名: M_区分判定 非該当表示有無 */
	@Value("${app.category.judgement.column.nonapplicabledisplayflag}")
	protected String columnNameCategoryJudgementNonApplicableDisplayFlag;
	/** カラム名: M_区分判定 非該当表示概要 */
	@Value("${app.category.judgement.column.nonapplicablesummary}")
	protected String columnNameCategoryJudgementNonApplicableSummary;
	/** カラム名: M_区分判定 非該当表示文言 */
	@Value("${app.category.judgement.column.nonapplicabledescription}")
	protected String columnNameCategoryJudgementNonApplicableDescription;
	/** カラム名: M_区分判定 テーブル名 */
	@Value("${app.category.judgement.column.tablename}")
	protected String columnNameCategoryJudgementTableName;
	/** カラム名: M_区分判定 フィールド名 */
	@Value("${app.category.judgement.column.fieldname}")
	protected String columnNameCategoryJudgementFieldName;
	/** カラム名: M_区分判定 判定レイヤ非該当時表示有無 */
	@Value("${app.category.judgement.column.nonapplicablelayerdisplayflag}")
	protected String columnNameCategoryJudgementNonApplicableLayerDisplayFlag;
	/** カラム名: M_区分判定 同時表示レイヤ */
	@Value("${app.category.judgement.column.simultaneousdisplaylayer}")
	protected String columnNameCategoryJudgementSimultaneousDisplayLayer;
	/** カラム名: M_区分判定 同時表示レイヤ表示有無 */
	@Value("${app.category.judgement.column.simultaneousdisplaylayerflag}")
	protected String columnNameCategoryJudgementSimultaneousDisplayLayerFlag;

	/** カラム名: M_部署 部署ID */
	@Value("${app.department.column.departmentid}")
	protected String columnNameDepartmentId;
	/** カラム名: M_部署 部署名 */
	@Value("${app.department.column.departmentname}")
	protected String columnNameDepartmentName;
	/** カラム名: M_部署 回答権限フラグ */
	@Value("${app.department.column.answerauthorityflag}")
	protected String columnNameDepartmentAnswerAuthorityFlag;
	/** カラム名: M_部署 メールアドレス */
	@Value("${app.department.column.mailaddress}")
	protected String columnNameDepartmentMailAddress;

	/** カラム名：O_申請 ステータス */
	@Value("${app.application.column.status}")
	protected String columnNameStatus;
	/** 申請登録時の概況診断レポート接頭句 */
	@Value("${app.application.report.filename.header}")
	protected String applicationReportFileNameHeader;
	/** 申請登録時の概況診断レポート接尾句(日付フォーマット) */
	@Value("${app.application.report.filename.footer}")
	protected String applicationReportFileNameFooter;
	/** 申請登録時の概況診断レポートのファイルID */
	@Value("${app.application.report.fileid}")
	protected String applicationReportFileId;

	/** M_申請者情報項目Repositoryインスタンス */
	@Autowired
	private ApplicantInformationItemRepository applicantInformationItemRepository;
	/** M_申請ファイルRepositoryインスタンス */
	@Autowired
	private ApplicationFileMasterRepository applicationFileMasterRepository;
	/** O_申請ファイルRepositoryインスタンス */
	@Autowired
	private ApplicationFileRepository applicationFileRepository;
	/** M_申請情報検索結果Repositoryインスタンス */
	@Autowired
	private ApplicationSearchResultRepository applicationSearchResultRepository;
	/** O_申請Repositoryインスタンス */
	@Autowired
	private ApplicationRepository applicationRepository;
	/** O_回答ファイルRepositoryインスタンス */
	@Autowired
	private AnswerFileRepository answerFileRepository;
	/** M_地番検索結果定義Repositoryインスタンス */
	@Autowired
	private LotNumberSearchResultDefinitionRepository lotNumberSearchResultDefinitionRepository;
	/** F_地番Repositoryインスタンス */
	@Autowired
	private LotNumberRepository lotNumberRepository;
	/** M_申請区分Repositoryインスタンス */
	@Autowired
	private ApplicationCategoryMasterRepository applicationCategoryMasterRepository;
	/** O_申請者情報Repositoryインスタンス */
	@Autowired
	private ApplicantInformationRepository applicantInformationRepository;

	/** O_申請JDBCインスタンス */
	@Autowired
	private ApplicationJdbc applicationJdbc;
	/** O_申請者情報JDBCインスタンス */
	@Autowired
	private ApplicantJdbc applicantJdbc;
	/** 申請区分JDBCインスタンス */
	@Autowired
	private ApplicationCategoryJdbc applicationCategoryJdbc;
	/** 申請地番JDBCインスタンス */
	@Autowired
	private ApplicationLotNumberJdbc applicationLotNumberJdbc;
	/** 回答情報JDBCインスタンス */
	@Autowired
	private AnswerJdbc answerJdbc;
	/** 申請ファイルJDBCインスタンス */
	@Autowired
	private ApplicationFileJdbc applicationFileJdbc;

	/**
	 * 申請者情報入力項目一覧取得
	 * 
	 * @return 申請者情報入力項目一覧
	 */
	public List<ApplicantInformationItemForm> getApplicantItems() {
		LOGGER.debug("申請者情報入力項目一覧取得 開始");
		try {
			List<ApplicantInformationItem> itemList = applicantInformationItemRepository.getApplicantItems();

			List<ApplicantInformationItemForm> formList = new ArrayList<ApplicantInformationItemForm>();

			for (ApplicantInformationItem item : itemList) {
				formList.add(getApplicantInformationItemFormFromEntity(item, EMPTY));
			}
			return formList;
		} finally {
			LOGGER.debug("申請者情報入力項目一覧取得 終了");
		}
	}

	/**
	 * 申請ファイル一覧取得
	 * 
	 * @param generalConditionDiagnosisResultFormList 検索条件
	 * @return 申請ファイル一覧
	 */
	public List<ApplicationFileForm> getApplicationFiles(
			List<GeneralConditionDiagnosisResultForm> generalConditionDiagnosisResultFormList) {
		LOGGER.debug("申請ファイル一覧取得 開始");
		try {
			List<ApplicationFileForm> formList = new ArrayList<ApplicationFileForm>();

			List<String> judgementItemIdList = new ArrayList<String>();
			for (GeneralConditionDiagnosisResultForm generalConditionDiagnosisResultForm : generalConditionDiagnosisResultFormList) {
				String judgementId = generalConditionDiagnosisResultForm.getJudgementId();
				if (!judgementItemIdList.contains(judgementId)) {
					LOGGER.trace("検索対象判定項目ID追加: " + judgementId);
					judgementItemIdList.add(judgementId);
				}
			}

			if (judgementItemIdList.size() > 0) {
				LOGGER.trace("申請ファイル一覧取得 開始");
				List<ApplicationFileMaster> applicationFileList = applicationFileMasterRepository
						.getApplicationFiles(judgementItemIdList);
				for (ApplicationFileMaster applicationFile : applicationFileList) {
					formList.add(getApplicationFileFormFromEntity(applicationFile));
				}
				LOGGER.trace("申請ファイル一覧取得 終了");
			}
			return formList;
		} finally {
			LOGGER.debug("申請ファイル一覧取得 終了");
		}
	}

	/**
	 * 申請情報検索結果表示項目一覧取得
	 * 
	 * @return 申請情報検索結果表示項目一覧
	 */
	public List<ApplicationInformationSearchResultHeaderForm> getApplicationInformationSearchResultHeader() {
		LOGGER.debug("申請情報検索結果表示項目一覧取得 開始");
		try {
			List<ApplicationInformationSearchResultHeaderForm> formList = new ArrayList<ApplicationInformationSearchResultHeaderForm>();

			List<ApplicationSearchResult> applicationSearchResultList = applicationSearchResultRepository
					.getApplicationSearchResultList();
			for (ApplicationSearchResult applicationSearchResult : applicationSearchResultList) {
				formList.add(getApplicationInformationSearchResultHeaderFormFromEntity(applicationSearchResult));
			}
			return formList;
		} finally {
			LOGGER.debug("申請情報検索結果表示項目一覧取得 終了");
		}
	}

	/**
	 * 申請情報検索
	 * 
	 * @param applicationSearchConditionForm 検索条件
	 * @return 申請情報検索結果
	 */
	public List<ApplicationSearchResultForm> searchApplicationInformation(
			ApplicationSearchConditionForm applicationSearchConditionForm) {
		LOGGER.debug("申請情報検索 開始");
		try {
			List<ApplicationSearchResultForm> formList = new ArrayList<ApplicationSearchResultForm>();
			ApplicationDao dao = new ApplicationDao(emf);

			// O_申請検索
			LOGGER.trace("O_申請検索 開始");
			List<Application> applicationList = dao.searchApplication(applicationSearchConditionForm);
			LOGGER.trace("O_申請検索 終了");

			// M_申請情報検索結果情報取得
			LOGGER.trace("M_申請情報検索結果情報取得 開始");
			List<ApplicationSearchResult> applicationSearchResultList = applicationSearchResultRepository
					.getApplicationSearchResultList();
			LOGGER.trace("M_申請情報検索結果情報取得 終了");

			// 申請情報ごとに各データを収集して格納
			for (Application application : applicationList) {
				formList.add(getApplicationSearchResultFormFromEntity(dao, application, applicationSearchResultList));
			}
			return formList;
		} finally {
			LOGGER.debug("申請情報検索 終了");
		}
	}

	/**
	 * 申請情報詳細取得
	 * 
	 * @param applicationId 申請ID
	 * @param departmentId  部署ID
	 * @param isGoverment   行政かどうか
	 * @return 申請情報詳細
	 */
	public ApplyAnswerForm getApplicationDetail(Integer applicationId, String departmentId, boolean isGoverment) {
		LOGGER.debug("申請情報詳細取得 開始");
		LOGGER.trace("申請ID: " + applicationId);
		LOGGER.trace("部署ID: " + departmentId);
		LOGGER.trace("行政かどうか: " + isGoverment);
		try {
			ApplyAnswerForm form = new ApplyAnswerForm();

			// 回答権限
			boolean notificable = false;
			// 部署検索
			if (isGoverment) {
				LOGGER.trace("部署検索 開始");
				List<Department> departmentList = departmentRepository.getDepartmentListById(departmentId);
				if (departmentList.size() > 0) {
					Department department = departmentList.get(0);
					notificable = department.getAnswerAuthorityFlag();
				}
				LOGGER.trace("部署検索 終了");
			}
			form.setNotificable(notificable);

			Application application = null;
			// O_申請検索
			LOGGER.trace("O_申請検索 開始");
			List<Application> applicationList = applicationRepository.getApplicationList(applicationId);
			if (applicationList.size() == 0) {
				LOGGER.warn("申請情報の件数が0");
				return null;
			} else {
				application = applicationList.get(0);
				form.setApplicationId(applicationId);
				form.setStatus(application.getStatus());
			}
			LOGGER.trace("O_申請検索 終了");

			ApplicationDao dao = new ApplicationDao(emf);

			// 申請区分選択一覧
			LOGGER.trace("申請区分選択一覧取得 開始");
			List<ApplicationCategorySelectionViewForm> viewFormList = new ArrayList<ApplicationCategorySelectionViewForm>();
			List<ApplicationCategorySelectionView> applicationCategorySelectionViewList = dao
					.getApplicationCategorySelectionViewList(applicationId);
			for (ApplicationCategorySelectionView applicationCategorySelectionView : applicationCategorySelectionViewList) {
				ApplicationCategorySelectionViewForm viewForm = getSelectionViewFormFromEntity(
						applicationCategorySelectionView, applicationId);
				viewFormList.add(viewForm);
			}
			form.setApplicationCategories(viewFormList);
			LOGGER.trace("申請区分選択一覧取得 終了");

			// 申請者情報一覧
			LOGGER.trace("申請者情報一覧取得 開始");
			List<ApplicantInformationItemForm> applicantFormList = new ArrayList<ApplicantInformationItemForm>();
			List<ApplicantInformation> applicantList = dao.getApplicantInformationList(applicationId);
			if (applicantList.size() > 0) {
				// 申請者は申請者IDに対して1件
				ApplicantInformation applicant = applicantList.get(0);

				// 申請者情報項目一覧
				List<ApplicantInformationItem> applicantInformationItemList = applicantInformationItemRepository
						.getApplicantItems();
				for (ApplicantInformationItem applicantInformationItem : applicantInformationItemList) {
					String value = null;

					switch (applicantInformationItem.getApplicantInformationItemId()) {
					case ApplicationDao.ITEM_1_ID:
						value = applicant.getItem1();
						break;
					case ApplicationDao.ITEM_2_ID:
						value = applicant.getItem2();
						break;
					case ApplicationDao.ITEM_3_ID:
						value = applicant.getItem3();
						break;
					case ApplicationDao.ITEM_4_ID:
						value = applicant.getItem4();
						break;
					case ApplicationDao.ITEM_5_ID:
						value = applicant.getItem5();
						break;
					case ApplicationDao.ITEM_6_ID:
						value = applicant.getItem6();
						break;
					case ApplicationDao.ITEM_7_ID:
						value = applicant.getItem7();
						break;
					case ApplicationDao.ITEM_8_ID:
						value = applicant.getItem8();
						break;
					case ApplicationDao.ITEM_9_ID:
						value = applicant.getItem9();
						break;
					case ApplicationDao.ITEM_10_ID:
						value = applicant.getItem10();
						break;
					default:
						LOGGER.warn("未知の項目ID: " + applicantInformationItem.getApplicantInformationItemId());
						break;
					}
					applicantFormList.add(getApplicantInformationItemFormFromEntity(applicantInformationItem, value));
				}
			}
			form.setApplicantInformations(applicantFormList);
			LOGGER.trace("申請者情報一覧取得 終了");

			// 回答一覧
			LOGGER.trace("回答一覧取得 開始");
			List<AnswerForm> answerFormList = new ArrayList<AnswerForm>();
			List<Answer> answerList = dao.getAnswerList(applicationId, isGoverment);
			for (Answer answer : answerList) {
				answerFormList.add(getAnswerFormFromEntity(answer, departmentId, isGoverment));
			}
			form.setAnswers(answerFormList);
			LOGGER.trace("回答一覧取得 終了");

			// 申請地番一覧
			LOGGER.trace("申請地番一覧取得 開始");
			List<LotNumberForm> lotNumberFormList = new ArrayList<LotNumberForm>();
			List<LotNumberSearchResultDefinition> lotNumberSearchResultDefinitionList = lotNumberSearchResultDefinitionRepository
					.getLotNumberSearchResultDefinitionList();
			List<LotNumberAndDistrict> lotNumberList = dao.getLotNumberList(applicationId, lonlatEpsg);
			for (LotNumberAndDistrict lotNumber : lotNumberList) {
				lotNumberFormList.add(getLotNumberFormFromEntity(lotNumber, lotNumberSearchResultDefinitionList));
			}
			form.setLotNumbers(lotNumberFormList);
			LOGGER.trace("申請地番一覧取得 終了");

			// 申請ファイル一覧
			LOGGER.trace("申請ファイル一覧取得 開始");
			List<ApplicationFileForm> applicationFileMasterFormList = new ArrayList<ApplicationFileForm>();
			List<ApplicationFileMaster> applicationFileMasterList = dao.getApplicationFileMasterList(applicationId);
			for (ApplicationFileMaster applicationFileMaster : applicationFileMasterList) {
				ApplicationFileForm fileForm = getApplicationFileFormFromEntity(applicationFileMaster);

				// アップロードファイル一式
				List<UploadApplicationFileForm> applicationFileFormList = new ArrayList<UploadApplicationFileForm>();
				List<ApplicationFile> applicationFileList = applicationFileRepository
						.getApplicationFiles(applicationFileMaster.getApplicationFileId(), applicationId);
				for (ApplicationFile applicationFile : applicationFileList) {
					applicationFileFormList.add(getUploadApplicationFileFormFromEntity(applicationFile));
				}
				fileForm.setUploadFileFormList(applicationFileFormList);

				applicationFileMasterFormList.add(fileForm);
			}
			form.setApplicationFiles(applicationFileMasterFormList);
			LOGGER.trace("申請ファイル一覧取得 終了");

			return form;
		} finally {
			LOGGER.debug("申請情報詳細取得 終了");
		}
	}

	/**
	 * 申請登録パラメータ検査
	 * 
	 * @param applicationRegisterForm パラメータ
	 * @return 判定結果
	 */
	public boolean validateRegisterApplicationParam(ApplicationRegisterForm applicationRegisterForm) {
		LOGGER.debug("申請登録パラメータ検査 開始");
		try {
			if (applicationRegisterForm.getFolderName() == null || "".equals(applicationRegisterForm.getFolderName())) {
				LOGGER.warn("一時フォルダパスが設定されていない: " + applicationRegisterForm.getFolderName());
				return false;
			}

			// 申請者情報
			List<ApplicantInformationItemForm> applicantInfomationList = applicationRegisterForm
					.getApplicantInformationItemForm();
			int mailAddressCount = 0;
			for (ApplicantInformationItemForm applicantInfomation : applicantInfomationList) {
				if (applicantInfomation.getMailAddress()) {
					mailAddressCount++;
				}
			}
			if (mailAddressCount != 1) {
				// メールアドレスは必ず1件のみ
				LOGGER.warn("メールアドレスの件数が不正");
				return false;
			}

			// O_申請区分
			LOGGER.trace("O_申請区分検査 開始");
			List<ApplicationCategorySelectionViewForm> categoryList = applicationRegisterForm
					.getApplicationCategories();
			for (ApplicationCategorySelectionViewForm category : categoryList) {
				List<ApplicationCategoryForm> applicationCategoryList = category.getApplicationCategory();
				for (ApplicationCategoryForm applicationCategory : applicationCategoryList) {
					List<ApplicationCategoryMaster> applicationCategoryMasterList = applicationCategoryMasterRepository
							.findByCategoryId(applicationCategory.getId(), applicationCategory.getScreenId());
					if (applicationCategoryMasterList.size() == 0) {
						// 申請区分ID、画面IDに適応した情報がない
						LOGGER.warn("申請区分ID、画面IDに適応した情報がない");
						return false;
					}
				}
			}
			LOGGER.trace("O_申請区分検査 終了");

			// O_申請地番
			LOGGER.trace("O_申請地番検査 開始");
			List<LotNumberForm> lotNumbers = applicationRegisterForm.getLotNumbers();
			for (LotNumberForm lotNumber : lotNumbers) {
				List<LotNumber> lotNumberList = lotNumberRepository.findByChibanId(lotNumber.getChibanId());
				if (lotNumberList.size() == 0) {
					// 地番IDに適応した地番情報が無い
					LOGGER.warn("地番IDに適応した地番情報が無い");
					return false;
				}
			}
			LOGGER.trace("O_申請地番検査 終了");

			// 区分判定ID
			LOGGER.trace("区分判定検査 開始");
			List<GeneralConditionDiagnosisResultForm> conditionList = applicationRegisterForm
					.getGeneralConditionDiagnosisResultForm();
			for (GeneralConditionDiagnosisResultForm condition : conditionList) {
				List<CategoryJudgement> categoryJudgementList = categoryJudgementRepository
						.getCategoryJudgementListById(condition.getJudgementId());
				if (categoryJudgementList.size() == 0) {
					// 区分判定IDに適応した区分判定情報が無い
					LOGGER.warn("区分判定IDに適応した区分判定情報が無い");
					return false;
				}
			}
			LOGGER.trace("区分判定検査 終了");

			return true;
		} finally {
			LOGGER.debug("申請登録パラメータ検査 終了");
		}
	}

	/**
	 * 申請登録
	 * 
	 * @param applicationRegisterForm パラメータ
	 * @throws Exception
	 */
	public int registerApplication(ApplicationRegisterForm applicationRegisterForm) throws RuntimeException {
		LOGGER.debug("申請登録 開始");
		try {
			// O_申請
			LOGGER.trace("O_申請登録 開始");
			// 申請ID
			int applicationId = applicationJdbc.insert();
			LOGGER.trace("O_申請登録 終了 新規申請ID: " + applicationId);

			// O_申請者情報
			LOGGER.trace("O_申請者情報登録 開始");
			ApplicantInformation applicantInfo = buildApplicantInformation(
					applicationRegisterForm.getApplicantInformationItemForm());
			applicantInfo.setApplicationId(applicationId);

			// 申請者情報ID
			int applicantId = applicantJdbc.insert(applicantInfo);
			LOGGER.trace("O_申請者情報登録 終了 新規申請者情報ID: " + applicantId);

			// O_申請の申請者ID更新処理
			LOGGER.trace("O_申請の申請者ID更新 開始");
			if (applicationJdbc.updateApplicantId(applicationId, applicantId) != 1) {
				LOGGER.warn("O_申請の更新件数不正");
				throw new RuntimeException("申請情報への申請者情報ID設定に失敗");
			}
			LOGGER.trace("O_申請の申請者ID更新 終了");

			// O_申請区分登録
			LOGGER.trace("O_申請区分登録 開始");
			List<ApplicationCategorySelectionViewForm> categoryList = applicationRegisterForm
					.getApplicationCategories();
			for (ApplicationCategorySelectionViewForm category : categoryList) {
				List<ApplicationCategoryForm> applicationCategoryList = category.getApplicationCategory();
				for (ApplicationCategoryForm applicationCategory : applicationCategoryList) {
					applicationCategoryJdbc.insert(applicationCategory, applicationId);
				}
			}
			LOGGER.trace("O_申請区分登録 終了");

			// O_申請ファイル
			// 申請ファイル登録は別APIで実施

			// O_申請地番登録
			LOGGER.trace("O_申請地番登録 開始");
			List<LotNumberForm> lotNumbers = applicationRegisterForm.getLotNumbers();
			for (LotNumberForm lotNumber : lotNumbers) {
				applicationLotNumberJdbc.insert(lotNumber, applicationId);
			}
			LOGGER.trace("O_申請地番登録 終了");

			// O_回答登録
			LOGGER.trace("O_回答登録 開始");
			List<GeneralConditionDiagnosisResultForm> conditionList = applicationRegisterForm
					.getGeneralConditionDiagnosisResultForm();
			for (GeneralConditionDiagnosisResultForm condition : conditionList) {
				if (condition.getAnswerRequireFlag()) {
					// 回答必須：初期回答なし
					answerJdbc.insert(applicationId, condition.getJudgementId(), condition.getSummary(), null, null,
							'0', '0');
				} else {
					// 回答任意：初期回答あり
					answerJdbc.insert(applicationId, condition.getJudgementId(), condition.getSummary(),
							condition.getDefaultAnswer(), condition.getDefaultAnswer(), '1', '1');
				}
			}
			LOGGER.trace("O_回答登録 終了");

			// 概況診断レポート生成
			LOGGER.trace("概況診断レポート生成 開始");
			GeneralConditionDiagnosisReportRequestForm reportForm = new GeneralConditionDiagnosisReportRequestForm();
			reportForm.setFolderName(applicationRegisterForm.getFolderName());
			reportForm.setLotNumbers(applicationRegisterForm.getLotNumbers());
			reportForm.setApplicationCategories(applicationRegisterForm.getApplicationCategories());
			reportForm.setGeneralConditionDiagnosisResults(
					applicationRegisterForm.getGeneralConditionDiagnosisResultForm());

			Workbook wb = null;
			try {
				wb = exportJudgeReportWorkBook(reportForm);
				if (wb == null) {
					throw new RuntimeException("概況診断レポート生成に失敗");
				}
			} catch (Exception ex) {
				LOGGER.error("概況診断レポート生成でエラー発生", ex);
				throw new RuntimeException(ex);
			}
			LOGGER.trace("概況診断レポート生成 終了");

			// 概況診断レポートをアップロードし、O_申請ファイルに登録する
			LOGGER.trace("概況診断レポートアップロード 開始");
			// ファイル名は「概況診断結果_<申請ID>_yyyy_mm_dd.xlsx」
			SimpleDateFormat sdf = new SimpleDateFormat(applicationReportFileNameFooter);
			String fileName = applicationReportFileNameHeader + applicationId + sdf.format(new Date()) + ".xlsx";
			UploadApplicationFileForm uploadForm = new UploadApplicationFileForm();
			uploadForm.setApplicationId(applicationId);
			uploadForm.setApplicationFileId(applicationReportFileId);
			uploadForm.setUploadFileName(fileName);

			uploadApplicationFile(uploadForm, wb);
			LOGGER.trace("概況診断レポートアップロード 終了");

			LOGGER.trace(" 一時フォルダーの削除処理 開始");
			// 一時フォルダーの削除処理
			deleteTmpFolder(reportForm);
			LOGGER.trace(" 一時フォルダーの削除処理 終了");

			return applicationId;
		} finally {
			LOGGER.debug("申請登録 終了");
		}
	}

	/**
	 * 申請ファイルアップロードパラメータチェック
	 * 
	 * @param uploadApplicationFileForm パラメータ
	 * @return 判定結果
	 */
	public boolean validateUploadApplicationFile(UploadApplicationFileForm uploadApplicationFileForm) {
		LOGGER.debug("申請ファイルアップロードパラメータチェック 開始");
		try {
			Integer applicationId = uploadApplicationFileForm.getApplicationId();
			String applicationFileId = uploadApplicationFileForm.getApplicationFileId();
			String fileName = uploadApplicationFileForm.getUploadFileName();
			MultipartFile uploadFile = uploadApplicationFileForm.getUploadFile();
			if (applicationId == null //
					|| applicationFileId == null || EMPTY.equals(applicationFileId) //
					|| fileName == null || EMPTY.equals(fileName) //
					|| uploadFile == null //
			) {
				// パラメータが空
				LOGGER.warn("パラメータにnullまたは空が含まれる");
				return false;
			}

			// 申請ID
			if (applicationRepository.getApplicationList(applicationId).size() != 1) {
				// 申請IDが不正
				LOGGER.warn("申請IDで得られる申請データ件数が不正");
				return false;
			}

			// 申請ファイルID
			if (applicationFileMasterRepository.getApplicationFile(applicationFileId).size() == 0) {
				// 申請ファイルIDが不正
				LOGGER.warn("申請ファイルIDで得られる申請ファイルデータ件数が不正");
				return false;
			}
			return true;
		} finally {
			LOGGER.debug("申請ファイルアップロードパラメータチェック 終了");
		}
	}

	/**
	 * 申請ファイルアップロード
	 * 
	 * @param form パラメータ
	 */
	public void uploadApplicationFile(UploadApplicationFileForm form, Workbook wb) {
		LOGGER.debug("申請ファイルアップロード 開始");
		try {
			// ファイルパスは「/application/<申請ID>/<申請ファイルID>/<ファイルID>/<アップロードファイル名>」

			// O_申請ファイル登録
			LOGGER.trace("O_申請ファイル登録 開始");
			Integer fileId = applicationFileJdbc.insert(form);
			form.setFileId(fileId);
			LOGGER.trace("O_申請ファイル登録 終了");

			// 相対フォルダパス
			String folderPath = applicationFolderName;
			folderPath += PATH_SPLITTER + form.getApplicationId();
			folderPath += PATH_SPLITTER + form.getApplicationFileId();
			folderPath += PATH_SPLITTER + form.getFileId();

			// 絶対フォルダパス
			String absoluteFolderPath = fileRootPath + folderPath;
			Path directoryPath = Paths.get(absoluteFolderPath);
			if (!Files.exists(directoryPath)) {
				// フォルダがないので生成
				LOGGER.debug("フォルダ生成: " + directoryPath);
				Files.createDirectories(directoryPath);
			}

			// 相対ファイルパス
			String filePath = folderPath + PATH_SPLITTER + form.getUploadFileName();
			// 絶対ファイルパス
			String absoluteFilePath = absoluteFolderPath + PATH_SPLITTER + form.getUploadFileName();

			// ファイルパスはrootを除いた相対パスを設定
			form.setFilePath(filePath);

			// ファイルパス更新
			LOGGER.trace("ファイルパス更新 開始");
			if (applicationFileJdbc.updateFilePath(fileId, filePath) != 1) {
				LOGGER.warn("ファイルパス更新件数が不正");
				throw new RuntimeException("ファイルパス更新件数が不正");
			}
			LOGGER.trace("ファイルパス更新 終了");

			// ファイル出力
			if (form.getUploadFile() != null) {
				LOGGER.trace("ファイル出力 開始");
				exportFile(form.getUploadFile(), absoluteFilePath);
				LOGGER.trace("ファイル出力 終了");
			} else if (wb != null) {
				LOGGER.trace("EXCELファイル出力 開始");
				exportWorkBook(wb, absoluteFilePath);
				LOGGER.trace("EXCELファイル出力 終了");
			} else {
				throw new RuntimeException("ファイル情報が設定されていない");
			}
		} catch (Exception ex) {
			// RuntimeExceptionで投げないとロールバックされない
			LOGGER.error("申請ファイルアップロードで例外発生", ex);
			throw new RuntimeException(ex);
		} finally {
			LOGGER.debug("申請ファイルアップロード 終了");
		}
	}

	/**
	 * ファイルダウンロードパラメータチェック
	 * 
	 * @param form パラメータ
	 * @return 判定結果
	 */
	public boolean validateDownloadApplicationFile(UploadApplicationFileForm form) {
		LOGGER.debug("申請ファイルダウンロードパラメータチェック 開始");
		try {
			Integer fileId = form.getFileId();
			Integer applicationId = form.getApplicationId();
			String applicationFileId = form.getApplicationFileId();
			if (fileId == null //
					|| applicationId == null//
					|| applicationFileId == null || EMPTY.equals(applicationFileId)) {
				LOGGER.warn("パラメータにnullまたは空が含まれる");
				return false;
			}

			// ファイルID
			if (applicationFileRepository.getApplicationFile(fileId).size() != 1) {
				// ファイルIDが不正
				LOGGER.warn("ファイルIDで取得される申請ファイルデータ件数が不正");
				return false;
			}

			// 申請ID
			if (applicationRepository.getApplicationList(applicationId).size() != 1) {
				// 申請IDが不正
				LOGGER.warn("申請IDで取得される申請データ件数が不正");
				return false;
			}

			// 申請ファイルID
			if (applicationFileMasterRepository.getApplicationFile(applicationFileId).size() == 0) {
				// 申請ファイルIDが不正
				LOGGER.warn("申請ファイルIDで取得される申請ファイルデータがない");
				return false;
			}

			return true;
		} finally {
			LOGGER.debug("申請ファイルダウンロードパラメータチェック 終了");
		}
	}

	/**
	 * 申請ファイルダウンロード
	 * 
	 * @param form パラメータ
	 * @return 応答Entity
	 */
	public ResponseEntity<Resource> downloadApplicationFile(UploadApplicationFileForm form) {
		LOGGER.debug("申請ファイルダウンロード 開始");
		try {
			LOGGER.trace("申請ファイルデータ取得 開始");
			List<ApplicationFile> applicationFileList = applicationFileRepository.getApplicationFile(form.getFileId());
			ApplicationFile applicationFile = applicationFileList.get(0);
			LOGGER.trace("申請ファイルデータ取得 終了");

			// 絶対ファイルパス
			String absoluteFilePath = fileRootPath + applicationFile.getFilePath();
			Path filePath = Paths.get(absoluteFilePath);
			if (!Files.exists(filePath)) {
				// ファイルが存在しない
				LOGGER.warn("ファイルが存在しない");
				return new ResponseEntity<Resource>(HttpStatus.NOT_FOUND);
			}

			Resource resource = new PathResource(filePath);
			return ResponseEntity.ok().contentType(getContentType(filePath))
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
					.body(resource);
		} catch (Exception ex) {
			LOGGER.error("申請ファイルダウンロードで例外発生", ex);
			return new ResponseEntity<Resource>(HttpStatus.SERVICE_UNAVAILABLE);
		} finally {
			LOGGER.debug("申請ファイルダウンロード 終了");
		}
	}

	/**
	 * 照合ID、パスワードから申請者情報を取得し、申請IDを取得する
	 * 
	 * @param form パラメータ
	 * @return 申請ID
	 */
	public Integer getApplicationIdFromApplicantInfo(AnswerConfirmLoginForm form) {
		LOGGER.debug("申請者情報の申請ID取得 開始");
		try {
			String id = form.getLoginId();
			String password = form.getPassword();
			if (id != null && !EMPTY.equals(id) //
					&& password != null && !EMPTY.equals(password)) {
				String hash = AuthUtil.createHash(password);
				List<ApplicantInformation> applicantList = applicantInformationRepository.getApplicantList(id, hash);
				if (applicantList.size() == 1) {
					ApplicantInformation applicant = applicantList.get(0);
					return applicant.getApplicationId();
				} else {
					LOGGER.warn("申請者情報の取得件数が不正");
				}
			} else {
				LOGGER.warn("パラメータにnullまたは空が含まれる");
			}
			return null;
		} finally {
			LOGGER.debug("申請者情報の申請ID取得 終了");
		}
	}

	/**
	 * 照合情報取得
	 * 
	 * @param applicationRegisterResultForm O_申請登録結果フォーム
	 * @return 照合情報
	 */
	public AnswerConfirmLoginForm notifyCollationInformation(
			ApplicationRegisterResultForm applicationRegisterResultForm) {
		// O_申請の登録ステータスをチェック
		List<Application> applicationList = applicationRepository
				.getApplicationList(applicationRegisterResultForm.getApplicationId());
		if (applicationList.size() != 1) {
			LOGGER.error("申請データの件数が不正");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}

		Application application = applicationList.get(0);
		String registerStatus = application.getRegisterStatus();
		if (STATE_APPLIED.equals(registerStatus)) {
			// 申請済なので不正
			LOGGER.error("対象データは登録ステータスが申請済");
			throw new ResponseStatusException(HttpStatus.CONFLICT);
		}

		List<ApplicantInformation> applicantList = applicantInformationRepository
				.getApplicantList(application.getApplicationId());
		if (applicantList.size() != 1) {
			LOGGER.error("申請者データの件数が不正");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}

		ApplicantInformation applicant = applicantList.get(0);
		String id = applicant.getCollationId();
		String password = applicant.getPassword();
		if ((id != null && !EMPTY.equals(id)) //
				|| (password != null && !EMPTY.equals(password))) {
			LOGGER.error("申請者データはIDまたはパスワードが設定済");
			throw new ResponseStatusException(HttpStatus.CONFLICT);
		}

		AnswerConfirmLoginForm form = new AnswerConfirmLoginForm();

		// ID、パスワードを設定
		// IDは申請者情報IDを使用する
		id = String.format("%0" + applicantIdLength + "d", applicant.getApplicantId());
		password = AuthUtil.generatePassword(passwordCharacter, passwordLength);
		form.setLoginId(id);
		form.setPassword(password);

		if (applicantJdbc.updateId(applicant.getApplicantId(), id, AuthUtil.createHash(password)) != 1) {
			LOGGER.error("申請者情報の更新件数が不正");
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}

		// 申請情報の登録ステータスを更新
		if (applicationJdbc.updateRegisterStatus(application.getApplicationId(), STATE_APPLIED) != 1) {
			LOGGER.error("申請情報の更新件数が不正");
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}

		// 事業者に回答確認ID/パスワード通知送付
		sendNoticeMailToBusinessUser(form.getLoginId(), form.getPassword(), applicant.getMailAddress());

		// 行政に申請受付通知送付
		sendNoticeMailToGovernmentUser(application, applicant);

		return form;
	}

	/**
	 * 事業者に回答確認ID/パスワード通知送付
	 * 
	 * @param loginId  ログインID
	 * @param password パスワード
	 */
	private void sendNoticeMailToBusinessUser(String loginId, String password, String mailAddress) {
		MailItem businessItem = new MailItem();
		businessItem.setId(loginId); // 回答確認ID
		businessItem.setPassword(password); // パスワード

		String subject = getMailPropValue(MailMessageUtil.KEY_BUSSINESS_ACCEPT_SUBJECT, businessItem);
		String body = getMailPropValue(MailMessageUtil.KEY_BUSSINESS_ACCEPT_BODY, businessItem);

		// 事業者に回答確認ID/パスワード通知送付
		LOGGER.trace(mailAddress);
		LOGGER.trace(subject);
		LOGGER.trace(body);

		try {
			mailSendutil.sendMail(mailAddress, subject, body);
		} catch (Exception e) {
			LOGGER.error("メール送信時にエラー発生", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 行政に受付通知送付
	 * 
	 * @param application 申請情報
	 * @param applicant   申請者情報
	 */
	private void sendNoticeMailToGovernmentUser(Application application, ApplicantInformation applicant) {
		MailItem baseItem = new MailItem();
		switch (applicantNameItemNumber) { // 氏名はプロパティで設定されたアイテムを設定
		case 1:
			baseItem.setName(applicant.getItem1());
			break;
		case 2:
			baseItem.setName(applicant.getItem2());
			break;
		case 3:
			baseItem.setName(applicant.getItem3());
			break;
		case 4:
			baseItem.setName(applicant.getItem4());
			break;
		case 5:
			baseItem.setName(applicant.getItem5());
			break;
		case 6:
			baseItem.setName(applicant.getItem6());
			break;
		case 7:
			baseItem.setName(applicant.getItem7());
			break;
		case 8:
			baseItem.setName(applicant.getItem8());
			break;
		case 9:
			baseItem.setName(applicant.getItem9());
			break;
		case 10:
			baseItem.setName(applicant.getItem10());
			break;
		default:
			LOGGER.error("氏名アイテム番号指定が不正: " + applicantNameItemNumber);
			throw new RuntimeException("氏名アイテム番号指定が不正");
		}
		baseItem.setMailAddress(applicant.getMailAddress()); // メールアドレス

		// 地番
		ApplicationDao applicationDao = new ApplicationDao(emf);
		List<LotNumberForm> lotNumberFormList = new ArrayList<LotNumberForm>();
		List<LotNumberSearchResultDefinition> lotNumberSearchResultDefinitionList = lotNumberSearchResultDefinitionRepository
				.getLotNumberSearchResultDefinitionList();
		List<LotNumberAndDistrict> lotNumberList = applicationDao.getLotNumberList(application.getApplicationId(),
				lonlatEpsg);
		for (LotNumberAndDistrict lotNumber : lotNumberList) {
			lotNumberFormList.add(getLotNumberFormFromEntity(lotNumber, lotNumberSearchResultDefinitionList));
		}
		ExportJudgeForm exportForm = new ExportJudgeForm();
		String addressText = exportForm.getAddressText(lotNumberFormList, lotNumberSeparators, separator);
		baseItem.setLotNumber(addressText);

		// 対象・判定結果
		AnswerDao answerDao = new AnswerDao(emf);
		List<Answer> answerList = applicationDao.getAnswerList(application.getApplicantId(), true);

		// 部署別の対象・判定結果リスト
		Map<String, List<MailResultItem>> resultMap = new HashMap<String, List<MailResultItem>>();

		for (Answer answer : answerList) {
			Integer answerId = answer.getAnswerId();
			List<CategoryJudgement> categoryList = answerDao.getCategoryJudgementList(answerId);
			if (categoryList.size() == 0) {
				LOGGER.warn("M_区分判定の値が取得できない 回答ID: " + answerId);
				continue;
			}
			CategoryJudgement category = categoryList.get(0);

			MailResultItem item = new MailResultItem();
			item.setTarget(category.getTitle()); // 対象
			item.setResult(answer.getJudgementResult()); // 判定結果

			// 回答に紐づく部署
			List<Department> answerDepartmentList = answerDao.getDepartmentList(answerId);
			for (Department answerDepartment : answerDepartmentList) {
				String answerDepartmentId = answerDepartment.getDepartmentId();
				if (!resultMap.containsKey(answerDepartmentId)) {
					// 初期化
					resultMap.put(answerDepartmentId, new ArrayList<MailResultItem>());
				}
				// 部署ごとに集約
				resultMap.get(answerDepartmentId).add(item);
			}
		}

		// 申請に紐づく全部署
		List<Department> departmentList = applicationDao.getDepartmentList(application.getApplicationId());

		// 行政に申請受付通知送付
		for (Department department : departmentList) {
			if (!resultMap.containsKey(department.getDepartmentId())) {
				// 対象・判定結果がないのでスキップ
				continue;
			}
			MailItem governmentItem = baseItem.clone();
			// 部署ごとに対象・判定結果は異なるのでここで設定
			governmentItem.setResultList(resultMap.get(department.getDepartmentId()));

			String subject = getMailPropValue(MailMessageUtil.KEY_ACCEPT_SUBJECT, governmentItem);
			String body = getMailPropValue(MailMessageUtil.KEY_ACCEPT_BODY, governmentItem);

			LOGGER.trace(department.getMailAddress());
			LOGGER.trace(subject);
			LOGGER.trace(body);

			try {
				final String[] mailAddressList = department.getMailAddress().split(",");
				for (String aMailAddress : mailAddressList) {
					mailSendutil.sendMail(aMailAddress, subject, body);
				}
			} catch (Exception e) {
				LOGGER.error("メール送信時にエラー発生", e);
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * O_申請Entityから申請情報検索結果データフォームを生成
	 * 
	 * @param dao                                 O_申請DAO
	 * @param application                         O_申請Entity
	 * @param applicationSearchResultList         M_申請情報検索結果情報
	 * @param lotNumberSearchResultDefinitionList 地番検索結果定義一覧
	 * @return 申請情報検索結果データフォーム
	 */
	private ApplicationSearchResultForm getApplicationSearchResultFormFromEntity(ApplicationDao dao,
			Application application, List<ApplicationSearchResult> applicationSearchResultList) {
		LOGGER.debug("申請情報検索結果データフォームを生成 開始");
		try {
			ApplicationSearchResultForm form = new ApplicationSearchResultForm();

			// 申請ID
			int applicationId = application.getApplicationId();
			form.setApplicationId(applicationId);

			// 地番
			List<LotNumberForm> lotNumberFormList = new ArrayList<LotNumberForm>();
			List<LotNumberAndDistrict> lotNumberList = dao.getLotNumberList(applicationId, lonlatEpsg);
			for (LotNumberAndDistrict lotNumber : lotNumberList) {
				lotNumberFormList.add(getLotNumberFormFromEntity(lotNumber, null));
			}
			form.setLotNumbers(lotNumberFormList);

			Map<String, Object> attributes = new LinkedHashMap<String, Object>();

			// O_申請区分
			LOGGER.trace("O_申請区分取得 開始");
			List<ApplicationCategory> categoryList = dao.getApplicationCategoryList(applicationId);
			LOGGER.trace("O_申請区分取得 終了");

			// M_申請区分
			LOGGER.trace("M_申請区分取得 開始");
			List<ApplicationCategoryMaster> categoryMasterList = dao.getApplicationCategoryMasterList(applicationId);
			LOGGER.trace("M_申請区分取得 終了");

			// O_申請者情報
			LOGGER.trace("O_申請者情報取得 開始");
			List<ApplicantInformation> applicantList = dao.getApplicantInformationList(applicationId);
			LOGGER.trace("O_申請者情報取得 終了");

			// O_回答
			LOGGER.trace("O_回答取得 開始");
			List<Answer> answerList = dao.getAnswerList(applicationId, true);
			LOGGER.trace("O_回答取得 終了");

			// M_区分判定
			LOGGER.trace("M_区分判定取得 開始");
			List<CategoryJudgement> judgementList = dao.getCategoryJudgementList(applicationId);
			LOGGER.trace("M_区分判定取得 終了");

			// M_部署
			LOGGER.trace("M_部署取得 開始");
			List<Department> departmentList = dao.getDepartmentList(applicationId);
			LOGGER.trace("M_部署取得 終了");

			// attributeの設定
			for (ApplicationSearchResult applicationSearchResult : applicationSearchResultList) {
				String responseKey = applicationSearchResult.getResponseKey();
				String refType = applicationSearchResult.getReferenceType();
				String tableName = applicationSearchResult.getTableName();
				String columnName = applicationSearchResult.getTableColumnName();

				List<Object> valueList = new ArrayList<Object>();
				// TODO 取得する必要のないカラムがある場合はコメントアウトを・・・
				if (typeCategory.equals(refType)) {
					// 申請区分
					if (tableNameApplicationCategory.equals(tableName)) {
						// O_申請区分
						for (ApplicationCategory category : categoryList) {
							if (columnNameApplicationId.equals(columnName)) {
								// 申請ID
								valueList.add(category.getApplicationId());
							} else if (columnNameApplicationViewId.equals(columnName)) {
								// 画面ID
								valueList.add(category.getViewId());
							} else if (columnNameApplicationCategoryId.equals(columnName)) {
								// 申請区分ID
								valueList.add(category.getCategoryId());
							} else {
								LOGGER.warn("非対応のカラム名: " + columnName);
							}
						}
					} else if (tableNameApplicationCategoryMaster.equals(tableName)) {
						// M_申請区分
						for (ApplicationCategoryMaster categoryMaster : categoryMasterList) {
							// if (columnNameApplicationCategoryMasterId.equals(columnName)) {
							// 申請区分ID
							// valueList.add(categoryMaster.getCategoryId());
							// } else if (columnNameApplicationCategoryMasterViewId.equals(columnName)) {
							// 画面ID
							// valueList.add(categoryMaster.getCategoryId());
							// } else if (columnNameApplicationCategoryMasterOrder.equals(columnName)) {
							// 昇順
							// valueList.add(categoryMaster.getOrder());
							// } else if (columnNameApplicationCategoryMasterLabelName.equals(columnName)) {
							// 選択肢名
							// valueList.add(categoryMaster.getLabelName());
							// } else {
							// LOGGER.warn("非対応のカラム名: " + columnName);
							// }
							// 指定したViewIdと等しいレコードを追加する
							if (categoryMaster.getViewId().equals(columnName)) {
								valueList.add(categoryMaster.getLabelName());
							}
						}
					}
				} else if (typeApplicant.equals(refType)) {
					// 申請者情報
					if (tableNameApplicantInformation.equals(tableName)) {
						// O_申請者情報
						for (ApplicantInformation applicant : applicantList) {
							if (columnNameApplicantInformationApplicationId.equals(columnName)) {
								// 申請ID
								valueList.add(applicant.getApplicationId());
							} else if (columnNameApplicantInformationApplicantId.equals(columnName)) {
								// 申請者情報ID
								valueList.add(applicant.getApplicantId());
							} else if (columnNameApplicantInformationItem1.equals(columnName)) {
								// 項目1
								valueList.add(applicant.getItem1());
							} else if (columnNameApplicantInformationItem2.equals(columnName)) {
								// 項目2
								valueList.add(applicant.getItem2());
							} else if (columnNameApplicantInformationItem3.equals(columnName)) {
								// 項目3
								valueList.add(applicant.getItem3());
							} else if (columnNameApplicantInformationItem4.equals(columnName)) {
								// 項目4
								valueList.add(applicant.getItem4());
							} else if (columnNameApplicantInformationItem5.equals(columnName)) {
								// 項目5
								valueList.add(applicant.getItem5());
							} else if (columnNameApplicantInformationItem6.equals(columnName)) {
								// 項目6
								valueList.add(applicant.getItem6());
							} else if (columnNameApplicantInformationItem7.equals(columnName)) {
								// 項目7
								valueList.add(applicant.getItem7());
							} else if (columnNameApplicantInformationItem8.equals(columnName)) {
								// 項目8
								valueList.add(applicant.getItem8());
							} else if (columnNameApplicantInformationItem9.equals(columnName)) {
								// 項目9
								valueList.add(applicant.getItem9());
							} else if (columnNameApplicantInformationItem10.equals(columnName)) {
								// 項目10
								valueList.add(applicant.getItem10());
							} else if (columnNameApplicantInformationMailAddress.equals(columnName)) {
								// メールアドレス
								valueList.add(applicant.getMailAddress());
							} else if (columnNameApplicantInformationCollationId.equals(columnName)) {
								// 照合ID
								valueList.add(applicant.getCollationId());
							} else if (columnNameApplicantInformationPassword.equals(columnName)) {
								// パスワード
								valueList.add(applicant.getPassword());
							} else {
								LOGGER.warn("非対応のカラム名: " + columnName);
							}
						}
					}
				} else if (typeOther.equals(refType)) {
					// その他
					if (tableNameAnswer.equals(tableName)) {
						// O_回答
						for (Answer answer : answerList) {
							if (columnNameAnswerId.equals(columnName)) {
								// 回答ID
								valueList.add(answer.getAnswerId());
							} else if (columnNameAnswerApplicationId.equals(columnName)) {
								// 申請ID
								valueList.add(answer.getApplicationId());
							} else if (columnNameAnswerJudgementId.equals(columnName)) {
								// 判定項目ID
								valueList.add(answer.getJudgementId());
							} else if (columnNameAnswerJudgementResult.equals(columnName)) {
								// 判定結果
								valueList.add(answer.getJudgementResult());
							} else if (columnNameAnswerContent.equals(columnName)) {
								// 回答内容
								valueList.add(answer.getAnswerContent());
							} else if (columnNameAnswerNotifiedText.equals(columnName)) {
								// 通知テキスト
								valueList.add(answer.getNotifiedText());
							} else if (columnNameAnswerRegisterDatetime.equals(columnName)) {
								// 登録日時
								valueList.add(answer.getRegisterDatetime());
							} else if (columnNameAnswerUpdateDatetime.equals(columnName)) {
								// 更新日時
								valueList.add(answer.getUpdateDatetime());
							} else if (columnNameAnswerCompleteFlag.equals(columnName)) {
								// 完了フラグ
								valueList.add(answer.getCompleteFlag());
							} else if (columnNameAnswerNotifiedFlag.equals(columnName)) {
								// 通知フラグ
								valueList.add(answer.getNotifiedFlag());
							} else {
								LOGGER.warn("非対応のカラム名: " + columnName);
							}
						}
					} else if (tableNameCategoryJudgement.equals(tableName)) {
						// M_区分設定
						for (CategoryJudgement judgement : judgementList) {
							if (columnNameCategoryJudgementId.equals(columnName)) {
								// 判定項目ID
								valueList.add(judgement.getJudgementItemId());
							} else if (columnNameCategoryJudgementDepartmentId.equals(columnName)) {
								// 担当部署ID
								valueList.add(judgement.getDepartmentId());
							} else if (columnNameCategoryJudgementCategory1.equals(columnName)) {
								// 区分1
								valueList.add(judgement.getCategory1());
							} else if (columnNameCategoryJudgementCategory2.equals(columnName)) {
								// 区分2
								valueList.add(judgement.getCategory2());
							} else if (columnNameCategoryJudgementCategory3.equals(columnName)) {
								// 区分3
								valueList.add(judgement.getCategory3());
							} else if (columnNameCategoryJudgementCategory4.equals(columnName)) {
								// 区分4
								valueList.add(judgement.getCategory4());
							} else if (columnNameCategoryJudgementCategory5.equals(columnName)) {
								// 区分5
								valueList.add(judgement.getCategory5());
							} else if (columnNameCategoryJudgementCategory6.equals(columnName)) {
								// 区分6
								valueList.add(judgement.getCategory6());
							} else if (columnNameCategoryJudgementCategory7.equals(columnName)) {
								// 区分7
								valueList.add(judgement.getCategory7());
							} else if (columnNameCategoryJudgementCategory8.equals(columnName)) {
								// 区分8
								valueList.add(judgement.getCategory8());
							} else if (columnNameCategoryJudgementCategory9.equals(columnName)) {
								// 区分9
								valueList.add(judgement.getCategory9());
							} else if (columnNameCategoryJudgementCategory10.equals(columnName)) {
								// 区分10
								valueList.add(judgement.getCategory10());
							} else if (columnNameCategoryJudgementGisJudgement.equals(columnName)) {
								// GIS判定
								valueList.add(judgement.getGisJudgement());
							} else if (columnNameCategoryJudgementBuffer.equals(columnName)) {
								// バッファ
								valueList.add(judgement.getBuffer());
							} else if (columnNameCategoryJudgementLayer.equals(columnName)) {
								// 判定対象レイヤ
								valueList.add(judgement.getJudgementLayer());
							} else if (columnNameCategoryJudgementTitle.equals(columnName)) {
								// タイトル
								valueList.add(judgement.getTitle());
							} else if (columnNameCategoryJudgementApplicableSummary.equals(columnName)) {
								// 該当表示概要
								valueList.add(judgement.getApplicableSummary());
							} else if (columnNameCategoryJudgementApplicableDescription.equals(columnName)) {
								// 該当表示文言
								valueList.add(judgement.getApplicableDescription());
							} else if (columnNameCategoryJudgementNonApplicableDisplayFlag.equals(columnName)) {
								// 非該当表示有無
								valueList.add(judgement.getNonApplicableDisplayFlag());
							} else if (columnNameCategoryJudgementNonApplicableSummary.equals(columnName)) {
								// 非該当表示概要
								valueList.add(judgement.getNonApplicableSummary());
							} else if (columnNameCategoryJudgementNonApplicableDescription.equals(columnName)) {
								// 非該当表示文言
								valueList.add(judgement.getNonApplicableDescription());
							} else if (columnNameCategoryJudgementTableName.equals(columnName)) {
								// テーブル名
								valueList.add(judgement.getTableName());
							} else if (columnNameCategoryJudgementFieldName.equals(columnName)) {
								// フィールド名
								valueList.add(judgement.getFieldName());
							} else if (columnNameCategoryJudgementNonApplicableLayerDisplayFlag.equals(columnName)) {
								// 判定レイヤ非該当時表示有無
								valueList.add(judgement.getNonApplicableLayerDisplayFlag());
							} else if (columnNameCategoryJudgementSimultaneousDisplayLayer.equals(columnName)) {
								// 同時表示レイヤ
								valueList.add(judgement.getSimultaneousDisplayLayer());
							} else if (columnNameCategoryJudgementSimultaneousDisplayLayerFlag.equals(columnName)) {
								// 同時表示レイヤ表示有無
								valueList.add(judgement.getSimultaneousDisplayLayerFlag());
							} else {
								LOGGER.warn("非対応のカラム名: " + columnName);
							}
						}
					} else if (tableNameDepartment.equals(tableName)) {
						// M_部署
						for (Department department : departmentList) {
							if (columnNameDepartmentId.equals(columnName)) {
								// 部署ID
								valueList.add(department.getDepartmentId());
							} else if (columnNameDepartmentName.equals(columnName)) {
								// 部署名
								valueList.add(department.getDepartmentName());
							} else if (columnNameDepartmentAnswerAuthorityFlag.equals(columnName)) {
								// 回答権限フラグ
								valueList.add(department.getAnswerAuthorityFlag());
							} else if (columnNameDepartmentMailAddress.equals(columnName)) {
								// メールアドレス
								valueList.add(department.getMailAddress());
							} else {
								LOGGER.warn("非対応のカラム名: " + columnName);
							}
						}
					} else if (tableNameApplication.equals(tableName)) {
						// O_申請
						if (columnNameStatus.equals(columnName)) {
							// ステータス
							try {
								valueList.add(getStatusMap().get(application.getStatus()));
							} catch (Exception e) {
								valueList.add("");
							}
						}
					} else {
						LOGGER.warn("非対応のテーブル名: " + tableName);
					}
				} else {
					LOGGER.warn("非対応の参照タイプ: " + refType);
				}

				// 要素をカンマ結合して文字列に変換後に設定 -> リストのまま格納する
				if (!attributes.containsKey(responseKey)) {
					// attributes.put(responseKey, joinList(valueList));
					attributes.put(responseKey, valueList);
				}
			}

			form.setAttributes(attributes);

			return form;
		} finally {
			LOGGER.debug("申請情報検索結果データフォームを生成 終了");
		}
	}

	/**
	 * M_申請者情報項目EntityをM_申請者情報項目フォームに詰めなおす
	 * 
	 * @param entity M_申請者情報項目Entity
	 * @param value  登録情報値
	 * @return M_申請者情報項目フォーム
	 */
	private ApplicantInformationItemForm getApplicantInformationItemFormFromEntity(ApplicantInformationItem entity,
			String value) {
		ApplicantInformationItemForm form = new ApplicantInformationItemForm();
		form.setDisplayFlag(entity.getDisplayFlag());
		form.setId(entity.getApplicantInformationItemId());
		form.setMailAddress(entity.getMailAddress());
		form.setName(entity.getItemName());
		form.setOrder(entity.getDisplayOrder());
		form.setRegularExpressions(entity.getRegex());
		form.setRequireFlag(entity.getRequireFlag());
		form.setSearchConditionFlag(entity.getSearchConditionFlag());
		form.setValue(value);
		return form;
	}

	/**
	 * M_申請ファイルEntityをM_申請ファイルフォームに詰めなおす
	 * 
	 * @param entity M_申請ファイルEntity
	 * @return M_申請ファイルフォーム
	 */
	private ApplicationFileForm getApplicationFileFormFromEntity(ApplicationFileMaster entity) {
		ApplicationFileForm form = new ApplicationFileForm();
		form.setApplicationFileId(entity.getApplicationFileId());
		form.setApplicationFileName(entity.getUploadFileName());
		form.setJudgementItemId(entity.getJudgementItemId());
		form.setRequireFlag(entity.getRequireFlag());
		return form;
	}

	/**
	 * O_申請ファイルEntityをO申請ファイルフォームに詰めなおす
	 * 
	 * @param entity O_申請ファイルEntity
	 * @return O_申請ファイルフォーム
	 */
	private UploadApplicationFileForm getUploadApplicationFileFormFromEntity(ApplicationFile entity) {
		UploadApplicationFileForm form = new UploadApplicationFileForm();
		form.setFileId(entity.getFileId());
		form.setApplicationId(entity.getApplicationId());
		form.setApplicationFileId(entity.getApplicationFileId());
		form.setUploadFileName(entity.getUploadFileName());
		form.setFilePath(entity.getFilePath());
		return form;
	}

	/**
	 * M_申請情報検索結果EntityをM_申請情報検索結果フォームに詰めなおす
	 * 
	 * @param entity M_申請情報検索結果Entity
	 * @return M_申請情報検索結果フォーム
	 */
	private ApplicationInformationSearchResultHeaderForm getApplicationInformationSearchResultHeaderFormFromEntity(
			ApplicationSearchResult entity) {
		ApplicationInformationSearchResultHeaderForm form = new ApplicationInformationSearchResultHeaderForm();
		form.setDisplayColumnName(entity.getDisplayColumnName());
		form.setReferenceType(entity.getReferenceType());
		form.setResonseKey(entity.getResponseKey());
		form.setTableWidth(entity.getTableWidth());
		form.setDisplayOrder(entity.getDisplayOrder());
		return form;
	}

	/**
	 * O_回答ファイルEntityを回答情報フォームに詰めなおす
	 * 
	 * @param entity       O_回答ファイルEntity
	 * @param departmentId 部署ID
	 * @param isGoverment  行政かどうか
	 * @return 回答情報フォーム
	 */
	private AnswerForm getAnswerFormFromEntity(Answer entity, String departmentId, boolean isGoverment) {
		LOGGER.debug("回答情報フォーム生成 開始");
		try {
			AnswerForm form = new AnswerForm();
			form.setAnswerId(entity.getAnswerId());
			form.setJudgementResult(entity.getJudgementResult());
			if (isGoverment) {
				// 行政
				form.setAnswerContent(entity.getAnswerContent());
			} else {
				// 事業者
				form.setAnswerContent(entity.getNotifiedText());
			}
			form.setUpdateDatetime(entity.getUpdateDatetime());
			form.setCompleteFlag(entity.getCompleteFlag());
			form.setNotifiedFlag(entity.getNotifiedFlag());

			// 区分判定一覧
			LOGGER.trace("区分判定一覧取得 開始");
			List<CategoryJudgement> categoryList = categoryJudgementRepository
					.getCategoryJudgementListById(entity.getJudgementId());
			LOGGER.trace("区分判定一覧取得 終了");

			AnswerJudgementForm judgeForm = new AnswerJudgementForm();
			boolean editable = false;
			if (categoryList.size() > 0) {
				CategoryJudgement category = categoryList.get(0);

				// 部署リスト
				LOGGER.trace("部署リスト取得 開始");
				List<Department> departmentList = departmentRepository
						.getDepartmentListById(category.getDepartmentId());
				LOGGER.trace("部署リスト取得 終了");

				judgeForm.setJudgementId(category.getJudgementItemId());
				judgeForm.setTitle(category.getTitle());

				// O_回答とJOINされたM_区分判定の部署IDがログインユーザの所属部署IDと一致かつ回答編集可能な区分判定の場合、編集可能とする
				if (isGoverment && departmentId.equals(category.getDepartmentId()) && category.getAnswerEditableFlag()) {
					editable = true;
				}

				// 部署の設定
				if (departmentList.size() > 0) {
					Department department = departmentList.get(0);
					judgeForm.setDepartment(getDepartmentFormFromEntity(department));
				}
			}
			form.setJudgementInformation(judgeForm);

			// 編集可否
			if (!isGoverment) {
				form.setEditable(false);
			} else {
				form.setEditable(editable);
			}

			// 回答ファイル一覧
			LOGGER.trace("回答ファイル一覧取得 開始");
			List<AnswerFile> answerFileList;
			if (isGoverment) {
				// 行政
				answerFileList = answerFileRepository.findByAnswerIdWithoutDeleted(entity.getAnswerId());
			} else {
				// 事業者
				answerFileList = answerFileRepository.findByAnswerIdWithoutDeletedForBusiness(entity.getAnswerId());
			}
			List<AnswerFileForm> answerFormList = new ArrayList<AnswerFileForm>();
			for (AnswerFile answer : answerFileList) {
				answerFormList.add(getAnswerFileFormFromEntity(answer));
			}
			form.setAnswerFiles(answerFormList);
			LOGGER.trace("回答ファイル一覧取得 終了");

			return form;
		} finally {
			LOGGER.debug("回答情報フォーム生成 終了");
		}
	}

	/**
	 * O_回答ファイルEntityをO_回答ファイルフォームに詰めなおす
	 * 
	 * @param entity O_回答ファイルEntity
	 * @return O_回答ファイルフォーム
	 */
	private AnswerFileForm getAnswerFileFormFromEntity(AnswerFile entity) {
		AnswerFileForm form = new AnswerFileForm();
		form.setAnswerId(entity.getAnswerId());
		form.setAnswerFileId(entity.getAnswerFileId());
		form.setAnswerFileName(entity.getAnswerFileName());
		form.setNotifiedFilePath(entity.getNotifiedFilePath());
		form.setDeleteUnnotifiedFlag(entity.getDeleteUnnotifiedFlag());
		return form;
	}

	/**
	 * 申請者情報を構築
	 * 
	 * @param applicantInformationList 申請者情報項目リスト
	 * @return 申請者情報
	 */
	private ApplicantInformation buildApplicantInformation(
			List<ApplicantInformationItemForm> applicantInformationList) {
		ApplicantInformation infomation = new ApplicantInformation();

		Set<String> workSet = new HashSet<String>();
		String mailAddress = null;
		for (ApplicantInformationItemForm applicantInformation : applicantInformationList) {
			if (workSet.contains(applicantInformation.getId())) {
				continue;
			}

			String value = applicantInformation.getValue();

			switch (applicantInformation.getId()) {
			case ApplicationDao.ITEM_1_ID:
				infomation.setItem1(value);
				break;
			case ApplicationDao.ITEM_2_ID:
				infomation.setItem2(value);
				break;
			case ApplicationDao.ITEM_3_ID:
				infomation.setItem3(value);
				break;
			case ApplicationDao.ITEM_4_ID:
				infomation.setItem4(value);
				break;
			case ApplicationDao.ITEM_5_ID:
				infomation.setItem5(value);
				break;
			case ApplicationDao.ITEM_6_ID:
				infomation.setItem6(value);
				break;
			case ApplicationDao.ITEM_7_ID:
				infomation.setItem7(value);
				break;
			case ApplicationDao.ITEM_8_ID:
				infomation.setItem8(value);
				break;
			case ApplicationDao.ITEM_9_ID:
				infomation.setItem9(value);
				break;
			case ApplicationDao.ITEM_10_ID:
				infomation.setItem10(value);
				break;
			default:
				LOGGER.warn("未対応の申請者情報項目ID: " + applicantInformation.getId());
				break;
			}

			workSet.add(applicantInformation.getId());

			if (applicantInformation.getMailAddress()) {
				mailAddress = value;
			}
		}
		infomation.setMailAddress(mailAddress);
		return infomation;
	}

}
