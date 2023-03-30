package developmentpermission.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:/application.properties")
public class PropertyUtil {
	@Autowired
	private Environment env;

	public String get(String key) {
		return env.getProperty(key);
	}
}
