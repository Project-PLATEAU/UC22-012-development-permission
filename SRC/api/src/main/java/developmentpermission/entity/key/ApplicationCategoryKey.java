package developmentpermission.entity.key;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

/**
 * O_申請区分Entityの複合キークラス
 */
@Embeddable
@Data
public class ApplicationCategoryKey implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請ID */
	@Column(name = "application_id")
	private Integer applicationId;

	/** 画面ID */
	@Column(name = "view_id")
	private String viewId;

	/** 申請区分ID */
	@Column(name = "category_id")
	private String categoryId;

}
