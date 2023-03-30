package developmentpermission.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

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
 * O_申請Entityクラス
 */
@Entity
@Data
@Table(name = "o_application")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Application implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請ID */
	@Id
	@Column(name = "application_id")
	private Integer applicationId;

	/** 申請者情報ID */
	@Column(name = "applicant_id")
	private Integer applicantId;

	/** ステータス */
	@Column(name = "status")
	private String status;
	
	/** 登録ステータス */
	@Column(name="register_status")
	private String registerStatus;
	
	/** 照合文字列 */
	@Column(name="collation_text")
	private String collationText;

	/** 登録日時 */
	@Column(name = "register_datetime")
	private LocalDateTime registerDatetime;

	/** 更新日時 */
	@Column(name = "update_datetime")
	private LocalDateTime updateDatetime;

}
