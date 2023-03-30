package developmentpermission.repository.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.form.LotNumberForm;

/**
 * 申請地番JDBC
 */
@Component
@Configuration
@ComponentScan
@EnableTransactionManagement
@Transactional
public class ApplicationLotNumberJdbc extends AbstractJdbc {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationLotNumberJdbc.class);

	/**
	 * 申請地番登録
	 * 
	 * @param form          申請地番情報
	 * @param applicationId 申請ID
	 * @return 更新件数
	 */
	public int insert(LotNumberForm form, int applicationId) {
		LOGGER.debug("申請地番登録 開始");
		try {
			String sql = "" + //
					"INSERT INTO o_application_lot_number ( " + //
					"  lot_number_id, " + //
					"  application_id, " + //
					"  regeister_datetime " + //
					") VALUES (?, ?, CURRENT_TIMESTAMP)";
			return jdbcTemplate.update(sql, //
					form.getChibanId(), //
					applicationId);
		} finally {
			LOGGER.debug("申請地番登録 終了");
		}
	}
}
