package developmentpermission.form;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 町丁目名フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class DistrictNameForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 町丁目ID */
	@ApiModelProperty(value = "町丁目ID", example = "0001")
	private String id;

	/** 町丁目名 */
	@ApiModelProperty(value = "町丁目名", example = "AA町")
	private String name;

	/** 町丁目名(かな) */
	@ApiModelProperty(value = "町丁目名（かな）", example = "えーえーちよう")
	private String kana;
}
