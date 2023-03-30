package developmentpermission.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import developmentpermission.form.ApplicationCategoryForm;
import developmentpermission.form.ApplicationCategorySelectionLogForm;
import developmentpermission.form.ApplicationCategorySelectionViewForm;
import developmentpermission.form.CategoryJudgementLogForm;
import developmentpermission.form.GeneralConditionDiagnosisReportRequestForm;
import developmentpermission.form.GeneralConditionDiagnosisRequestForm;
import developmentpermission.form.GeneralConditionDiagnosisResultForm;
import developmentpermission.form.GeneralConditionDiagnosisResultLogForm;
import developmentpermission.form.LotNumberForm;
import developmentpermission.form.ResponseEntityForm;
import developmentpermission.form.UploadForGeneralConditionDiagnosisForm;
import developmentpermission.service.JudgementService;
import developmentpermission.util.AuthUtil;
import developmentpermission.util.LogUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * 概況診断APIコントローラ
 */
@Api(tags = "概況診断")
@RestController
@RequestMapping("/judgement")
public class JudgementApiController extends AbstractApiController {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(JudgementApiController.class);

	/** 区分判定Serviceインスタンス */
	@Autowired
	private JudgementService judgementService;

	/** 概況診断結果ログファイルパス */
	@Value("${app.json.log.rootPath.judgeresult}")
	private String logJudgeresultRootPath;

	/** 概況診断結果レポート（出力件数） csvログファイルヘッダー */
	@Value("${app.csv.log.header.judge.report}")
	private String[] judgeReportLogHeader;

	/** 概況診断結果レポート（出力件数） csvログファイルパス */
	@Value("${app.csv.log.path.judge.report}")
	private String judgeReportLogPath;

