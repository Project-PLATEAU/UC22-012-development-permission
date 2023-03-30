package developmentpermission.form;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 地番取得フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class GetLotNumberForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 緯度 */
	@ApiModelProperty(value = "緯度", example = "35.4321")
	private String latiude;

	/** 経度 */
	@ApiModelProperty(value = "経度", example = "135.4321")
	private String longitude;
}
