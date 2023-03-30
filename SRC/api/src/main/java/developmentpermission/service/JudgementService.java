package developmentpermission.service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import developmentpermission.dao.CategoryJudgementDao;
import developmentpermission.dao.JudgementLayerDao;
import developmentpermission.dao.LayerDao;
import developmentpermission.entity.CategoryJudgement;
import developmentpermission.entity.ColumnValue;
import developmentpermission.entity.Distance;
import developmentpermission.entity.Layer;
import developmentpermission.entity.Oid;
import developmentpermission.form.ApplicationCategoryForm;
import developmentpermission.form.ApplicationCategorySelectionViewForm;
import developmentpermission.form.GeneralConditionDiagnosisReportRequestForm;
import developmentpermission.form.GeneralConditionDiagnosisRequestForm;
import developmentpermission.form.GeneralConditionDiagnosisResultForm;
import developmentpermission.form.LayerForm;
import developmentpermission.form.LotNumberForm;
import developmentpermission.form.UploadForGeneralConditionDiagnosisForm;
import developmentpermission.repository.jdbc.JudgementJdbc;
import developmentpermission.util.AuthUtil;
import developmentpermission.util.ExportJudgeForm;

/**
 * 概況診断Serviceクラス
 */
@Service
@Transactional
public class JudgementService extends AbstractJudgementService {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(JudgementService.class);

	/** GIS判定:判定無し */
	public static final String GIS_JUDGEMENT_0 = "0";
	/** GIS判定:重なる */
	public static final String GIS_JUDGEMENT_1 = "1";
	/** GIS判定:重ならない */
	public static final String GIS_JUDGEMENT_2 = "2";
	/** GIS判定:バッファに重なる */
	public static final String GIS_JUDGEMENT_3 = "3";
	/** GIS判定:バッファに重ならない */
	public static final String GIS_JUDGEMENT_4 = "4";

	/** 重なり属性表示: なし */
	public static final String DISPLAY_ATTRIBUTE_NONE = "0";
	/** 重なり属性表示: 連結 */
	public static final String DISPLAY_ATTRIBUTE_JOINT = "1";
	/** 重なり属性表示: 改行リピート */
	public static final String DISPLAY_ATTRIBUTE_NEWLINE = "2";

	/** 区分判定:判定なし */
	public static final String CATEGORY_NONE = "0";
	/** 区分1 */
	public static final String CATEGORY_1 = "1";
	/** 区分2 */
	public static final String CATEGORY_2 = "2";
	/** 区分3 */
	public static final String CATEGORY_3 = "3";
	/** 区分4 */
	public static final String CATEGORY_4 = "4";
	/** 区分5 */
	public static final String CATEGORY_5 = "5";
	/** 区分6 */
	public static final String CATEGORY_6 = "6";
	/** 区分7 */
	public static final String CATEGORY_7 = "7";
	/** 区分8 */
	public static final String CATEGORY_8 = "8";
	/** 区分9 */
	public static final String CATEGORY_9 = "9";
	/** 区分10 */
	public static final String CATEGORY_10 = "0";

	/** 概況診断結果画像格納フォルダのランダム文字列に使用する文字 */
	public static final String JUDGEMENT_FOLDER_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	/** 概況診断結果画像格納フォルダのランダム文字列長 */
	public static final int JUDGEMENT_FOLDER_LENGTH = 10;
	/** 概況診断結果画像格納フォルダの日時フォーマット */
	public static final String JUDGEMENT_FOLDER_DATE_FORMAT = "yyyyMMddHHmmssSSS";

	/** 概況表示文言置換ターゲット文字 */
	private static final String DESCRIPTION_REPLACE_TARGET_CHARACTER = "@";
	
	/** 概況表示文言距離置換後文字(申請地範囲内の場合) */
	@Value("${app.category.judgement.attribute.distance.applicationAreaText}")
	private String distanceApplicationAreaText;
	
	/** 概況表示文言距離置換ターゲット文字 */
	@Value("${app.category.judgement.attribute.distance.replaceText}")
	private String distanceReplaceText;
	
	/** 概況表示文言距離置換後文字 */
	@Value("${app.category.judgement.attribute.distance.replacedText}")
	private String distanceReplacedText;
	
	/** 距離算出時に使用するepsg */
	@Value("${app.category.judgement.distance.epsg}")
	private Integer distanceEpsg;

	/** 重なり属性表示フラグが1の場合の属性区切り文字 */
	@Value("${app.category.judgement.attribute.joint}")
	private String descriptionJointCharacter;

	/** 判定JDBCインスタンス */
	@Autowired
	private JudgementJdbc judgementJdbc;
	
