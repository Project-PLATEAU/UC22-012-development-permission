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
 * M_部署Entiryクラス
 */
@Entity
@Data
@Table(name = "m_department")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Department implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 部署ID */
	@Id
	@Column(name = "department_id")
	private String departmentId;

	/** 部署名 */
	@Column(name = "department_name")
	private String departmentName;

	/** 回答権限フラグ */
	@Column(name = "answer_authority_flag", columnDefinition = "char(1)")
	private Boolean answerAuthorityFlag;
	
	/** メールアドレス */
	@Column(name = "mail_address")
	private String mailAddress;

}
