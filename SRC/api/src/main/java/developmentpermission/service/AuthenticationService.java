package developmentpermission.service;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import developmentpermission.entity.Department;
import developmentpermission.entity.GovernmentUser;
import developmentpermission.form.GovernmentUserForm;
import developmentpermission.repository.DepartmentRepository;
import developmentpermission.util.AuthUtil;

/**
 * 認証系サービスクラス
 */
@Service
@Transactional
public class AuthenticationService extends AbstractService {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);
	/** M_部署Repositoryインスタンス */
	@Autowired
	protected DepartmentRepository departmentRepository;

	/**
	 * 行政ユーザ情報取得
	 * 
	 * @param loginId  ログインID
	 * @param password パスワード
	 * @return 行政ユーザ情報
	 */
	public List<GovernmentUserForm> getGovermentUserList(String loginId, String password) {
		LOGGER.debug("行政ユーザ情報取得 開始: " + loginId);
		try {
			List<GovernmentUserForm> formList = new ArrayList<GovernmentUserForm>();
			String hash = AuthUtil.createHash(password);
			List<GovernmentUser> userList = governmentUserRepository.login(loginId, hash);
			for (GovernmentUser user : userList) {
				formList.add(getGovernmentUserFormFromEntity(user));
			}
			return formList;
		} finally {
			LOGGER.debug("行政ユーザ情報取得 終了: " + loginId);
		}
	}

	/**
	 * M_行政ユーザEntityをM_行政ユーザフォームに詰めなおす
	 * 
	 * @param entity M_行政ユーザEntity
	 * @return M_行政ユーザフォーム
	 */
	private GovernmentUserForm getGovernmentUserFormFromEntity(GovernmentUser entity) {
		GovernmentUserForm form = new GovernmentUserForm();
		form.setUserId(entity.getUserId());
		form.setLoginId(entity.getLoginId());
		form.setRoleCode(entity.getRoleCode());
		form.setDepartmentId(entity.getDepartmentId());
		if (entity.getDepartmentId() != null) {
			List<Department> departmentList = departmentRepository.getDepartmentListById(entity.getDepartmentId());
			if (departmentList.size() > 0) {
				Department department = departmentList.get(0);
				form.setDepartmentName(department.getDepartmentName());
			}
		}
		return form;
	}
}
