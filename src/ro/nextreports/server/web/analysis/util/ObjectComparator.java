package ro.nextreports.server.web.analysis.util;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.Date;

public class ObjectComparator implements Comparator {

	@Override
	public int compare(Object o1, Object o2) {
		
		if ((o1 == null) || (o2 == null)) {
			return -1;
		}

		if (!o1.getClass().equals(o2.getClass())) {
			return -1;
		}
		
		String className = o1.getClass().getName();		
		
		 if (className.equals("java.lang.String")) {
             return ((String)o1).compareTo((String)o2);
         } else if (className.equals("java.lang.Byte")) {
             return ((Byte)o1).compareTo((Byte)o2);
         } else if (className.equals("java.lang.Short")) {
             return ((Short)o1).compareTo((Short)o2);
         } else if (className.equals("java.lang.Integer")) {
             return ((Integer)o1).compareTo((Integer)o2);
         } else if (className.equals("java.lang.Float")) {
             return ((Float)o1).compareTo((Float)o2);
         } else if (className.equals("java.lang.Double")) {
             return ((Double)o1).compareTo((Double)o2);
         } else if (className.equals("java.lang.BigInteger")) {
             return ((BigInteger)o1).compareTo((BigInteger)o2);
         } else if (className.equals("java.lang.Character")) {
             return ((Character)o1).compareTo((Character)o2);
         } else if (className.equals("java.lang.Boolean")) {
             return ((Boolean)o1).compareTo((Boolean)o2);
         } else if (className.equals("java.util.Date")) {
             return ((Date)o1).compareTo((Date)o2);
         } else if (className.equals("java.sql.Timestamp")) {
             return ((Timestamp)o1).compareTo((Timestamp)o2);
         }
		 
		 return -1;

	}

}
