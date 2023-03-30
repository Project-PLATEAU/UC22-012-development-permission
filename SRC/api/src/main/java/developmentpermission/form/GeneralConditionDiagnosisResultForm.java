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
 * 概況診断結果フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class GeneralConditionDiagnosisResultForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 区分判定ID */
	@ApiModelProperty(value = "区分判定ID", example = "0001")
	private String judgementId;

	/** タイトル */
	@ApiModelProperty(value = "タイトル", example = "○○市生活環境保全条例（観光")
	private String title;

	/** 結果 */
	@ApiModelProperty(value = "結果", example = "true")
	private Boolean result;

	/** 概要 */
	@ApiModelProperty(value = "概要", example = "○生活環境保護条例（環境）：対応あり")
	private String summary;

	/** 文言 */
	@ApiModelProperty(value = "文言", example = "○生活環境保護条例（環境）：対応あり 〇〇課に相談")
	private String description;

	/** 判定レイヤ表示有無 */
	@ApiModelProperty(value = "判定レイヤ表示有無", example = "true")
	private Boolean judgementLayerDisplayFlag;

	/** 同時表示レイヤ表示有無 */
	@ApiModelProperty(value = "同時表示レイヤ表示有無", example = "true")
	private Boolean simultameousLayerDisplayFlag;

	/** 概況診断結果ID */
	@ApiModelProperty(value = "概況診断結果ID", example = "1")
	private Integer generalConditionDiagnosisResultId;
	
	/** 回答必須フラグ */
	@ApiModelProperty(value = "回答必須フラグ", example = "true")
	private Boolean answerRequireFlag;
	
	/** デフォルト回答 */
	@ApiModelProperty(value = "デフォルト回答", example="判定結果の通り")
	private String defaultAnswer;
	
	/** レイヤ一覧 */
	@ApiModelProperty(value = "レイヤ一覧")
	private List<LayerForm> layers;
	
	/** 距離 */
	@ApiModelProperty(value = "判定レイヤとの距離")
	private String distance;
}
