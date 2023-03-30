package developmentpermission.form;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * M_申請情報検索結果フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class ApplicationInformationSearchResultHeaderForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 表示カラム名 */
	@ApiModelProperty(value = "表示カラム名", example = "申請者名")
	private String displayColumnName;

	/** 参照タイプ */
	@ApiModelProperty(value = "参照タイプ", example = "1")
	private String referenceType;

	/** レスポンスキー */
	@ApiModelProperty(value = "レスポンスキー", example = "applicantName")
	private String resonseKey;

	/** テーブル幅 */
	@ApiModelProperty(value = "テーブル幅(%)", example = "22.5")
	private Float tableWidth;

	/** 表示順 */
	@ApiModelProperty(value = "表示順", example = "1")
	private Integer displayOrder;

}
