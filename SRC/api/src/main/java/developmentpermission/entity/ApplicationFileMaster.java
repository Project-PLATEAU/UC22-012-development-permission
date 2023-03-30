package developmentpermission.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import developmentpermission.entity.key.ApplicationFileKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * M_申請ファイルEntityクラス
 */
@Entity
@Data
@Table(name = "m_application_file")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@IdClass(value = ApplicationFileKey.class)
public class ApplicationFileMaster implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請ファイルID */
	@Id
	@Column(name = "application_file_id")
	private String applicationFileId;

	/** 判定項目ID */
	@Id
	@Column(name = "judgement_item_id")
	private String judgementItemId;

	/** 必須有無 */
	@Column(name = "require_flag", columnDefinition = "char(1)")
	private Boolean requireFlag;

	/** アップロードファイル名 */
	@Id
	@Column(name = "upload_file_name")
	private String uploadFileName;

}
