package developmentpermission.repository.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.form.UploadApplicationFileForm;

/**
 * 申請ファイルJDBC
 */
@Component
@Configuration
@ComponentScan
@EnableTransactionManagement
@Transactional
public class ApplicationFileJdbc extends AbstractJdbc {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationFileJdbc.class);

	/**
	 * 登録
	 * 
	 * @param form パラメータ
	 * @return ファイルID
	 */
	public Integer insert(UploadApplicationFileForm form) {
		LOGGER.debug("申請ファイル登録 開始");
		try {
			String sql = "" + //
					"INSERT INTO o_application_file " + //
					"(file_id, application_id, application_file_id, upload_file_name) " + //
					"VALUES (nextval('seq_file'), ?, ?, ?)";
			jdbcTemplate.update(sql, //
					form.getApplicationId(), //
					form.getApplicationFileId(), //
					form.getUploadFileName());
			return jdbcTemplate.queryForObject("SELECT lastval()", Integer.class);
		} finally {
			LOGGER.debug("申請ファイル登録 終了");
		}
	}

	/**
	 * ファイルパス更新
	 * 
	 * @param fileId   ファイルID
	 * @param filePath ファイルパス
	 * @return 更新件数
	 */
	public int updateFilePath(int fileId, String filePath) {
		LOGGER.debug("申請ファイルパス更新 開始");
		try {
			String sql = "" + //
					"UPDATE o_application_file " + //
					"SET file_path=? " + //
					"WHERE file_id=?";
			return jdbcTemplate.update(sql, //
					filePath, //
					fileId);
		} finally {
			LOGGER.debug("申請ファイルパス更新 終了");
		}
	}
}