	/**
	 * 概況診断結果取得
	 * 
	 * @param generalConditionDiagnosisRequestFrom パラメータ
	 * @return 診断結果
	 */
	public List<GeneralConditionDiagnosisResultForm> executeGeneralConditionDiagnosis(
			GeneralConditionDiagnosisRequestForm generalConditionDiagnosisRequestFrom) {
		LOGGER.debug("概況診断結果取得 開始");
		try {
			JudgementLayerDao judgementLayerDao = new JudgementLayerDao(emf);
			CategoryJudgementDao categoryJudgementDao = new CategoryJudgementDao(emf);

			List<GeneralConditionDiagnosisResultForm> formList = new ArrayList<GeneralConditionDiagnosisResultForm>();

			// 選択区分集約
			Map<String, Set<String>> categoryMap = new HashMap<String, Set<String>>();
			// 各区分単位で初期化
			categoryMap.put(CATEGORY_1, new HashSet<String>());
			categoryMap.put(CATEGORY_2, new HashSet<String>());
			categoryMap.put(CATEGORY_3, new HashSet<String>());
			categoryMap.put(CATEGORY_4, new HashSet<String>());
			categoryMap.put(CATEGORY_5, new HashSet<String>());
			categoryMap.put(CATEGORY_6, new HashSet<String>());
			categoryMap.put(CATEGORY_7, new HashSet<String>());
			categoryMap.put(CATEGORY_8, new HashSet<String>());
			categoryMap.put(CATEGORY_9, new HashSet<String>());
			categoryMap.put(CATEGORY_10, new HashSet<String>());

			if (generalConditionDiagnosisRequestFrom.getApplicationCategories() != null) {
				for (ApplicationCategorySelectionViewForm viewForm : generalConditionDiagnosisRequestFrom
						.getApplicationCategories()) {
					if (viewForm.getApplicationCategory() != null) {
						for (ApplicationCategoryForm categoryForm : viewForm.getApplicationCategory()) {
							String tmpCategoryId = categoryForm.getId();
							String screenId = categoryForm.getScreenId();
							if (!EMPTY.equals(screenId)) {
								// 区分判定(画面IDの末尾1文字(0～9)で区分を振り分け)
								String c = screenId.substring(screenId.length() - 1);
								Set<String> categorySet = categoryMap.get(c);
								// checkedの確認は不要
								if (/* categoryForm.getChecked() && */tmpCategoryId != null
										&& !EMPTY.equals(tmpCategoryId) && !categorySet.contains(tmpCategoryId)) {
									// checkedのIDのみを集約
									categorySet.add(tmpCategoryId);
								}
							}
						}
					}
				}
			}
			// 地番IDリストを取得
			List<Integer> lotNumberList = new ArrayList<Integer>();
			if (generalConditionDiagnosisRequestFrom.getLotNumbers() != null) {
				for (LotNumberForm lotNumberForm : generalConditionDiagnosisRequestFrom.getLotNumbers()) {
					int tmpLotNumber = lotNumberForm.getChibanId();
					if (!lotNumberList.contains(tmpLotNumber)) {
						lotNumberList.add(tmpLotNumber);
					}
				}
			}
			// 区分判定リスト取得categoryJudgementDao
			List<CategoryJudgement> categoryJudgementList = categoryJudgementDao.getCategoryJudgementList();
			// 判定結果IDを採番
			int generalConditionDiagnosisId = judgementJdbc.generateGeneralConditionDiagnosisId();
			// categoryJudgementListで取得を実行すると、何故か処理の先でUPDATE文が実行されてエラーになるので、DAOで実行する
			// List<CategoryJudgement> categoryJudgementList =
			// categoryJudgementRepository.getCategoryJudgementList();

			for (CategoryJudgement categoryJudgement : categoryJudgementList) {
				LOGGER.debug("概況診断判定実行開始 区分判定ID=" + categoryJudgement.getJudgementItemId());
				// 判定結果
				boolean judgeResult = false;
				
				// 重なり属性表示フラグ
				String displayAttributeFlag = categoryJudgement.getDisplayAttributeFlag();
				// テーブル名
				String tableName = categoryJudgement.getTableName();
				// フィールド名
				String fieldName = categoryJudgement.getFieldName();
				// フィールド名配列
				String[] fieldArray = new String[0];
				if (fieldName != null && !fieldName.isEmpty()) {
					fieldArray = fieldName.split(COMMA);
				}

				// 重なり属性用属性値リスト
				List<List<String>> valuesList = new ArrayList<List<String>>();

				// レイヤリスト
				List<Layer> targetLayers = null;
				// DistanceとLayerの紐づけ用Map　距離結果を保持する
				Map<String, Distance> layerDistanceMap = new HashMap<>();
				// oidとLayerの紐づけ用Map　GIS判定の結果を保持する
				Map<String, List<Oid>> layerOidMap = new HashMap<>();
				if (lotNumberList.size() > 0) {
					// 区分判定結果
					boolean categoryJudgeResult = false;
					// GIS判定結果
					boolean gisJudgeResult = true;
					// 区分判定有無
					boolean isCatagoryJudgeExists = isCategoryJudgeExists(categoryJudgement);
					// GIS判定有無
					String gisJudgement = categoryJudgement.getGisJudgement();
					boolean gisJudgeExists = (gisJudgement != null && !GIS_JUDGEMENT_0.equals(gisJudgement));
					// 区分判定を実施
					if (isCatagoryJudgeExists && executeCategoryJudgement(categoryMap, categoryJudgement)) {
						categoryJudgeResult = true;
					}
					// GIS判定を実施
					if (gisJudgeExists) {
						LOGGER.debug(categoryJudgement.getJudgementLayer());
						if (categoryJudgement.getJudgementLayer() != null) {
							// 判定対象レイヤ取得
							targetLayers = getLayers(categoryJudgement.getJudgementLayer());
							for (Layer targetLayer : targetLayers) {
								String layerTableName = targetLayer.getTableName();
								List<Oid> oidList;
								if (GIS_JUDGEMENT_1.equals(gisJudgement) || GIS_JUDGEMENT_2.equals(gisJudgement)) {
									LOGGER.debug("GIS重なり判定開始 区分判定ID=" + categoryJudgement.getJudgementItemId()
											+ " レイヤテーブル名=" + layerTableName);
									// 重なる・重ならない判定(1,2)
									oidList = judgementLayerDao.getIntersectsOid(lotNumberList, layerTableName);
									layerOidMap.put(targetLayer.getLayerId(), oidList);
									if ((GIS_JUDGEMENT_1.equals(gisJudgement) && oidList.size() == 0)
											|| (GIS_JUDGEMENT_2.equals(gisJudgement) && oidList.size() > 0)) {
										gisJudgeResult = false;
										break;
									}
								} else if (GIS_JUDGEMENT_3.equals(gisJudgement)
										|| GIS_JUDGEMENT_4.equals(gisJudgement)) {
									LOGGER.debug("GISバッファ判定開始 区分判定ID=" + categoryJudgement.getJudgementItemId()
											+ " レイヤテーブル名=" + layerTableName);
									// バッファに重なる・重ならない判定(3,4)
									oidList = judgementLayerDao.getBufferIntersectsOid(lotNumberList, layerTableName,
											epsg, categoryJudgement.getBuffer());
									layerOidMap.put(targetLayer.getLayerId(), oidList);
									if ((GIS_JUDGEMENT_3.equals(gisJudgement) && oidList.size() == 0)
											|| (GIS_JUDGEMENT_4.equals(gisJudgement) && oidList.size() > 0)) {
										gisJudgeResult = false;
										break;
									}
								} else {
									LOGGER.error("未対応のGIS判定コード: " + gisJudgement);
									throw new RuntimeException("未対応のGIS判定コード");
								}
								if (displayAttributeFlag != null
										&& !DISPLAY_ATTRIBUTE_NONE.equals(displayAttributeFlag)) {
									// 重なり属性表示フラグが設定されている
									if (oidList.size() > 0 && tableName != null && !tableName.isEmpty()
											&& fieldArray.length > 0) {
										// 該当表示文言用のデータを収集する
										for (int oidIdx = 0; oidIdx < oidList.size(); oidIdx++) {
											Oid tmpOid = oidList.get(oidIdx);
											List<String> values = new ArrayList<String>();
											for (int fieldIdx = 0; fieldIdx < fieldArray.length; fieldIdx++) {
												List<ColumnValue> columnValueList = judgementLayerDao.getColumnValue(
														tableName, fieldArray[fieldIdx], tmpOid.getOid());
												if (columnValueList.size() != 1) {
													LOGGER.error("テーブル値の取得に失敗 テーブル名: " + tableName + ", カラム名: "
															+ fieldArray[fieldIdx] + ", OID: " + tmpOid.getOid());
													throw new RuntimeException("テーブル値の取得に失敗");
												}
												ColumnValue tmpValue = columnValueList.get(0);
												values.add(tmpValue.getVal());
											}
											valuesList.add(values);
										}
									}
								}
							}
						}
					}

					// 区分判定結果とGIS判定結果を結合
					if (isCatagoryJudgeExists && gisJudgeExists) {
						judgeResult = categoryJudgeResult && gisJudgeResult;
					} else if (!isCatagoryJudgeExists && gisJudgeExists) {
						judgeResult = gisJudgeResult;
					} else if (isCatagoryJudgeExists && !gisJudgeExists) {
						judgeResult = categoryJudgeResult;
					} else {
						judgeResult = false;
					}
					LOGGER.debug("概況診断判定実行終了 区分判定ID=" + categoryJudgement.getJudgementItemId() + ", 区分判定有無="
							+ isCatagoryJudgeExists + ", GIS判定有無=" + gisJudgeExists + ", 区分判定結果=" + categoryJudgeResult
							+ ", GIS判定結果=" + gisJudgeResult + ", 判定結果=" + judgeResult);
				} else {
					// 地番が0件のときは判定を実施しない
					LOGGER.info("地番が0件のため、判定を実施しない");
				}
				
				if (judgeResult || categoryJudgement.getNonApplicableDisplayFlag()) {
					// 診断結果が「該当」または「非該当かつ 非該当時表示がtrue」のときデータを追加

					// GIS判定有無
					String gisJudgement = categoryJudgement.getGisJudgement();
					
					//　判定レイヤとの距離
					Double distance = 0.0;
					
					// 判定されていないレイヤーがある場合残りのGIS判定を行い、距離を算出
					// layerOidMapに保持されている場合保持結果を使用すること
					if(targetLayers != null) {
						for (Layer targetLayer : targetLayers) {
							String layerTableName = targetLayer.getTableName();
							List<Oid> oidList;
							if(!layerDistanceMap.containsKey(targetLayer.getLayerId())){
								if (GIS_JUDGEMENT_1.equals(gisJudgement) || GIS_JUDGEMENT_2.equals(gisJudgement)) {
									// 重なる・重ならない判定(1,2)
									if(!layerOidMap.containsKey(targetLayer.getLayerId())){
										LOGGER.debug("GIS重なり判定開始 区分判定ID=" + categoryJudgement.getJudgementItemId()
										+ " レイヤテーブル名=" + layerTableName);
										oidList = judgementLayerDao.getIntersectsOid(lotNumberList, layerTableName);
									}else {
										oidList = layerOidMap.get(targetLayer.getLayerId());
									}
									if (oidList.size() == 0) {
										// 距離を算出
										List<Distance> distanceResult = judgementLayerDao.getDistance(lotNumberList, layerTableName, distanceEpsg);
										if(distanceResult.size() > 0) {
											layerDistanceMap.put(targetLayer.getLayerId(), distanceResult.get(0));
											LOGGER.debug("距離を算出 区分判定ID=" + categoryJudgement.getJudgementItemId()
											+ " レイヤテーブル名=" + layerTableName+ " 距離=" + distanceResult.get(0).getDistance());
										}
										
									}
								} else if (GIS_JUDGEMENT_3.equals(gisJudgement)
										|| GIS_JUDGEMENT_4.equals(gisJudgement)) {
									// バッファに重なる・重ならない判定(3,4)
									if(!layerOidMap.containsKey(targetLayer.getLayerId())){
										LOGGER.debug("GISバッファ判定開始 区分判定ID=" + categoryJudgement.getJudgementItemId()
										+ " レイヤテーブル名=" + layerTableName);
										oidList = judgementLayerDao.getBufferIntersectsOid(lotNumberList, layerTableName,
											epsg, categoryJudgement.getBuffer());
									}else {
										oidList = layerOidMap.get(targetLayer.getLayerId());
									}
									if (oidList.size() == 0) {
										// 距離を算出
										List<Distance> distanceResult = judgementLayerDao.getDistance(lotNumberList, layerTableName, distanceEpsg);
										if(distanceResult.size() > 0) {
											layerDistanceMap.put(targetLayer.getLayerId(), distanceResult.get(0));
											LOGGER.debug("距離を算出 区分判定ID=" + categoryJudgement.getJudgementItemId()
											+ " レイヤテーブル名=" + layerTableName+ " 距離=" + distanceResult.get(0).getDistance());
										}
									}
								} 
							}
						}
					}
					// 全ての算出した距離を昇順でsortし該当すれば最短距離を更新
					if(layerDistanceMap.size() > 0) {
						List<Distance> layerDistanceList = new ArrayList<>(layerDistanceMap.values());
						if(layerDistanceList != null && layerDistanceList.size() > 0 ) {
							layerDistanceList.removeAll(Collections.singleton(null));
							layerDistanceList.sort(Comparator.comparing(Distance::getDistance));
							if(layerDistanceList.size() > 0 && layerDistanceList.get(0) != null && layerDistanceList.get(0).getDistance() >= 0) {
								distance = layerDistanceList.get(0).getDistance();
							}
						}
					}
					
					// レイヤリスト取得
					List<LayerForm> layers = new ArrayList<LayerForm>();

					// 判定対象レイヤ取得
					if (targetLayers != null) {
						layers.addAll(getLayerForms(targetLayers));
					} else if (categoryJudgement.getJudgementLayer() != null) {
						layers.addAll(getLayerForms(getLayers(categoryJudgement.getJudgementLayer())));
					}

					if (categoryJudgement.getSimultaneousDisplayLayer() != null) {
						// 同時表示レイヤ取得
						layers.addAll(getLayerForms(getLayers(categoryJudgement.getSimultaneousDisplayLayer())));
					}

					// 該当表示文言を取得
					String applicableDescription = categoryJudgement.getApplicableDescription();
					// 非該当時は非該当表示文言（非該当表示概要）で該当表示文言（該当表示概要）を上書きする
					if(!judgeResult) {
						if(categoryJudgement.getNonApplicableDescription()!=null) {
							applicableDescription = categoryJudgement.getNonApplicableDescription();
						}else {
							applicableDescription = "";
						}
						if(categoryJudgement.getNonApplicableSummary()!=null) {
							categoryJudgement.setApplicableSummary(categoryJudgement.getNonApplicableSummary());
						}else {
							categoryJudgement.setApplicableSummary("");
						}
					}
					// 該当表示文言の距離表示箇所を置き換え
					int intDistance= 0;
					String distanceText = "";
					String distanceResultText = "";
					if(targetLayers != null && distance!=null) {
						intDistance= (int)Double.parseDouble(distance.toString());
						if(intDistance > 0) {
							distanceResultText = intDistance +"m" + "";
							distanceText = distanceReplacedText+String.format("%,d", intDistance)+"m";
						}else{
							distanceResultText = distanceApplicationAreaText + "";
							distanceText = distanceReplacedText+distanceApplicationAreaText;
						}
						applicableDescription = applicableDescription.replace(distanceReplaceText,distanceText);
					}
					// 置換後文字列で上書き
					categoryJudgement.setApplicableDescription(applicableDescription);
					
					if (displayAttributeFlag != null && !DISPLAY_ATTRIBUTE_NONE.equals(displayAttributeFlag)) {
						// 重なり属性表示フラグが設定されているので、該当表示文言を置換する
						if (DISPLAY_ATTRIBUTE_JOINT.equals(displayAttributeFlag)) {
							// 重なり属性表示: 連結
							applicableDescription = replaceDescriptionJoint(
									applicableDescription, valuesList);
						} else if (DISPLAY_ATTRIBUTE_NEWLINE.equals(displayAttributeFlag)) {
							// 重なり属性表示: 改行リピート
							applicableDescription = replaceDescriptionRepeat(
									applicableDescription, valuesList);
						} else {
							LOGGER.error("未対応の重なり属性フラグ: " + displayAttributeFlag);
							throw new RuntimeException("未対応の重なり属性フラグ");
						}
						// 置換後文字列で上書き
						categoryJudgement.setApplicableDescription(applicableDescription);
					}

					formList.add(
							getGeneralConditionDiagnosisResultFormFromEntity(categoryJudgement, judgeResult, layers, generalConditionDiagnosisId,distanceResultText));
				}
			}
			return formList;
		} finally {
			LOGGER.debug("概況診断結果取得 終了");
		}
	}

