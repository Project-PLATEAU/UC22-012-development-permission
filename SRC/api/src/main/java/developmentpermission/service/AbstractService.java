package developmentpermission.service;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.transaction.Transactional;

import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CaseFormat;

import developmentpermission.dao.ApplicationDao;
import developmentpermission.entity.ApplicationCategoryMaster;
import developmentpermission.entity.ApplicationCategorySelectionView;
import developmentpermission.entity.Department;
import developmentpermission.entity.LotNumberAndDistrict;
import developmentpermission.entity.LotNumberSearchResultDefinition;
import developmentpermission.form.ApplicationCategoryForm;
import developmentpermission.form.ApplicationCategorySelectionViewForm;
import developmentpermission.form.DepartmentForm;
import developmentpermission.form.LotNumberForm;
import developmentpermission.form.StatusForm;
import developmentpermission.repository.DepartmentRepository;
import developmentpermission.repository.GovernmentUserRepository;
import developmentpermission.util.MailMessageUtil;
import developmentpermission.util.MailSendUtil;
import developmentpermission.util.model.MailItem;

/**
 * 共通Serviceクラス
 */
@Service
@Transactional
public abstract class AbstractService {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractService.class);

	/** 空文字 */
	public static final String EMPTY = "";
	/** コンマ */
	public static final String COMMA = ",";
	/** パス区切り文字 */
	public static final String PATH_SPLITTER = "/";
	/** 改行文字(LF) */
	public static final String LF = "\n";
	/** 改行文字(CR) */
	public static final String CR = "\r";

	/** 回答ステータス: 申請中 */
	public static final String STATE_APPLYING = "0";
	/** 回答ステータス: 回答中 */
	public static final String STATE_ANSWERING = "1";
	/** 回答ステータス: 回答完了 */
	public static final String STATE_ANSWERED = "2";
	/** 回答ステータス: 通知済み */
	public static final String STATE_NOTIFIED = "3";

	/** 登録ステータス: 仮申請中 */
	public static final String STATE_PROVISIONAL = "0";
	/** 登録ステータス: 申請済 */
	public static final String STATE_APPLIED = "1";

	/** EPSG(初期値:4612) */
	@Value("${app.epsg:4612}")
	protected int epsg; // 変数をstaticにすると@Valueで値が設定されなくなるので注意
	/** 地理座標系用EPSG(初期値:4612) */
	@Value("${app.lonlat.epsg:4612}")
	protected int lonlatEpsg;

	/** 市町村名 */
	@Value("${app.city.name}")
	protected String cityName;

	/** ステータス定義JSON */
	@Value("${app.def.status}")
	protected String statusJSON;

	/** 範囲選択時地番取得上限 */
	@Value("${app.lotnumber.getfigure.limit}")
	protected int figureLotNumberLimit;

	/** テーブル種別: 地番 */
	@Value("${app.lotnumber.result.type.lotnumber:1}")
	protected String typeLotNumber;
	/** テーブル種別: 大字 */
	@Value("${app.lotnumber.result.type.district:0}")
	protected String typeDistrict;

	/** ファイル管理rootパス */
	@Value("${app.file.rootpath}")
	protected String fileRootPath;
	/** 申請ファイル管理フォルダパス */
	@Value("${app.file.application.folder}")
	protected String applicationFolderName;
	/** 回答ファイル管理フォルダパス */
	@Value("${app.file.answer.folder}")
	protected String answerFolderName;
	/** 概況診断画像管理フォルダパス */
	@Value("${app.file.judgement.folder}")
	protected String judgementFolderPath;

	/** メール通知系定義プロパティパス */
	@Value("${app.mail.properties.path}")
	protected String mailPropertiesPath;
	/** メールの申請登録日時フォーマット */
	@Value("${app.mail.accept.timestamp.format}")
	protected String mailTimestampFormat;

	/** O_申請者情報の氏名を格納したアイテム番号 */
	@Value("${app.applicant.name.item.number:1}")
	protected int applicantNameItemNumber;

	/** メール送信ユーティリティ */
	@Autowired
	MailSendUtil mailSendutil;

	/** メールメッセージ定義ユーティリティ */
	private MailMessageUtil mailUtil = null;

	/** Entityマネージャファクトリ */
	@Autowired
	protected EntityManagerFactory emf; // DAOでは@Autowiredが効かないのでここで定義...

	/** M_部署Repositoryインスタンス */
	@Autowired
	protected DepartmentRepository departmentRepository;
	/** M_行政ユーザRepositoryインスタンス */
	@Autowired
	protected GovernmentUserRepository governmentUserRepository;

	/**
	 * ステータスフォームリストを取得
	 * 
	 * @return ステータスフォームリスト
	 * @throws Exception 例外
	 */
	public List<StatusForm> getStatusList() throws Exception {
		LOGGER.trace("ステータスフォームリスト取得 開始");
		try {
			List<StatusForm> formList = new ArrayList<StatusForm>();
			Map<String, String> statusMap = getStatusMap();
			for (Map.Entry<String, String> entry : statusMap.entrySet()) {
				StatusForm form = new StatusForm();
				form.setValue(entry.getKey());
				form.setText(entry.getValue());
				form.setChecked(false);
				formList.add(form);
			}
			return formList;
		} finally {
			LOGGER.trace("ステータスフォームリスト取得 終了");
		}
	}

	/**
	 * M_部署一覧を取得
	 * 
	 * @return M_部署一覧
	 */
	public List<DepartmentForm> getDepartmentList() {
		LOGGER.trace("M_部署一覧取得 開始");
		try {
			List<DepartmentForm> formList = new ArrayList<DepartmentForm>();
			List<Department> departmentList = departmentRepository.getDepartmentList();
			for (Department department : departmentList) {
				formList.add(getDepartmentFormFromEntity(department));
			}
			return formList;
		} finally {
			LOGGER.trace("M_部署一覧取得 終了");
		}
	}

	/**
	 * ステータス定義を取得
	 * 
	 * @return ステータス定義
	 * @throws Exception 例外
	 */
	protected Map<String, String> getStatusMap() throws Exception {
		LOGGER.trace("ステータス定義取得 開始");
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, String> map = objectMapper.readValue(statusJSON,
					new TypeReference<LinkedHashMap<String, String>>() {
					});
			return map;
		} finally {
			LOGGER.trace("ステータス定義取得 終了");
		}
	}

	/**
	 * リストの要素をコンマで決号した文字列にして返す
	 * 
	 * @param list リスト
	 * @return 結合文字列
	 */
	protected String joinList(List<Object> list) {
		StringBuffer sb = new StringBuffer();
		for (Object obj : list) {
			if (obj != null) {
				if (sb.length() > 0) {
					sb.append(COMMA);
				}
				sb.append(obj.toString());
			}
		}
		return sb.toString();
	}

	/**
	 * F_部署EntityをF_部署フォームに詰めなおす
	 * 
	 * @param entity M_部署Entity
	 * @return M_部署フォーム
	 */
	protected DepartmentForm getDepartmentFormFromEntity(Department entity) {
		DepartmentForm form = new DepartmentForm();
		form.setDepartmentId(entity.getDepartmentId());
		form.setDepartmentName(entity.getDepartmentName());
		form.setChecked(false);
		form.setMailAddress(entity.getMailAddress());
		return form;
	}

	/**
	 * M_申請区分選択画面EntityをM_申請区分選択画面フォームに詰めなおす
	 * 
	 * @param entity M_申請区分選択画面Entity
	 * @return M_申請区分選択画面フォーム
	 */
	protected ApplicationCategorySelectionViewForm getSelectionViewFormFromEntity(
			ApplicationCategorySelectionView entity) {
		ApplicationCategorySelectionViewForm form = new ApplicationCategorySelectionViewForm();
		form.setEnable(entity.getViewFlag());
		form.setExplanation(entity.getDescription());
		form.setMultiple(entity.getMultipleFlag());
		form.setRequire(entity.getRequireFlag());
		form.setScreenId(entity.getViewId());
		form.setTitle(entity.getTitle());
		return form;
	}

	/**
	 * M_申請区分選択画面EntityをM_申請区分選択画面フォームに詰めなおす
	 * 
	 * @param entity M_申請区分選択画面Entity
	 * @return M_申請区分選択画面フォーム
	 */
	protected ApplicationCategorySelectionViewForm getSelectionViewFormFromEntity(
			ApplicationCategorySelectionView entity, int applicationId) {
		ApplicationCategorySelectionViewForm form = new ApplicationCategorySelectionViewForm();
		form.setEnable(entity.getViewFlag());
		form.setExplanation(entity.getTitle());
		form.setMultiple(entity.getMultipleFlag());
		form.setRequire(entity.getRequireFlag());
		form.setScreenId(entity.getViewId());
		form.setTitle(entity.getTitle());

		List<ApplicationCategoryForm> categoryFormList = new ArrayList<ApplicationCategoryForm>();

		ApplicationDao dao = new ApplicationDao(emf);
		List<ApplicationCategoryMaster> categoryList = dao.getApplicationCategoryMasterList(applicationId,
				entity.getViewId());
		for (ApplicationCategoryMaster category : categoryList) {
			categoryFormList.add(getApplicationCategoryFormFromEntity(category));
		}
		form.setApplicationCategory(categoryFormList);

		return form;
	}

	/**
	 * M_申請区分EntityをM_申請区分フォームに詰めなおす
	 * 
	 * @param entity M_申請区分Entity
	 * @return M_申請区分フォーム
	 */
	protected ApplicationCategoryForm getApplicationCategoryFormFromEntity(ApplicationCategoryMaster entity) {
		ApplicationCategoryForm form = new ApplicationCategoryForm();
		form.setId(entity.getCategoryId());
		form.setScreenId(entity.getViewId());
		form.setOrder(entity.getOrder());
		form.setContent(entity.getLabelName());
		return form;
	}

	/**
	 * F_地番EntityをF_地番フォームに詰めなおす
	 * 
	 * @param entity F_地番Entity
	 * @return F_地番フォーム
	 */
	protected LotNumberForm getLotNumberFormFromEntity(LotNumberAndDistrict entity,
			List<LotNumberSearchResultDefinition> lotNumberSearchResultDefinitionList) {
		LotNumberForm form = new LotNumberForm();
		form.setChibanId(entity.getChibanId());
		form.setChiban(entity.getChiban());
		form.setCityName(cityName);
		form.setDistrictId(entity.getDistrictId());
		form.setDistrictName(entity.getOoazaDistrictName());
		form.setDistrictKana(entity.getOoazaDistrictKana());
		form.setLon(entity.getLon());
		form.setLat(entity.getLat());
		form.setMinlon(entity.getMinlon());
		form.setMinlat(entity.getMinlat());
		form.setMaxlon(entity.getMaxlon());
		form.setMaxlat(entity.getMaxlat());
		form.setStatus(entity.getStatus());
		form.setApplicationId(entity.getApplicationId());

		// attributesの設定
		Map<String, Object> attributes = new LinkedHashMap<String, Object>();
		if (lotNumberSearchResultDefinitionList != null) {
			for (LotNumberSearchResultDefinition lotNumberSearchResultDefinition : lotNumberSearchResultDefinitionList) {
				String tableType = lotNumberSearchResultDefinition.getTableType();
				String columnName = lotNumberSearchResultDefinition.getTableColumnName();
				String responseKey = lotNumberSearchResultDefinition.getResponseKey();
				if (columnName != null) {
					// 小文字に揃えて比較する
					columnName = columnName.toLowerCase();
					// snake_case から CamelCase への変換
					columnName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, columnName);
					//　先頭文字は常に小文字にする
					if(columnName != null) {
						columnName = columnName.substring(0, 1).toLowerCase() + columnName.substring(1);
					}
				}
				
				if (tableType != null && responseKey != null && columnName != null) {
					if (typeLotNumber.equals(tableType)) {
						for (int i = 1; i <= 5; i++) {
							String targetColumnName = "resultColumn" + i;
							if (columnName.equals(targetColumnName)) {
								Field field;
								try {
									field = LotNumberAndDistrict.class.getDeclaredField(targetColumnName);
									field.setAccessible(true);
									attributes.put(responseKey, field.get(entity));
									break;
								} catch (Exception e) {
									LOGGER.error("LotNumberAndDistrictのリフレクションで例外発生", e);
								}
							}
						}
					} else if (typeDistrict.equals(tableType)) {
						for (int i = 1; i <= 5; i++) {
							String targetColumnName = "ooazaResultColumn" + i;
							String comparisonColumnName = "resultColumn" + i;
							if (columnName.equals(comparisonColumnName)) {
								Field field;
								try {
									field = LotNumberAndDistrict.class.getDeclaredField(targetColumnName);
									field.setAccessible(true);
									attributes.put(responseKey, field.get(entity));
									break;
								} catch (Exception e) {
									LOGGER.error("LotNumberAndDistrictのリフレクションで例外発生", e);
								}
							}
						}

					}
				}
			}
		}
		form.setAttributes(attributes);

		return form;
	}

	/**
	 * ファイルのContentTypeを取得
	 * 
	 * @param path ファイルパス
	 * @return ContentType
	 * @throws IOException 例外
	 */
	protected MediaType getContentType(Path path) throws IOException {
		try {
			return MediaType.parseMediaType(Files.probeContentType(path));
		} catch (IOException e) {
			return MediaType.APPLICATION_OCTET_STREAM;
		}
	}

	/**
	 * ファイル出力
	 * 
	 * @param fileData     ファイルデータ
	 * @param filePathText 出力ファイルパス
	 * @throws IOException 例外
	 */
	protected void exportFile(MultipartFile fileData, String filePathText) throws IOException {
		Path filePath = Paths.get(filePathText);
		byte[] bytes = fileData.getBytes();
		try (OutputStream stream = Files.newOutputStream(filePath)) {
			stream.write(bytes);
		}
	}

	/**
	 * EXCELファイル出力
	 * 
	 * @param workBook     ファイルデータ
	 * @param filePathText 出力ファイルパス
	 * @throws IOException 例外
	 */
	protected void exportWorkBook(Workbook workBook, String filePathText) throws IOException {
		Path filePath = Paths.get(filePathText);
		try (OutputStream stream = Files.newOutputStream(filePath)) {
			workBook.write(stream);
		}
	}

	/**
	 * メールメッセージ定義を取得
	 * 
	 * @param key キー
	 * @return メッセージ
	 */
	protected String getMailPropValue(String key, MailItem mailItem) {
		if (mailUtil == null) {
			try {
				mailUtil = new MailMessageUtil(mailPropertiesPath);
			} catch (Exception e) {
				LOGGER.error("メールメッセージ定義初期化エラー", e);
				throw new RuntimeException("メールメッセージ定義初期化エラー");
			}
		}
		return mailUtil.getFormattedValue(key, mailItem);
	}
}
