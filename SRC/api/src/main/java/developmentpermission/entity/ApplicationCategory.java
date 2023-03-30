package developmentpermission.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import developmentpermission.entity.key.ApplicationCategoryKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * O_申請区分Entityクラス
 */
@Entity
@Data
@Table(name = "o_application_category")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@IdClass(value = ApplicationCategoryKey.class)
public class ApplicationCategory implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請ID */
	@Id
	@Column(name = "application_id")
	private Integer applicationId;

	/** 画面ID */
	@Id
	@Column(name = "view_id")
	private String viewId;

	/** 申請区分ID */
	@Id
	@Column(name = "category_id")
	private String categoryId;

}
