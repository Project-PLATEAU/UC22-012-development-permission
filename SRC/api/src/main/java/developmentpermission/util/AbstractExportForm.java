package developmentpermission.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Units;
import org.springframework.stereotype.Component;

import developmentpermission.util.model.CellValue;
import developmentpermission.util.model.Picture;
import developmentpermission.util.model.TextValue;

/**
 * 帳票出力共通処理
 */
@Component
public abstract class AbstractExportForm {

	/** 空文字 */
	public static final String EMPTY = "";
	/** コンマ */
	public static final String COMMA = ",";
	/** パス区切り文字 */
	public static final String PATH_SPLITTER = "/";

	/**
	 * ワークブックにデータを書き込む
	 * 
	 * @param wb       ワークブック
	 * @param formData 出力情報
	 * @throws Exception 例外
	 */
	protected void writeCells(Workbook wb, List<CellValue> formData) throws Exception {
		for (int i = 0; i < formData.size(); i++) {
			if (formData.get(i) instanceof TextValue) {
				writeTextValue(wb, (TextValue) formData.get(i));
			} else if (formData.get(i) instanceof Picture) {
				writePicture(wb, (Picture) formData.get(i));
			} else if (formData.get(i) instanceof developmentpermission.util.model.CellStyle) {
				setStyle(wb, (developmentpermission.util.model.CellStyle) formData.get(i));
			}
		}
	}

	/**
	 * 文字列を出力
	 * 
	 * @param wb        ワークブック
	 * @param textValue 出力情報
	 * @throws Exception 例外
	 */
	protected void writeTextValue(Workbook wb, TextValue textValue) throws Exception {
		final Sheet sheet = wb.getSheetAt(textValue.getSheet());
		Row row1 = sheet.getRow(textValue.getRow());
		if (row1 == null) {
			row1 = sheet.createRow(textValue.getRow());
		}
		Cell cell = row1.getCell(textValue.getCol());
		if (cell == null) {
			cell = row1.createCell(textValue.getCol());
		}
		// リンク文字列を置換
		final String exportValue = textValue.getValue().replace("<a>", "").replace("</a>", "");
		cell.setCellValue(exportValue);
	}

	/**
	 * セルのスタイルを設定
	 * 
	 * @param wb        ワークブック
	 * @param cellStyle 出力情報
	 * @throws Exception 例外
	 */
	protected void setStyle(Workbook wb, developmentpermission.util.model.CellStyle cellStyle)
			throws Exception {
		final Sheet sheet = wb.getSheetAt(cellStyle.getSheet());
		Row row1 = sheet.getRow(cellStyle.getRow());
		if (row1 == null) {
			row1 = sheet.createRow(cellStyle.getRow());
		}
		Cell cell = row1.getCell(cellStyle.getCol());
		if (cell == null) {
			cell = row1.createCell(cellStyle.getCol());
		}

		CellStyle style = wb.createCellStyle();

		// 罫線線種設定
		style.setBorderTop(cellStyle.getBorderTop());
		style.setBorderBottom(cellStyle.getBorderBottom());
		style.setBorderLeft(cellStyle.getBorderLeft());
		style.setBorderRight(cellStyle.getBorderRight());

		// 罫線色設定
		style.setTopBorderColor(cellStyle.getColorTop());
		style.setBottomBorderColor(cellStyle.getColorBottom());
		style.setLeftBorderColor(cellStyle.getColorLeft());
		style.setRightBorderColor(cellStyle.getColorRight());

		// 文字列配置設定
		style.setVerticalAlignment(cellStyle.getValign());
		style.setAlignment(cellStyle.getHalign());

		// 文字列行折り返し設定
		style.setWrapText(cellStyle.isWrap());

		// フォント設定
		if (cellStyle.getFont() != null) {
			style.setFont(cellStyle.getFont());
		}

		cell.setCellStyle(style);
	}

	/**
	 * 行方向の改ページを設定
	 * 
	 * @param wb         ワークブック
	 * @param sheetIndex シート番号
	 * @param rowIndex   行番号
	 */
	protected void setRowBreak(Workbook wb, int sheetIndex, int rowIndex) {
		final Sheet sheet = wb.getSheetAt(sheetIndex);
		sheet.setRowBreak(rowIndex);
	}

