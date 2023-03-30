package developmentpermission.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import developmentpermission.form.ApplicationCategorySelectionViewForm;
import developmentpermission.form.ResponseEntityForm;
import developmentpermission.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * 申請区分APIコントローラ
 */
@Api(tags = "申請区分")
@RestController
@RequestMapping("/category")
public class CategoryApiController extends AbstractApiController {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(CategoryApiController.class);

	/**
	 * 申請区分Serviceインスタンス
	 */
	@Autowired
	private CategoryService categoryService;

	/**
	 * 申請区分画面一覧取得
	 * 
	 * @return 申請区分画面一覧
	 */
	@RequestMapping(value = "/views", method = RequestMethod.GET)
	@ApiOperation(value = "申請区分画面一覧取得", notes = "申請区分画面一覧を選択肢含め取得する.")
	@ResponseBody
	@ApiResponses(value = { @ApiResponse(code = 401, message = "認証エラー", response = ResponseEntityForm.class) })
	public List<ApplicationCategorySelectionViewForm> getCategories() {
		LOGGER.info("申請区分画面一覧取得 開始");
		try {
			List<ApplicationCategorySelectionViewForm> applicationCategoryViewFormList = categoryService
					.getApplicationCategorySelectionViewList();
			return applicationCategoryViewFormList;
		} finally {
			LOGGER.info("申請区分画面一覧取得 終了");
		}
	}
}
