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
 * M_申請区分Entityクラス
 */
@Entity
@Data
@Table(name = "m_application_category")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ApplicationCategoryMaster implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請区分ID */
	@Id
	@Column(name = "category_id")
	private String categoryId;

	/** 画面ID */
	@Column(name = "view_id")
	private String viewId;

	/** 昇順 */
	@Column(name = "\"order\"")
	private Integer order;

	/** 選択肢名 */
	@Column(name = "label_name")
	private String labelName;
}
