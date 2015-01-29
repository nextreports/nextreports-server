package ro.nextreports.server.web.analysis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AnalysisRow implements Serializable {
	
	private List<Object> cellValues;
	private List<Map<String, Object>> styles;

	public AnalysisRow(Object[] cellValues) {
		this.cellValues = Arrays.asList(cellValues);
		styles = new ArrayList<Map<String, Object>>();
	}

    public AnalysisRow(List<Object> cellValues) {
		this.cellValues = cellValues;
		styles = new ArrayList<Map<String, Object>>();
	}

    public List<Object> getCellValues() {
		return cellValues;
	}

	public void setCellValues(List<Object> cellValues) {
		this.cellValues = cellValues;
	}	
	
	public Object getCellValues(int index) {
		return cellValues.get(index);
	}

	public List<Map<String, Object>> getStyles() {
		return styles;
	}

	public void setStyles(List<Map<String, Object>> styles) {
		this.styles = styles;
	}		
	
}
