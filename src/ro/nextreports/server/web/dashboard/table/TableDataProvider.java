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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.IteratorUtils;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.domain.DrillEntityContext;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.engine.exporter.exception.NoDataFoundException;
import ro.nextreports.engine.exporter.util.TableData;

/**
 * @author Decebal Suiu
 */
public class TableDataProvider extends SortableDataProvider<RowData, String> {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(TableDataProvider.class);

    private transient List<RowData> cache;
    private transient List<String> header;
    private String widgetId;
    private DrillEntityContext drillContext;
    private Map<String, Object> urlQueryParameters;

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
			return getCache().subList((int) first, (int) (first + count)).iterator();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return IteratorUtils.EMPTY_ITERATOR;
		}
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
	
    private List<RowData> getCache() throws NoDataFoundException, Exception {
        if (cache == null) {
            if (widgetId != null) {
            	TableData tableData = dashboardService.getTableData(widgetId, drillContext, urlQueryParameters);
            	List<List<Object>> data = tableData.getData();
            	List<List<Map<String, Object>>> style = tableData.getStyle();
            	header = tableData.getHeader();
            	if (data != null) {
                    cache = new ArrayList<RowData>();
                    for (int i=0, size=data.size(); i<size; i++) {
                    	List<Object> row = data.get(i);
            			RowData rowData = new RowData(row);
            			rowData.setStyles(style.get(i));
            			cache.add(rowData);
            		}
            	}
            }
        }
        
        return cache;
    }       
    
}
