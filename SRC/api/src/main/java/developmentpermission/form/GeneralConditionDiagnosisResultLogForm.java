package developmentpermission.form;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 概況診断結果ログフォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class GeneralConditionDiagnosisResultLogForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	/** アクセス日時 */
	@ApiModelProperty(value = "アクセス日時", example = "2022-08-30T12:56:00.000")
	private String アクセス日時;

	/** 概況診断結果ID */
	@ApiModelProperty(value = "概況診断結果ID", example = "1")
	private Integer 概況診断結果ID;
	
	/** 申請地番一覧 */
	@ApiModelProperty(value = "申請地番一覧")
	private List<String> 申請地番一覧;

	/** 申請区分一覧 */
	@ApiModelProperty(value = "申請区分選択一覧")
	private List<ApplicationCategorySelectionLogForm> 申請区分選択一覧;
	
	/** 区分判定（概況診断結果）一覧 */
	@ApiModelProperty(value = "区分判定（概況診断結果）一覧")
	private List<CategoryJudgementLogForm> 概況診断結果一覧;
}
