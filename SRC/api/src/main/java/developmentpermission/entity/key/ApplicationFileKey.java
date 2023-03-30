package developmentpermission.entity.key;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

/**
 * 申請ファイルEntityの複合キー定義クラス
 */
@Embeddable
@Data
public class ApplicationFileKey implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 申請ファイルID */
	@Column(name = "application_file_id")
	private String applicationFileId;

	/** 判定項目ID */
	@Column(name = "judgement_item_id")
	private String judgementItemId;
}
