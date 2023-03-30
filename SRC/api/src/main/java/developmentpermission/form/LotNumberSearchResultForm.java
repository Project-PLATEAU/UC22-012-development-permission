package developmentpermission.form;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * M_地番検索定義フォーム
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class LotNumberSearchResultForm implements Serializable {
	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	/** 地番検索定義ID */
	@ApiModelProperty(value = "地番検索定義ID", example = "2001")
	private String lotNumberSearchDefinitionId;
	/** 表示順 */
	@ApiModelProperty(value = "表示順", example = "1")
	private Integer displayOrder;
	/** テーブル種別 */
	@ApiModelProperty(value = "テーブル種別", example = "1")
	private String tableType;
	/** 表示カラム名 */
	@ApiModelProperty(value = "表示カラム名", example = "大字")
	private String displayColumnName;
	/** テーブル幅 */
	@ApiModelProperty(value = "テーブル幅(%)", example = "22.5")
	private Float tableWidth;
	/** レスポンスキー */
	@ApiModelProperty(value = "JSONキー", example = "ooaza")
	private String responseKey;
}
