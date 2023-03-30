package developmentpermission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.GovernmentUser;

/**
 * M_行政ユーザRepositoryインタフェース
 */
@Transactional
@Repository
public interface GovernmentUserRepository extends JpaRepository<GovernmentUser, String> {

	/**
	 * ユーザ情報取得
	 * 
	 * @param id       ログインID
	 * @param password パスワード
	 * @return ユーザ情報
	 */
	@Query(value = "SELECT user_id, login_id, password, role_code, department_id FROM m_government_user WHERE login_id = :loginId AND password = :password ORDER BY user_id ASC", nativeQuery = true)
	List<GovernmentUser> login(@Param("loginId") String id, @Param("password") String password);
}
