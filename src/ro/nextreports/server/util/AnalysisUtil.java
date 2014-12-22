package ro.nextreports.server.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Iterator;

import ro.nextreports.server.domain.AnalysisFilter;

public class AnalysisUtil {

	public static String COUNT = "COUNT";
	public static String AVG = "AVG";
	public static String MIN = "MIN";
	public static String MAX = "MAX";
	public static String SUM = "SUM";
	public static List<String> aggregates = Arrays.asList(COUNT, AVG, MIN, MAX, SUM);

	public static final String AS = " as ";

	public static List<String> getJavaTypes() {
		List<String> result = new ArrayList<String>();
		result.add("java.lang.String");
		result.add("java.lang.Boolean");
		result.add("java.lang.Short");
		result.add("java.lang.Integer");
		result.add("java.lang.Float");
		result.add("java.lang.Double");
		result.add("java.util.Date");
		result.add("java.math.BigInteger");
		result.add("java.math.BigDecimal");
		return result;
	}

	public static String getJavaType(int jdbcType) {
		switch (jdbcType) {
		case Types.BIT:
			return Boolean.class.getName();
		case Types.TINYINT:
			return Byte.class.getName();
		case Types.SMALLINT:
			return Short.class.getName();
		case Types.CHAR:
			return String.class.getName();
		case Types.VARCHAR:
			return String.class.getName();
		case Types.DATE:
			return Date.class.getName();
		case Types.TIME:
			return Time.class.getName();
		case Types.TIMESTAMP:
			return Timestamp.class.getName();
		case Types.DOUBLE:
			return Double.class.getName();
		case Types.FLOAT:
			return Float.class.getName();
		case Types.INTEGER:
			return Integer.class.getName();
		case Types.BIGINT:
			return BigInteger.class.getName();
		case Types.NUMERIC:
			return BigDecimal.class.getName();
		case Types.DECIMAL:
			return BigDecimal.class.getName();
		case Types.BINARY:
			return byte[].class.getName();
		case Types.VARBINARY:
			return byte[].class.getName();
		case Types.OTHER:
			return Object.class.getName();
		default:
			return String.class.getName();
		}
	}

	public static String getColumnAlias(String fullColumnName) {
		if (fullColumnName.contains(AS)) {
			int index = fullColumnName.indexOf(AS);
			return fullColumnName.substring(index + AS.length());
		} else {
			return fullColumnName;
		}
	}

	public static String getColumnFullName(List<String> columns, String alias) {
		for (String col : columns) {
			if (col.contains(AS)) {
				if (alias.equals(getColumnAlias(col))) {
					return col;
				}
			} else {
				if (col.equals(alias)) {
					return col;
				}
			}
		}
		return alias;
	}

	public static String getColumnWithoutAlias(List<String> columns, String name) {
		for (String col : columns) {
			if (col.contains(AS)) {
				if (name.equals(getColumnAlias(col))) {
					int index = col.indexOf(AS);
					return col.substring(0, index);
				}
			} else {
				if (col.equals(name)) {
					return col;
				}
			}
		}
		return name;
	}

	public static boolean isAggregateColumn(String fullName) {
		for (String agg : aggregates) {
			if (fullName.toLowerCase().contains(agg.toLowerCase() + "(")) {
				return true;
			}
		}
		return false;
	}

	public static void removeFilterByColumnName(List<AnalysisFilter> list, String columnName) {
		if (list == null) {
			return;
		}
		for (Iterator<AnalysisFilter> it = list.iterator(); it.hasNext();) {
			AnalysisFilter fo = it.next();
			if (fo.getColumn().equals(columnName)) {
				it.remove();
				break;
			}
		}
	}

	public static boolean removeSortByColumnName(List<String> sortProperty, List<Boolean> ascending, String columnName) {
		if (sortProperty == null) {
			return false;
		}
		int index = -1;
		for (int i = 0, size = sortProperty.size(); i < size; i++) {
			String sort = sortProperty.get(i);
			if (sort.equals(columnName)) {
				index = i;
				break;
			}
		}
		if (index != -1) {
			sortProperty.remove(index);
			ascending.remove(index);
		}
		return (index == 0);
	}

	public static void removeGroupByColumnName(List<String> groups, String columnName) {
		if (groups == null) {
			return;
		}
		for (Iterator<String> it = groups.iterator(); it.hasNext();) {
			String group = it.next();
			if (group.equals(columnName)) {
				it.remove();
				break;
			}
		}
	}

	public static void removeSortColumns(List<String> selectColumns, List<String> sorts, List<Boolean> ascending) {
		List<String> removedSorts = new ArrayList<String>();
		for (String sort : sorts) {
			if (!selectColumns.contains(sort)) {
				removedSorts.add(sort);
			}
		}
		for (String sort : removedSorts) {
			removeSortByColumnName(sorts, ascending, sort);
		}

	}

	// Group by columns must be removed if the columns from select sql are
	// deleted
	public static void removeGroupColumns(List<String> selectColumns, List<String> groups) {
		List<String> removedGroups = new ArrayList<String>();
		for (String group : groups) {
			if (!containsColumnByAlias(selectColumns, group)) {
				removedGroups.add(group);
			}
		}
		groups.removeAll(removedGroups);
	}

	public static boolean containsColumnByAlias(List<String> columns, String alias) {
		for (String column : columns) {
			if (getColumnAlias(column).equals(alias)) {
				return true;
			}
		}
		return false;
	}

	public static List<AnalysisFilter> getFilters(List<String> columns, List<AnalysisFilter> list, boolean isAgg) {
		List<AnalysisFilter> result = new ArrayList<AnalysisFilter>();
		if (list == null) {
			return result;
		}
		for (AnalysisFilter fo : list) {
			String name = getColumnFullName(columns, fo.getColumn());
			boolean isAggCol = isAggregateColumn(name);
			if (isAgg) {
				if (isAggCol) {
					result.add(fo);
				}
			} else {
				if (!isAggCol) {
					result.add(fo);
				}
			}
		}
		return result;
	}

}
