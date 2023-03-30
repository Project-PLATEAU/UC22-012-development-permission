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
 * O_申請者情報Entityクラス
 */
@Entity
@Data
@Table(name = "o_applicant_information")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ApplicantInformation implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請ID */
	@Column(name = "application_id")
	private Integer applicationId;

	/** 申請者情報ID */
	@Id
	@Column(name = "applicant_id")
	private Integer applicantId;

	/** 項目1 */
	@Column(name = "item_1")
	private String item1;

	/** 項目2 */
	@Column(name = "item_2")
	private String item2;

	/** 項目3 */
	@Column(name = "item_3")
	private String item3;

	/** 項目4 */
	@Column(name = "item_4")
	private String item4;

	/** 項目5 */
	@Column(name = "item_5")
	private String item5;

	/** 項目6 */
	@Column(name = "item_6")
	private String item6;

	/** 項目7 */
	@Column(name = "item_7")
	private String item7;

	/** 項目8 */
	@Column(name = "item_8")
	private String item8;

	/** 項目9 */
	@Column(name = "item_9")
	private String item9;

	/** 項目10 */
	@Column(name = "item_10")
	private String item10;

	/** メールアドレス */
	@Column(name = "mail_address")
	private String mailAddress;

	/** 照合ID */
	@Column(name = "collation_id")
	private String collationId;

	/** パスワード */
	@Column(name = "password")
	private String password;

}
