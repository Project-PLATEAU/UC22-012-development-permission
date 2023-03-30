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
 * M_区分判定ログフォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class CategoryJudgementLogForm implements Serializable {
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	/** 区分判定ID */
	@ApiModelProperty(value = "区分判定ID", example = "0001")
	private String 区分判定ID;

	/** タイトル */
	@ApiModelProperty(value = "タイトル", example = "○○市生活環境保全条例（観光")
	private String タイトル;

	/** 概要 */
	@ApiModelProperty(value = "概要", example = "○生活環境保護条例（環境）：対応あり")
	private String 概要;

	/** 文言 */
	@ApiModelProperty(value = "文言", example = "○生活環境保護条例（環境）：対応あり 〇〇課に相談")
	private String 文言;
}
