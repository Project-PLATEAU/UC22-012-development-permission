package developmentpermission.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import developmentpermission.dao.AnswerDao;
import developmentpermission.dao.ApplicationDao;
import developmentpermission.entity.Answer;
import developmentpermission.entity.AnswerFile;
import developmentpermission.entity.ApplicantInformation;
import developmentpermission.entity.Application;
import developmentpermission.entity.Department;
import developmentpermission.entity.LotNumberAndDistrict;
import developmentpermission.entity.LotNumberSearchResultDefinition;
import developmentpermission.form.AnswerFileForm;
import developmentpermission.form.AnswerForm;
import developmentpermission.form.ApplyAnswerForm;
import developmentpermission.form.LotNumberForm;
import developmentpermission.repository.AnswerFileRepository;
import developmentpermission.repository.AnswerRepository;
import developmentpermission.repository.ApplicantInformationRepository;
import developmentpermission.repository.ApplicationRepository;
import developmentpermission.repository.DepartmentRepository;
import developmentpermission.repository.LotNumberSearchResultDefinitionRepository;
import developmentpermission.repository.jdbc.AnswerFileJdbc;
import developmentpermission.repository.jdbc.AnswerJdbc;
import developmentpermission.repository.jdbc.ApplicationJdbc;
import developmentpermission.util.AuthUtil;
import developmentpermission.util.ExportJudgeForm;
import developmentpermission.util.LogUtil;
import developmentpermission.util.MailMessageUtil;
import developmentpermission.util.model.MailItem;

/**
 * 回答Serviceクラス
 */
@Service
@Transactional
public class AnswerService extends AbstractJudgementService {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(AnswerService.class);

	/** O_回答Repositoryインスタンス */
	@Autowired
	private AnswerRepository answerRepository;
	/** O_回答ファイルRepositoryインスタンス */
	@Autowired
	private AnswerFileRepository answerFileRepository;
	/** O_申請Repositoryインスタンス */
	@Autowired
	private ApplicationRepository applicationRepository;
	/** O_申請者情報Repositoryインスタンス */
	@Autowired
	private ApplicantInformationRepository applicantInformationRepository;
	/** M_部署Repositoryインスタンス */
	@Autowired
	private DepartmentRepository departmentRepository;
	/** M_地番検索結果定義Repositoryインスタンス */
	@Autowired
	private LotNumberSearchResultDefinitionRepository lotNumberSearchResultDefinitionRepository;
	/** 回答情報JDBCインスタンス */
	@Autowired
	private AnswerJdbc answerJdbc;
	/** 申請情報JDBCインスタンス */
	@Autowired
	private ApplicationJdbc applicationJdbc;
	/** 回答ファイルJDBCインスタンス */
	@Autowired
	private AnswerFileJdbc answerFileJdbc;

	/** 回答ファイル用フォルダのtimestampフォーマット */
	@Value("${app.file.answer.foldername.format}")
	protected String answerFolderNameFormat;

	/** 回答更新通知（行政向け）を送信するかどうか (0:送信しない、1:送信する) */
	@Value("${app.mail.send.answer.update}")
	protected Integer mailSendAnswerUpdateFlg;
	
	/** 回答登録 csvログファイルヘッダー */
	@Value("${app.csv.log.header.answer.register}")
	private String[] answerRegisterLogHeader;
	
	/** 回答登録 csvログファイルパス */
	@Value("${app.csv.log.path.answer.register}")
	private String answerRegisterLogPath;

