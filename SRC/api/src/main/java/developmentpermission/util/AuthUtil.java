package developmentpermission.util;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Date;

import javax.servlet.http.Cookie;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

/**
 * 認証処理ユーティリティクラス
 */
@Component
public class AuthUtil {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthUtil.class);

	/** ロール: 事業者 */
	public static final String ROLE_BUSINESS = "1";
	/** ロール: 行政 */
	public static final String ROLE_GOVERMENT = "2";

	/** キー: ロール */
	public static final String ROLE_KEY = "X-ROLE";
	/** キー: ユーザID */
	public static final String USERID_KEY = "X-USERID";
	/** キー: ログインID */
	public static final String LOGINID_KEY = "X-LOGINID";
	/** キー: 部署ID */
	public static final String DEPARTMENT_KEY = "X-DEPARTMENT";
	/** キー: 部署名 */
	public static final String DEPARTMENTNAME_KEY = "X-DEPARTMENTNAME";

	/** 認証用キー */
	public static String SECRET_KEY = "secret";
	/** Subject */
	public static final String SUBJECT = "devpermission";
	/** トークンキー */
	public static final String TOKEN = "token";

	@Value("${app.jwt.token.secretkey}")
	public void setSECRET_KEY(String secretKey) {
		this.SECRET_KEY = secretKey;
	}

	/**
	 * ユーザのCookieを生成
	 * 
	 * @param userId         ユーザID
	 * @param loginId        ログインID
	 * @param role           ロール
	 * @param department     部署
	 * @param departmentName 部署名
	 * @return Cookie
	 * @throws IllegalArgumentException     例外
	 * @throws UnsupportedEncodingException 例外
	 */
	public static Cookie createUserCookie(String userId, String loginId, String role, String department,
			String departmentName, int expireTime) throws IllegalArgumentException, UnsupportedEncodingException {
		LOGGER.debug("Cookie生成 開始");
		try {
			String secretKey = SECRET_KEY;
			Date issuedAt = new Date();
			Date notBefore = new Date(issuedAt.getTime());
			Date expiresAt = new Date(issuedAt.getTime() + 1000L * expireTime);

			Algorithm algorithm = Algorithm.HMAC256(secretKey);
			String token = JWT.create()
					// registered claims
					// .withJWTId("jwtId") //"jti" : JWT ID
					// .withAudience("audience") //"aud" : Audience
					// .withIssuer("issuer") //"iss" : Issuer
					.withSubject(SUBJECT) // "sub" : Subject
					.withIssuedAt(issuedAt) // "iat" : Issued At
					.withNotBefore(notBefore) // "nbf" : Not Before
					.withExpiresAt(expiresAt) // "exp" : Expiration Time
					// private claims
					.withClaim(USERID_KEY, userId) //
					.withClaim(LOGINID_KEY, loginId) //
					.withClaim(ROLE_KEY, role) //
					.withClaim(DEPARTMENT_KEY, department) //
					.withClaim(DEPARTMENTNAME_KEY, LogUtil.convertUnicode(departmentName)) //
					.sign(algorithm);

			LOGGER.trace("generate token : " + token);

			Cookie cookie = new Cookie(TOKEN, token);
			cookie.setMaxAge(expireTime);
			cookie.setPath("/");
			return cookie;
		} finally {
			LOGGER.debug("Cookie生成 終了");
		}
	}

	/**
	 * トークン文字列から jwt を復元 アルゴリズムは HMAC256
	 * 
	 * @param token  トークン
	 * @param secret 暗号化キー
	 * @return JWT
	 * @throws Exception                例外
	 * @throws IllegalArgumentException 例外
	 */
	public static DecodedJWT decodeTokenHMAC256(String token, String secret)
			throws IllegalArgumentException, Exception {
		Algorithm algorithm = Algorithm.HMAC256(secret);
		JWTVerifier verifier = JWT.require(algorithm).build();
		DecodedJWT jwt = verifier.verify(token);
		return jwt;
	}

	/**
	 * ユーザーID取得
	 * 
	 * @param token トークン
	 * @return 判定結果
	 */
	public static String getUserId(String token) {
		String userId = null;
		if (token == null || "".equals(token)) {
			return userId;
		}
		try {
			// JWT トークンを検証
			DecodedJWT jwt = AuthUtil.decodeTokenHMAC256(token, AuthUtil.SECRET_KEY);
			if (AuthUtil.SUBJECT.equals(jwt.getSubject())
					&& System.currentTimeMillis() < jwt.getExpiresAt().getTime()) {
				userId = jwt.getClaim(AuthUtil.USERID_KEY).asString();
			}
		} catch (Exception ex) {
			LOGGER.error("ユーザーID取得で例外発生", ex);
			userId = null;
		}
		return userId;
	}

	/**
	 * ログインID取得
	 * 
	 * @param token トークン
	 * @return 判定結果
	 */
	public static String getLoginId(String token) {
		String loginId = null;
		if (token == null || "".equals(token)) {
			return loginId;
		}
		try {
			// JWT トークンを検証
			DecodedJWT jwt = AuthUtil.decodeTokenHMAC256(token, AuthUtil.SECRET_KEY);
			if (AuthUtil.SUBJECT.equals(jwt.getSubject())
					&& System.currentTimeMillis() < jwt.getExpiresAt().getTime()) {
				loginId = jwt.getClaim(AuthUtil.LOGINID_KEY).asString();
			}
		} catch (Exception ex) {
			LOGGER.error("ログインID取得で例外発生", ex);
			loginId = null;
		}
		return loginId;
	}

	/**
	 * ロール取得
	 * 
	 * @param token トークン
	 * @return 判定結果
	 */
	public static String getRole(String token) {
		// 認証結果
		String role = null;
		if (token == null || "".equals(token)) {
			return role;
		}
		try {
			// JWT トークンを検証
			DecodedJWT jwt = AuthUtil.decodeTokenHMAC256(token, AuthUtil.SECRET_KEY);
			if (AuthUtil.SUBJECT.equals(jwt.getSubject())
					&& System.currentTimeMillis() < jwt.getExpiresAt().getTime()) {
				role = jwt.getClaim(AuthUtil.ROLE_KEY).asString();
			}
		} catch (Exception ex) {
			LOGGER.error("ロール取得で例外発生", ex);
			role = null;
		}
		return role;
	}

	/**
	 * 部署ID取得
	 * 
	 * @param token トークン
	 * @return 判定結果
	 */
	public static String getDepartmentId(String token) {
		String departmentId = null;
		if (token == null || "".equals(token)) {
			return departmentId;
		}
		try {
			// JWT トークンを検証
			DecodedJWT jwt = AuthUtil.decodeTokenHMAC256(token, AuthUtil.SECRET_KEY);
			if (AuthUtil.SUBJECT.equals(jwt.getSubject())
					&& System.currentTimeMillis() < jwt.getExpiresAt().getTime()) {
				departmentId = jwt.getClaim(AuthUtil.DEPARTMENT_KEY).asString();
			}
		} catch (Exception ex) {
			LOGGER.error("部署ID取得で例外発生", ex);
			departmentId = null;
		}
		return departmentId;
	}

	/**
	 * 部署名取得
	 * 
	 * @param token トークン
	 * @return 判定結果
	 */
	public static String getDepartmentName(String token) {
		String departmentName = null;
		if (token == null || "".equals(token)) {
			return departmentName;
		}
		try {
			// JWT トークンを検証
			DecodedJWT jwt = AuthUtil.decodeTokenHMAC256(token, AuthUtil.SECRET_KEY);
			if (AuthUtil.SUBJECT.equals(jwt.getSubject())
					&& System.currentTimeMillis() < jwt.getExpiresAt().getTime()) {
				departmentName = jwt.getClaim(AuthUtil.DEPARTMENTNAME_KEY).asString();
				if(departmentName != null) {
					departmentName = LogUtil.convertString(departmentName);
				}
			}
		} catch (Exception ex) {
			LOGGER.error("部署名取得で例外発生", ex);
			departmentName = null;
		}
		return departmentName;
	}

	/**
	 * 認証情報チェック
	 * 
	 * @param token トークン
	 * @return 判定結果
	 */
	public static boolean validate(String token) {
		// 認証結果
		boolean res = false;
		if (token == null || "".equals(token)) {
			return res;
		}
		try {
			// JWT トークンを検証
			DecodedJWT jwt = AuthUtil.decodeTokenHMAC256(token, AuthUtil.SECRET_KEY);
			if (AuthUtil.SUBJECT.equals(jwt.getSubject())
					&& System.currentTimeMillis() < jwt.getExpiresAt().getTime()) {
				res = true;
			}
		} catch (Exception ex) {
			LOGGER.error("認証情報チェックで例外発生", ex);
			res = false;
		}
		return res;
	}

	/**
	 * ハッシュ生成
	 * 
	 * @param text 文字列
	 * @return ハッシュ文字列
	 */
	public static String createHash(String text) {
		// ハッシュ化はSHA3-512を使用
		return DigestUtils.sha3_512Hex(text);
	}

	/**
	 * パスワード生成
	 * 
	 * @param useCharacters 使用する文字
	 * @param length        パスワード長
	 * @return パスワード文字列
	 */
	public static String generatePassword(String useCharacters, int length) {
		SecureRandom random = new SecureRandom();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			int randomIndex = random.nextInt(useCharacters.length());
			sb.append(useCharacters.charAt(randomIndex));
		}
		return sb.toString();
	}

	/**
	 * ハッシュ生成確認用
	 * 
	 * @param args 引数(使用しない)
	 */
	public static void main(String[] args) {
		System.out.println(AuthUtil.createHash("password"));
	}
}
