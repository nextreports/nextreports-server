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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.engine.exporter.exception.NoDataFoundException;
import ro.nextreports.engine.exporter.util.StyleFormatConstants;
import ro.nextreports.engine.i18n.I18nLanguage;
import ro.nextreports.engine.util.HtmlUtil;
import ro.nextreports.engine.util.StringUtil;
import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.DrillEntityContext;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.WidgetUtil;
import ro.nextreports.server.web.common.table.BaseTable;
import ro.nextreports.server.web.common.table.LinkPropertyColumn;
import ro.nextreports.server.web.dashboard.EntityWidget;
import ro.nextreports.server.web.dashboard.Widget;

/**
 * @author Mihai Dinca-Panaitescu
 */
public class TableRendererPanel extends GenericPanel<Report> {
	
	// if we let a huge table to be added to a dashboard, an OutOfMemeory will raise
	// for such cases we will show only the first MAX_PER_PAGE rows
	private static final int MAX_PER_PAGE = 100;
	
	private static final Logger LOG = LoggerFactory.getLogger(TableRendererPanel.class);
	private DrillEntityContext drillContext;	
	private String drillPattern;
	private boolean allowSorting = false;
	// font size specified as a parameter in iframe
	private String iframeFontSize = null;
	private String iframeCellPadding = null;
	
	@SpringBean
	private DashboardService dashboardService;
	
	@SpringBean
	private StorageService storageService;
	
	public TableRendererPanel(String id, IModel<Report> model, String widgetId, DrillEntityContext drillContext,  boolean zoom) throws NoDataFoundException {
		this(id, model, widgetId, drillContext, zoom, null);
	}
		