	/**
	 * 概況診断実行
	 * 
	 * @param generalConditionDiagnosisRequestForm パラメータ
	 * @return 診断結果
	 */
	@RequestMapping(value = "/execute", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "概況診断実行", notes = "概況診断を実行し結果を返す")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 503, message = "判定処理に失敗した場合", response = ResponseEntityForm.class) })
	public List<GeneralConditionDiagnosisResultForm> executeGeneralConditionDiagnosis(
			@ApiParam(required = true, value = "概況診断結果リクエストフォーム")@RequestBody GeneralConditionDiagnosisRequestForm generalConditionDiagnosisRequestForm) {
		LOGGER.info("概況診断実行 開始");
		try {
			// パラメータチェック
			if (isValidParam(generalConditionDiagnosisRequestForm)) {
				try {
					List<GeneralConditionDiagnosisResultForm> generalConditionDiagnosisResultFormList = judgementService
							.executeGeneralConditionDiagnosis(generalConditionDiagnosisRequestForm);
					// 概況診断結果ログ出力
					if (generalConditionDiagnosisResultFormList.size() > 0) {
						int generalConditionDiagnosisId = generalConditionDiagnosisResultFormList.get(0)
								.getGeneralConditionDiagnosisResultId();
						try {
							final GeneralConditionDiagnosisResultLogForm logForm = generateLogForm(
									generalConditionDiagnosisRequestForm, generalConditionDiagnosisResultFormList);

							LogUtil.writeGeneralConditionDiagnosisRequestFormLogToJson(
									logJudgeresultRootPath + generalConditionDiagnosisId + ".json", logForm);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					return generalConditionDiagnosisResultFormList;
				} catch (Exception ex) {
					LOGGER.error("概況診断処理で例外発生", ex);
					throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
				}
			} else {
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("概況診断実行 終了");
		}
	}

	/**
	 * 概況診断結果レポート出力
	 * 
	 * @param generalConditionDiagnosisReportRequestForm パラメータ
	 * @param response                                   HttpServletResponse
	 */
	@RequestMapping(value = "/report", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "概況診断結果レポート出力", notes = "概況診断結果レポートを出力する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 503, message = "帳票作成に失敗した場合", response = ResponseEntityForm.class) })
	public void exportGeneralConditionDiagnosisReport(
			@ApiParam(required = true, value = "概況診断結果レポート出力リクエストフォーム")@RequestBody GeneralConditionDiagnosisReportRequestForm generalConditionDiagnosisReportRequestForm,
			HttpServletResponse response) {
		LOGGER.info("概況診断結果レポート出力 開始");
		try {
			// パラメータチェック
			if (isValidParam(generalConditionDiagnosisReportRequestForm)) {
				if (!judgementService.exportJudgeReportWorkBook(generalConditionDiagnosisReportRequestForm, response)) {
					LOGGER.warn("帳票生成失敗");
					throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
				}
			} else {
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			// 概況診断結果レポート（出力件数） ログ出力
			try {
				Object[] logData = { LogUtil.localDateTimeToString(LocalDateTime.now()),
						generalConditionDiagnosisReportRequestForm.getGeneralConditionDiagnosisResults().get(0)
								.getGeneralConditionDiagnosisResultId() };
				LogUtil.writeLogToCsv(judgeReportLogPath, judgeReportLogHeader, logData);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			LOGGER.info("概況診断結果レポート出力 終了");
		}
	}

	/**
	 * 概況診断 画像アップロード 一時フォルダー生成・取得
	 * 
	 * @return UploadForGeneralConditionDiagnosisForm 概況診断画像アップロードDTO（folderName）
	 */
	@ResponseStatus(code = HttpStatus.CREATED)
	@RequestMapping(value = "/image/upload/preparation", method = RequestMethod.GET)
	@ApiOperation(value = "概況診断 画像アップロード 一時フォルダー生成・取得", notes = "生成された一時フォルダーを取得する.")
	@ResponseBody
	@ApiResponses(value = { 
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class)})
	public UploadForGeneralConditionDiagnosisForm getFolderName(
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("概況診断 画像アップロード 一時フォルダー生成・取得 開始");
		try {
			String role = AuthUtil.getRole(token);
			if (AuthUtil.ROLE_BUSINESS.equals(role) || AuthUtil.ROLE_GOVERMENT.equals(role)) {
				UploadForGeneralConditionDiagnosisForm uploadForGeneralConditionDiagnosisForm = judgementService
						.getFolderName();
				return uploadForGeneralConditionDiagnosisForm;
			} else {
				// 事業者ユーザしかアクセス不可
				LOGGER.warn("ロール不適合: " + role);
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}
		} finally {
			LOGGER.info("概況診断 画像アップロード 一時フォルダー生成・取得 終了");
		}
	}

	/**
	 * 概況診断画像アップロード
	 * 
	 * @param UploadForGeneralConditionDiagnosisForm 概況診断画像アップロードDTO(folderName,judgementId,image)
	 * @return ResponseEntityForm 処理結果
	 */
	@ResponseStatus(code = HttpStatus.CREATED)
	@RequestMapping(value = "/image/upload", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	@ApiOperation(value = "概況診断画像アップロード", notes = "概況診断画像を一時フォルダーにアップロードする.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class) })
	public ResponseEntityForm uploadImageFile(
			@ApiParam(required = true, value = "概況診断画像アップロードフォーム[multipart/form-data]")@ModelAttribute UploadForGeneralConditionDiagnosisForm uploadForGeneralConditionDiagnosisForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("概況診断画像アップロード 開始");
		try {
			String role = AuthUtil.getRole(token);
			if (AuthUtil.ROLE_BUSINESS.equals(role) || AuthUtil.ROLE_GOVERMENT.equals(role)) {
				if (uploadForGeneralConditionDiagnosisForm.getImage() != null
						&& !uploadForGeneralConditionDiagnosisForm.getImage().isEmpty()
						&& uploadForGeneralConditionDiagnosisForm.getFolderName() != null
						&& !uploadForGeneralConditionDiagnosisForm.getFolderName().isEmpty()
						&& uploadForGeneralConditionDiagnosisForm.getCurrentSituationMapFlg() != null) {
					judgementService.uploadImageFile(uploadForGeneralConditionDiagnosisForm);
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
			LOGGER.info("概況診断画像アップロード 終了");
		}
	}

	/**
	 * パラメータ確認
	 * 
	 * @param generalConditionDiagnosisRequestForm リクエストフォーム
	 * @return 判定結果
	 */
	private boolean isValidParam(
			GeneralConditionDiagnosisReportRequestForm generalConditionDiagnosisReportRequestForm) {
		List<ApplicationCategorySelectionViewForm> applicationCategories = generalConditionDiagnosisReportRequestForm
				.getApplicationCategories();
		List<GeneralConditionDiagnosisResultForm> generalConditionDiagnosisResults = generalConditionDiagnosisReportRequestForm
				.getGeneralConditionDiagnosisResults();
		List<LotNumberForm> lotNumbers = generalConditionDiagnosisReportRequestForm.getLotNumbers();
		if (applicationCategories == null || applicationCategories.size() == 0
				|| generalConditionDiagnosisResults == null || generalConditionDiagnosisResults.size() == 0
				|| lotNumbers == null || lotNumbers.size() == 0) {
			LOGGER.warn("申請区分選択一覧または概況診断結果一覧または申請地番一覧がnullまたは空");
			return false;
		}
		return true;
	}

	/**
	 * パラメータ確認
	 * 
	 * @param generalConditionDiagnosisRequestForm リクエストフォーム
	 * @return 判定結果
	 */
	private boolean isValidParam(GeneralConditionDiagnosisRequestForm generalConditionDiagnosisRequestForm) {
		List<ApplicationCategorySelectionViewForm> applicationCategories = generalConditionDiagnosisRequestForm
				.getApplicationCategories();
		List<LotNumberForm> lotNumbers = generalConditionDiagnosisRequestForm.getLotNumbers();
		if (applicationCategories == null || applicationCategories.size() == 0 || lotNumbers == null
				|| lotNumbers.size() == 0) {
			LOGGER.warn("申請区分一覧または申請地番一覧がnullまたは空");
			return false;
		}
		return true;
	}

	/**
	 * 概況診断結果ログフォームに整形
	 * 
	 * @param generalConditionDiagnosisRequestForm
	 * @param generalConditionDiagnosisResultFormList
	 * @return
	 */
	private GeneralConditionDiagnosisResultLogForm generateLogForm(
			GeneralConditionDiagnosisRequestForm generalConditionDiagnosisRequestForm,
			List<GeneralConditionDiagnosisResultForm> generalConditionDiagnosisResultFormList) {
		GeneralConditionDiagnosisResultLogForm logForm = new GeneralConditionDiagnosisResultLogForm();
		try {
			// 概況診断結果ID
			logForm.set概況診断結果ID(generalConditionDiagnosisResultFormList.get(0).getGeneralConditionDiagnosisResultId());
			// 日時
			final String executeDateTime = LogUtil.localDateTimeToString(LocalDateTime.now());
			logForm.setアクセス日時(executeDateTime);
			// 地番
			List<String> lotNumbers = new ArrayList<String>();
			for (LotNumberForm aLotNumber : generalConditionDiagnosisRequestForm.getLotNumbers()) {
				lotNumbers.add(aLotNumber.getDistrictName() + aLotNumber.getChiban());
			}
			logForm.set申請地番一覧(lotNumbers);
			// 申請区分
			List<ApplicationCategorySelectionLogForm> applicationCategorySelectionLogFormList = new ArrayList<ApplicationCategorySelectionLogForm>();
			for (ApplicationCategorySelectionViewForm viewForm : generalConditionDiagnosisRequestForm
					.getApplicationCategories()) {
				ApplicationCategorySelectionLogForm aApplicationCategorySelectionLogForm = new ApplicationCategorySelectionLogForm();
				aApplicationCategorySelectionLogForm.set画面ID(viewForm.getScreenId());
				aApplicationCategorySelectionLogForm.set申請区分選択項目(viewForm.getTitle());
				List<String> categoryList = new ArrayList<String>();
				for (ApplicationCategoryForm applicationCategory : viewForm.getApplicationCategory()) {
					categoryList.add(applicationCategory.getContent());
				}
				aApplicationCategorySelectionLogForm.set申請区分(categoryList);
				applicationCategorySelectionLogFormList.add(aApplicationCategorySelectionLogForm);
			}
			logForm.set申請区分選択一覧(applicationCategorySelectionLogFormList);
			// 概況診断結果
			List<CategoryJudgementLogForm> pushFormList = new ArrayList<CategoryJudgementLogForm>();
			for (GeneralConditionDiagnosisResultForm aGeneralConditionDiagnosisResultForm : generalConditionDiagnosisResultFormList) {
				CategoryJudgementLogForm pushForm = new CategoryJudgementLogForm();
				pushForm.set区分判定ID(aGeneralConditionDiagnosisResultForm.getJudgementId());
				pushForm.setタイトル(aGeneralConditionDiagnosisResultForm.getTitle());
				pushForm.set概要(aGeneralConditionDiagnosisResultForm.getSummary());
				pushForm.set文言(aGeneralConditionDiagnosisResultForm.getDescription());
				pushFormList.add(pushForm);
			}
			logForm.set概況診断結果一覧(pushFormList);
		} catch (Exception e) {
			LOGGER.error("概況診断結果ログフォーム生成に失敗", e);
		}
		return logForm;
	}
}
