package developmentpermission.form;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 登録完了通知フォーム
 */
@AllArgsConstructor
@Getter
@Setter
public class ResponseEntityForm implements Serializable {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** ステータス */
	private int status;

	/** メッセージ */
	private String message;
}
