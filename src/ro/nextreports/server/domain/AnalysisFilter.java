package ro.nextreports.server.domain;

import java.io.Serializable;

import org.jcrom.annotations.JcrProperty;
import org.jcrom.annotations.JcrSerializedProperty;

public class AnalysisFilter extends EntityFragment {
	
	private static final long serialVersionUID = 1L;
	
	@JcrProperty
	private String column;
	
	@JcrProperty
	private String operator;
	
	@JcrSerializedProperty 
	private Serializable value;
	
	public AnalysisFilter() {		
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public Serializable getValue() {
		return value;
	}

	public void setValue(Serializable value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((column == null) ? 0 : column.hashCode());
		result = prime * result + ((operator == null) ? 0 : operator.hashCode());		
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}
	
	public AnalysisFilter clone() {
		AnalysisFilter obj = new AnalysisFilter();
		obj.setColumn(getColumn());
		obj.setOperator(getOperator());
		obj.setValue(getValue());		
		obj.setName(name);
		obj.setPath(path);
		return obj;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		if (! super.equals(obj)) return false;
		AnalysisFilter other = (AnalysisFilter) obj;
		if (column == null) {
			if (other.column != null) return false;
		} else if (!column.equals(other.column))
			return false;
		if (operator == null) {
			if (other.operator != null) return false;
		} else if (!operator.equals(other.operator))
			return false;		
		if (value == null) {
			if (other.value != null) return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AnalysisFilter [" +  super.toString() + " ,column=" + column + ", operator=" + operator + ", value=" + value + "]";
	}		

}
