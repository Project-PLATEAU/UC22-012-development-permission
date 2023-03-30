package developmentpermission.form;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * M_申請ファイルフォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class ApplicationFileForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請ファイルID */
	@ApiModelProperty(value = "申請ファイルID", example = "1001")
	private String applicationFileId;

	/** 判定項目ID */
	@ApiModelProperty(value = "判定項目ID", example = "2001")
	private String judgementItemId;

	/** 必須有無 */
	@ApiModelProperty(value = "必須有無", example = "true")
	private Boolean requireFlag;

	/** 申請ファイル名 */
	@ApiModelProperty(value = "申請ファイル名", example = "給排水平面図")
	private String applicationFileName;

	/** アップロードファイル一式 */
	@ApiModelProperty(value = "アップロードファイル一式")
	private List<UploadApplicationFileForm> uploadFileFormList;
}