	public TableRendererPanel(String id, IModel<Report> model, String widgetId, DrillEntityContext drillContext,  boolean zoom,  Map<String, Object> urlQueryParameters) throws NoDataFoundException {
		super(id, model);
		this.drillContext = drillContext;
		
		ro.nextreports.engine.Report rep = null; 
				
		if ((drillContext != null) && (drillContext.getColumn() > 0)) {
			rep = NextUtil.getNextReport(storageService.getSettings(), model.getObject());
			drillPattern = NextUtil.getDetailColumnPattern(rep, drillContext.getColumn()-1);			
		} else {
			if (model != null) {
				rep = NextUtil.getNextReport(storageService.getSettings(), model.getObject());
			} else {
				try {
					EntityWidget widget = (EntityWidget)dashboardService.getWidgetById(widgetId);	
					if (widget.getEntity() instanceof Chart) {						
						rep = NextUtil.getNextReport((Chart)widget.getEntity());						
					} else {
						rep = NextUtil.getNextReport(storageService.getSettings(), (Report)widget.getEntity());
					}
				} catch (NotFoundException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
		
		if (TableWidget.ALLOW_COLUMNS_SORTING) {
			this.allowSorting = reportHasHeaderAndOneDetail(rep);
		}
		
		if (urlQueryParameters != null) {
			Object tableFontSizeObj = urlQueryParameters.get("tableFontSize");
			if (tableFontSizeObj != null) {
				iframeFontSize = (String)tableFontSizeObj;
			}
			Object tableCellPaddingObj = urlQueryParameters.get("tableCellPadding");
			if (tableCellPaddingObj != null) {
				iframeCellPadding = (String)tableCellPaddingObj;
			}
		}
						
		TableDataProvider dataProvider = new TableDataProvider(widgetId, drillContext, urlQueryParameters);
		WebMarkupContainer container = new WebMarkupContainer("tableContainer");
		container.add(getCurrentTable(dataProvider, widgetId));
		boolean single = dashboardService.isSingleWidget(widgetId);		
		// table is the single widget in a dashboard with one column
		// make the height 100%
		if (single) {		
			container.add(AttributeModifier.replace("class", "tableWidgetViewFull"));
		}
        add(container);        
	}	
	
	private BaseTable getCurrentTable(TableDataProvider dataProvider, String widgetId ) throws NoDataFoundException {    	        
        List<String> tableHeader;
        List<String> tablePattern;
        I18nLanguage language;
		try {
			tableHeader = dataProvider.getHeader();
			tablePattern = dataProvider.getPattern();
			language = dataProvider.getLanguage();
		} catch (Exception e) {		
			e.printStackTrace();
			LOG.error(e.getMessage(), e);
			if (e instanceof NoDataFoundException) {
				throw (NoDataFoundException)e;
			} else {
				throw new RuntimeException(ExceptionUtils.getRootCauseMessage(e));
			}
		}
		int rowsPerPage = Integer.MAX_VALUE;				
		try {
			Widget widget = dashboardService.getWidgetById(widgetId);
			rowsPerPage = WidgetUtil.getRowsPerPage(dashboardService, widget);						
		} catch (NotFoundException e) {
			LOG.error(e.getMessage(), e);
		}
		BaseTable<RowData> table = new BaseTable<RowData>("table", getPropertyColumns(tableHeader, tablePattern, language), dataProvider, rowsPerPage) {
			
		};					
        return table;
    }
	
	private PropertyColumn<RowData, String> createPropertyColumn(List<String> header, final List<String> pattern, final I18nLanguage language, final int i) {
		return new PropertyColumn<RowData, String>(new Model<String>(header.get(i)),header.get(i), "cellValues." + i) {
	    	 public void populateItem(Item cellItem, String componentId, IModel rowModel) {
	    		setCellStyle(cellItem, rowModel, i);
				super.populateItem(cellItem, componentId, rowModel); 
	    	  }

			@Override
			public Component getHeader(String componentId) {
				Component header = super.getHeader(componentId);
				setCellStyle(header);
				return header;
			}	
			
			@Override				    
			public IModel<Object> getDataModel(IModel rowModel) {
				IModel<Object> model = super.getDataModel(rowModel);						
				String s = StringUtil.getValueAsString(model.getObject(), pattern.get(i), language);				
				return new Model(s);
			}
			
			@Override
			public String getSortProperty() {
				if (allowSorting) {
					return super.getSortProperty();
				} else {
					return null;
				}
			}
			
	    };
	}
	
	private LinkPropertyColumn<RowData> createLinkPropertyColumn(List<String> header, final List<String> pattern, final I18nLanguage language, final int i) {
		return new LinkPropertyColumn<RowData>(new Model<String>(header.get(i)), header.get(i), "cellValues." + i) {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(Item item, String componentId, IModel model, AjaxRequestTarget target) {
					
				String clickedValue;
				if (TableWidget.ALLOW_COLUMNS_SORTING) {
					clickedValue = StringUtil.getValueAsString(((RowData) model.getObject()).getCellValues().get(drillContext.getColumn() - 1), pattern.get(i), language);
				} else {
					clickedValue = ((RowData) model.getObject()).getCellValues().get(drillContext.getColumn() - 1).toString();
				}				
				try {
					onClickLink(target, clickedValue, drillPattern);
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}	
			}
			
			public void populateItem(Item cellItem, String componentId, IModel rowModel) {
				setCellStyle(cellItem, rowModel, i);
	    		super.populateItem(cellItem, componentId, rowModel); 
	    	}
			
			@Override
			public Component getHeader(String componentId) {
				Component header = super.getHeader(componentId);
				setCellStyle(header);
				return header;
			}
			
			@Override				    
			public IModel<Object> getDataModel(IModel rowModel) {
				IModel<Object> model = super.getDataModel(rowModel);				
				String s = StringUtil.getValueAsString(model.getObject(), pattern.get(i), language);
				return new Model(s);
			}
			
			@Override
			public String getSortProperty() {
				if (allowSorting) {
					return super.getSortProperty();
				} else {
					return null;
				}
			}
		};
	}

    private List<IColumn<RowData, String>> getPropertyColumns(List<String> header, final List<String> pattern, final I18nLanguage language) {
		List<IColumn<RowData, String>> columns = new ArrayList<IColumn<RowData, String>>();
		int columnCount = header.size();		
		boolean isDrillDownlable =  drillContext != null;
		boolean isLastDrill = (drillContext != null) && drillContext.isLast();		
		for (int i = 0; i < columnCount; i++) {					
			if (!isDrillDownlable || isLastDrill) {				
			    columns.add(createPropertyColumn(header, pattern, language, i));
			} else {				
				// link is added only for the column from the drill down report				
				if (drillContext.getColumn() != i+1) {
					columns.add(createPropertyColumn(header, pattern, language, i));
				} else {					
					columns.add(createLinkPropertyColumn(header, pattern, language, i));
				}
			}
		}

		return columns;
    }    
    
    protected void onClickLink(AjaxRequestTarget target, String value, String pattern) throws Exception {		
	}
    
    private void setCellStyle(Item cellItem, IModel rowModel, int rowIndex) {
    	List<Map<String, Object>> styles = ((RowData)rowModel.getObject()).getStyles();    	
		if (styles.size() > rowIndex) {				
			Map<String, Object> style = styles.get(rowIndex);
			boolean needed = setIframeStyleParameters(style);
			Color val = (Color) style.get(StyleFormatConstants.FONT_COLOR);
			if ((val != null) || (iframeFontSize != null)) {				
				String text = HtmlUtil.getCssCode(null, style, needed);
				cellItem.add(new AttributeAppender("style", Model.of(text)));
			}
		}
    }
    
    private void setCellStyle(Component header) {
    	Map<String, Object> style = new HashMap<String, Object>();
    	boolean needed = setIframeStyleParameters(style);
    	if (needed) {
    		String text = HtmlUtil.getCssCode(null, style, needed);
			header.add(new AttributeAppender("style", Model.of(text)));
    	}
    }
    
    private boolean setIframeStyleParameters(Map<String, Object> style) {
    	boolean needed = false;
		if (iframeFontSize != null) {
			style.put(StyleFormatConstants.FONT_SIZE, Float.parseFloat(iframeFontSize));
			if (iframeCellPadding != null) {
				Float padding = Float.parseFloat(iframeCellPadding);					
				style.put(StyleFormatConstants.PADDING_LEFT, padding);
				style.put(StyleFormatConstants.PADDING_RIGHT, padding);
				style.put(StyleFormatConstants.PADDING_TOP, padding);
				style.put(StyleFormatConstants.PADDING_BOTTOM, padding);				
			}
			needed = true;
		}	
		return needed;
    }
    
    private boolean reportHasHeaderAndOneDetail(ro.nextreports.engine.Report report) {
    	if ((report == null) || (report.getLayout() == null)) {
    		return false;
    	}
        return (report.getLayout().getHeaderBand().getRowCount() > 0) &&
         	   (report.getLayout().getDetailBand().getRowCount() == 1);
     }
}
