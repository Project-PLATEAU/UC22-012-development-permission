package developmentpermission.form;

import java.io.Serializable;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * F_地番フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class LotNumberForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 地番ID */
	@ApiModelProperty(value = "地番ID", example = "20012004")
	private Integer chibanId;

	/** 地番 */
	@ApiModelProperty(value = "地番", example = "14-5")
	private String chiban;

	/** 自治体名 */
	/** GETリクエスト時に設定ファイルから取得する項目 */
	@ApiModelProperty(value = "自治体名", example = "海山市")
	private String cityName;

	/** 大字ID */
	@ApiModelProperty(value = "大字ID", example = "2001")
	private String districtId;

	/** 大字名 */
	@ApiModelProperty(value = "大字名", example = "山川町")
	private String districtName;

	/** 大字名かな */
	@ApiModelProperty(value = "大字名かな", example = "やまかわちょう")
	private String districtKana;

	/** 属性情報 */
	@ApiModelProperty(value = "属性情報")
	private Map<String, Object> attributes;

	/** 中心座標(経度) */
	@ApiModelProperty(value = "中心座標（経度）", example = "135.01")
	private Double lon;

	/** 中心座標(緯度) */
	@ApiModelProperty(value = "中心座標（緯度）", example = "35.01")
	private Double lat;

	/** 最小座標(経度) */
	@ApiModelProperty(value = "最小座標（経度）", example = "135.01")
	private Double minlon;

	/** 最小座標(緯度) */
	@ApiModelProperty(value = "最小座標（緯度）", example = "35.01")
	private Double minlat;

	/** 最大座標(経度) */
	@ApiModelProperty(value = "最大座標（経度）", example = "135.01")
	private Double maxlon;

	/** 最大座標(緯度) */
	@ApiModelProperty(value = "最大座標（緯度）", example = "35.01")
	private Double maxlat;

	/** ステータス */
	@ApiModelProperty(value = "ステータス", example = "1")
	private String status;

	/** 申請ID */
	@ApiModelProperty(value = "申請ID", example = "20012004")
	private Integer applicationId;
}
