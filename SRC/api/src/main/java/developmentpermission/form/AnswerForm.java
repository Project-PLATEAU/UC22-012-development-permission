package developmentpermission.form;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 回答情報フォーム
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class AnswerForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 回答ID */
	@ApiModelProperty(value = "回答ID", example = "1")
	private Integer answerId;

	/** 編集可否 */
	@ApiModelProperty(value = "編集可否（事業者の場合常にfalse）", example = "true")
	private Boolean editable;

	/** 判定結果 */
	@ApiModelProperty(value = "判定結果", example = "相談必要")
	private String judgementResult;

	/** 回答内容 */
	@ApiModelProperty(value = "回答内容", example = "xxxxxxx")
	private String answerContent;

	/** 更新日時 */
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn")
	@ApiModelProperty(value = "更新日時", example = "2022-08-30T12:56:00.000")
	private LocalDateTime updateDatetime;

	/** 完了フラグ */
	@ApiModelProperty(value = "完了フラグ", example = "true")
	private Boolean CompleteFlag;
	
	/** 通知フラグ */
	@ApiModelProperty(value = "通知フラグ", example = "true")
	private Boolean notifiedFlag;

	/** 区分判定情報 */
	@ApiModelProperty(value = "区分判定情報")
	private AnswerJudgementForm judgementInformation;

	/** 回答ファイル */
	@ApiModelProperty(value = "回答ファイル")
	private List<AnswerFileForm> answerFiles;
}
