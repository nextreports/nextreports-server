package ro.nextreports.server.audit;

import org.springframework.jdbc.support.incrementer.MySQLMaxValueIncrementer;

public class MySqlAuditor extends JdbcAuditor {

	@Override
	public int getNextEventId() {
		return new MySQLMaxValueIncrementer(getDataSource(), "NS_AUDIT_SEQ", "EVENT_ID").nextIntValue();
	}

}