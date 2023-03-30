package developmentpermission.repository.jdbc;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * 区分判定JDBCクラス
 *
 */
@Component
@Configuration
@ComponentScan
@EnableTransactionManagement
@Transactional
public class JudgementJdbc extends AbstractJdbc {

	/**
	 * 概況診断結果ID採番
	 * 
	 * @return seq 概況診断結果ID
	 */
	public int generateGeneralConditionDiagnosisId() {
		int seq = this.jdbcTemplate.queryForObject("SELECT nextval('seq_general_condition_diagnosis')", Integer.class);
		return seq;
	}
}
