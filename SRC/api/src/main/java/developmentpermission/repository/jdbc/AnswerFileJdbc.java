package developmentpermission.repository.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.AnswerFile;
import developmentpermission.form.AnswerFileForm;

/**
 * 回答ファイルJDBC
 */
@Component
@Configuration
@ComponentScan
@EnableTransactionManagement
@Transactional
public class AnswerFileJdbc extends AbstractJdbc {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(AnswerFileJdbc.class);

	/**
	 * 登録
	 * 
	 * @param form パラメータ
	 * @return 回答ファイルID
	 */
	public Integer insert(AnswerFileForm form) {
		LOGGER.debug("回答ファイル登録 開始");
		try {
			String sql = "" + //
					"INSERT INTO o_answer_file " + //
					"(answer_file_id, answer_id, answer_file_name, delete_unnotified_flag) " + //
					"VALUES (nextval('seq_answer_file'), ?, ?, '0')";
			jdbcTemplate.update(sql, //
					form.getAnswerId(), //
					form.getAnswerFileName());
			return jdbcTemplate.queryForObject("SELECT lastval()", Integer.class);
		} finally {
			LOGGER.debug("回答ファイル登録 終了");
		}
	}

	/**
	 * ファイルパス更新
	 * 
	 * @param answerFileId 回答ファイルID
	 * @param filePath     ファイルパス
	 * @return 更新件数
	 */
	public int updateFilePath(int answerFileId, String filePath) {
		LOGGER.debug("回答ファイルパス更新 開始");
		try {
			String sql = "" + //
					"UPDATE o_answer_file " + //
					"SET file_path=? " + //
					"WHERE answer_file_id=?";
			return jdbcTemplate.update(sql, //
					filePath, //
					answerFileId);
		} finally {
			LOGGER.debug("回答ファイルパス更新 終了");
		}
	}

	/**
	 * 回答ファイル削除未通知フラグ設定
	 * 
	 * @param answerFileId 回答ファイルID
	 * @return 更新件数
	 */
	public int setDeleteFlag(int answerFileId) {
		LOGGER.debug("回答ファイル削除 開始");
		try {
			String sql = "" + //
					"UPDATE o_answer_file " + //
					"SET delete_unnotified_flag='1' " + //
					"WHERE answer_file_id=?";
			return jdbcTemplate.update(sql, answerFileId);
		} finally {
			LOGGER.debug("回答ファイルパス更新 終了");
		}
	}

	/**
	 * ファイルパスを通知済みファイルパスに設定
	 * 
	 * @param answerFile パラメータ
	 * @return 更新件数
	 */
	public int copyNotifyPath(AnswerFile answerFile) {
		LOGGER.debug("回答ファイル通知パス更新 開始");
		try {
			String sql = "" + //
					"UPDATE o_answer_file " + //
					"SET notified_file_path=? " + //
					"WHERE answer_file_id=?";
			return jdbcTemplate.update(sql, //
					answerFile.getFilePath(), //
					answerFile.getAnswerFileId());
		} finally {
			LOGGER.debug("回答ファイル通知パス更新 終了");
		}
	}

	/**
	 * 回答ファイル削除
	 * 
	 * @param answerFile パラメータ
	 * @return 更新件数
	 */
	public int delete(AnswerFile answerFile) {
		LOGGER.debug("回答ファイル削除 開始");
		try {
			String sql = "" + //
					"DELETE FROM o_answer_file " + //
					"WHERE answer_file_id=?";
			return jdbcTemplate.update(sql, //
					answerFile.getAnswerFileId());
		} finally {
			LOGGER.debug("回答ファイル削除 終了");
		}
	}
}
