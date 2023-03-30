package developmentpermission.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * M_申請区分選択画面Entityクラス
 */
@Entity
@Data
@Table(name = "m_application_category_selection_view")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ApplicationCategorySelectionView implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 画面ID */
	@Id
	@Column(name = "view_id")
	private String viewId;

	/** 表示有無 */
	@Column(name = "view_flag", columnDefinition = "char(1)")
	private Boolean viewFlag;

	/** 複数選択有無 */
	@Column(name = "multiple_flag", columnDefinition = "char(1)")
	private Boolean multipleFlag;

	/** タイトル */
	@Column(name = "title")
	private String title;

	/** 説明文 */
	@Column(name = "description")
	private String description;

	/** 申請区分リスト */
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "view_id")
	private List<ApplicationCategoryMaster> aplicationCategoryList;

	/** 必須有無 */
	@Column(name = "require_flag", columnDefinition = "char(1)")
	private Boolean requireFlag;
}
