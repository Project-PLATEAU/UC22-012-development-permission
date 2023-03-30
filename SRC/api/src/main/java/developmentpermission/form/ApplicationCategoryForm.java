package developmentpermission.form;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * M_申請区分フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class ApplicationCategoryForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請区分ID */
	@ApiModelProperty(value = "申請区分ID", example = "2001")
	private String id;

	/** 画面ID */
	@ApiModelProperty(value = "画面ID", example = "1001")
	private String screenId;

	/** 昇順 */
	@ApiModelProperty(value = "昇順", example = "1")
	private Integer order;

	/** 選択肢名 */
	@ApiModelProperty(value = "選択肢名", example = "観光開発")
	private String content;

	/** 必須チェック有無 */
	@ApiModelProperty(value = "必須チェック有無", example = "true")
	private Boolean checked;

}
