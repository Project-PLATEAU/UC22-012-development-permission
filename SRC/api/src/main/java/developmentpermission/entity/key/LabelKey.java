package developmentpermission.entity.key;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

/**
 * ラベルEntityの複合キー定義クラス
 */
@Embeddable
@Data
public class LabelKey implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** ラベルID */
	@Column(name = "label_id")
	private String labelId;

	/** 画面コード */
	@Column(name = "view_code")
	private String viewCode;
}
