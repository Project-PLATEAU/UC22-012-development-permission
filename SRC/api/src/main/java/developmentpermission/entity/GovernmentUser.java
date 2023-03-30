package developmentpermission.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * M_行政ユーザEntiryクラス
 */
@Entity
@Data
@Table(name = "m_government_user")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GovernmentUser implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** ユーザID */
	@Id
	@Column(name = "user_id")
	private String userId;

	/** ログインID */
	@Column(name = "login_id")
	private String loginId;

	/** パスワード */
	@Column(name = "password")
	private String password;

	/** ロールコード */
	@Column(name = "role_code")
	private String roleCode;

	/** 部署ID */
	@Column(name = "department_id")
	private String departmentId;
}
