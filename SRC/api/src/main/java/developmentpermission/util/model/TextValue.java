package developmentpermission.util.model;

import lombok.Getter;
import lombok.Setter;

/**
 * セル出力定義
 */
@Getter
@Setter
public class TextValue implements CellValue {

	/** 値 */
	private String value;

	/** シート番号 */
	private int sheet;

	/** 行番号 */
	private int row;

	/** 列番号 */
	private int col;

	/**
	 * コンストラクタ
	 */
	public TextValue() {
		value = null;
		sheet = 0;
		row = 0;
		col = 0;
	}

	/**
	 * コンストラクタ
	 * 
	 * @param value 値
	 * @param sheet シート
	 * @param row   行
	 * @param col   列
	 */
	public TextValue(String value, int sheet, int row, int col) {
		this.value = value;
		this.sheet = sheet;
		this.row = row;
		this.col = col;
	}
}
