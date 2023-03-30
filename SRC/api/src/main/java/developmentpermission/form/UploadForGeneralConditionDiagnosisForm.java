package developmentpermission.form;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 概況診断画像アップロードDTO
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter

// 一時フォルダ生成
@Data
public class UploadForGeneralConditionDiagnosisForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	/** 現況図 */
	@ApiModelProperty(value = "現況図フラグ", example = "true")
	private Boolean currentSituationMapFlg;

	/** 一時フォルダ名 */
	@ApiModelProperty(value = "一時フォルダ名", example = "XXXXXXXX")
	private String folderName;

	/** 区分判定ID */
	@ApiModelProperty(value = "区分判定ID", example = "0001")
	private String judgementId;

	/** MultipartFile形式のfile */
	@ApiModelProperty(value = "アップロードファイル")
	MultipartFile image;

}
