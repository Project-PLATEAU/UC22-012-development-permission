package developmentpermission.repository.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import developmentpermission.form.ApplicationCategoryForm;

/**
 * 申請区分JDBC
 */
@Component
@Configuration
@ComponentScan
@EnableTransactionManagement
@Transactional
public class ApplicationCategoryJdbc extends AbstractJdbc {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationCategoryJdbc.class);

	/**
	 * 申請区分情報登録
	 * 
	 * @param form          申請区分情報
	 * @param applicationId 申請ID
	 * @return 更新件数
	 */
	public int insert(ApplicationCategoryForm form, int applicationId) {
		LOGGER.debug("申請区分情報登録 開始");
		try {
			String sql = "" + //
					"INSERT INTO o_application_category ( " + //
					"  application_id, " + //
					"  view_id, " + //
					"  category_id " + //
					") VALUES (?, ?, ?)";
			return jdbcTemplate.update(sql, //
					applicationId, //
					form.getScreenId(), //
					form.getId());
		} finally {
			LOGGER.debug("申請区分情報登録 終了");
		}
	}
}
