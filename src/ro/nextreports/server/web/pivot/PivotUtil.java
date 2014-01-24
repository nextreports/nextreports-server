/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.nextreports.server.web.pivot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.collections.MultiMap;

import ro.nextreports.server.pivot.Aggregator;
import ro.nextreports.server.pivot.PivotField;
import ro.nextreports.server.pivot.PivotModel;
import ro.nextreports.server.pivot.PivotUtils;
import ro.nextreports.server.web.dashboard.pivot.PivotWidget;

import ro.nextreports.engine.exporter.util.TableData;

/**
 * 
 * @author Mihai Dinca-Panaitescu
 *
 */
public class PivotUtil {		
	
	private static int NUMBER_OF_DECIMALS = 3;
	private static double FACTOR = Math.pow(10.0, NUMBER_OF_DECIMALS);
	
	//@see PivoTable for same algorithm
	public static TableData getTableData(PivotModel pivotModel) {
		
		List<List<Object>> data = new ArrayList<List<Object>>();
		
		List<PivotField> columnFields = pivotModel.getFields(PivotField.Area.COLUMN);
		List<PivotField> rowFields = pivotModel.getFields(PivotField.Area.ROW);
		List<PivotField> dataFields = pivotModel.getFields(PivotField.Area.DATA);
		
		List<List<Object>> rowKeys = pivotModel.getRowKeys();
		List<List<Object>> columnKeys = pivotModel.getColumnKeys();
				
		int headerRowCount = columnFields.size();
		if (headerRowCount == 0) {
			headerRowCount = 1;
		}
		if (dataFields.size() > 1) {
			// add an extra row (the row with data field titles)
			headerRowCount++;
		}
				
		for (int i = 0; i < headerRowCount; i++) {
			
			List<Object> row = new ArrayList<Object>();
			data.add(row);
			
			for (int j = 0; j < rowFields.size(); j++) {
				if (i < headerRowCount - 1) {
					// empty cell
					row.add("");
				} else {
					row.add(rowFields.get(j).getName());					
				}
			}
			
			// column keys			
			for (List<Object> columnKey : columnKeys) {
				if (i < columnFields.size()) {					
					row.add(columnKey.get(i));	
					for (int k=0; k<dataFields.size()-1; k++) {
						row.add("");
					}					
				} else {
					for (PivotField dataField : dataFields) {
						row.add(dataField.getTitle());
					}
				}
			}
			
			// grand total column
			if (!columnFields.isEmpty() && pivotModel.isShowGrandTotalForRow()) {
				if (i == 0) {
					row.add(new StringResourceModel("pivot.grandTotal", null).getString());
					for (int k = 0; k < dataFields.size() - 1; k++) {
						row.add("");
					}
				} else if (i < columnFields.size()) {
					for (int k = 0; k < dataFields.size() - 1; k++) {
						row.add("");
					}
				} else {
					for (PivotField dataField : dataFields) {
						row.add(dataField.getName());
					}
				}
			}
		}
		
		// rows		
		for (List<Object> rowKey : rowKeys) {
			List<Object> row = new ArrayList<Object>();		
			data.add(row);
			for (int k = 0; k < rowKey.size(); k++) {				
			    row.add(convert(rowKey.get(k)));				
			}
								
			for (List<Object> columnKey : columnKeys) {
				for (PivotField dataField : dataFields) {
					Number cellValue = (Number) pivotModel.getValueAt(dataField, rowKey, columnKey);
					row.add(convert(cellValue));														
				}
			}
				
			if (!columnFields.isEmpty() && pivotModel.isShowGrandTotalForRow()) {
				MultiMap<PivotField, Object> values = new MultiMap<PivotField, Object>();
				for (List<Object> columnKey: columnKeys) {
					for (PivotField dataField : dataFields) {
						values.addValue(dataField, pivotModel.getValueAt(dataField, rowKey, columnKey));
					}
				}
				for (PivotField dataField : dataFields) {
					double grandTotalForRow = PivotUtils.getSummary(dataField, values.get(dataField)).doubleValue();
					row.add(convert(grandTotalForRow));					
				}
			}
		}
								
		if (!rowFields.isEmpty() && pivotModel.isShowGrandTotalForColumn()) {
			List<Object> row = new ArrayList<Object>();		
			data.add(row);
			
			row.add(new StringResourceModel("pivot.grandTotal", null).getString());
			for (int k = 0; k < rowFields.size() - 1; k++) {
				row.add("");
			}

			Map<PivotField, Double> grandTotal = new HashMap<PivotField, Double>();
			for (List<Object> columnKey : columnKeys) {
				MultiMap<PivotField, Object> values = new MultiMap<PivotField, Object>();
				for (List<Object> rowKey : rowKeys) {
					for (PivotField dataField : dataFields) {
						values.addValue(dataField, pivotModel.getValueAt(dataField, rowKey, columnKey));
					}
				}
				for (PivotField dataField : dataFields) {
					double grandTotalForColumn = PivotUtils.getSummary(dataField, values.get(dataField)).doubleValue();
					if (!grandTotal.containsKey(dataField)) {
						grandTotal.put(dataField, grandTotalForColumn);
					} else {
						grandTotal.put(dataField, grandTotal.get(dataField) + grandTotalForColumn);
					}
					row.add(convert(grandTotalForColumn));
				}
			}
			
			if (!columnFields.isEmpty() && pivotModel.isShowGrandTotalForRow()) {
				for (PivotField dataField : dataFields) {
					row.add(convert(grandTotal.get(dataField)));				
				}
			}
		}
								
		return new TableData(null, data, null);
	}
	
