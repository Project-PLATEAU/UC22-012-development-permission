package developmentpermission.form;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * M_申請区分選択画面フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class ApplicationCategorySelectionViewForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 画面ID */
	@ApiModelProperty(value = "画面ID", example = "0001")
	private String screenId;

	/** 表示有無 */
	@ApiModelProperty(value = "表示有無", example = "true")
	private boolean enable;

	/** 複数選択有無 */
	@ApiModelProperty(value = "複数選択有無", example = "true")
	private boolean multiple;

	/** 必須有無 */
	@ApiModelProperty(value = "必須有無", example = "true")
	private boolean require;

	/** 画面タイトル */
	@ApiModelProperty(value = "画面タイトル", example = "開発行為")
	private String title;

	/** 画面説明 */
	@ApiModelProperty(value = "画面説明", example = "下記より開発予定行為の選択をお願いします（複数可）。")
	private String explanation;

	/** 申請区分選択肢一覧 */
	@ApiModelProperty(value = "申請区分選択肢一覧")
	private List<ApplicationCategoryForm> applicationCategory;
}
