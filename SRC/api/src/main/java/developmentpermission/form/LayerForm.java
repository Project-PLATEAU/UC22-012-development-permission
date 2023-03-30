package developmentpermission.form;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * M_レイヤフォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class LayerForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** レイヤID */
	@ApiModelProperty(value = "レイヤID", example = "0001")
	private String layerId;

	/** レイヤ種別 */
	@ApiModelProperty(value = "レイヤ種別（true=判定対象レイヤ false=関連レイヤ）", example = "true")
	private Boolean layerType;

	/** レイヤ名 */
	@ApiModelProperty(value = "レイヤ名", example = "自然公園")
	private String layerName;

	/** レイヤコード */
	@ApiModelProperty(value = "レイヤコード（GeoServerのレイヤID）", example = "layer:sample")
	private String layerCode;

	/** レイヤクエリ */
	@ApiModelProperty(value = "レイヤクエリ", example = "district=?")
	private String layerQuery;

	/** クエリ必須フラグ */
	@ApiModelProperty(value = "クエリ必須フラグ", example = "true")
	private Boolean queryRequireFlag;
}
