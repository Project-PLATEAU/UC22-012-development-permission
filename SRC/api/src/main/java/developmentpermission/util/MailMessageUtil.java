package developmentpermission.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import developmentpermission.util.model.MailItem;
import developmentpermission.util.model.MailResultItem;

/**
 * メール文言設定用プロパティ
 */
public class MailMessageUtil {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(MailMessageUtil.class);

	/** キー: 回答確認ID/パスワード通知(事業者向け) 件名 */
	public static final String KEY_BUSSINESS_ACCEPT_SUBJECT = "mail.bussiness.application.accept.subject";
	/** キー: 回答確認ID/パスワード通知(事業者向け) 本文 */
	public static final String KEY_BUSSINESS_ACCEPT_BODY = "mail.bussiness.application.accept.body";
	/** キー: 申請受付通知(行政向け) 件名 */
	public static final String KEY_ACCEPT_SUBJECT = "mail.application.accept.subject";
	/** キー: 申請受付通知(行政向け) 本文 */
	public static final String KEY_ACCEPT_BODY = "mail.application.accept.body";
	/** キー: 回答完了通知(事業者向け) 件名 */
	public static final String KEY_BUSSINESS_FINISH_SUBJECT = "mail.bussiness.answer.finish.subject";
	/** キー: 回答完了通知(事業者向け) 本文 */
	public static final String KEY_BUSSINESS_FINISH_BODY = "mail.bussiness.answer.finish.body";
	/** キー: 回答更新通知(行政向け) 件名 */
	public static final String KEY_UPDATE_SUBJECT = "mail.answer.update.subject";
	/** キー: 回答更新通知(行政向け) 本文 */
	public static final String KEY_UPDATE_BODY = "mail.answer.update.body";
	/** キー: 全部署回答完了通知(行政向け) 件名 */
	public static final String KEY_FINISH_SUBJECT = "mail.answer.finish.subject";
	/** キー: 全部署回答完了通知(行政向け) 本文 */
	public static final String KEY_FINISH_BODY = "mail.answer.finish.body";

	/** キー: 照合ID */
	public static final String KEY_COLLATION_ID = "${照合ID}";
	/** キー: パスワード */
	public static final String KEY_PASSWORD = "${パスワード}";
	/** キー: 申請者氏名 */
	public static final String KEY_NAME = "${申請者氏名}";
	/** キー: メールアドレス */
	public static final String KEY_MAIL_ADDRESS = "${申請者メールアドレス}";
	/** キー: 申請地番 */
	public static final String KEY_LOT_NUMBER = "${申請地番}";
	/** キー: 対象 */
	public static final String KEY_TARGET = "${対象}";
	/** キー: 判定結果 */
	public static final String KEY_RESULT = "${判定結果}";
	/** キー: 申請登録日時 */
	public static final String KEY_TIMESTAMP = "${申請登録日時}";

	/** キー: 繰り返し開始 */
	public static final String KEY_REPEAT_START = "$rep[";
	/** キー: 繰り返し終了 */
	public static final String KEY_REPEAT_END = "]$rep";

	/** プロパティ */
	private static Properties PROP = null;

	/**
	 * コンストラクタ
	 * 
	 * @param resourcePath プロパティファイルのパス
	 * @throws Exception 例外
	 */
	public MailMessageUtil(String resourcePath) throws Exception {
		if (PROP == null) {
			// プロパティ読み込み
			loadProperties(resourcePath);
		}
	}

	/**
	 * プロパティの値を取得する
	 * 
	 * @param key キー
	 * @return 値
	 */
	public String getValue(String key) {
		return PROP.getProperty(key);
	}

	/**
	 * 整形済みの値を取得する
	 * 
	 * @param key キー
	 * @return 値
	 */
	public String getFormattedValue(String key, MailItem mailItem) {
		String text = getValue(key);
		text = replaceAll(text, KEY_COLLATION_ID, mailItem.getId()); // 照合ID
		text = replaceAll(text, KEY_PASSWORD, mailItem.getPassword()); // パスワード
		text = replaceAll(text, KEY_NAME, mailItem.getName()); // 申請者氏名
		text = replaceAll(text, KEY_MAIL_ADDRESS, mailItem.getMailAddress()); // 申請者メールアドレス
		text = replaceAll(text, KEY_LOT_NUMBER, mailItem.getLotNumber()); // 申請地番
		text = replaceAll(text, KEY_TIMESTAMP, mailItem.getTimestamp()); // 申請登録日時

		text = getFormattedRepeatValue(text, mailItem.getResultList());
		return text;
	}

	/**
	 * 整形済みの文字列を取得する
	 * 
	 * @param text       文字列
	 * @param resultList 判定結果リスト
	 * @return 整形済み文字列
	 */
	private String getFormattedRepeatValue(String text, List<MailResultItem> resultList) {
		String tmpText = text;
		boolean repeatFlg = true;
		while (repeatFlg) {
			int repeatStartIndex = tmpText.indexOf(KEY_REPEAT_START);
			int repeatEndIndex = tmpText.indexOf(KEY_REPEAT_END);
			if (repeatStartIndex < 0 && repeatEndIndex < 0) {
				repeatFlg = false;
				continue;
			}
			if (repeatStartIndex > repeatEndIndex) {
				// 定義ミス
				LOGGER.error("繰り返し開始定義より前に繰り返し終了定義がある");
				throw new RuntimeException("繰り返し開始定義より前に繰り返し終了定義がある");
			}

			String repeatBeforeText = tmpText.substring(0, repeatStartIndex);
			String repeatText = tmpText.substring(repeatStartIndex + KEY_REPEAT_START.length(), repeatEndIndex);
			String repeatAfterText = tmpText.substring(repeatEndIndex + KEY_REPEAT_END.length());

			for (int i = 0; i < resultList.size(); i++) {
				MailResultItem resultItem = resultList.get(i);
				String tmpRepeatText = repeatText;
				tmpRepeatText = replaceAll(tmpRepeatText, KEY_TARGET, resultItem.getTarget()); // 対象
				tmpRepeatText = replaceAll(tmpRepeatText, KEY_RESULT, resultItem.getResult()); // 判定結果
				repeatBeforeText += tmpRepeatText;
			}
			repeatBeforeText += repeatAfterText;
			tmpText = repeatBeforeText;
		}
		return tmpText;
	}

	/**
	 * 指定キーの文字を全て置換する
	 * 
	 * @param text         文字列
	 * @param replaceKey   キー
	 * @param replaceValue 置換文字列
	 * @return 置換後文字列
	 */
	private String replaceAll(String text, String replaceKey, String replaceValue) {
		String tmpText = text;
		boolean differenceFlg = true;
		while (differenceFlg) {
			String afterText = tmpText.replace(replaceKey, replaceValue);
			if (tmpText.equals(afterText)) {
				// 変化がない = 全て置き換えした
				differenceFlg = false;
			}
			tmpText = afterText;
		}
		return tmpText;
	}

	/**
	 * プロパティ読み込み
	 * 
	 * @param resourcePath リソースパス
	 * @throws IOException 例外
	 */
	private void loadProperties(String resourcePath) throws Exception {
		try (InputStream is = new FileInputStream(resourcePath);
				InputStreamReader isr = new InputStreamReader(is, "UTF-8");
				BufferedReader reader = new BufferedReader(isr)) {
			PROP = new Properties();
			PROP.load(reader);
		}
	}
}
