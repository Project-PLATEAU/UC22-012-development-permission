package developmentpermission.repository.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.entity.ApplicantInformation;

/**
 * 申請者情報JDBC
 */
@Component
@Configuration
@ComponentScan
@EnableTransactionManagement
@Transactional
public class ApplicantJdbc extends AbstractJdbc {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicantJdbc.class);

	/**
	 * 申請者情報登録(照合IDとパスワードはnull固定)
	 * 
	 * @param applicantInformation 申請者情報
	 * @return 申請者ID
	 */
	public Integer insert(ApplicantInformation applicantInformation) {
		LOGGER.debug("申請者情報登録 開始");
		try {
			String sql = "" + //
					"INSERT INTO o_applicant_information ( " + //
					"  applicant_id, " + //
					"  application_id, " + //
					"  item_1, " + //
					"  item_2, " + //
					"  item_3, " + //
					"  item_4, " + //
					"  item_5, " + //
					"  item_6, " + //
					"  item_7, " + //
					"  item_8, " + //
					"  item_9, " + //
					"  item_10, " + //
					"  mail_address " + //
					") " + //
					"VALUES (nextval('seq_applicant'), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			jdbcTemplate.update(sql, //
					applicantInformation.getApplicationId(), //
					applicantInformation.getItem1(), //
					applicantInformation.getItem2(), //
					applicantInformation.getItem3(), //
					applicantInformation.getItem4(), //
					applicantInformation.getItem5(), //
					applicantInformation.getItem6(), //
					applicantInformation.getItem7(), //
					applicantInformation.getItem8(), //
					applicantInformation.getItem9(), //
					applicantInformation.getItem10(), //
					applicantInformation.getMailAddress());
			return jdbcTemplate.queryForObject("SELECT lastval()", Integer.class);
		} finally {
			LOGGER.debug("申請者情報登録 終了");
		}
	}

	/**
	 * 申請者情報更新
	 * 
	 * @param applicantId 申請者情報ID
	 * @param id 照合ID
	 * @param password パスワード
	 * @return 更新件数
	 */
	public int updateId(Integer applicantId, String id, String password) {
		LOGGER.debug("申請者情報更新 開始");
		try {
			String sql = "" + //
					"UPDATE o_applicant_information " + //
					"SET collation_id=?, password=? " + //
					"WHERE applicant_id=?";

			return jdbcTemplate.update(sql, //
					id, //
					password, //
					applicantId);
		} finally {
			LOGGER.debug("申請者情報更新 終了");
		}
	}
}
