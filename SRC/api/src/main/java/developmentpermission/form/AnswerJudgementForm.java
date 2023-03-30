package developmentpermission.form;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * M_区分判定_回答フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class AnswerJudgementForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 区分判定ID */
	@ApiModelProperty(value = "区分判定ID", example = "0001")
	private String judgementId;

	/** タイトル */
	@ApiModelProperty(value = "タイトル", example = "○○市生活環境保全条例（観光")
	private String title;

	/** 部署 */
	@ApiModelProperty(value = "部署")
	private DepartmentForm department;
}
