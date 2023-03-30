package developmentpermission.form;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * M_行政ユーザフォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class GovernmentUserForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** ユーザID */
	@ApiModelProperty(value = "ユーザID", example = "0001")
	private String userId;
	
	/** ログインID */
	@ApiModelProperty(value = "ログインID", example = "kankyoka_mri")
	private String loginId;

	/** ロールコード */
	@ApiModelProperty(value = "ロールコード", example = "2")
	private String roleCode;

	/** 部署ID */
	@ApiModelProperty(value = "部署ID", example = "0001")
	private String departmentId;
	
	/** 部署名 */
	@ApiModelProperty(value = "部署名", example = "環境課")
	private String departmentName;
}
