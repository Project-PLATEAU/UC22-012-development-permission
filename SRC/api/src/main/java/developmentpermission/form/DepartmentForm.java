package developmentpermission.form;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * M_部署フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class DepartmentForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 部署UID */
	@ApiModelProperty(value = "部署ID", example = "1001")
	private String departmentId;

	/** 部署名 */
	@ApiModelProperty(value = "部署名", example = "環境課")
	private String departmentName;

	/** 選択状態 */
	@ApiModelProperty(value = "選択状態", example = "true")
	private Boolean checked;

	/** メールアドレス */
	@ApiModelProperty(value = "メールアドレス", example = "xxx@xxx.xxx")
	private String mailAddress;
}
