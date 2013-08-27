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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.collections.MultiMap;
import org.apache.wicket.util.convert.IConverter;

import ro.nextreports.server.pivot.PivotField;
import ro.nextreports.server.pivot.PivotModel;
import ro.nextreports.server.pivot.PivotUtils;


/**
 * @author Decebal Suiu
 */
public class PivotTable extends Panel {

	private static final long serialVersionUID = 1L;

	public PivotTable(String id, PivotModel pivotModel) {
		super(id);
		
		List<PivotField> columnFields = pivotModel.getFields(PivotField.Area.COLUMN);
		List<PivotField> rowFields = pivotModel.getFields(PivotField.Area.ROW);
		List<PivotField> dataFields = pivotModel.getFields(PivotField.Area.DATA);
		
		List<List<Object>> rowKeys = pivotModel.getRowKeys();
		List<List<Object>> columnKeys = pivotModel.getColumnKeys();
		
		// rendering header
		RepeatingView column = new RepeatingView("header");
		add(column);
		int headerRowCount = columnFields.size();
		if (headerRowCount == 0) {
			headerRowCount = 1;
		}
		if (dataFields.size() > 1) {
			// add an extra row (the row with data field titles)
			headerRowCount++;
		}
		
		Component tmp = null;
		for (int i = 0; i < headerRowCount; i++) {
			// rendering row header (first columns)
			WebMarkupContainer tr = new WebMarkupContainer(column.newChildId());
			column.add(tr);
			RepeatingView rowHeader = new RepeatingView("rowHeader");
			tr.add(rowHeader);
			
			for (int j = 0; j < rowFields.size(); j++) {
				if (i < headerRowCount - 1) {
					// rendering an empty cell
					tmp = new Label(rowHeader.newChildId(), "");
					tmp.add(AttributeModifier.append("class", "empty"));
					rowHeader.add(tmp);
				} else {
					// rendering row field
					tmp = createTitleLabel(rowHeader.newChildId(), rowFields.get(j));
					rowHeader.add(tmp);
				}
			}
			
			// rendering column keys
			RepeatingView value = new RepeatingView("value");
			tr.add(value);
			for (List<Object> columnKey : columnKeys) {
				if (i < columnFields.size()) {
					PivotField columnField = columnFields.get(i);
					tmp = createValueLabel(value.newChildId(), columnKey.get(i), columnField);
					tmp.add(AttributeModifier.append("colspan", dataFields.size()));
					value.add(tmp);
				} else {
					for (PivotField dataField : dataFields) {
						tmp = createTitleLabel(value.newChildId(), dataField);
						value.add(tmp);
					}
				}
			}
			
			// rendering grand total column
			RepeatingView grandTotalColumn = new RepeatingView("grandTotalColumn");
			if (i == 0) {
				tmp = new Label(grandTotalColumn.newChildId(), "Grand Total");
				tmp.add(AttributeModifier.append("colspan", dataFields.size()));
				grandTotalColumn.add(tmp);
			} else if (i < columnFields.size()) {
				tmp = new WebMarkupContainer(grandTotalColumn.newChildId());
				tmp.add(AttributeModifier.append("colspan", dataFields.size()));
				tmp.add(AttributeModifier.append("class", "empty"));
				grandTotalColumn.add(tmp);
			} else {
				for (PivotField dataField : dataFields) {
					tmp = createTitleLabel(value.newChildId(), dataField);
					grandTotalColumn.add(tmp);
				}				
			}
			grandTotalColumn.setVisible(!columnFields.isEmpty() && pivotModel.isShowGrandTotalForRow());
			tr.add(grandTotalColumn);
		}
		
		// rendering rows
		RepeatingView row = new RepeatingView("row");
		add(row);
		for (List<Object> rowKey : rowKeys) {
			WebMarkupContainer tr = new WebMarkupContainer(row.newChildId());
			row.add(tr);
			RepeatingView rowHeader = new RepeatingView("rowHeader");
			tr.add(rowHeader);

			for (int k = 0; k < rowKey.size(); k++) {
				PivotField rowField = rowFields.get(k);
				tmp = createValueLabel(rowHeader.newChildId(), rowKey.get(k), rowField);
				rowHeader.add(tmp);
			}
			
			RepeatingView value = new RepeatingView("value");
			tr.add(value);
			
			for (List<Object> columnKey : columnKeys) {
				for (PivotField dataField : dataFields) {
					Number cellValue = (Number) pivotModel.getValueAt(dataField, rowKey, columnKey);
					tmp = createValueLabel(value.newChildId(), cellValue, dataField);				
					value.add(tmp);					
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
					tmp = createGrandTotalLabel(value.newChildId(), grandTotalForRow, true);
					tmp.add(AttributeModifier.append("class", "grand-total"));
					value.add(tmp);
				}
			}
		}
		
		WebMarkupContainer grandTotalRow = new WebMarkupContainer("grandTotalRow");
		grandTotalRow.setVisible(!rowFields.isEmpty() && pivotModel.isShowGrandTotalForColumn());
		add(grandTotalRow);
		
		Label grandTotalRowHeader = new Label("rowHeader", "Grand Total");
		grandTotalRowHeader.add(AttributeModifier.append("colspan", rowFields.size()));
		grandTotalRow.add(grandTotalRowHeader);
		
		RepeatingView value = new RepeatingView("value");
		grandTotalRow.add(value);
		Map<PivotField, Double> grandTotal = new HashMap<PivotField, Double>();
		for (List<Object> columnKey : columnKeys) {
			MultiMap<PivotField, Object> values = new MultiMap<PivotField, Object>();
			for (List<Object> rowKey: rowKeys) {
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
				tmp = createGrandTotalLabel(value.newChildId(), grandTotalForColumn, false);
				value.add(tmp);
			}
		}
		if (!columnFields.isEmpty() && pivotModel.isShowGrandTotalForRow()) {
			for (PivotField dataField : dataFields) {
				tmp = createGrandTotalLabel(value.newChildId(), grandTotal.get(dataField), true);
				value.add(tmp);
			}
		}
	}

	/**
	 * Retrieves a label that display the pivot table title (for fields on ROW and DATA areas) 
	 */
	protected Label createTitleLabel(String id, PivotField pivotField) {
		String title = pivotField.getTitle();
		if (pivotField.getArea().equals(PivotField.Area.DATA)) {
			title += " (" + pivotField.getAggregator().getFunction().toUpperCase() + ")"; 
		}

		return new Label(id, title);
	}

	protected Label createValueLabel(String id, Object value, final PivotField pivotField) {
		return new Label(id, Model.of((Serializable) value)) {
			
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			public <C> IConverter<C> getConverter(Class<C> type) {
				IConverter<C> converter = (IConverter<C>) pivotField.getConverter();
				if (converter != null) {
					return converter;
				}
				
				return super.getConverter(type);
			}

		};
	}
	
	protected Label createGrandTotalLabel(String id, Object value, boolean forRow) {
		return new Label(id, Model.of((Serializable) value));
	}

}
