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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import developmentpermission.form.AnswerConfirmLoginForm;
import developmentpermission.form.AnswerFileForm;
import developmentpermission.form.AnswerForm;
import developmentpermission.form.ApplyAnswerForm;
import developmentpermission.form.ResponseEntityForm;
import developmentpermission.service.AnswerService;
import developmentpermission.service.ApplicationService;
import developmentpermission.util.AuthUtil;
import developmentpermission.util.LogUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * 回答APIコントローラ
 */
@Api(tags = "回答")
@RestController
@RequestMapping("/answer")
public class AnswerApiController extends AbstractApiController {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(AnswerApiController.class);

	/** 申請Serviceインスタンス */
	@Autowired
	private ApplicationService applicationService;

	/** 回答Serviceインスタンス */
	@Autowired
	private AnswerService answerService;
	
	/** 回答確認 csvログファイルヘッダー */
	@Value("${app.csv.log.header.answer.confirm}")
	private String[] answerConfirmLogHeader;
	
	/** 回答確認 csvログファイルパス */
	@Value("${app.csv.log.path.answer.confirm}")
	private String answerConfirmLogPath;
	
	/** 回答通知 csvログファイルヘッダー */
	@Value("${app.csv.log.header.answer.notification}")
	private String[] answerNotificationLogHeader;
	
	/** 回答通知 csvログファイルパス */
	@Value("${app.csv.log.path.answer.notification}")
	private String answerNotificationLogPath;

