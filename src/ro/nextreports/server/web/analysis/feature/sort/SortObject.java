package ro.nextreports.server.web.analysis.feature.sort;

import java.io.Serializable;

public class SortObject implements Serializable {
	private String column;
	private Boolean order;
	
	public SortObject() {			
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public Boolean getOrder() {
		return order;
	}

	public void setOrder(Boolean order) {
		this.order = order;
	}				
}