package developmentpermission.util.model;

import lombok.Getter;
import lombok.Setter;

/**
 * メールメッセージ対象アイテム
 *
 */
@Getter
@Setter
public class MailResultItem {

	/** 対象 */
	private String target;
	/** 判定結果 */
	private String result;

	/**
	 * コンストラクタ
	 */
	public MailResultItem() {
		target = "";
		result = "";
	}
}
