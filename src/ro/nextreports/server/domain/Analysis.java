package ro.nextreports.server.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrProperty;

import ro.nextreports.engine.querybuilder.sql.Operator;
import ro.nextreports.server.util.AnalysisUtil;

public class Analysis extends Entity {

	private static final long serialVersionUID = 1L;

	@JcrProperty
	private String reportId;

	@JcrProperty
	private String tableName;

	@JcrProperty(converter = JcrListExpressionConverter.class)
	private List<String> columns;

	@JcrProperty(converter = JcrMapExpressionConverter.class)
	private Map<String, String> columnTypes = new HashMap<String, String>();

	@JcrProperty
	private List<Boolean> selected = new LinkedList<Boolean>();

	@JcrProperty
	private List<String> sortProperty = new LinkedList<String>();

	@JcrProperty
	private List<Boolean> ascending = new LinkedList<Boolean>();

	@JcrProperty
	private int rowsPerPage;

	@JcrChildNode
	private List<AnalysisFilter> filters = new ArrayList<AnalysisFilter>();

	@JcrChildNode
	private List<AnalysisDeclaredColumn> declaredColumns = new ArrayList<AnalysisDeclaredColumn>();

	@JcrProperty
	private List<String> groups = new LinkedList<String>();
	
	@JcrProperty
    private boolean freezed;

	// business fields unimportant for current Analysis object's state
	private boolean firstSortRemoved;
	private boolean changeFirstSortOrder;

	public Analysis() {
	}			

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public List<String> getColumns() {
		return columns;
	}

	public List<String> getSimpleColumns() {
		List<String> result = new ArrayList<String>();
		for (String name : columns) {
			result.add(AnalysisUtil.getColumnAlias(name));
		}
		return result;
	}

	public void setColumns(List<String> columns) {
		this.columns = columns;
	}

	public List<String> getSortProperty() {
		return sortProperty;
	}

	public void setSortProperty(List<String> sortProperty) {
		this.sortProperty = sortProperty;
	}

	public List<Boolean> getAscending() {
		return ascending;
	}

	public void setAscending(List<Boolean> ascending) {
		this.ascending = ascending;
	}

	public int getRowsPerPage() {
		return rowsPerPage;
	}

	public void setRowsPerPage(int rowsPerPage) {
		this.rowsPerPage = rowsPerPage;
	}

	public List<Boolean> getSelected() {
		return selected;
	}

	public void setSelected(List<Boolean> selected) {
		this.selected = selected;
	}

	public boolean isFirstSortRemoved() {
		return firstSortRemoved;
	}

	public void setFirstSortRemoved(boolean firstSortRemoved) {
		this.firstSortRemoved = firstSortRemoved;
	}

	public boolean isChangeFirstSortOrder() {
		return changeFirstSortOrder;
	}

	public void setChangeFirstSortOrder(boolean changeFirstSortOrder) {
		this.changeFirstSortOrder = changeFirstSortOrder;
	}

	public List<AnalysisFilter> getFilters() {
		return filters;
	}

	public void setFilters(List<AnalysisFilter> filters) {
		this.filters = filters;
	}

	public List<AnalysisDeclaredColumn> getDeclaredColumns() {
		return declaredColumns;
	}

	public void addDeclaredColumn(AnalysisDeclaredColumn col) {
		if (declaredColumns == null) {
			declaredColumns = new ArrayList<AnalysisDeclaredColumn>();
		}
		declaredColumns.add(col);
		String colName = col.getExpression() + AnalysisUtil.AS + col.getColumnName();
		columns.add(colName);
		columnTypes.put(colName, col.getType());
		selected.add(true);
	}

	public void editDeclaredColumn(int index, int oldColumnIndex, int oldFilterIndex, int oldSortIndex, int oldGroupIndex,
			AnalysisDeclaredColumn col) {
		declaredColumns.set(index, col);
		String colName = col.getExpression() + AnalysisUtil.AS + col.getColumnName();
		int foundIndex = oldColumnIndex;
		columns.set(foundIndex, colName);
		columnTypes.put(colName, col.getType());
		selected.set(foundIndex, true);
		// modify column name in filters, sorts, groups,..
		if (oldFilterIndex != -1) {
			filters.get(oldFilterIndex).setColumn(col.getColumnName());
		}
		if (oldSortIndex != -1) {
			sortProperty.set(oldSortIndex, col.getColumnName());
		}
		if (oldGroupIndex != -1) {
			groups.set(oldGroupIndex, col.getColumnName());
		}
	}

