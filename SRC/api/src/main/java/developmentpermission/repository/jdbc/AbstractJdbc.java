package developmentpermission.repository.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

/**
 * JDBC共通処理
 */
@Component
@Configuration
@ComponentScan
@EnableTransactionManagement
@Transactional
public abstract class AbstractJdbc {

	/** JDBCテンプレート */
	@Autowired
	protected JdbcTemplate jdbcTemplate;
}
