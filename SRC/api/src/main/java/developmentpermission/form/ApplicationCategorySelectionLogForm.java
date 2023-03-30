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
 * 申請区分ログフォーム
 * 
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class ApplicationCategorySelectionLogForm  implements Serializable{
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	/** 画面ID */
	@ApiModelProperty(value = "画面ID", example = "0001")
	private String 画面ID;
	/** 画面タイトル */
	@ApiModelProperty(value = "画面タイトル", example = "開発行為")
	private String 申請区分選択項目;
	/** 申請区分選択肢一覧 */
	@ApiModelProperty(value = "申請区分選択肢一覧")
	private List<String> 申請区分;
}
