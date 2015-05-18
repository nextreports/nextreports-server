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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.IteratorUtils;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.IFilterStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.domain.DrillEntityContext;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.util.ServerUtil;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.engine.exporter.exception.NoDataFoundException;
import ro.nextreports.engine.exporter.util.TableData;
import ro.nextreports.engine.i18n.I18nLanguage;
import ro.nextreports.engine.util.ObjectCloner;

/**
 * @author Decebal Suiu
 */
public class TableDataProvider extends SortableDataProvider<RowData, String> implements IFilterStateLocator<RowData> {

    private static final long serialVersionUID = 1L;
    
    public static final String SORT_PROP_POS_SUFFIX = "_sortPos";
    public static final String SORT_PROP_DIR_SUFFIX = "_sortDir";

    private static final Logger LOG = LoggerFactory.getLogger(TableDataProvider.class);

    private transient List<RowData> cache;
    private transient List<String> header;
    private transient List<String> pattern;
    private transient I18nLanguage language;
    private String widgetId;
    private DrillEntityContext drillContext;
    private Map<String, Object> urlQueryParameters; 
    
    private int sortPos;
    private int sortDir;
    
    private RowData tableFilter;
    
    @SpringBean
    private DashboardService dashboardService;

    public TableDataProvider() {
        this(null, null);
    }

    public TableDataProvider(String widgetId, Map<String, Object> urlQueryParameters) {
        this(widgetId, null, urlQueryParameters);
    }
    
    public TableDataProvider(String widgetId, DrillEntityContext drillContext, Map<String, Object> urlQueryParameters) {        
        this.widgetId = widgetId;
        this.drillContext = drillContext;
        this.urlQueryParameters = urlQueryParameters;     
       
        Injector.get().inject(this);
    }

    @SuppressWarnings("unchecked")
    @Override
	public Iterator<RowData> iterator(long first, long count) {
        try {
        	List<RowData> data = getCache();
        	String username = ServerUtil.getUsername();
        	String sortPosKey = username == null ? widgetId + SORT_PROP_POS_SUFFIX : widgetId + "_" + username + SORT_PROP_POS_SUFFIX;
        	String sortDirKey = username == null ? widgetId + SORT_PROP_DIR_SUFFIX : widgetId + "_" + username + SORT_PROP_DIR_SUFFIX;
        	if (getSort() != null) {      
        		sortDir = getSort().isAscending() ? 1 : -1;
            	sortPos = getPropertyPosition(getSort().getProperty());
            	// save these properties to be used by TableResource (save to excel)            	
        		System.setProperty(sortPosKey, String.valueOf(sortPos));
        		System.setProperty(sortDirKey, String.valueOf(sortDir));
				Collections.sort(data, new Comparator<RowData>() {
					public int compare(RowData o1, RowData o2) {											
						if (sortPos != -1) {
							if ((o1.getCellValues(sortPos) == null) && (o2.getCellValues(sortPos) == null)) {
								return 0;
							} else if (o1.getCellValues(sortPos) == null) {
								return sortDir;
							} else if (o2.getCellValues(sortPos) == null) {
								return -sortDir;
							} else {								
								return sortDir * new TableObjectComparator().compare(o1.getCellValues(sortPos), o2.getCellValues(sortPos)); 
							}	
						}
						return 0;
					}
				});
        	} else {
        		System.clearProperty(sortPosKey);
            	System.clearProperty(sortDirKey);
        	}
			return getCache().subList((int) first, (int) (first + count)).iterator();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return IteratorUtils.EMPTY_ITERATOR;
		}
    }
    
    private int getPropertyPosition(String property) {
    	List<String> header = new ArrayList<String>();
		try {
			header = getHeader();
			for (int i=0, size=header.size(); i<size; i++) {
	    		if (header.get(i).equals(property)) {
	    			return i;
	    		}
	    	}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}    	
    	return -1;
    }

    @Override
    public IModel<RowData> model(RowData object) {
        return new Model<RowData>(object);
    }

    @Override
    public long size() {
        try {
			return getCache().size();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return 0;
		}
    }
    
    public int getColumnCount() {
    	try {
			return getCache().get(0).getCellValues().size();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return 0;
		}
    }

    @Override
    public void detach() {
        cache = null;
    }

	public String getWidgetId() {
		return widgetId;
	}

	public List<String> getHeader() throws NoDataFoundException, Exception {
        getCache();
        
        return header;
    }
	
	public List<String> getPattern() throws NoDataFoundException, Exception {
        getCache();
        
        return pattern;
    }
	
	public I18nLanguage getLanguage() throws NoDataFoundException, Exception {
        getCache();
        
        return language;
    }
	
    private List<RowData> getCache() throws NoDataFoundException, Exception {
        if (cache == null) {
            if (widgetId != null) {
            	TableData td = dashboardService.getTableData(widgetId, drillContext, urlQueryParameters);
            	// tableData may be kept in cache (if there are cache settings set)!
            	// so we must use a clone here to not modify the original in case of filtering
            	TableData tableData = ObjectCloner.silenceDeepCopy(td); 
            	List<List<Object>> data = tableData.getData();
            	List<List<Map<String, Object>>> style = tableData.getStyle();
            	header = tableData.getHeader();
            	if (data != null) {
                    cache = new ArrayList<RowData>();
                    tableFilter = NextServerSession.get().getTableFilter(widgetId);
                    if (tableFilter == null) {
                		List<Object> cellValues = new ArrayList<Object>();
                		if (data.size() > 0) {
                			for (int k=0; k<data.get(0).size(); k++) {
                				cellValues.add(null);
                			}
                		}            			
            			tableFilter = new RowData(cellValues);
            		} else {                          
                		tableData.search(tableFilter.getCellValues());                		
                	}                	
                    for (int i=0, size=data.size(); i<size; i++) {
                    	List<Object> row = data.get(i);
            			RowData rowData = new RowData(row);
            			if (i == 0) {
            				pattern = tableData.getPattern();
            				language = tableData.getLanguage();
            			}	
            			rowData.setStyles(style.get(i));
            			cache.add(rowData);
            		}
            	}
            }
        }
        
        return cache;
    }
    
    @Override
	public RowData getFilterState() {
		return tableFilter;
	}

	@Override
	public void setFilterState(RowData state) {
		this.tableFilter = tableFilter;		
	}       
}