	/**
	 * 申請・回答内容確認情報取得(事業者)
	 * 
	 * @param answerConfirmLoginForm ログインID,パスワード
	 * @return ApplyAnswerForm 申請情報詳細
	 */
	@RequestMapping(value = "/confirm/answer", method = RequestMethod.POST)
	@ApiOperation(value = "申請・回答内容確認情報取得", notes = "申請情報詳細を取得する.")
	@ResponseBody
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "申請情報取得不能", response = ResponseEntityForm.class)})
	public ApplyAnswerForm getApplicationConfirm(@ApiParam(required = true, value = "申請者ログインパラメータフォーム")@RequestBody AnswerConfirmLoginForm answerConfirmLoginForm) {
		LOGGER.info("申請・回答内容確認情報取得(事業者) 開始");
		try {
			String id = answerConfirmLoginForm.getLoginId();
			String password = answerConfirmLoginForm.getPassword();

			if (id != null && !"".equals(id) //
					|| password != null && !"".equals(password)) {
				// 申請ID
				Integer applicationId = applicationService.getApplicationIdFromApplicantInfo(answerConfirmLoginForm);
				if (applicationId != null) {
					ApplyAnswerForm applyAnswerForm = applicationService.getApplicationDetail(applicationId, null,
							false);
					// 回答確認ログ出力
					try {
						Object[] logData = {LogUtil.localDateTimeToString(LocalDateTime.now()),applicationId};
						LogUtil.writeLogToCsv(answerConfirmLogPath, answerConfirmLogHeader, logData);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					return applyAnswerForm;
				} else {
					LOGGER.warn("申請情報取得不能");
					throw new ResponseStatusException(HttpStatus.FORBIDDEN);
				}
			} else {
				LOGGER.warn("IDまたはパスワードが空またはnull");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("申請・回答内容確認情報取得(事業者) 終了");
		}
	}

	/**
	 * 回答登録(行政のみ)
	 * 
	 * @param List<AnswerForm> 回答一覧
	 * @return List<AnswerForm> 回答一覧
	 */
	@ResponseStatus(code = HttpStatus.CREATED)
	@RequestMapping(value = "/input", method = RequestMethod.POST)
	@ApiOperation(value = "回答登録(行政)", notes = "回答を登録する")
	@ResponseBody
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class) })
	public ResponseEntityForm registAnswers(@ApiParam(required = true, value = "回答情報フォーム一覧")@RequestBody List<AnswerForm> answerFormList,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("回答登録 開始");
		try {
			String departmentId = AuthUtil.getDepartmentId(token);
			String loginId = AuthUtil.getLoginId(token);
			String departmentName = AuthUtil.getDepartmentName(token);
			if (answerService.validateRegistAnswersParam(answerFormList, departmentId)) {
				answerService.registAnswers(answerFormList,loginId,departmentName);
			} else {
				// パラメータ不正
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			return new ResponseEntityForm(HttpStatus.CREATED.value(), "Application registration successful.");
		} finally {
			LOGGER.info("回答登録 終了");
		}
	}

	/**
	 * 回答通知
	 * 
	 * @param ApplyAnswerForm 申請情報詳細
	 */
	@RequestMapping(value = "/notification", method = RequestMethod.POST)
	@ApiOperation(value = "回答通知(行政)", notes = "申請者に回答を通知する.")
	@ResponseBody
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "権限不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 409, message = "申請ステータスが回答通知不可の状態", response = ResponseEntityForm.class) })
	public void answerNotification(@ApiParam(required = true, value = "申請・回答内容確認情報フォーム")@RequestBody ApplyAnswerForm applyAnswerForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("回答通知 開始");
		try {
			// 回答通知権限チェック
			String departmentId = AuthUtil.getDepartmentId(token);
			String loginId = AuthUtil.getLoginId(token);
			String departmentName = AuthUtil.getDepartmentName(token);
			if (!answerService.checkAnswerAuthority(departmentId)) {
				// 権限不正
				LOGGER.warn("権限不正");
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}
			// 回答通知・ステータス更新
			answerService.notifyAnswer(applyAnswerForm);
			// 回答通知ログ出力
			try {
				Object[] logData = {LogUtil.localDateTimeToString(LocalDateTime.now()),loginId,departmentName,applyAnswerForm.getApplicationId()};
				LogUtil.writeLogToCsv(answerNotificationLogPath, answerNotificationLogHeader, logData);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} finally {
			LOGGER.info("回答登録 終了");
		}
	}

	/**
	 * 回答ファイルアップロード
	 * 
	 * @param AnswerFileForm O_回答ファイルファイルフォーム
	 * @return
	 */
	@RequestMapping(value = "/file/upload", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	@ApiOperation(value = "回答ファイルアップロード(行政)", notes = "回答ファイルをアップロードする.")
	@ResponseBody
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class) })
	public ResponseEntityForm uploadAnswerFile(
			@ApiParam(required = true, value = "回答ファイルフォーム[multipart/form-data]")@ModelAttribute AnswerFileForm answerFileForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("回答ファイルアップロード 開始");
		try {
			String departmentId = AuthUtil.getDepartmentId(token);
			if (answerService.validateUploadAnswerFile(answerFileForm, departmentId)) {
				answerService.uploadAnswerFile(answerFileForm);
				ResponseEntityForm responseEntityForm = new ResponseEntityForm(HttpStatus.CREATED.value(),
						"Answer File registration successful.");
				return responseEntityForm;
			} else {
				// パラメータ不正
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("回答ファイルアップロード 終了");
		}
	}

	/**
	 * 回答ファイルダウンロード
	 * 
	 * @param AnswerFileForm O_回答ファイルファイルフォーム
	 * @return 応答Entity
	 */
	@RequestMapping(value = "/file/download", method = RequestMethod.POST)
	@ApiOperation(value = "回答ファイルダウンロード", notes = "回答ファイルをダウンロードする.")
	@ResponseBody
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 404, message = "ファイルが存在しない場合", response = ResponseEntityForm.class) })
	public ResponseEntity<Resource> downloadAnswerFile(@ApiParam(required = true, value = "回答ファイルフォーム")@RequestBody AnswerFileForm answerFileForm,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("回答ファイルダウンロード 開始");
		try {
			
			String role = AuthUtil.getRole(token);
			if (AuthUtil.ROLE_BUSINESS.equals(role)) {
				String id = answerFileForm.getLoginId();
				String password = answerFileForm.getPassword();
				if (id != null && !"".equals(id) //
						&& password != null && !"".equals(password)) {
					AnswerConfirmLoginForm answerConfirmLoginForm = new AnswerConfirmLoginForm(id,password);
					// 申請ID
					Integer applicationId = applicationService.getApplicationIdFromApplicantInfo(answerConfirmLoginForm);
					if (applicationId == null || !applicationId.equals(answerFileForm.getApplicationId())) {
						// 照合IDとパスワードによる認証失敗
						LOGGER.warn("回答ファイルダウンロードでの照合IDとパスワードによる認証失敗："+role);
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
			
			if (answerService.validateDownloadAnswerFile(answerFileForm)) {
				return answerService.downloadAnswerFile(answerFileForm, role);
			} else {
				// パラメータ不正
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} finally {
			LOGGER.info("回答ファイルダウンロード 終了");
		}
	}

	/**
	 * 回答ファイル論理削除
	 * 
	 * @param AnswerFileForm O_回答ファイルファイルフォーム
	 * @return 応答Entity
	 */
	@RequestMapping(value = "/file/delete", method = RequestMethod.POST)
	@ApiOperation(value = "回答ファイル論理削除(行政)", notes = "回答ファイルを論理削除する.")
	@ResponseBody
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 404, message = "ファイルが存在しない場合", response = ResponseEntityForm.class) })
	public ResponseEntityForm deleteAnswerFile(@ApiParam(required = true, value = "回答ファイルフォーム")@RequestBody AnswerFileForm answerFileForm) {
		LOGGER.info("回答ファイル論理削除 開始");
		try {
			answerService.deleteAnswerFile(answerFileForm);
			ResponseEntityForm responseEntityForm = new ResponseEntityForm(HttpStatus.OK.value(),
					"Answer File Delete successful.");
			return responseEntityForm;
		} finally {
			LOGGER.info("回答ファイル論理削除 終了");
		}
	}
}
