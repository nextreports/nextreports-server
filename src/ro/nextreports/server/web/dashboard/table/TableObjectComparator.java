package ro.nextreports.server.web.dashboard.table;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Date;

public class TableObjectComparator implements Comparator<Object> {

	/**
	 * Constructor
	 */
	public TableObjectComparator() {
	}

	public int compare(Object o1, Object o2) {
		
		if ((o1 == null) && (o2 == null)) {
			return 0;
		}
		if (o1 == null) {
			return -1;
		}
		if (o2 == null) {
			return 1;
		}

		if (!o1.getClass().equals(o2.getClass())) {
			return o1.getClass().toString().compareTo(o2.getClass().toString());
		}

		String className = o1.getClass().getName();

		if (className.equals("java.lang.String")) {
			return ((String) o1).compareTo((String) o2);
		} else if (className.equals("java.lang.Byte") || className.equals("byte")) {
			return ((Byte) o1).compareTo((Byte) o2);
		} else if (className.equals("java.lang.Short") || className.equals("short")) {
			return ((Short) o1).compareTo((Short) o2);
		} else if (className.equals("java.lang.Integer") || className.equals("int")) {
			return ((Integer) o1).compareTo((Integer) o2);
		} else if (className.equals("java.lang.Float") || className.equals("float")) {
			return ((Float) o1).compareTo((Float) o2);
		} else if (className.equals("java.lang.Double") || className.equals("double")) {
			return ((Double) o1).compareTo((Double) o2);
		} else if (className.equals("java.math.BigDecimal")) {
			return ((BigDecimal) o1).compareTo((BigDecimal) o2);	
		} else if (className.equals("java.lang.BigInteger")) {
			return ((BigInteger) o1).compareTo((BigInteger) o2);
		} else if (className.equals("java.lang.Character") || className.equals("char")) {
			return ((Character) o1).compareTo((Character) o2);
		} else if (className.equals("java.lang.Boolean") || className.equals("boolean")) {
			return ((Boolean) o1).compareTo((Boolean) o2);
		} else if (className.equals("java.util.Date")) {
			return ((Date) o1).compareTo((Date) o2);
		} else if (className.equals("java.sql.Date")) {
			return ((java.sql.Date) o1).compareTo((java.sql.Date) o2);
		} else if (className.equals("java.sql.Timestamp")) {
			return ((java.sql.Timestamp) o1).compareTo((java.sql.Timestamp) o2);
		} else if (className.equals("java.sql.Time")) {
			return ((java.sql.Time) o1).compareTo((java.sql.Time) o2);
		}

		return o1.toString().compareTo(o2.toString());
	}

}
