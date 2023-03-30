package developmentpermission.util.model;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import lombok.Getter;
import lombok.Setter;

/**
 * セル罫線出力定義
 */
@Getter
@Setter
public class CellStyle implements CellValue {

	/** 色(左) */
	private short colorLeft;
	/** 色(右) */
	private short colorRight;
	/** 色(上) */
	private short colorTop;
	/** 色(下) */
	private short colorBottom;

	/** 線種(左) */
	private BorderStyle borderLeft;
	/** 線種(右) */
	private BorderStyle borderRight;
	/** 線種(上) */
	private BorderStyle borderTop;
	/** 線種(下) */
	private BorderStyle borderBottom;

	/** 垂直方向配置 */
	private VerticalAlignment valign;
	/** 水平方向配置 */
	private HorizontalAlignment halign;

	/** シート番号 */
	private int sheet;

	/** 行番号 */
	private int row;

	/** 列番号 */
	private int col;

	/** 行折り返し */
	private boolean wrap;

	/** フォント設定 */
	private Font font;

	/**
	 * コンストラクタ
	 */
	public CellStyle() {
		this(0, 0, 0);
	}

	/**
	 * コンストラクタ
	 * 
	 * @param value 値
	 * @param sheet シート
	 * @param row   行
	 * @param col   列
	 */
	public CellStyle(int sheet, int row, int col) {
		this.sheet = sheet;
		this.row = row;
		this.col = col;
		colorLeft = IndexedColors.BLACK.getIndex();
		colorRight = IndexedColors.BLACK.getIndex();
		colorTop = IndexedColors.BLACK.getIndex();
		colorBottom = IndexedColors.BLACK.getIndex();
		borderLeft = BorderStyle.THIN;
		borderRight = BorderStyle.THIN;
		borderTop = BorderStyle.THIN;
		borderBottom = BorderStyle.THIN;
		valign = VerticalAlignment.TOP;
		halign = HorizontalAlignment.LEFT;
		wrap = true;
		font = null;
	}
}
