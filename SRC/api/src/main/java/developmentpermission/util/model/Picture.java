package developmentpermission.util.model;

import org.apache.poi.ss.usermodel.Workbook;

import lombok.Getter;
import lombok.Setter;

/**
 * 画像出力定義
 */
@Getter
@Setter
public class Picture implements CellValue {

	/** 画像ファイルパス */
	private String filePath;
	/** 画像データ */
	private byte[] fileData;
	/** 画像種別(Workbook.PICTURE_TYPE_JPEG等) */
	private int type;
	/** シート番号 */
	private int sheet;
	/** 画像開始行 */
	private int startRow;
	/** 画像終了行 */
	private int endRow;
	/** 画像開始列 */
	private int startCol;
	/** 画像終了列 */
	private int endCol;
	/** 画像余白 左 (実際はUnits.EMU_PER_PIXEL * margin_left) */
	private double marginLeft;
	/** 画像余白 上 */
	private double marginTop;
	/** 画像余白 右 */
	private double marginRight;
	/** 画像余白 下 */
	private double marginBottom;

	/**
	 * コンストラクタ
	 */
	public Picture() {
		filePath = null;
		fileData = null;
		// type = Workbook.PICTURE_TYPE_JPEG;
		type = Workbook.PICTURE_TYPE_PNG;
		sheet = 0;
		startRow = 0;
		endRow = 0;
		startCol = 0;
		endCol = 0;
		marginLeft = 0;
		marginTop = 0;
		marginRight = 0;
		marginBottom = 0;
	}
}