	/**
	 * 概況診断結果レポート帳票生成
	 * 
	 * @param generalConditionDiagnosisReportRequestForm リクエストパラメータ
	 * @return 生成帳票
	 * @throws Exception 例外
	 */
	public boolean exportJudgeReportWorkBook(
			GeneralConditionDiagnosisReportRequestForm generalConditionDiagnosisReportRequestForm,
			HttpServletResponse response) {
		LOGGER.debug("概況診断結果レポート帳票生成 開始");
		try {
			Workbook wb = exportJudgeReportWorkBook(generalConditionDiagnosisReportRequestForm);

			if (wb != null) {
				try (OutputStream os = response.getOutputStream()) {
					// ファイルサイズ測定
					int fileSize = -1;
					try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
						wb.write(byteArrayOutputStream);
						fileSize = byteArrayOutputStream.size();
					}

					// 帳票ダウンロード出力
					response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
					response.setHeader("Content-Disposition", "attachment; filename=" + judgeReportFileName);
					response.setContentLength(fileSize);
					wb.write(os);
					os.flush();
				}
			} else {
				return false;
			}

			// 一時フォルダーの削除処理
			deleteTmpFolder(generalConditionDiagnosisReportRequestForm);

			return true;
		} catch (Exception ex) {
			LOGGER.error("概況診断結果レポート帳票生成で例外発生", ex);
			return false;
		} finally {
			LOGGER.debug("概況診断結果レポート帳票生成 終了");
		}
	}

	/**
	 * 一時フォルダ生成(同時アクセスで奇跡的にフォルダ名が被る可能性があるためsynchronizedとしておく)
	 * 
	 * @return 概況診断画像アップロードフォーム
	 */
	public synchronized UploadForGeneralConditionDiagnosisForm getFolderName() {
		LOGGER.debug("一時フォルダ生成 開始");
		try {
			UploadForGeneralConditionDiagnosisForm form = new UploadForGeneralConditionDiagnosisForm();

			boolean createFlg = false;
			while (!createFlg) {
				// 一時フォルダ名は「ランダム文字列_日時文字列」とする
				String randomText = AuthUtil.generatePassword(JUDGEMENT_FOLDER_CHARACTERS, JUDGEMENT_FOLDER_LENGTH);
				SimpleDateFormat df = new SimpleDateFormat(JUDGEMENT_FOLDER_DATE_FORMAT);
				String timeText = df.format(new Date());
				String folderName = randomText + "_" + timeText;

				// 一時フォルダ生成
				String absoluteFolderPath = judgementFolderPath;
				absoluteFolderPath += PATH_SPLITTER + folderName;

				Path directoryPath = Paths.get(absoluteFolderPath);
				if (!Files.exists(directoryPath)) {
					// フォルダがないので生成
					LOGGER.debug("フォルダ生成: " + directoryPath);
					Files.createDirectories(directoryPath);
					form.setFolderName(folderName);
					createFlg = true;
				} else {
					LOGGER.info("フォルダが存在するため生成を再試行: " + directoryPath);
				}
			}

			return form;
		} catch (Exception ex) {
			// RuntimeExceptionで投げないとロールバックされない
			LOGGER.error("一時フォルダ生成で例外発生", ex);
			throw new RuntimeException(ex);
		} finally {
			LOGGER.debug("一時フォルダ生成 終了");
		}
	}

	/**
	 * 概況診断画像アップロード
	 * 
	 * @param uploadForGeneralConditionDiagnosisForm 画像情報
	 */
	public void uploadImageFile(UploadForGeneralConditionDiagnosisForm uploadForGeneralConditionDiagnosisForm) {
		LOGGER.debug("概況診断画像アップロード 開始");
		try {
			String folderName = uploadForGeneralConditionDiagnosisForm.getFolderName();
			boolean currentSituationMapFlg = uploadForGeneralConditionDiagnosisForm.getCurrentSituationMapFlg();
			String judgementId = uploadForGeneralConditionDiagnosisForm.getJudgementId();

			String absoluteFolderPath = judgementFolderPath;
			absoluteFolderPath += PATH_SPLITTER + folderName;

			Path directoryPath = Paths.get(absoluteFolderPath);
			if (!Files.exists(directoryPath)) {
				LOGGER.error("指定フォルダが存在しない: " + absoluteFolderPath);
				throw new RuntimeException("指定フォルダが存在しません");
			}

			if (!currentSituationMapFlg) {
				if (judgementId == null) {
					throw new RuntimeException("区分判定IDがnull");
				}
				absoluteFolderPath += PATH_SPLITTER + judgementId;
			}
			directoryPath = Paths.get(absoluteFolderPath);
			if (!Files.exists(directoryPath)) {
				// フォルダがないので生成
				LOGGER.debug("フォルダ生成: " + directoryPath);
				Files.createDirectories(directoryPath);
			}

			String absoluteFilePath = absoluteFolderPath + PATH_SPLITTER;
			if (currentSituationMapFlg) {
				absoluteFilePath += ExportJudgeForm.OVERVIEW_FILE_NAME;
			} else {
				absoluteFilePath += judgementId + ExportJudgeForm.JUDGEMENT_IMAGE_EXTENTION;
			}
			LOGGER.trace("ファイル出力 開始");
			exportFile(uploadForGeneralConditionDiagnosisForm.getImage(), absoluteFilePath);
			LOGGER.trace("ファイル出力 終了");

		} catch (Exception ex) {
			// RuntimeExceptionで投げないとロールバックされない
			LOGGER.error("概況診断画像アップロードで例外発生", ex);
			throw new RuntimeException(ex);
		} finally {
			LOGGER.debug("概況診断画像アップロード 終了");
		}
	}

	/**
	 * 区分判定処理
	 * 
	 * @param categoryMap       申請区分
	 * @param categoryJudgement 区分判定リスト
	 * @return 判定結果
	 */
	private boolean executeCategoryJudgement(Map<String, Set<String>> categoryMap,
			CategoryJudgement categoryJudgement) {

		if (isContainsCategoryCodes(categoryMap.get(CATEGORY_1), categoryJudgement.getCategory1())
				|| isContainsCategoryCodes(categoryMap.get(CATEGORY_2), categoryJudgement.getCategory2())
				|| isContainsCategoryCodes(categoryMap.get(CATEGORY_3), categoryJudgement.getCategory3())
				|| isContainsCategoryCodes(categoryMap.get(CATEGORY_4), categoryJudgement.getCategory4())
				|| isContainsCategoryCodes(categoryMap.get(CATEGORY_5), categoryJudgement.getCategory5())
				|| isContainsCategoryCodes(categoryMap.get(CATEGORY_6), categoryJudgement.getCategory6())
				|| isContainsCategoryCodes(categoryMap.get(CATEGORY_7), categoryJudgement.getCategory7())
				|| isContainsCategoryCodes(categoryMap.get(CATEGORY_8), categoryJudgement.getCategory8())
				|| isContainsCategoryCodes(categoryMap.get(CATEGORY_9), categoryJudgement.getCategory9())
				|| isContainsCategoryCodes(categoryMap.get(CATEGORY_10), categoryJudgement.getCategory10())) {
			return true;
		}
		return false;
	}

	/**
	 * 区分判定の有無を返す.
	 * 
	 * @param categoryJudgement 区分判定
	 * @return
	 */
	private boolean isCategoryJudgeExists(CategoryJudgement categoryJudgement) {
		if (categoryJudgement.getCategory1().equals(CATEGORY_NONE)
				&& categoryJudgement.getCategory1().equals(CATEGORY_NONE)
				&& categoryJudgement.getCategory2().equals(CATEGORY_NONE)
				&& categoryJudgement.getCategory3().equals(CATEGORY_NONE)
				&& categoryJudgement.getCategory4().equals(CATEGORY_NONE)
				&& categoryJudgement.getCategory5().equals(CATEGORY_NONE)
				&& categoryJudgement.getCategory6().equals(CATEGORY_NONE)
				&& categoryJudgement.getCategory7().equals(CATEGORY_NONE)
				&& categoryJudgement.getCategory8().equals(CATEGORY_NONE)
				&& categoryJudgement.getCategory9().equals(CATEGORY_NONE)
				&& categoryJudgement.getCategory10().equals(CATEGORY_NONE)) {
			return false;
		}
		return true;
	}

	/**
	 * 申請区分リストに指定のコードが含まれるか判定
	 * 
	 * @param categoryList 申請区分リスト
	 * @param codes        区分コード(カンマ区切りのコード文字列)
	 * @return 判定結果
	 */
	private boolean isContainsCategoryCodes(Set<String> categorySet, String codes) {
		if (codes != null && !EMPTY.equals(codes)) {
			// 申請区分リストにコードが含まれるか判定
			String[] codeArray = codes.split(COMMA);
			for (String code : codeArray) {
				String tmpCode = code.trim();
				if (!EMPTY.equals(tmpCode)) {
					if (categorySet.contains(tmpCode)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * レイヤリスト取得
	 * 
	 * @param layerIds レイヤID(カンマ区切りの文字列)
	 * @return レイヤリスト
	 */
	private List<Layer> getLayers(String layerIds) {
		LayerDao layerDao = new LayerDao(emf);
		if (layerIds != null) {
			// レイヤID集約
			List<String> layerIdList = new ArrayList<String>();
			String[] layerIdArray = layerIds.split(COMMA);
			for (String layerId : layerIdArray) {
				if (!EMPTY.equals(layerId)) {
					if (!layerIdList.contains(layerId)) {
						layerIdList.add(layerId);
					}
				}
			}

			if (layerIdList.size() > 0) {
				// レイヤ情報取得
				List<Layer> layerList = layerDao.getLayers(layerIdList);

				// layerRepositoryで取得を何度か繰り返すと、何故か処理の先でUPDATE文が実行されてエラーになるので、DAOで実行する
				// List<Layer> layerList = layerRepository.getLayers(layerIdList);

				return layerList;
			}
		}
		return new ArrayList<Layer>();
	}

	/**
	 * レイヤフォームリスト取得
	 * 
	 * @param layerList レイヤリスト
	 * @return レイヤフォームリスト
	 */
	private List<LayerForm> getLayerForms(List<Layer> layerList) {
		List<LayerForm> layerFormList = new ArrayList<LayerForm>();
		if (layerList != null) {
			for (Layer layer : layerList) {
				layerFormList.add(getLayerFormFromEntity(layer));
			}
		}
		return layerFormList;
	}

	/**
	 * M_区分判定EntityをM_区分判定フォームに詰めなおす
	 * 
	 * @param entity M_区分判定Entity
	 * @return M_区分判定フォーム
	 */
	private GeneralConditionDiagnosisResultForm getGeneralConditionDiagnosisResultFormFromEntity(
			CategoryJudgement entity, boolean judgeResult, List<LayerForm> layers, int generalConditionDiagnosisId, String distanceText) {
		GeneralConditionDiagnosisResultForm form = new GeneralConditionDiagnosisResultForm();
		form.setJudgementId(entity.getJudgementItemId());
		form.setTitle(entity.getTitle());
		form.setResult(judgeResult);
		form.setSummary(entity.getApplicableSummary());
		form.setDescription(entity.getApplicableDescription());
		form.setJudgementLayerDisplayFlag(entity.getNonApplicableLayerDisplayFlag());
		form.setSimultameousLayerDisplayFlag(entity.getSimultaneousDisplayLayerFlag());
		form.setLayers(layers);
		form.setAnswerRequireFlag(entity.getAnswerRequireFlag());
		form.setDefaultAnswer(entity.getDefaultAnswer());
		form.setGeneralConditionDiagnosisResultId(generalConditionDiagnosisId);
		if(distanceText==null || distanceText=="") {
			distanceText = "-";
		}
		form.setDistance(distanceText);
		return form;
	}

	/**
	 * M_レイヤEntityをM_レイヤフォームに詰めなおす
	 * 
	 * @param entity M_レイヤEntity
	 * @return M_レイヤフォーム
	 */
	private LayerForm getLayerFormFromEntity(Layer entity) {
		LayerForm form = new LayerForm();
		form.setLayerId(entity.getLayerId());
		form.setLayerType(entity.getLayerType());
		form.setLayerName(entity.getLayerName());
		form.setLayerCode(entity.getLayerCode());
		form.setLayerQuery(entity.getLayerQuery());
		form.setQueryRequireFlag(entity.getQueryRequireFlag());
		return form;
	}

	/**
	 * 概況表示文言の置換(連結)
	 * 
	 * @param baseText   変換前文字列
	 * @param valuesList 属性値リスト
	 * @return 置換後文字列
	 */
	private String replaceDescriptionJoint(String baseText, List<List<String>> valuesList) {
		String description = baseText;

		int count = 0;
		if (valuesList.size() > 0) {
			List<String> values = valuesList.get(0);
			count = values.size();
		}

		Set<String> addSet = new HashSet<String>();
		for (int c = 0; c < count; c++) {
			String jointText = "";
			for (int listIdx = 0; listIdx < valuesList.size(); listIdx++) {
				List<String> values = valuesList.get(listIdx);
				String tmpText = values.get(c);

				// 重複定義は除く
				if (!addSet.contains(tmpText)) {
					if (jointText.length() > 0) {
						jointText += descriptionJointCharacter;
					}
					jointText += tmpText;
					addSet.add(tmpText);
				}
			}

			description = description.replace(DESCRIPTION_REPLACE_TARGET_CHARACTER + (c + 1), jointText);
		}

		return description;
	}

	/**
	 * 概況表示文言の置換(改行リピート)
	 * 
	 * @param baseText   変換前文字列
	 * @param valuesList 属性値リスト
	 * @return 置換後文字列
	 */
	private String replaceDescriptionRepeat(String baseText, List<List<String>> valuesList) {
		String description = "";

		// 改行コードで分解
		List<String> workList = new ArrayList<String>();
		String[] textArray = baseText.split(CR + LF);
		for (int i = 0; i < textArray.length; i++) {
			String[] textArray2 = textArray[i].split(CR);
			for (int j = 0; j < textArray2.length; j++) {
				String[] textArray3 = textArray2[j].split(LF);
				for (int k = 0; k < textArray3.length; k++) {
					workList.add(textArray3[k]);
				}
			}
		}

		for (String tmpText : workList) {
			if (tmpText.contains(DESCRIPTION_REPLACE_TARGET_CHARACTER)) {
				// @を含むので置換が必要
				String repeatBaseText = tmpText;

				Set<String> addSet = new HashSet<String>();
				for (int i = 0; i < valuesList.size(); i++) {
					String repeatText = repeatBaseText;
					List<String> values = valuesList.get(i);
					for (int j = 0; j < values.size(); j++) {
						repeatText = repeatText.replace(DESCRIPTION_REPLACE_TARGET_CHARACTER + (j + 1), values.get(j));
					}

					// 重複定義は除く
					if (!addSet.contains(repeatText)) {
						if (description.length() != 0) {
							description += CR + LF;
						}
						description += repeatText;
						addSet.add(repeatText);
					}
				}
			} else {
				// 置換の必要がないのでそのまま追加
				if (description.length() != 0) {
					description += CR + LF;
				}
				description += tmpText;
			}
		}
		return description;
	}
}
