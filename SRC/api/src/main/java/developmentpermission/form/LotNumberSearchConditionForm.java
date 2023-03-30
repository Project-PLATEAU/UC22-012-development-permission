package developmentpermission.form;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 地番検索条件フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class LotNumberSearchConditionForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 町丁目ID */
	@ApiModelProperty(value = "町丁目ID", example = "2001")
	private String districtId;

	/** 地番 */
	@ApiModelProperty(value = "地番(LIKE検索)", example = "20011224")
	private String chiban;
}
