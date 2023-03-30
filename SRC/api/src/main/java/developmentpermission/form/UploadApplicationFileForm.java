package developmentpermission.form;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * O_申請ファイルファイルフォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class UploadApplicationFileForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** ファイルID */
	@ApiModelProperty(value = "ファイルID", example = "1")
	private Integer fileId;

	/** 申請ID */
	@ApiModelProperty(value = "申請ID", example = "1")
	private Integer applicationId;

	/** 申請ファイルID */
	@ApiModelProperty(value = "申請ファイルID", example = "1001")
	private String applicationFileId;

	/** アップロードファイル名 */
	@ApiModelProperty(value = "アップロードファイル名", example = "xxx.pdf")
	private String uploadFileName;

	/** ファイルパス */
	@ApiModelProperty(value = "ファイルパス", example = "xxx/xxxx/")
	private String filePath;
	
	/** アップロードファイル */
	@ApiModelProperty(value = "アップロードファイル")
	private MultipartFile uploadFile;
	
	/** ログインID */
	@ApiModelProperty(value = "ログインID", example = "00111223344")
	private String loginId;

	/** パスワード */
	@ApiModelProperty(value = "パスワード", example = "password")
	private String password;
}
