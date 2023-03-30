package developmentpermission.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * O_回答Entityクラス
 */
@Entity
@Data
@Table(name = "o_answer")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Answer implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 回答ID */
	@Id
	@Column(name = "answer_id")
	private Integer answerId;

	/** 申請ID */
	@Column(name = "application_id")
	private Integer applicationId;

	/** 判定項目ID */
	@Column(name = "judgement_id")
	private String judgementId;

	/** 判定結果 */
	@Column(name = "judgement_result")
	private String judgementResult;

	/** 回答内容 */
	@Column(name = "answer_content")
	private String answerContent;
	
	/** 通知テキスト */
	@Column(name = "notified_text")
	private String notifiedText;

	/** 登録日時 */
	@Column(name = "register_datetime")
	private LocalDateTime registerDatetime;

	/** 更新日時 */
	@Column(name = "update_datetime")
	private LocalDateTime updateDatetime;

	/** 完了フラグ */
	@Column(name = "complete_flag", columnDefinition = "char(1)")
	private Boolean completeFlag;
	
	/** 通知フラグ */
	@Column(name = "notified_flag", columnDefinition = "char(1)")
	private Boolean notifiedFlag;

}
