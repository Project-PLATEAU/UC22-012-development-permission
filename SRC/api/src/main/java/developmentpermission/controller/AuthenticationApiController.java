package developmentpermission.controller;

import java.time.LocalDateTime;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import developmentpermission.form.GovermentLoginForm;
import developmentpermission.form.GovernmentUserForm;
import developmentpermission.form.ResponseEntityForm;
import developmentpermission.util.AuthUtil;
import developmentpermission.util.LogUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * 認証APIコントローラ
 */
@Api(tags = "認証")
@RestController
@RequestMapping("/auth")
public class AuthenticationApiController extends AbstractApiController {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationApiController.class);

	/** Cookieの有効期間(秒) */
	@Value("${app.filter.cookie.expire}")
	private int expireTime;
	/** 事業者ユーザのロール */
	@Value("${app.role.business}")
	private String businessUserRole;
	/** 事業者の部署コード */
	@Value("${app.department.business}")
	private String businessUserDepartment;
	/** 事業者ログイン（アクセス）ログ csvログファイルヘッダー */
	@Value("${app.csv.log.header.business.login}")
	private String[] businessLoginLogHeader;
	/** 事業者ログイン（アクセス）ログ csvログファイルパス */
	@Value("${app.csv.log.path.business.login}")
	private String businessLoginLogPath;
	/** 行政ログインログ csvログファイルヘッダー */
	@Value("${app.csv.log.header.administration.login}")
	private String[] administrationLoginLogHeader;
	/** 行政ログインログ csvログファイルパス */
	@Value("${app.csv.log.path.administration.login}")
	private String administrationLoginLogPath;
	/**
	 * 認証用API
	 * 
	 * @param response    HttpServletResponse
	 * @param businessFlg 事業者フラグ
	 */
	@RequestMapping(value = "/checkAuth", method = RequestMethod.GET)
	@ApiOperation(value = "認証", notes = "認証状態の確認および事業者の認証情報設定.")
	@ResponseBody
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "事業者フラグ無しでアクセスし、認証がOKの場合", response = ResponseEntityForm.class),
			@ApiResponse(code = 201, message = "事業者フラグありで認証情報が生成された場合", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "事業者フラグ無しでアクセスし、認証がNGの場合", response = ResponseEntityForm.class),
			@ApiResponse(code = 503, message = "処理エラー", response = ResponseEntityForm.class)})
	public void checkAuth(HttpServletResponse response, @ApiParam(required = false, value = "事業者フラグ")@RequestParam(value="jigyousya", required = false) Boolean businessFlg,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("認証 開始");
		boolean businessLoginLogFlg = false;
		boolean administrationLoginFlg = false;
		try {
			if (businessFlg != null && businessFlg) {
				// 事業者ユーザを生成
				LOGGER.info("事業者ユーザログイン情報生成 開始");
				response.addCookie(AuthUtil.createUserCookie("","",businessUserRole, businessUserDepartment,"",expireTime));
				response.setStatus(HttpServletResponse.SC_CREATED);
				LOGGER.info("事業者ユーザログイン情報生成 終了");
				businessLoginLogFlg = true;
			} else {
				// 認証情報を確認
				if (!AuthUtil.validate(token)) {
					LOGGER.warn("認証情報が不正");
					throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
				}else if(AuthUtil.ROLE_BUSINESS.equals(AuthUtil.getRole(token))){
					businessLoginLogFlg = true;
				}else if(AuthUtil.ROLE_GOVERMENT.equals(AuthUtil.getRole(token))){
					administrationLoginFlg = true;
				}
				response.setStatus(HttpServletResponse.SC_OK);
			}
		} catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		} finally {
			if(businessLoginLogFlg) {
				// 事業者ログイン ログ出力
				try {
					Object[] logData = {LogUtil.localDateTimeToString(LocalDateTime.now())};
					LogUtil.writeLogToCsv(businessLoginLogPath, businessLoginLogHeader, logData);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}else if(administrationLoginFlg) {
				// 行政ログインログ出力
				try {
					Object[] logData = {LogUtil.localDateTimeToString(LocalDateTime.now()),AuthUtil.getLoginId(token),AuthUtil.getDepartmentName(token)};
					LogUtil.writeLogToCsv(administrationLoginLogPath, administrationLoginLogHeader, logData);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			LOGGER.info("認証 終了");
		}
	}

	/**
	 * ログアウトAPI
	 * 
	 * @param response HttpServletResponse
	 */
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	@ApiOperation(value = "ログアウト", notes = "ログアウトの実施.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 200, message = "ログアウト完了の場合", response = ResponseEntityForm.class) })
	public void logout(HttpServletResponse response) {
		LOGGER.info("ログアウト 開始");
		try {
			Cookie cookie = new Cookie(AuthUtil.TOKEN, "");
			cookie.setMaxAge(0);
			cookie.setPath("/");
			response.addCookie(cookie);
			response.setStatus(HttpServletResponse.SC_OK);
		} finally {
			LOGGER.info("ログアウト 終了");
		}
	}

	/**
	 * 行政ログイン
	 * 
	 * @param AnswerConfirmLoginForm ログインID,パスワード
	 * @return ApplyAnswerForm 申請情報詳細
	 */
	@RequestMapping(value = "/goverment/login", method = RequestMethod.POST)
	@ApiOperation(value = "行政ログイン", notes = "行政向け画面にログインする.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 201, message = "認証OK", response = ResponseEntityForm.class),
			@ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class),
			@ApiResponse(code = 503, message = "処理エラー", response = ResponseEntityForm.class)})
	public void loginGoverment(HttpServletResponse response, @ApiParam(required = true, value = "行政ログインフォーム")@RequestBody GovermentLoginForm govermentLoginForm) {
		LOGGER.info("行政ログイン 開始");
		try {
			String id = govermentLoginForm.getLoginId();
			String password = govermentLoginForm.getPassword();
			if (id != null && !"".equals(id) //
					|| password != null && !"".equals(password)) {
				List<GovernmentUserForm> userFormList = authenticationService
						.getGovermentUserList(govermentLoginForm.getLoginId(), govermentLoginForm.getPassword());
				if (userFormList.size() == 0) {
					LOGGER.warn("ID、パスワードからユーザ情報が取得できなかった");
					throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
				} else if (userFormList.size() > 1) {
					// ユーザ情報が複数取れるのは異常
					LOGGER.warn("ID、パスワードからユーザ情報が複数取得された");
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
				}

				GovernmentUserForm userForm = userFormList.get(0);
				
				// ユーザ情報設定
				response.addCookie(
						AuthUtil.createUserCookie(userForm.getUserId(),userForm.getLoginId(),userForm.getRoleCode(), userForm.getDepartmentId(),userForm.getDepartmentName(), expireTime));
				response.setStatus(HttpServletResponse.SC_CREATED);
				
			} else {
				// パラメータ不正
				LOGGER.warn("パラメータ不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			LOGGER.error("行政ログイン処理で例外発生", ex);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		} finally {
			LOGGER.info("行政ログイン 終了");
		}
	}
}
