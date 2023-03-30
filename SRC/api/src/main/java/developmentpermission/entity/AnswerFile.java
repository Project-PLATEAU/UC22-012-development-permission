package developmentpermission.entity;

import java.io.Serializable;

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
 * O_回答ファイルEntityクラス
 */
@Entity
@Data
@Table(name = "o_answer_file")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AnswerFile implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 回答ファイルID */
	@Id
	@Column(name = "answer_file_id")
	private Integer answerFileId;

	/** 回答ID */
	@Column(name = "answer_id")
	private Integer answerId;

	/** 回答ファイル名 */
	@Column(name = "answer_file_name")
	private String answerFileName;

	/** ファイルパス */
	@Column(name = "file_path")
	private String filePath;

	/** 通知済みファイルパス */
	@Column(name = "notified_file_path")
	private String notifiedFilePath;

	/** 削除未通知フラグ */
	@Column(name = "delete_unnotified_flag", columnDefinition = "char(1)")
	private Boolean deleteUnnotifiedFlag;

}
