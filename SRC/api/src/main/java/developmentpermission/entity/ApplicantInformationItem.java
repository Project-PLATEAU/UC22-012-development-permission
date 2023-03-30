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
 * M_申請者情報項目Entityクラス
 */
@Entity
@Data
@Table(name = "m_applicant_information_item")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ApplicantInformationItem implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請者情報項目ID */
	@Id
	@Column(name = "applicant_information_item_id")
	private String applicantInformationItemId;

	/** 昇順 */
	@Column(name = "display_order")
	private Integer displayOrder;

	/** 表示有無 */
	@Column(name = "display_flag", columnDefinition = "char(1)")
	private Boolean displayFlag;

	/** 必須有無 */
	@Column(name = "require_flag", columnDefinition = "char(1)")
	private Boolean requireFlag;

	/** 項目名 */
	@Column(name = "item_name")
	private String itemName;

	/** 正規表現 */
	@Column(name = "regex")
	private String regex;

	/** メールアドレス */
	@Column(name = "mail_address", columnDefinition = "char(1)")
	private Boolean mailAddress;
	
	/** 検索条件表示有無 */
	@Column(name = "search_condition_flag", columnDefinition = "char(1)")
	private Boolean searchConditionFlag;
}
