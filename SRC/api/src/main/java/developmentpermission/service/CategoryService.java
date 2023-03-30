package developmentpermission.service;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import developmentpermission.entity.ApplicationCategoryMaster;
import developmentpermission.entity.ApplicationCategorySelectionView;
import developmentpermission.form.ApplicationCategoryForm;
import developmentpermission.form.ApplicationCategorySelectionViewForm;
import developmentpermission.repository.CategoryRepository;
import developmentpermission.repository.CategorySelectionViewRepository;

/**
 * 申請区分Serviceクラス
 */
@Service
@Transactional
public class CategoryService extends AbstractService {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(CategoryService.class);

	/** M_申請区分選択画面Repositoryインスタンス */
	@Autowired
	private CategorySelectionViewRepository categorySelectionViewRepository;

	/** M_申請区分Repositoryインスタンス */
	@Autowired
	private CategoryRepository categoryRepository;

	/**
	 * 申請区分選択画面一覧取得
	 * 
	 * @return 申請区分選択画面一覧
	 */
	public List<ApplicationCategorySelectionViewForm> getApplicationCategorySelectionViewList() {
		LOGGER.debug("申請区分選択画面一覧取得 開始");
		try {
			List<ApplicationCategorySelectionViewForm> formList = new ArrayList<ApplicationCategorySelectionViewForm>();

			LOGGER.trace("申請区分選択画面リスト取得 開始");
			List<ApplicationCategorySelectionView> viewList = categorySelectionViewRepository
					.getCategorySelectionViewList();
			LOGGER.trace("申請区分選択画面リスト取得 終了");

			for (ApplicationCategorySelectionView view : viewList) {
				ApplicationCategorySelectionViewForm form = getSelectionViewFormFromEntity(view);

				// 画面毎の申請区分リストを取得
				List<ApplicationCategoryForm> applicationCategoryList = new ArrayList<ApplicationCategoryForm>();

				LOGGER.trace("申請区分リスト取得 開始: " + view.getViewId());
				List<ApplicationCategoryMaster> categoryList = categoryRepository
						.getApplicationCategoryList(view.getViewId());
				for (ApplicationCategoryMaster category : categoryList) {
					ApplicationCategoryForm categoryForm = getCategoryFormFromEntity(category);
					applicationCategoryList.add(categoryForm);
				}
				form.setApplicationCategory(applicationCategoryList);
				LOGGER.trace("申請区分リスト取得 終了: " + view.getViewId());

				formList.add(form);
			}

			return formList;
		} finally {
			LOGGER.debug("申請区分選択画面一覧取得 終了");
		}
	}

	/**
	 * M_申請区分EntityをM_申請区分選択画面フォームに詰めなおす
	 * 
	 * @param entity M_申請区分Entity
	 * @return M_申請区分選択画面フォーム
	 */
	private ApplicationCategoryForm getCategoryFormFromEntity(ApplicationCategoryMaster entity) {
		ApplicationCategoryForm form = new ApplicationCategoryForm();
		form.setChecked(false);
		form.setContent(entity.getLabelName());
		form.setId(entity.getCategoryId());
		form.setOrder(entity.getOrder());
		form.setScreenId(entity.getViewId());
		return form;
	}
}
