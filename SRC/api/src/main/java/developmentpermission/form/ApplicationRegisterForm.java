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
 * 申請登録リクエストフォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class ApplicationRegisterForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 一時フォルダ名*/
	@ApiModelProperty(value = "一時フォルダ名", example = "XXXXXXXX")
	private String folderName;
	
	/** 申請地番一覧 */
	@ApiModelProperty(value = "申請地番一覧")
	private List<LotNumberForm> lotNumbers;

	/** 申請区分選択一覧 */
	@ApiModelProperty(value = "申請区分選択一覧")
	private List<ApplicationCategorySelectionViewForm> applicationCategories;

	/** 申請者情報一覧 */
	@ApiModelProperty(value = "申請者情報一覧")
	private List<ApplicantInformationItemForm> applicantInformationItemForm;

	/** 申請ファイル一覧 */
	@ApiModelProperty(value = "申請ファイル一覧")
	private List<ApplicationFileForm> applicationFileForm;
	
	/** 区分判定（概況診断結果）一覧 */
	@ApiModelProperty(value = "区分判定（概況診断結果）一覧")
	private List<GeneralConditionDiagnosisResultForm> generalConditionDiagnosisResultForm;
}
