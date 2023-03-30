package developmentpermission.repository.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

/**
 * 申請情報JDBC
 */
@Component
@Configuration
@ComponentScan
@EnableTransactionManagement
@Transactional
public class ApplicationJdbc extends AbstractJdbc {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationJdbc.class);

	/**
	 * 申請情報登録(申請者IDはnull固定)
	 * 
	 * @return 申請ID
	 */
	public Integer insert() {
		LOGGER.debug("申請情報登録 開始");
		try {
			String sql = "" + //
					"INSERT INTO o_application " + //
					"(application_id, status, register_status, register_datetime, update_datetime) " + //
					"VALUES (nextval('seq_application'), 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
			jdbcTemplate.update(sql);
			return jdbcTemplate.queryForObject("SELECT lastval()", Integer.class);
		} finally {
			LOGGER.debug("申請情報登録 終了");
		}
	}

	/**
	 * 申請情報の申請者情報IDを更新
	 * 
	 * @param applicationId 申請ID
	 * @param applicantId   申請者情報ID
	 * @return 更新件数
	 */
	public int updateApplicantId(int applicationId, int applicantId) {
		LOGGER.debug("申請情報の申請者情報ID更新 開始");
		try {
			String sql = "" + //
					"UPDATE o_application " + //
					"SET applicant_id=?, update_datetime=CURRENT_TIMESTAMP " + //
					"WHERE application_id=?";
			return jdbcTemplate.update(sql, //
					applicantId, //
					applicationId);
		} finally {
			LOGGER.debug("申請情報の申請者情報ID更新 終了");
		}
	}

	/**
	 * 申請情報のステータス更新
	 * 
	 * @param applicationId 申請ID
	 * @param status        ステータス
	 * @return 更新件数
	 */
	public int updateApplicationStatus(int applicationId, String status) {
		LOGGER.debug("申請情報ステータス更新 開始");
		try {
			String sql = "" + //
					"UPDATE o_application " + //
					"SET status=?, update_datetime=CURRENT_TIMESTAMP " + //
					"WHERE application_id=?";
			return jdbcTemplate.update(sql, //
					status, //
					applicationId);
		} finally {
			LOGGER.debug("申請情報ステータス更新 終了");
		}
	}

	/**
	 * 申請情報の登録ステータス更新
	 * 
	 * @param applicationId 申請ID
	 * @param status        ステータス
	 * @return 更新件数
	 */
	public int updateRegisterStatus(int applicationId, String status) {
		LOGGER.debug("申請情報登録ステータス更新 開始");
		try {
			String sql = "" + //
					"UPDATE o_application " + //
					"SET register_status=?, update_datetime=CURRENT_TIMESTAMP " + //
					"WHERE application_id=?";
			return jdbcTemplate.update(sql, //
					status, //
					applicationId);
		} finally {
			LOGGER.debug("申請情報登録ステータス更新 終了");
		}
	}
}
