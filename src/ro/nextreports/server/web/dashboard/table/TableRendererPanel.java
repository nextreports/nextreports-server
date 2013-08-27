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
package ro.nextreports.server.web.dashboard.table;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.domain.DrillEntityContext;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.web.common.panel.GenericPanel;
import ro.nextreports.server.web.common.table.BaseTable;
import ro.nextreports.server.web.common.table.LinkPropertyColumn;

import ro.nextreports.engine.exporter.util.StyleFormatConstants;
import ro.nextreports.engine.util.HtmlUtil;

/**
 * @author Mihai Dinca-Panaitescu
 */
public class TableRendererPanel extends GenericPanel<Report> {
	
	// if we let a huge table to be added to a dashboard, an OutOfMemeory will raise
	// for such cases we will show only the first MAX_PER_PAGE rows
	private static final int MAX_PER_PAGE = 100;
	
	private static final Logger LOG = LoggerFactory.getLogger(TableRendererPanel.class);
	private DrillEntityContext drillContext;	
	
	public TableRendererPanel(String id, IModel<Report> model, String widgetId, DrillEntityContext drillContext,  boolean zoom) {
		this(id, model, widgetId, drillContext, zoom, null);
	}
		
	public TableRendererPanel(String id, IModel<Report> model, String widgetId, DrillEntityContext drillContext,  boolean zoom,  Map<String, Object> urlQueryParameters) {
		super(id, model);
		this.drillContext = drillContext;		
						
		TableDataProvider dataProvider = new TableDataProvider(widgetId, drillContext, urlQueryParameters);
        add(getCurrentTable(dataProvider));        
	}	
	
	private BaseTable getCurrentTable(TableDataProvider dataProvider ) {    	        
        List<String> tableHeader;
		try {
			tableHeader = dataProvider.getHeader();			
		} catch (Exception e) {		
			e.printStackTrace();
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(ExceptionUtils.getRootCauseMessage(e));
		}
		int rowsPerPage = Integer.MAX_VALUE;
		if (dataProvider.size() > MAX_PER_PAGE) {
			rowsPerPage = MAX_PER_PAGE;
		}
		BaseTable<RowData> table = new BaseTable<RowData>("table", getPropertyColumns(tableHeader), dataProvider, rowsPerPage);		
        return table;
    }

    private List<IColumn<RowData>> getPropertyColumns(List<String> header) {
		List<IColumn<RowData>> columns = new ArrayList<IColumn<RowData>>();
		int columnCount = header.size();		
		boolean isDrillDownlable =  drillContext != null;
		boolean isLastDrill = (drillContext != null) && drillContext.isLast();		
		for (int i = 0; i < columnCount; i++) {		
			final int j = i;
			if (!isDrillDownlable || isLastDrill) {				
			    columns.add(new PropertyColumn<RowData>(new Model<String>(header.get(i)), "cellValues." + i) {
			    	 public void populateItem(Item cellItem, String componentId, IModel rowModel) {
			    		setCellStyle(cellItem, rowModel, j);
						super.populateItem(cellItem, componentId, rowModel); 
			    	  }
			    	
			    });
			} else {				
				// link is added only for the column from the drill down report				
				if (drillContext.getColumn() != i+1) {
					columns.add(new PropertyColumn<RowData>(new Model<String>(header.get(i)), "cellValues." + i));
				} else {					
					columns.add(new LinkPropertyColumn<RowData>(new Model<String>(header.get(i)), "cellValues." + i) {
						
						private static final long serialVersionUID = 1L;

						@Override
						public void onClick(Item item, String componentId, IModel model, AjaxRequestTarget target) {
													
							String clickedValue = ((RowData) model.getObject()).getCellValues().get(drillContext.getColumn() - 1).toString();																																
							try {
								onClickLink(target, clickedValue);
							} catch (Exception e) {
								LOG.error(e.getMessage(), e);
							}	
						}
						
						public void populateItem(Item cellItem, String componentId, IModel rowModel) {
							setCellStyle(cellItem, rowModel, j);
				    		super.populateItem(cellItem, componentId, rowModel); 
				    	  }
					});
				}
			}
		}

		return columns;
    }    
    
    protected void onClickLink(AjaxRequestTarget target, String value) throws Exception {		
	}
    
    private void setCellStyle(Item cellItem, IModel rowModel, int rowIndex) {
    	List<Map<String, Object>> styles = ((RowData)rowModel.getObject()).getStyles();
		if (styles.size() > rowIndex) {
			Map<String, Object> style = styles.get(rowIndex);
			Color val = (Color) style.get(StyleFormatConstants.FONT_COLOR);
			if (val != null) {
				String text = HtmlUtil.getCssCode(null, style, false);
				cellItem.add(new AttributeAppender("style", Model.of(text)));
			}
		}
    }
}
