package developmentpermission.repository.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.Answer;
import developmentpermission.form.AnswerForm;

/**
 * 回答情報JDBC
 */
@Component
@Configuration
@ComponentScan
@EnableTransactionManagement
@Transactional
public class AnswerJdbc extends AbstractJdbc {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(AnswerJdbc.class);

	/**
	 * 回答登録
	 * @param applicationId 申請ID
	 * @param judgementId 判定項目ID
	 * @param judgementResult 判定結果
	 * @param answerContent 回答文言
	 * @param notifiedText 通知文言
	 * @param completeFlag 完了フラグ
	 * @param notifiedFlag 通知済みフラグ
	 * @return
	 */
	public Integer insert(int applicationId, String judgementId, String judgementResult, String answerContent,
			String notifiedText, char completeFlag, char notifiedFlag) {
		LOGGER.debug("回答登録 開始");
		try {
			String sql = "" + //
					"INSERT INTO o_answer ( " + //
					"  answer_id, " + //
					"  application_id, " + //
					"  judgement_id, " + //
					"  judgement_result, " + //
					"  register_datetime, " + //
					"  update_datetime, " + //
					"  complete_flag, " + //
					"  notified_flag, " + //
					"  answer_content, " + //
					"  notified_text " + //
					") " + //
					"VALUES ( " + //
					"  nextval('seq_answer'), " + //
					"  ?, " + // applicationId
					"  ?, " + // judgementId
					"  ?, " + // judgementResult
					"  CURRENT_TIMESTAMP, " + //
					"  CURRENT_TIMESTAMP, " + //
					"  ?, " + // completeFlag
					"  ?, " + //notifiedFlag
					"  ?, " + // answerContent
					"  ? " + // notifiedText
					")";
			jdbcTemplate.update(sql, //
					applicationId, //
					judgementId, //
					judgementResult,
					completeFlag,
					notifiedFlag,
					answerContent,
					notifiedText
					);
			return jdbcTemplate.queryForObject("SELECT lastval()", Integer.class);
		} finally {
			LOGGER.debug("回答登録 終了");
		}
	}

	/**
	 * 回答更新
	 * 
	 * @param answerForm パラメータ
	 * @return 更新件数
	 */
	public int update(AnswerForm answerForm) {
		LOGGER.debug("回答更新 開始");
		try {
			String sql = "" + //
					"UPDATE " + //
					"  o_answer " + //
					"SET " + //
					"  answer_content=?, " + //
					"  complete_flag='1', " + // 回答登録時は「1」固定
					"  update_datetime=CURRENT_TIMESTAMP " + //
					"WHERE " + //
					"  answer_id=?" + //
					"  AND update_datetime=?";
			return jdbcTemplate.update(sql, //
					answerForm.getAnswerContent(), //
					answerForm.getAnswerId(), //
					answerForm.getUpdateDatetime());
		} finally {
			LOGGER.debug("回答更新 終了");
		}
	}

	/**
	 * 回答内容を通知テキストに設定
	 * 
	 * @param answerForm パラメータ
	 * @return 更新件数
	 */
	public int copyNotifyText(Answer answer) {
		LOGGER.debug("回答通知設定 開始");
		try {
			String sql = "" + //
					"UPDATE " + //
					"  o_answer " + //
					"SET " + //
					"  notified_text=?, " + //
					"  notified_flag='1'," + // 通知フラグに1を設定
					"  update_datetime=CURRENT_TIMESTAMP " + //
					"WHERE " + //
					"  answer_id=?" + //
					"  AND update_datetime=?";
			return jdbcTemplate.update(sql, //
					answer.getAnswerContent(), //
					answer.getAnswerId(), //
					answer.getUpdateDatetime());
		} finally {
			LOGGER.debug("回答通知設定 終了");
		}
	}
}