	/**
	 * 画像を出力
	 * 
	 * @param wb      ワークブック
	 * @param picture 出力情報
	 * @throws Exception 例外
	 */
	protected void writePicture(Workbook wb, Picture picture) throws Exception {
		InputStream in = null;
		try {
			final Sheet sheet = wb.getSheetAt(picture.getSheet());

			// 画像読み込み
			if (picture.getFileData() != null) {
				in = new ByteArrayInputStream(picture.getFileData());
			} else {
				in = new FileInputStream(picture.getFilePath());
			}
			final byte[] b = IOUtils.toByteArray(in);

			// 画像サイズ取得
			final BufferedImage bImg = ImageIO.read(new ByteArrayInputStream(b));
			final double imgWidth = bImg.getWidth();
			final double imgHeight = bImg.getHeight();
			final double imgRate = imgWidth / imgHeight;

			// セルサイズ取得
			final double cellWidth = getPictureCellWidth(wb, picture);
			final double cellHeight = getPictureRowHeight(wb, picture);
			final double cellRate = cellWidth / cellHeight;

			// セルサイズに合わせて画像の余白を設定
			double allHeightMargin = 4; // 0にすると罫線と被る
			double allWidthMargin = 4; // 0にすると罫線と被る
			if (cellRate >= 1) {
				// セルは横長
				if (imgRate >= cellRate) {
					// セルよりも画像の方が横長 -> 画像の横幅を保持し、上下にマージンを発生させる
					final double afterHeight = imgHeight / (imgWidth / cellWidth);
					allHeightMargin = cellHeight - afterHeight;
				} else {
					// 画像よりセルの方が横長 -> 画像の縦幅を保持し、左右にマージンを発生させる
					final double afterWidth = imgWidth / (imgHeight / cellHeight);
					allWidthMargin = cellWidth - afterWidth;
				}
			} else {
				// セルは縦長
				if (imgRate <= cellRate) {
					// セルよりも画像の方が縦長 -> 画像の縦幅を保持し、左右にマージンを発生させる
					final double afterWidth = imgWidth / (imgHeight / cellHeight);
					allWidthMargin = cellWidth - afterWidth;
				} else {
					// 画像よりセルの方が縦長 -> 画像の横幅を保持し、上下にマージンを発生させる
					final double afterHeight = imgHeight / (imgWidth / cellWidth);
					allHeightMargin = cellHeight - afterHeight;
				}
			}

			setWidthMargin(wb, picture, allWidthMargin);
			setHeightMargin(wb, picture, allHeightMargin);

			// 画像書き込み
			final int pidx = wb.addPicture(b, picture.getType());

			// 画像表示位置を設定する
			final ClientAnchor ca = wb.getCreationHelper().createClientAnchor();
			ca.setCol1(picture.getStartCol()); // 表示位置（開始列）
			ca.setRow1(picture.getStartRow()); // 表示位置（開始行）
			ca.setCol2(picture.getEndCol()); // 表示位置（終了列）
			ca.setRow2(picture.getEndRow()); // 表示位置（終了行）
			ca.setDx1((int) Math.round(Units.EMU_PER_PIXEL * picture.getMarginLeft())); // 余白（左）
			ca.setDy1((int) Math.round(Units.EMU_PER_PIXEL * picture.getMarginTop())); // 余白（上）
			ca.setDx2((int) Math.round(Units.EMU_PER_PIXEL * -picture.getMarginRight())); // 余白（右）
			ca.setDy2((int) Math.round(Units.EMU_PER_PIXEL * -picture.getMarginBottom())); // 余白（下）

			// 画像を挿入する
			sheet.createDrawingPatriarch().createPicture(ca, pidx);
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	/**
	 * 画像出力領域のセル幅を取得
	 * 
	 * @param wb      ワークブック
	 * @param picture 出力情報
	 * @return 幅
	 */
	private double getPictureCellWidth(Workbook wb, Picture picture) {
		final Sheet sheet = wb.getSheetAt(picture.getSheet());
		int sumWidth = 0;
		for (int i = picture.getStartCol(); i < picture.getEndCol(); i++) {
			double columnWidth = getColumnWidthByPixel(sheet, i);
			sumWidth += columnWidth;
		}
		return sumWidth;
	}

	/**
	 * セル幅をピクセル単位に変換
	 * 
	 * @param sheet シート
	 * @param index 列番号
	 * @return 幅
	 */
	private double getColumnWidthByPixel(Sheet sheet, int index) {
		double columnWidth = sheet.getColumnWidth(index);
		if (columnWidth <= 256) {
			columnWidth = Math.round(columnWidth / 28); // なぜか256以下ではこちらという噂(めったに使用されない)
		} else {
			columnWidth = Math.round(columnWidth / 31); // マジックナンバー・・・
		}
		return columnWidth;
	}

	/**
	 * 画像出力領域のセル高さを取得
	 * 
	 * @param wb      ワークブック
	 * @param picture 出力情報
	 * @return 高さ
	 */
	private int getPictureRowHeight(Workbook wb, Picture picture) {
		final Sheet sheet = wb.getSheetAt(picture.getSheet());
		int sumHeight = 0;
		for (int i = picture.getStartRow(); i < picture.getEndRow(); i++) {
			Row row = sheet.getRow(i);
			if (row == null) {
				row = sheet.createRow(i);
			}
			final double rowHeight = row.getHeightInPoints();
			// 1px=0.75point
			sumHeight += rowHeight / 0.75d;
		}
		return sumHeight;
	}

	/**
	 * 画像の横余白を設定する
	 * 
	 * @param wb             ワークブック
	 * @param picture        出力情報
	 * @param allWidthMargin 左右マージンの和
	 */
	private void setWidthMargin(Workbook wb, Picture picture, double allWidthMargin) {
		final Sheet sheet = wb.getSheetAt(picture.getSheet());
		// 左マージン設定
		double margin = allWidthMargin / 2;
		int startCol = picture.getStartCol();
		int endCol = picture.getEndCol();
		for (int i = startCol; i < endCol; i++) {
			double columnWidth = getColumnWidthByPixel(sheet, i);
			if (margin > columnWidth) {
				// このカラムは不使用
				margin -= columnWidth;
				picture.setStartCol(i + 1);
			} else {
				// このカラムに余白設定
				// picture.setMarginLeft(margin * 14); // マジックナンバー・・・
				picture.setMarginLeft(margin);
				break;
			}
		}

		// 右マージン設定
		margin = allWidthMargin / 2;
		startCol = picture.getStartCol();
		endCol = picture.getEndCol();
		for (int i = endCol - 1; i >= startCol; i--) {
			double columnWidth = getColumnWidthByPixel(sheet, i);
			if (margin > columnWidth) {
				margin -= columnWidth;
				// このカラムは不使用
				picture.setEndCol(i);
			} else {
				// このカラムに余白設定
				// picture.setMarginRight(margin * 14); // マジックナンバー・・・
				picture.setMarginRight(margin);
				break;
			}
		}
	}

	/**
	 * 画像の縦余白を設定する
	 * 
	 * @param wb              ワークブック
	 * @param picture         出力情報
	 * @param allHeightMargin 上下マージンの和
	 */
	private void setHeightMargin(Workbook wb, Picture picture, double allHeightMargin) {
		final Sheet sheet = wb.getSheetAt(picture.getSheet());
		// 上マージン設定
		double margin = allHeightMargin / 2;
		int startRow = picture.getStartRow();
		int endRow = picture.getEndRow();
		for (int i = startRow; i < endRow; i++) {
			Row row = sheet.getRow(i);
			if (row == null) {
				row = sheet.createRow(i);
			}
			double rowHeight = row.getHeightInPoints() / 0.79; // マジックナンバー・・・
			if (margin > rowHeight) {
				// このカラムは不使用
				margin -= rowHeight;
				picture.setStartRow(i + 1);
			} else {
				// このカラムに余白設定
				picture.setMarginTop(margin);
				break;
			}
		}

		// 下マージン設定
		margin = allHeightMargin / 2;
		startRow = picture.getStartRow();
		endRow = picture.getEndRow();
		for (int i = endRow - 1; i >= startRow; i--) {
			Row row = sheet.getRow(i);
			if (row == null) {
				row = sheet.createRow(i);
			}
			double rowHeight = row.getHeightInPoints() / 0.79; // マジックナンバー・・・
			if (margin > rowHeight) {
				margin -= rowHeight;
				// このカラムは不使用
				picture.setEndRow(i);
			} else {
				// このカラムに余白設定
				picture.setMarginBottom(margin);
				break;
			}
		}
	}
}