	private static Object convert(Object obj) {
		
		if (obj instanceof Double) {	
			if (Math.round((Double)obj) == ((Double)obj).longValue()) {
				return ((Double)obj).intValue();
			} else {
				return Math.round( (Double)obj * FACTOR) / FACTOR;
			}
		} 
		return obj;
	}
	
	public static void readPivotPropertiesFromWidget(PivotModel pivotModel, PivotWidget widget) {
		pivotModel.setShowGrandTotalForRow(widget.showRowTotal());
    	pivotModel.setShowGrandTotalForColumn(widget.showColumnTotal());
    	String[] rowFields = widget.getRowFields().split(",");
    	int index = 0;
    	for (String rowField : rowFields) {
    		if (!rowField.isEmpty() && (pivotModel.getField(rowField) != null)) {
    			pivotModel.getField(rowField).setArea(PivotField.Area.ROW).setAreaIndex(index);
    			index++;
    		}	
    	}
    	String[] columnFields = widget.getColumnFields().split(",");
    	index = 0;
    	for (String columnField : columnFields) {
    		if (!columnField.isEmpty() && (pivotModel.getField(columnField) != null)) {
    			pivotModel.getField(columnField).setArea(PivotField.Area.COLUMN).setAreaIndex(index);
    			index++;
    		}
    	}	
    	String[] dataFields = widget.getDataFields().split(",");
    	index = 0;
    	for (String dataField : dataFields) {
    		if (!dataField.isEmpty() && (pivotModel.getField(dataField) != null)) {
    			pivotModel.getField(dataField).setArea(PivotField.Area.DATA).setAreaIndex(index);
    			index++;
    		}
    	}	
    	String[] aggregators = widget.getDataAggregators().split(",");
    	for (int i=0, size=aggregators.length; i<size; i++) {
    		String aggField = aggregators[i];
    		if (!aggField.isEmpty() && (pivotModel.getField(dataFields[i]) != null)) {
    			pivotModel.getField(dataFields[i]).setAggregator(Aggregator.get(aggField));
    		}
    	}	
	}
	
	public static void writePivotPropertiesToWidget(PivotModel pivotModel, PivotWidget widget) {		
		widget.setRowFields(getFieldsAsString(pivotModel.getFields(PivotField.Area.ROW)));
		widget.setColumnFields(getFieldsAsString(pivotModel.getFields(PivotField.Area.COLUMN)));
		widget.setDataFields(getFieldsAsString(pivotModel.getFields(PivotField.Area.DATA)));
		widget.setDataAggregators(getAggregators(pivotModel.getFields(PivotField.Area.DATA)));
		widget.setShowRowTotal(pivotModel.isShowGrandTotalForRow());
		widget.setShowColumnTotal(pivotModel.isShowGrandTotalForColumn());		
	}
	
	private static String getFieldsAsString(List<PivotField> fields) {		
		StringBuilder sb = new StringBuilder();
		if (fields != null) {
			for (int i = 0, size = fields.size(); i < size; i++) {
				sb.append(fields.get(i).getName());
				if (i < size-1) {
					sb.append(",");
				}
			}
		}
		return sb.toString();
	}
	
	private static String getAggregators(List<PivotField> fields) {
		StringBuilder sb = new StringBuilder();
		if (fields != null) {
			for (int i = 0, size = fields.size(); i < size; i++) {
				sb.append(fields.get(i).getAggregator().getFunction());
				if (i < size-1) {
					sb.append(",");
				}
			}
		}
		return sb.toString();
	}

}