	/**
	 * 回答登録のパラメータチェック
	 * 
	 * @param answerFormList 登録パラメータ
	 * @param departmentId   部署ID
	 * @return 判定結果
	 */
	public boolean validateRegistAnswersParam(List<AnswerForm> answerFormList, String departmentId) {
		LOGGER.debug("回答登録のパラメータチェック 開始");
		try {
			if (answerFormList.size() == 0 //
					|| departmentId == null || EMPTY.equals(departmentId)) {
				// 登録データが空
				LOGGER.warn("登録パラメータが空、または部署IDがnullまたは空");
				return false;
			}

			// 基準申請ID
			Integer baseApplicationId = null;

			for (AnswerForm answerForm : answerFormList) {
				// 部署チェック
				LOGGER.trace("部署チェック 開始");
				boolean departmentFlg = false;
				AnswerDao dao = new AnswerDao(emf);
				List<Department> departmentList = dao.getDepartmentList(answerForm.getAnswerId());
				for (Department department : departmentList) {
					if (departmentId.equals(department.getDepartmentId())) {
						departmentFlg = true;
						break;
					}
				}
				if (!departmentFlg) {
					// 回答アクセス権限がない
					LOGGER.warn("回答アクセス権限がない");
					return false;
				}
				LOGGER.trace("部署チェック 終了");

				LOGGER.trace("回答データチェック 開始");
				List<Answer> answerList = answerRepository.findByAnswerId(answerForm.getAnswerId());
				if (answerList.size() != 1) {
					// 回答データの件数不正
					LOGGER.warn("回答データ件数不正");
					return false;
				} else {
					Answer answer = answerList.get(0);
					if (baseApplicationId == null) {
						baseApplicationId = answer.getApplicationId();
					} else if (!baseApplicationId.equals(answer.getApplicationId())) {
						// パラメータの回答IDが持つ申請IDに異なるものがある
						LOGGER.warn("回答データリストの申請IDが全て一致していない");
						return false;
					}
				}
				LOGGER.trace("回答データチェック 終了");
			}
			return true;
		} finally {
			LOGGER.debug("回答登録のパラメータチェック 終了");
		}
	}

