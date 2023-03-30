package developmentpermission.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import developmentpermission.form.LabelForm;
import developmentpermission.form.ResponseEntityForm;
import developmentpermission.service.LabelService;
import developmentpermission.util.AuthUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * ラベルAPIコントローラ
 */
@Api(tags = "ラベル")
@RestController
@RequestMapping("/label")
public class LabelApiController extends AbstractApiController {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(LabelApiController.class);

	/** 事業者ユーザのロール */
	@Value("${app.role.business}")
	private String businessUserRole;
	
	/** ラベルServiceインスタンス */
	@Autowired
	private LabelService labelService;

	/**
	 * 指定した画面のラベル一覧取得
	 * 
	 * @param viewCode 画面コード
	 * @return ラベル一覧
	 */
	@RequestMapping(value = "/{view_code}", method = RequestMethod.GET)
	@ApiOperation(value = "指定した画面のラベル一覧取得", notes = "指定した画面のラベル一覧をJSON形式で取得する.")
	@ResponseBody
	@ApiResponses(value = { 
			@ApiResponse(code = 400, message = "パラメータ不正", response = ResponseEntityForm.class),
			@ApiResponse(code = 403, message = "ロール不適合", response = ResponseEntityForm.class) })
	public List<LabelForm> getLabel(@ApiParam(required = true, value = "画面ID")@PathVariable(value = "view_code") String viewCode,
			@CookieValue(value = "token", required = false) String token) {
		LOGGER.info("ラベル一覧取得 開始");
		try {
			// ロールを取得
			String role = AuthUtil.getRole(token);
			if (role == null || "".equals(role)) {
				role = businessUserRole;
			}
			if (role != null && !"".equals(role)) {
				if (isValidViewCode(viewCode)) {
					List<LabelForm> labelFormList = new ArrayList<LabelForm>();
					LabelForm form = labelService.getLabelByViewCode(viewCode, role);
					labelFormList.add(form);
					return labelFormList;
				} else {
					LOGGER.warn("パラメータ不正");
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
				}
			} else {
				LOGGER.warn("ロール不適合");
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}
		} finally {
			LOGGER.info("ラベル一覧取得 終了");
		}
	}

	/**
	 * ViewCodeが正しいものか判定する
	 * 
	 * @param viewCode 画面コード
	 * @return 判定結果
	 */
	private boolean isValidViewCode(String viewCode) {
		if (viewCode == null || "".equals(viewCode)) {
			LOGGER.warn("画面コードがnullまたは空");
			return false;
		}
		return true;
	}
}
