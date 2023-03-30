package developmentpermission.util.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * メールメッセージアイテム
 */
@Getter
@Setter
public class MailItem {

	/** 照合ID */
	private String id;
	/** パスワード */
	private String password;
	/** 申請者氏名 */
	private String name;
	/** 申請者メールアドレス */
	private String mailAddress;
	/** 申請地番 */
	private String lotNumber;
	/** 申請登録日時 */
	private String timestamp;
	/** 判定結果リスト */
	private List<MailResultItem> resultList;

	/**
	 * コンストラクタ
	 */
	public MailItem() {
		id = "";
		password = "";
		name = "";
		mailAddress = "";
		lotNumber = "";
		timestamp = "";
		resultList = new ArrayList<MailResultItem>();
	}

	/**
	 * 複製
	 */
	public MailItem clone() {
		MailItem tmpItem = new MailItem();
		tmpItem.id = id;
		tmpItem.password = password;
		tmpItem.name = name;
		tmpItem.mailAddress = mailAddress;
		tmpItem.lotNumber = lotNumber;
		tmpItem.timestamp = timestamp;
		tmpItem.resultList = new ArrayList<MailResultItem>();
		if (resultList != null) {
			for (MailResultItem item : resultList) {
				MailResultItem resultItem = new MailResultItem();
				resultItem.setTarget(item.getTarget());
				resultItem.setResult(item.getResult());
				tmpItem.resultList.add(resultItem);
			}
		}
		return tmpItem;
	}
}