	/**
	 * 回答登録(行政のみ)
	 * 
	 * @param answerFormList 登録パラメータ
	 */
	public void registAnswers(List<AnswerForm> answerFormList,String loginId,String departmentName) {
		LOGGER.debug("回答登録 開始");
		try {
			LOGGER.trace("回答情報更新 開始");
			// 「登録」と言いつつ、Updateのみ
			Integer baseApplicationId = null;
			for (AnswerForm answerForm : answerFormList) {
				if (answerJdbc.update(answerForm) != 1) {
					LOGGER.warn("回答情報の更新件数不正");
					throw new RuntimeException("回答情報の更新に失敗");
				}
				if (baseApplicationId == null) {
					List<Answer> answerList = answerRepository.findByAnswerId(answerForm.getAnswerId());
					if (answerList.size() != 1) {
						LOGGER.error("回答情報の取得に失敗");
						throw new RuntimeException("回答情報の取得に失敗");
					}
					Answer answer = answerList.get(0);
					baseApplicationId = answer.getApplicationId();
				}
				// 回答登録ログ出力
				try {
					Object[] logData = {LogUtil.localDateTimeToString(LocalDateTime.now()),loginId,departmentName,baseApplicationId,
												answerForm.getAnswerId(),answerForm.getJudgementInformation().getTitle(),answerForm.getAnswerContent()};
					LogUtil.writeLogToCsv(answerRegisterLogPath, answerRegisterLogHeader, logData);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			LOGGER.trace("回答情報更新 終了");

			if (baseApplicationId == null) {
				LOGGER.error("申請IDの取得に失敗");
				throw new RuntimeException("申請IDの取得に失敗");
			}

			// O_回答ファイル
			// 回答ファイル登録は別APIで実施

			LOGGER.trace("申請ステータス更新 開始");
			// 回答更新の際にO_申請のステータスも更新する
			List<Answer> unansweredAnswer = answerRepository.findUnansweredByApplicationId(baseApplicationId);
			String status = STATE_ANSWERING;
			if (unansweredAnswer.size() == 0) {
				status = STATE_ANSWERED;
			}
			if (applicationJdbc.updateApplicationStatus(baseApplicationId, status) != 1) {
				LOGGER.warn("申請情報の更新件数不正");
				throw new RuntimeException("申請情報の更新に失敗");
			}
			LOGGER.trace("申請ステータス更新 終了");

			if (STATE_ANSWERING.equals(status)) {
				// 回答中
				// 行政に回答更新通知送付
				LOGGER.trace("行政に回答更新通知送付 開始");
				sendUpdatedMainToGovernmentUser(baseApplicationId, false);
				LOGGER.trace("行政に回答更新通知送付 終了");
			} else {
				// 回答完了
				// 行政に全部署回答完了通知送付
				LOGGER.trace("行政に全部署回答完了通知送付 開始");
				sendUpdatedMainToGovernmentUser(baseApplicationId, true);
				LOGGER.trace("行政に全部署回答完了通知送付 終了");
			}
		} finally {
			LOGGER.debug("回答登録 終了");
		}
	}

	/**
	 * 行政に回答更新/完了通知送付
	 * 
	 * @param applicationId 申請ID
	 * @param isFinished    完了かどうか
	 */
	private void sendUpdatedMainToGovernmentUser(Integer applicationId, boolean isFinished) {
		MailItem item = new MailItem();

		List<ApplicantInformation> applicantList = applicantInformationRepository.getApplicantList(applicationId);
		if (applicantList.size() != 1) {
			LOGGER.error("申請者データの件数が不正");
			throw new RuntimeException("申請者データの件数が不正");
		}
		ApplicantInformation applicant = applicantList.get(0);
		switch (applicantNameItemNumber) { // 氏名はプロパティで設定されたアイテムを設定
		case 1:
			item.setName(applicant.getItem1());
			break;
		case 2:
			item.setName(applicant.getItem2());
			break;
		case 3:
			item.setName(applicant.getItem3());
			break;
		case 4:
			item.setName(applicant.getItem4());
			break;
		case 5:
			item.setName(applicant.getItem5());
			break;
		case 6:
			item.setName(applicant.getItem6());
			break;
		case 7:
			item.setName(applicant.getItem7());
			break;
		case 8:
			item.setName(applicant.getItem8());
			break;
		case 9:
			item.setName(applicant.getItem9());
			break;
		case 10:
			item.setName(applicant.getItem10());
			break;
		default:
			LOGGER.error("氏名アイテム番号指定が不正: " + applicantNameItemNumber);
			throw new RuntimeException("氏名アイテム番号指定が不正");
		}
		item.setMailAddress(applicant.getMailAddress()); // メールアドレス

		// 地番
		ApplicationDao applicationDao = new ApplicationDao(emf);
		List<LotNumberForm> lotNumberFormList = new ArrayList<LotNumberForm>();
		List<LotNumberSearchResultDefinition> lotNumberSearchResultDefinitionList = lotNumberSearchResultDefinitionRepository
				.getLotNumberSearchResultDefinitionList();
		List<LotNumberAndDistrict> lotNumberList = applicationDao.getLotNumberList(applicationId, lonlatEpsg);
		for (LotNumberAndDistrict lotNumber : lotNumberList) {
			lotNumberFormList.add(getLotNumberFormFromEntity(lotNumber, lotNumberSearchResultDefinitionList));
		}
		ExportJudgeForm exportForm = new ExportJudgeForm();
		String addressText = exportForm.getAddressText(lotNumberFormList, lotNumberSeparators, separator);
		item.setLotNumber(addressText);

		String subject, body;
		if (!isFinished) {
			// 行政に回答更新通知送付
			if (mailSendAnswerUpdateFlg == 1) {
				subject = getMailPropValue(MailMessageUtil.KEY_UPDATE_SUBJECT, item);
				body = getMailPropValue(MailMessageUtil.KEY_UPDATE_BODY, item);
			} else {
				LOGGER.debug("行政に回答更新通知を送付しない設定になっているのでメール送信はスキップ");
				return;
			}
		} else {
			// 行政に全部署回答完了通知送付
			subject = getMailPropValue(MailMessageUtil.KEY_FINISH_SUBJECT, item);
			body = getMailPropValue(MailMessageUtil.KEY_FINISH_BODY, item);
		}

		// 部署リスト
		List<Department> departmentList = departmentRepository.getDepartmentList();
		for (Department department : departmentList) {
			if (!department.getAnswerAuthorityFlag()) {
				// 回答権限がない
				continue;
			}
			LOGGER.trace(department.getMailAddress());
			LOGGER.trace(subject);
			LOGGER.trace(body);

			try {
				final String[] mailAddressList = department.getMailAddress().split(",");
				for (String aMailAddress: mailAddressList) {
					mailSendutil.sendMail(aMailAddress, subject, body);
				}
			} catch (Exception e) {
				LOGGER.error("メール送信時にエラー発生", e);
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * 回答権限チェック
	 * 
	 * @param departmentId 部署ID
	 * @return 判定結果
	 */
	public boolean checkAnswerAuthority(String departmentId) {
		LOGGER.debug("回答権限チェック 開始");
		try {
			List<Department> departmentList = departmentRepository.getDepartmentListById(departmentId);
			if (departmentList.size() != 1) {
				LOGGER.warn("部署取得件数不正");
				return false;
			}
			return departmentList.get(0).getAnswerAuthorityFlag();
		} finally {
			LOGGER.debug("回答権限チェック 終了");
		}
	}

	/**
	 * 回答通知
	 * 
	 * @param applyAnswerForm 申請・回答内容確認情報フォーム
	 */
	public void notifyAnswer(ApplyAnswerForm applyAnswerForm) {
		LOGGER.debug("回答通知 開始");
		try {
			// O_申請の登録ステータスをチェック
			List<Application> applicationList = applicationRepository
					.getApplicationList(applyAnswerForm.getApplicationId());
			if (applicationList.size() != 1) {
				LOGGER.error("申請データの件数が不正");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			Application application = applicationList.get(0);
			String status = application.getStatus();
			if (!STATE_ANSWERING.equals(status) && !STATE_ANSWERED.equals(status)) {
				// ステータスが回答中、または回答完了ではないので不正
				LOGGER.error("ステータスが回答中、または回答完了でない");
				throw new ResponseStatusException(HttpStatus.CONFLICT);
			}

			LOGGER.debug("回答ファイル更新処理 開始");
			List<Answer> answerList = answerRepository.findByApplicationId(applyAnswerForm.getApplicationId());
			for (Answer answer : answerList) {
				LOGGER.debug("回答ID: " + answer.getAnswerId());

				// 申請IDと紐づくO_回答の回答内容(answer_content)を通知テキスト(notified_text)にCopy
				if (answerJdbc.copyNotifyText(answer) != 1) {
					LOGGER.error("回答データの更新件数が不正");
					throw new RuntimeException("回答データの更新件数が不正");
				}

				// 申請IDと紐づくO_回答ファイルのファイルパス(file_path)を通知済みファイルパス(notified_file_path)にCopy
				List<AnswerFile> answerFileList = answerFileRepository.findByAnswerId(answer.getAnswerId());
				for (AnswerFile answerFile : answerFileList) {
					LOGGER.debug("回答ファイルID: " + answerFile.getAnswerFileId());
					LOGGER.debug("削除未通知フラグ: " + answerFile.getDeleteUnnotifiedFlag());

					if (answerFileJdbc.copyNotifyPath(answerFile) != 1) {
						LOGGER.error("回答ファイルデータの更新件数が不正");
						throw new RuntimeException("回答ファイルデータの更新件数が不正");
					}

					// 申請IDと紐づくO_回答ファイルのファイルパス(file_path)に記載してあるファイル以外の、回答ファイルIDと紐づくファイルの実体を削除する
					// 削除しないファイルパス
					String baseFilePath = answerFile.getFilePath();
					LOGGER.debug("回答ファイルベースパス: " + baseFilePath);

					File baseFile = new File(fileRootPath + baseFilePath);
					File baseFolder = baseFile.getParentFile(); // timestampフォルダ
					String baseTimestampFolderName = baseFolder.getName();

					// ファイルパスは「/answer/<回答ID>/<回答ファイルID>/<timestamp>/<アップロードファイル名>」
					// 相対フォルダパス(回答ファイルIDまで)
					String folderPath = answerFolderName;
					folderPath += PATH_SPLITTER + answerFile.getAnswerId();
					folderPath += PATH_SPLITTER + answerFile.getAnswerFileId();

					// 絶対フォルダパス(回答ファイルIDまで)
					String absoluteFolderPath = fileRootPath + folderPath;
					File absoluteFolder = new File(absoluteFolderPath);

					// 指定フォルダ内のフォルダリストを取得(timestampリスト)
					File[] timestampFileArray = absoluteFolder.listFiles();
					for (File timestamp : timestampFileArray) {
						LOGGER.debug("ファイルのtimestamp: " + timestamp.getName());
						if (timestamp.isFile()) {
							// ここにファイルがあるはずがない
							LOGGER.debug("ここにファイルがあるはずがないので削除");
							if (!timestamp.delete()) {
								LOGGER.error("ファイルの削除に失敗: " + timestamp.getAbsolutePath());
								throw new RuntimeException("ファイルの削除に失敗");
							}
						} else {
							LOGGER.debug("ファイルではない場合");
							if (!timestamp.getName().equals(baseTimestampFolderName)) { // timestampが異なる
								// 削除するパスなので、フォルダごと削除
								String tmpAbsoluteFolderPath = absoluteFolderPath + PATH_SPLITTER + timestamp.getName();
								LOGGER.debug("ベースのtimestampと等しくない場合なのでフォルダごと削除: " + tmpAbsoluteFolderPath);
								File tmpFile = new File(tmpAbsoluteFolderPath);
								if (tmpFile.exists()) {
									if (!FileSystemUtils.deleteRecursively(tmpFile)) {
										LOGGER.error("フォルダの削除に失敗: " + tmpAbsoluteFolderPath);
										throw new RuntimeException("フォルダの削除に失敗");
									}
								} else {
									LOGGER.warn("削除するディレクトリが存在しない");
								}
							}
						}
					}
					LOGGER.debug("更新ファイル削除 完了");

					if (answerFile.getDeleteUnnotifiedFlag()) {
						// 申請IDと紐づくO_回答ファイルの削除未通知フラグ（delete_unnotidied_flag）がtrueの回答ファイルIDと紐づくファイルの実体をすべて削除する
						File tmpFile = new File(fileRootPath + answerFile.getFilePath());
						LOGGER.debug("回答ファイル削除開始: " + tmpFile.getAbsolutePath());
						if (tmpFile.exists()) {
							if (!FileSystemUtils.deleteRecursively(tmpFile)) {
								LOGGER.error("フォルダの削除に失敗: " + tmpFile.getAbsolutePath());
								throw new RuntimeException("フォルダの削除に失敗");
							}
						} else {
							LOGGER.warn("削除する回答ファイルが存在しない");
						}
						LOGGER.debug("回答ファイル削除 完了");

						// 申請IDと紐づくO_回答ファイルの削除未通知フラグ（delete_unnotidied_flag）がtrueのレコードを削除する
						LOGGER.debug("回答ファイルDB削除開始 回答ファイルID: " + answerFile.getAnswerFileId());
						if (answerFileJdbc.delete(answerFile) != 1) {
							LOGGER.error("回答ファイルデータの削除件数が不正");
							throw new RuntimeException("回答ファイルデータの削除件数が不正");
						}
						LOGGER.debug("回答ファイルDB削除 完了");
					}
				}
			}
			LOGGER.debug("回答ファイル更新処理 完了");

			// O_申請のステータスを更新
			if (applicationJdbc.updateApplicationStatus(applyAnswerForm.getApplicationId(), STATE_NOTIFIED) != 1) {
				LOGGER.warn("申請情報の更新不正");
				throw new RuntimeException("申請情報の更新に失敗");
			}

			// 事業者に回答完了通知送付
			sendFinishedMailToBusinessUser(application);

		} finally {
			LOGGER.debug("回答通知 終了");
		}
	}

	/**
	 * 事業者に回答完了通知送付
	 * 
	 * @param application 申請情報
	 */
	private void sendFinishedMailToBusinessUser(Application application) {
		List<ApplicantInformation> applicantList = applicantInformationRepository
				.getApplicantList(application.getApplicationId());
		if (applicantList.size() != 1) {
			LOGGER.error("申請者データの件数が不正");
			throw new RuntimeException("申請者データの件数が不正");
		}
		ApplicantInformation applicant = applicantList.get(0);

		MailItem item = new MailItem();

		// 日時
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(mailTimestampFormat);
		item.setTimestamp(application.getRegisterDatetime().format(dateFormat));

		// 地番
		ApplicationDao applicationDao = new ApplicationDao(emf);
		List<LotNumberForm> lotNumberFormList = new ArrayList<LotNumberForm>();
		List<LotNumberSearchResultDefinition> lotNumberSearchResultDefinitionList = lotNumberSearchResultDefinitionRepository
				.getLotNumberSearchResultDefinitionList();
		List<LotNumberAndDistrict> lotNumberList = applicationDao.getLotNumberList(application.getApplicationId(),
				lonlatEpsg);
		for (LotNumberAndDistrict lotNumber : lotNumberList) {
			lotNumberFormList.add(getLotNumberFormFromEntity(lotNumber, lotNumberSearchResultDefinitionList));
		}
		ExportJudgeForm exportForm = new ExportJudgeForm();
		String addressText = exportForm.getAddressText(lotNumberFormList, lotNumberSeparators, separator);
		item.setLotNumber(addressText);

		// 事業者に回答完了通知送付
		String subject = getMailPropValue(MailMessageUtil.KEY_BUSSINESS_FINISH_SUBJECT, item);
		String body = getMailPropValue(MailMessageUtil.KEY_BUSSINESS_FINISH_BODY, item);

		LOGGER.trace(applicant.getMailAddress());
		LOGGER.trace(subject);
		LOGGER.trace(body);

		try {
			mailSendutil.sendMail(applicant.getMailAddress(), subject, body);
		} catch (Exception e) {
			LOGGER.error("メール送信時にエラー発生", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 回答ファイルアップロードパラメータ確認
	 * 
	 * @param form         パラメータ
	 * @param departmentId 部署ID
	 * @return 確認結果
	 */
	public boolean validateUploadAnswerFile(AnswerFileForm form, String departmentId) {
		LOGGER.debug("回答ファイルアップロードパラメータ確認 開始");
		try {
			Integer answerId = form.getAnswerId();
			String answerFileName = form.getAnswerFileName();
			MultipartFile uploadFile = form.getUploadFile();
			if (answerId == null //
					|| answerFileName == null || EMPTY.equals(answerFileName) //
					|| uploadFile == null //
					|| departmentId == null || EMPTY.equals(departmentId)) {
				// パラメータ不足
				LOGGER.warn("パラメータ不足");
				return false;
			}

			// 回答ID
			if (answerRepository.findByAnswerId(answerId).size() != 1) {
				LOGGER.warn("回答データ取得件数不正");
				return false;
			}

			// 回答ファイルID
			Integer answerFileId = form.getAnswerFileId();
			if (answerFileId != null) {
				if (answerFileRepository.findByAnswerFileId(answerFileId).size() != 1) {
					LOGGER.warn("回答ファイルの件数が不正");
					return false;
				}
			}

			// 部署チェック
			LOGGER.trace("部署チェック 開始");
			boolean departmentFlg = false;
			AnswerDao dao = new AnswerDao(emf);
			List<Department> departmentList = dao.getDepartmentList(answerId);
			for (Department department : departmentList) {
				if (departmentId.equals(department.getDepartmentId())) {
					departmentFlg = true;
					break;
				}
			}
			if (!departmentFlg) {
				// 回答アクセス権限がない
				LOGGER.warn("回答アクセス権限がない");
				return false;
			}
			LOGGER.trace("部署チェック 終了");

			return true;
		} finally {
			LOGGER.debug("回答ファイルアップロードパラメータ確認 終了");
		}
	}

	/**
	 * 回答ファイルアップロード
	 * 
	 * @param form パラメータ
	 */
	public void uploadAnswerFile(AnswerFileForm form) {
		LOGGER.debug("回答ファイルアップロード 開始");
		try {
			// ファイルパスは「/answer/<回答ID>/<回答ファイルID>/<timestamp>/<アップロードファイル名>」
			SimpleDateFormat sdf = new SimpleDateFormat(answerFolderNameFormat);
			String nowTime = sdf.format(new Date());

			Integer answerFileId = form.getAnswerFileId();
			if (answerFileId == null) {
				// ■ 新規登録
				// O_回答ファイル登録
				LOGGER.trace("O_回答ファイル登録 開始");
				answerFileId = answerFileJdbc.insert(form);
				form.setAnswerFileId(answerFileId);
				LOGGER.trace("O_回答ファイル登録 終了");
			}

			// 相対フォルダパス
			String folderPath = answerFolderName;
			folderPath += PATH_SPLITTER + form.getAnswerId();
			folderPath += PATH_SPLITTER + form.getAnswerFileId();
			folderPath += PATH_SPLITTER + nowTime;

			// 絶対フォルダパス
			String absoluteFolderPath = fileRootPath + folderPath;
			Path directoryPath = Paths.get(absoluteFolderPath);
			if (!Files.exists(directoryPath)) {
				// フォルダがないので生成
				LOGGER.debug("フォルダ生成: " + directoryPath);
				Files.createDirectories(directoryPath);
			}

			// 相対ファイルパス
			String filePath = folderPath + PATH_SPLITTER + form.getAnswerFileName();
			// 絶対ファイルパス
			String absoluteFilePath = absoluteFolderPath + PATH_SPLITTER + form.getAnswerFileName();

			// ファイルパスはrootを除いた相対パスを設定
			form.setAnswerFilePath(filePath);

			// O_回答ファイル更新
			LOGGER.trace("O_回答ファイル更新 開始");
			if (answerFileJdbc.updateFilePath(answerFileId, filePath) != 1) {
				LOGGER.warn("ファイルパス更新件数が不正");
				throw new RuntimeException("ファイルパス更新件数が不正");
			}
			LOGGER.trace("O_回答ファイル更新 終了");

			// ファイル出力
			LOGGER.trace("ファイル出力 開始");
			exportFile(form.getUploadFile(), absoluteFilePath);
			LOGGER.trace("ファイル出力 終了");
		} catch (Exception ex) {
			// RuntimeExceptionで投げないとロールバックされない
			LOGGER.error("回答ファイルアップロードで例外発生", ex);
			throw new RuntimeException(ex);
		} finally {
			LOGGER.debug("回答ファイルアップロード 終了");
		}
	}

	/**
	 * 回答ファイルダウンロードパラメータ確認
	 * 
	 * @param form パラメータ
	 * @return 確認結果
	 */
	public boolean validateDownloadAnswerFile(AnswerFileForm form) {
		LOGGER.debug("回答ファイルダウンロードパラメータ確認 開始");
		try {
			Integer answerId = form.getAnswerId();
			Integer answerFileId = form.getAnswerFileId();

			// 回答ID
			if (answerRepository.findByAnswerId(answerId).size() != 1) {
				LOGGER.warn("回答データの件数が不正");
				return false;
			}

			// 回答ファイルID
			if (answerFileRepository.findByAnswerFileId(answerFileId).size() != 1) {
				LOGGER.warn("回答ファイルの件数が不正");
				return false;
			}
			return true;
		} finally {
			LOGGER.debug("回答ファイルダウンロードパラメータ確認 終了");
		}
	}

	/**
	 * 回答ファイルダウンロード
	 * 
	 * @param form パラメータ
	 * @return 応答Entity
	 */
	public ResponseEntity<Resource> downloadAnswerFile(AnswerFileForm form, String role) {
		LOGGER.debug("回答ファイルダウンロード 開始");
		try {
			LOGGER.trace("回答ファイルデータ取得 開始: " + form.getAnswerFileId());
			List<AnswerFile> answerFileList = answerFileRepository.findByAnswerFileId(form.getAnswerFileId());
			AnswerFile answerFile = answerFileList.get(0);
			LOGGER.trace("回答ファイルデータ取得 終了:" + form.getAnswerFileId());

			Path filePath = null;

			if (AuthUtil.ROLE_GOVERMENT.equals(role)) {
				// ■ 行政
				if (answerFile.getDeleteUnnotifiedFlag()) {
					LOGGER.warn("削除済みファイルの取得要求(行政)");
					return new ResponseEntity<Resource>(HttpStatus.NOT_FOUND);
				}

				// 絶対ファイルパス
				String absoluteFilePath = fileRootPath + answerFile.getFilePath();
				filePath = Paths.get(absoluteFilePath);
			} else if (AuthUtil.ROLE_BUSINESS.equals(role)) {
				// ■ 事業者
				// 回答情報取得
				List<Answer> answerList = answerRepository.findByAnswerId(form.getAnswerId());
				if (answerList.size() != 1) {
					LOGGER.warn("回答データ取得件数不正");
					return new ResponseEntity<Resource>(HttpStatus.SERVICE_UNAVAILABLE);
				}
				Answer answer = answerList.get(0);
				if (!answer.getNotifiedFlag()) {
					LOGGER.warn("通知フラグがfalseのファイルの取得要求(事業者)");
					return new ResponseEntity<Resource>(HttpStatus.FORBIDDEN);
				}

				// 絶対ファイルパス
				String absoluteFilePath = fileRootPath + answerFile.getNotifiedFilePath();
				filePath = Paths.get(absoluteFilePath);
			} else {
				LOGGER.error("未知のロール: " + role);
				return new ResponseEntity<Resource>(HttpStatus.SERVICE_UNAVAILABLE);
			}

			if (!Files.exists(filePath)) {
				// ファイルが存在しない
				LOGGER.warn("ファイルが存在しない: " + filePath);
				return new ResponseEntity<Resource>(HttpStatus.NOT_FOUND);
			}
			Resource resource = new PathResource(filePath);
			return ResponseEntity.ok().contentType(getContentType(filePath))
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
					.body(resource);
		} catch (Exception ex) {
			LOGGER.error("回答ファイルダウンロードで例外発生", ex);
			return new ResponseEntity<Resource>(HttpStatus.SERVICE_UNAVAILABLE);
		} finally {
			LOGGER.debug("回答ファイルダウンロード 終了");
		}
	}

	/**
	 * 回答ファイル削除
	 * 
	 * @param answerFileForm パラメータ
	 * @return 削除結果
	 */
	public void deleteAnswerFile(AnswerFileForm form) {
		LOGGER.debug("回答ファイル削除 開始: " + form.getAnswerFileId());
		try {
			// O_回答ファイル更新
			LOGGER.trace("O_回答ファイル削除 開始");
			if (answerFileJdbc.setDeleteFlag(form.getAnswerFileId()) != 1) {
				LOGGER.warn("ファイル削除件数が不正");
				throw new RuntimeException("ファイル削除件数が不正");
			}
			LOGGER.trace("O_回答ファイル削除 終了");
		} finally {
			LOGGER.debug("回答ファイル削除 終了: " + form.getAnswerFileId());
		}
	}
}