	public void deleteDeclaredColumn(AnalysisDeclaredColumn col) {
		declaredColumns.remove(col);
		String colName = col.getExpression() + AnalysisUtil.AS + col.getColumnName();
		int foundIndex = getColumnIndexByName(colName);
		columns.remove(colName);
		columnTypes.remove(colName);
		if (foundIndex != -1) {
			selected.remove(foundIndex);
		}
		// / must also remove declared column from filters, sorts, groups ,..
		AnalysisUtil.removeFilterByColumnName(filters, col.getColumnName());
		boolean isFirstSort = AnalysisUtil.removeSortByColumnName(sortProperty, ascending, col.getColumnName());
		if (isFirstSort) {
			firstSortRemoved = true;
		}
		AnalysisUtil.removeGroupByColumnName(groups, col.getColumnName());
	}

	public int getColumnIndexByName(String name) {
		if ((columns == null) || columns.isEmpty()) {
			return -1;
		}
		for (int i = 0, size = columns.size(); i < size; i++) {			
			if (columns.get(i).equals(name)) {
				return i;
			}
		}
		return -1;
	}

	public int getFilterIndexByName(String name) {
		if ((filters == null) || filters.isEmpty()) {
			return -1;
		}
		for (int i = 0, size = filters.size(); i < size; i++) {
			if (filters.get(i).getColumn().equals(name)) {
				return i;
			}
		}
		return -1;
	}

	public int getSortIndexByName(String name) {
		if ((sortProperty == null) || sortProperty.isEmpty()) {
			return -1;
		}
		for (int i = 0, size = sortProperty.size(); i < size; i++) {
			if (sortProperty.get(i).equals(name)) {
				return i;
			}
		}
		return -1;
	}

	public int getGroupIndexByName(String name) {
		if ((groups == null) || groups.isEmpty()) {
			return -1;
		}
		for (int i = 0, size = groups.size(); i < size; i++) {
			if (groups.get(i).equals(name)) {
				return i;
			}
		}
		return -1;
	}

	public Map<String, String> getColumnTypes() {
		return columnTypes;
	}

	public void setColumnTypes(Map<String, String> columnTypes) {
		this.columnTypes = columnTypes;
	}

	public List<String> getGroups() {
		return groups;
	}

	public void setGroups(List<String> groups) {
		this.groups = groups;
	}
		
	public boolean isFreezed() {
		return freezed;
	}

	public void setFreezed(boolean freezed) {
		this.freezed = freezed;
	}

	public String toSql(boolean allColumns) {
		StringBuilder sb = new StringBuilder("select ");
		if ((columns == null) || columns.isEmpty() || allColumns) {
			sb.append("* ");
		} else {
			List<String> selColumns = getSelectedColumns();			
			for (int i = 0, size = selColumns.size(); i < size; i++) {
				sb.append(selColumns.get(i));
				if (i < size - 1) {
					sb.append(", ");
				}
			}
		}

		sb.append(" from ").append(tableName);

		List<AnalysisFilter> nonAggFilters = AnalysisUtil.getFilters(columns, filters, false);
		if (!nonAggFilters.isEmpty()) {
			sb.append(" where ");
			appendFilters(nonAggFilters, sb, false);
		}

		if ((groups != null) && !groups.isEmpty()) {
			sb.append(" group by ");
			for (int i = 0, size = groups.size(); i < size; i++) {
				sb.append(groups.get(i));
				if (i < size - 1) {
					sb.append(", ");
				}
			}
		}

		// orient db does not support having
		// so we create a subselect at the end
		// keep this code if we want to change orientdb in the future
		//List<AnalysisFilter> aggFilters = AnalysisUtil.getFilters(columns, filters, true);
		//if (!aggFilters.isEmpty()) {
		//	sb.append(" having ");
		//	appendFilters(aggFilters, sb);
		//}

		if ((sortProperty != null) && !sortProperty.isEmpty()) {
			sb.append(" order by ");
			for (int i = 0, size = sortProperty.size(); i < size; i++) {
				sb.append(sortProperty.get(i));
				if (!ascending.get(i)) {
					sb.append(" desc");
				} else {
					sb.append(" asc");
				}
				if (i < size - 1) {
					sb.append(", ");
				}
			}

		}
		
		List<AnalysisFilter> aggFilters = AnalysisUtil.getFilters(columns, filters, true);
		if (!aggFilters.isEmpty()) {
			// create a subselect because orient db does not support having
			StringBuilder result = new StringBuilder("select from ( ");
			result.append(sb.toString());
			result.append(" ) where ");
			appendFilters(aggFilters, result, true);
			return result.toString();
		} else {		
			return sb.toString();
		}
	}

