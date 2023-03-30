package developmentpermission.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import developmentpermission.form.AnswerConfirmLoginForm;
import developmentpermission.form.ApplicantInformationItemForm;
import developmentpermission.form.ApplicationCategorySelectionViewForm;
import developmentpermission.form.ApplicationFileForm;
import developmentpermission.form.ApplicationInformationSearchResultHeaderForm;
import developmentpermission.form.ApplicationRegisterForm;
import developmentpermission.form.ApplicationRegisterResultForm;
import developmentpermission.form.ApplicationSearchConditionForm;
import developmentpermission.form.ApplicationSearchResultForm;
import developmentpermission.form.ApplyAnswerForm;
import developmentpermission.form.DepartmentForm;
import developmentpermission.form.GeneralConditionDiagnosisResultForm;
import developmentpermission.form.ResponseEntityForm;
import developmentpermission.form.StatusForm;
import developmentpermission.form.UploadApplicationFileForm;
import developmentpermission.service.ApplicationService;
import developmentpermission.service.CategoryService;
import developmentpermission.util.AuthUtil;
import developmentpermission.util.LogUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * 申請APIコントローラ
 */
@Api(tags = "申請")
@RestController
@RequestMapping("/application")
public class ApplicationApiController extends AbstractApiController {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationApiController.class);

	/** 申請Serviceインスタンス */
	@Autowired
	private ApplicationService applicationService;

	/** 申請区分Serviceインスタンス */
	@Autowired
	private CategoryService categoryService;

	/** 申請登録 csvログファイルヘッダー */
	@Value("${app.csv.log.header.application.register}")
	private String[] applicationRegisterLogHeader;

	/** 申請登録 csvログファイルパス */
	@Value("${app.csv.log.path.application.register}")
	private String applicationRegisterLogPath;

	/**
	 * 申請者情報入力項目一覧取得(事業者)
	 * 
	 * @return 申請者情報入力項目一覧
	 */
	@RequestMapping(value = "/applicantItems", method = RequestMethod.GET)
	@ApiOperation(value = "申請者情報入力項目一覧取得", notes = "申請者情報入力項目一覧を取得する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class) })
	public List<ApplicantInformationItemForm> getApplicantItems() {
		LOGGER.info("申請者情報入力項目一覧取得 開始");
		try {
			List<ApplicantInformationItemForm> applicantInformationItemFormList = applicationService
					.getApplicantItems();
			return applicantInformationItemFormList;
		} finally {
			LOGGER.info("申請者情報入力項目一覧取得 終了");
		}
	}

	/**
	 * 申請ファイル一覧取得(事業者)
	 * 
	 * @param generalConditionDiagnosisResultForm 検索条件
	 * @return 申請ファイル一覧
	 */
	@RequestMapping(value = "/applicationFiles", method = RequestMethod.POST)
	@ApiOperation(value = "申請ファイル一覧取得", notes = "アップロード対象の申請ファイル一覧を取得する.")
	@ResponseBody
	@ApiResponses(value = { 
			@ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class) })
	public List<ApplicationFileForm> getApplicationFiles(
			@ApiParam(required = true, value = "概況診断結果フォーム一覧")@RequestBody List<GeneralConditionDiagnosisResultForm> generalConditionDiagnosisResultFormList) {
		LOGGER.info("申請ファイル一覧取得 開始");
		try {
			if (generalConditionDiagnosisResultFormList != null && generalConditionDiagnosisResultFormList.size() > 0) {
				List<ApplicationFileForm> applicationFileFormList = applicationService
						.getApplicationFiles(generalConditionDiagnosisResultFormList);
				return applicationFileFormList;
			} else {
				LOGGER.warn("パラメータが空またはnull");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("申請ファイル一覧取得 終了");
		}
	}

	/**
	 * 申請登録(事業者)
	 * 
	 * @param applicationRegisterForm 登録情報
	 */
	@ResponseStatus(code = HttpStatus.CREATED)
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	@ApiOperation(value = "申請登録", notes = "申請を登録する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 409, message = "登録に失敗した場合", response = ResponseEntityForm.class),
			@ApiResponse(code = 503, message = "処理エラー", response = ResponseEntityForm.class)})
	public ApplicationRegisterResultForm registerApplication(
			@ApiParam(required = true, value = "申請登録リクエストフォーム")@RequestBody ApplicationRegisterForm applicationRegisterForm) {
		LOGGER.info("申請登録 開始");
		try {
			// パラメータチェック
			if (applicationService.validateRegisterApplicationParam(applicationRegisterForm)) {
				try {
					// 登録処理
					int applicationId = applicationService.registerApplication(applicationRegisterForm);
					ApplicationRegisterResultForm form = new ApplicationRegisterResultForm();
					form.setApplicationId(applicationId);
					// 申請登録 ログ出力
					try {
						Object[] logData = { LogUtil.localDateTimeToString(LocalDateTime.now()), applicationId,
								applicationRegisterForm.getGeneralConditionDiagnosisResultForm().get(0)
										.getGeneralConditionDiagnosisResultId() };
						LogUtil.writeLogToCsv(applicationRegisterLogPath, applicationRegisterLogHeader, logData);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					return form;
				} catch (RuntimeException ex) {
					LOGGER.error("申請登録時にエラー発生", ex);
					throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
				}
			} else {
				// パラメータ不正
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("申請登録 終了");
		}
	}

	/**
	 * 申請情報検索条件一覧取得(行政)
	 * 
	 * @return 申請情報検索条件一覧取得
	 */
	@RequestMapping(value = "/search/conditions", method = RequestMethod.GET)
	@ApiOperation(value = "申請情報検索条件一覧取得(行政)", notes = "申請情報検索条件一覧を取得する.")
	@ResponseBody
	@ApiResponses(value = { 
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 503, message = "処理エラー", response = ResponseEntityForm.class)
			})
	public ApplicationSearchConditionForm getApplicationSearchConditions() {
		LOGGER.info("申請情報検索条件一覧取得 開始");
		try {
			ApplicationSearchConditionForm applicationSearchConditionForm = new ApplicationSearchConditionForm();

			// 申請者情報一覧
			LOGGER.debug("申請者情報一覧取得 開始");
			List<ApplicantInformationItemForm> applicantInformationItemFormList = applicationService
					.getApplicantItems();
			applicationSearchConditionForm.setApplicantInformationItemForm(applicantInformationItemFormList);
			LOGGER.debug("申請者情報一覧取得 終了");

			// 申請区分選択一覧
			LOGGER.debug("申請区分選択一覧取得 開始");
			List<ApplicationCategorySelectionViewForm> applicationCategories = categoryService
					.getApplicationCategorySelectionViewList();
			applicationSearchConditionForm.setApplicationCategories(applicationCategories);
			LOGGER.debug("申請区分選択一覧取得 終了");

			// ステータス一覧
			LOGGER.debug("ステータス一覧取得 開始");
			List<StatusForm> statusList = applicationService.getStatusList();
			applicationSearchConditionForm.setStatus(statusList);
			LOGGER.debug("ステータス一覧取得 終了");

			// 部署一覧
			LOGGER.debug("部署一覧取得 開始");
			List<DepartmentForm> departmentList = applicationService.getDepartmentList();
			applicationSearchConditionForm.setDepartment(departmentList);
			LOGGER.debug("部署一覧取得 終了");

			return applicationSearchConditionForm;
		} catch (Exception ex) {
			LOGGER.error("申請情報検索条件一覧取得時に例外発生", ex);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		} finally {
			LOGGER.info("申請情報検索条件一覧取得 終了");
		}
	}

	/**
	 * 申請情報検索結果表示項目一覧取得(行政)
	 * 
	 * @return 申請情報検索結果表示項目一覧取得
	 */
	@RequestMapping(value = "/search/columns", method = RequestMethod.GET)
	@ApiOperation(value = "申請情報検索結果表示項目一覧取得(行政)", notes = "申請情報検索結果表示項目一覧を取得する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class) })
	public List<ApplicationInformationSearchResultHeaderForm> getApplicationInformationSearchResultHeader() {
		LOGGER.info("申請情報検索結果表示項目一覧取得 開始");
		try {
			List<ApplicationInformationSearchResultHeaderForm> applicationInformationSearchResultFormList = applicationService
					.getApplicationInformationSearchResultHeader();
			return applicationInformationSearchResultFormList;
		} finally {
			LOGGER.info("申請情報検索結果表示項目一覧取得 終了");
		}
	}

	/**
	 * 申請情報検索(行政)
	 * 
	 * @param applicationSearchConditionForm 検索条件
	 * @return 検索結果
	 */
	@RequestMapping(value = "/search", method = RequestMethod.POST)
	@ApiOperation(value = "申請情報検索(行政)", notes = "申請情報を検索する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class) })
	public List<ApplicationSearchResultForm> searchApplicationInformation(
			@ApiParam(required = true, value = "申請情報検索条件フォーム")@RequestBody ApplicationSearchConditionForm applicationSearchConditionForm) {
		LOGGER.info("申請情報検索 開始");
		try {
			List<StatusForm> status = applicationSearchConditionForm.getStatus();
			List<DepartmentForm> department = applicationSearchConditionForm.getDepartment();
			if ((status != null && status.size() > 1) || (department != null && department.size() > 1)) {
				// ステータス、部署は複数の場合エラーとする
				LOGGER.warn("ステータスまたは部署情報がnullまたは複数設定されている");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			List<ApplicationSearchResultForm> applicationSearchResultFormList = applicationService
					.searchApplicationInformation(applicationSearchConditionForm);
			return applicationSearchResultFormList;
		} finally {
			LOGGER.info("申請情報検索 終了");
		}
	}

	/**
	 * 申請情報詳細取得(行政)
	 * 
	 * @param Integer 申請ID
	 * @return ApplyAnswerForm 申請情報詳細
	 */
	@RequestMapping(value = "/detail/{application_id}", method = RequestMethod.GET)
	@ApiOperation(value = "申請情報詳細取得(行政)", notes = "申請情報詳細を取得する.")
	@ResponseBody
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class) })
	public ApplyAnswerForm getApplicationDetail(@ApiParam(required = true, value = "申請ID")@PathVariable(value = "application_id") Integer applicationId,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("申請情報詳細取得 開始");
		try {
			String departmentId = AuthUtil.getDepartmentId(token);
			if (departmentId != null && !"".equals(departmentId)) {
				ApplyAnswerForm applyAnswerForm = applicationService.getApplicationDetail(applicationId, departmentId,
						true);
				return applyAnswerForm;
			} else {
				LOGGER.warn("部署IDがnullまたは空");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("申請情報詳細取得 終了");
		}
	}

	/**
	 * 申請ファイルアップロード
	 * 
	 * @param uploadApplicationFileForm O_申請ファイルファイルフォーム
	 * @return
	 */
	@RequestMapping(value = "/file/upload", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	@ApiOperation(value = "申請ファイルアップロード", notes = "申請ファイルをアップロードする.")
	@ResponseBody
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class)})
	public ResponseEntityForm uploadApplicationFile(
			/* @RequestBody // MultipartFileを含む場合、RequestBodyでは415エラーで弾かれる・・・？ */
			@ApiParam(required = true, value = "申請ファイルフォーム[multipart/form-data]")@ModelAttribute UploadApplicationFileForm uploadApplicationFileForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("申請ファイルアップロード 開始");
		try {
			String role = AuthUtil.getRole(token);
			if (AuthUtil.ROLE_BUSINESS.equals(role) || AuthUtil.ROLE_GOVERMENT.equals(role)) {
				if (applicationService.validateUploadApplicationFile(uploadApplicationFileForm)) {
					applicationService.uploadApplicationFile(uploadApplicationFileForm, null);
					ResponseEntityForm responseEntityForm = new ResponseEntityForm(HttpStatus.CREATED.value(),
							"Application File registration successful.");
					return responseEntityForm;
				} else {
					// パラメータ不正
					LOGGER.warn("パラメータ不正");
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
				}
			} else {
				// 事業者ユーザしかアクセス不可
				LOGGER.warn("ロール不適合: " + role);
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}
		} finally {
			LOGGER.info("申請ファイルアップロード 終了");
		}
	}

	/**
	 * 申請ファイルダウンロード
	 * 
	 * @param uploadApplicationFileForm O_申請ファイルファイルフォーム
	 * @return 応答Entity
	 */
	@RequestMapping(value = "/file/download", method = RequestMethod.POST)
	@ApiOperation(value = "申請ファイルダウンロード", notes = "申請ファイルをダウンロードする.")
	@ResponseBody
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 404, message = "ファイルが存在しない場合", response = ResponseEntityForm.class) })
	public ResponseEntity<Resource> downloadApplicationFile(
			@ApiParam(required = true, value = "申請ファイルフォーム")@RequestBody UploadApplicationFileForm uploadApplicationFileForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("申請ファイルダウンロード 開始");
		try {
			
			String role = AuthUtil.getRole(token);
			if (AuthUtil.ROLE_BUSINESS.equals(role)) {
				String id = uploadApplicationFileForm.getLoginId();
				String password = uploadApplicationFileForm.getPassword();
				if (id != null && !"".equals(id) //
						&& password != null && !"".equals(password)) {
					AnswerConfirmLoginForm answerConfirmLoginForm = new AnswerConfirmLoginForm(id,password);
					// 申請ID
					Integer applicationId = applicationService.getApplicationIdFromApplicantInfo(answerConfirmLoginForm);
					if (applicationId == null || !applicationId.equals(uploadApplicationFileForm.getApplicationId())) {
						// 照合IDとパスワードによる認証失敗
						LOGGER.warn("申請ファイルダウンロードでの照合IDとパスワードによる認証失敗："+role);
						throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
					}
					
				}else {
					// パラメータ不正
					LOGGER.warn("パラメータ不正");
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
				}
			}else if(!AuthUtil.ROLE_GOVERMENT.equals(role)) {
				LOGGER.warn("不正なroleによる認証："+role);
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);	
			}
			
			if (applicationService.validateDownloadApplicationFile(uploadApplicationFileForm)) {
				return applicationService.downloadApplicationFile(uploadApplicationFileForm);
			} else {
				// パラメータ不正
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("申請ファイルダウンロード 終了");
		}
	}

	/**
	 * 照合情報を申請者に通知する
	 * 
	 * @param applicationRegisterResultForm
	 * @return
	 */
	@RequestMapping(value = "/notify/collation", method = RequestMethod.POST)
	@ApiOperation(value = "照合情報通知", notes = "照合ID/パスワードを発行し申請者に通知する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 409, message = "ステータス不正", response = ResponseEntityForm.class) })
	public AnswerConfirmLoginForm notifyCollationInformation(
			@ApiParam(required = true, value = "申請登録結果フォーム")@RequestBody ApplicationRegisterResultForm applicationRegisterResultForm) {
		LOGGER.info("照合情報通知 開始");
		try {
			AnswerConfirmLoginForm form = null;
			if (applicationRegisterResultForm != null //
					&& applicationRegisterResultForm.getApplicationId() != null) {
				form = applicationService.notifyCollationInformation(applicationRegisterResultForm);
				if (form == null) {
					LOGGER.warn("パラメータ不正");
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
				}
			} else {
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			return form;
		} finally {
			LOGGER.info("照合情報通知 終了");
		}
	}

}
