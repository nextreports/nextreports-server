package ro.nextreports.server.domain;

import org.jcrom.annotations.JcrProperty;

import ro.nextreports.engine.util.ObjectCloner;

public class AnalysisDeclaredColumn extends EntityFragment {
	
	private static final long serialVersionUID = 1L;
	
	@JcrProperty
	private String columnName;
	
	@JcrProperty(converter = JcrExpressionConverter.class)
	private String expression;
	
	@JcrProperty
	private String type;	
	
	public AnalysisDeclaredColumn() {		
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}	
	
	public AnalysisDeclaredColumn clone() {
		return ObjectCloner.silenceDeepCopy(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((expression == null) ? 0 : expression.hashCode());		
		result = prime * result + ((columnName == null) ? 0 : columnName.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		if (! super.equals(obj)) return false;
		AnalysisDeclaredColumn other = (AnalysisDeclaredColumn) obj;
		if (expression == null) {
			if (other.expression != null) return false;
		} else if (!expression.equals(other.expression))
			return false;		
		if (columnName == null) {
			if (other.columnName != null) return false;
		} else if (!columnName.equals(other.columnName))
			return false;
		if (type == null) {
			if (other.type != null) return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AnalysisDeclaredColumn [" + super.toString() + " ,columnName=" + columnName + ", expression=" + expression + ", type=" + type + "]";
	}

}