	public ArrayList<String> getSelectedColumns() {
		ArrayList<String> result = new ArrayList<String>();
		if ((selected == null) || selected.isEmpty()) {
			return new ArrayList<String>(columns);
		} else {
			for (int i = 0, size = selected.size(); i < size; i++) {
				if (selected.get(i)) {
					result.add(columns.get(i));
				}
			}
			return result;
		}
	}

	private void appendFilters(List<AnalysisFilter> filtersList, StringBuilder sb, boolean useAlias) {
		for (int i = 0, size = filtersList.size(); i < size; i++) {
			AnalysisFilter fo = filtersList.get(i);
			String operator = fo.getOperator();
			if (useAlias) {
				sb.append(AnalysisUtil.getColumnAlias(fo.getColumn()));
			} else {
				sb.append(AnalysisUtil.getColumnWithoutAlias(columns, fo.getColumn()));
			}
			sb.append(" ").append(operator);
			if (!Operator.isUnar(operator)) {
				sb.append(" ");
				boolean in = Operator.IN.equals(operator) || Operator.NOT_IN.equals(operator);				
				boolean needApos = false;
				boolean isDate = false;
				if (columnTypes != null) {
					needApos = "java.lang.String".equals(columnTypes.get(AnalysisUtil.getColumnFullName(columns, fo.getColumn())));
					isDate = "java.util.Date".equals(columnTypes.get(AnalysisUtil.getColumnFullName(columns, fo.getColumn()))) ||
							"java.sql.Timestamp".equals(columnTypes.get(AnalysisUtil.getColumnFullName(columns, fo.getColumn()))) ||
							"java.sql.Time".equals(columnTypes.get(AnalysisUtil.getColumnFullName(columns, fo.getColumn())));					
				}
				if (in) {
					sb.append("[");
					String[] values = fo.getValue().toString().split(";");
					for (int j = 0, len = values.length; j < len; j++) {
						if (needApos && !values[j].startsWith("'")) {
							sb.append("'");
						} else if (isDate) {
							sb.append("date('");
						}
						sb.append(values[j]);
						if (needApos && !values[j].endsWith("'")) {
							sb.append("'");
						} else if (isDate) {
							sb.append("','yyyyMMdd')");
						}
						if (j < len - 1) {
							sb.append(",");
						}
					}
					sb.append("]");
				} else if (Operator.BETWEEN.equals(operator)) {
					String[] values = fo.getValue().toString().split(";");
					for (int j = 0, len = values.length; j < len; j++) {
						if (needApos && !values[j].startsWith("'")) {
							sb.append("'");
						} else if (isDate) {
							sb.append("date('");
						}

						sb.append(values[j]);
						if (needApos && !values[j].endsWith("'")) {
							sb.append("'");
						}  else if (isDate) {
							sb.append("','yyyyMMdd')");
						}
						if (j < len - 1) {
							sb.append(" and ");
						}
					}
				} else {
					if (needApos && !fo.getValue().toString().startsWith("'")) {
						sb.append("'");
					} else if (isDate) {
						sb.append("date('");
					}
					sb.append(fo.getValue());
					if (needApos && !fo.getValue().toString().endsWith("'")) {
						sb.append("'");
					}  else if (isDate) {
						sb.append("','yyyyMMdd')");
					}
				}

			}
			if (i < size - 1) {
				sb.append(" and ");
			}
		}
	}
	
	@Override
    public boolean allowPermissions() {
        return true;
    }

	@Override
	public String toString() {
		return "Analysis [" + super.toString() + " ,tableName=" + tableName + ", columns=" + columns + ", columnTypes=" + columnTypes + ", selected=" + selected + ", sortProperty="
				+ sortProperty + ", ascending=" + ascending + ", filters=" + filters + ", declaredColumns=" + declaredColumns
				+ ", groups=" + groups + ", rowsPerPage=" + rowsPerPage + ", freezed=" + freezed + "]";
	}

}
