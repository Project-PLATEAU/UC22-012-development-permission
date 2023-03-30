package developmentpermission.form;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 申請情報検索結果データフォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class ApplicationSearchResultForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請ID */
	@ApiModelProperty(value = "申請ID", example = "1")
	private Integer applicationId;

	/** 検索結果属性情報（O_申請, O_申請者情報, O_申請区分の情報をMap形式で格納.） */
	@ApiModelProperty(value = "属性情報")
	private Map<String, Object> attributes;
	
	/** 地番一覧 */
	@ApiModelProperty(value = "地番一覧")
	private List<LotNumberForm> lotNumbers;
}
