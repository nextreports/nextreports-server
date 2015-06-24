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

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.DrillEntityContext;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.util.FileUtil;
import ro.nextreports.server.util.ServerUtil;
import ro.nextreports.engine.exporter.util.TableData;

/**
 * User: mihai.panaitescu
 * Date: 12-Apr-2010
 * Time: 15:38:04
 */
public class TableResource extends ByteArrayResource {

    private static final long serialVersionUID = 1L;

	private String widgetId;
	private TableData data;
	private String fileName = "table.xls";
	private DrillEntityContext drillContext;

    @SpringBean
    private DashboardService dashboardService;

    public TableResource(String widgetId, DrillEntityContext drillContext) {
        super("excel/ms-excel");        
        
        this.widgetId = widgetId;
        this.drillContext = drillContext;
        Injector.get().inject(this);
    }
    
    public TableResource(TableData data, String fileName) {
        super("excel/ms-excel");        
        
        this.data = data;
        this.fileName = fileName;
        Injector.get().inject(this);
    }

    @Override
	protected byte[] getData(Attributes attributes) {
        try {
        	String username = ServerUtil.getUsername();
        	String sortPosKey = username == null ? widgetId + TableDataProvider.SORT_PROP_POS_SUFFIX : widgetId + "_" + username + TableDataProvider.SORT_PROP_POS_SUFFIX;
        	String sortDirKey = username == null ? widgetId + TableDataProvider.SORT_PROP_DIR_SUFFIX : widgetId + "_" + username + TableDataProvider.SORT_PROP_DIR_SUFFIX;
        	String pos = System.getProperty(sortPosKey);
    		String dir = System.getProperty(sortDirKey);    		
        	if (widgetId != null) {
        		data = dashboardService.getTableData(widgetId, drillContext, null);
        	}
        	if (pos != null) {
        		final int sortPos = Integer.parseInt(pos);
        		final int sortDir = Integer.parseInt(dir);
        		//!! Here we sort only data (style list is not, but it is not used inside TableDataExporter)
	        	Collections.sort(data.getData(), new Comparator<List<Object>>() {
					public int compare(List<Object> o1, List<Object> o2) {
						if ((o1.get(sortPos) == null) && (o2.get(sortPos) == null)) {
							return 0;
						} else if (o1.get(sortPos) == null) {
							return sortDir;
						} else if (o2.get(sortPos) == null) {
							return -sortDir;
						} else {
							return sortDir * new TableObjectComparator().compare(o1.get(sortPos), o2.get(sortPos));
						}								
					}
				});
        	}
            String file = new TableDataExporter().toExcel(data);
            return FileUtil.getBytes(new File(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return new byte[0];
    }

    @Override
	protected void setResponseHeaders(ResourceResponse data, Attributes attributes) {    	
    	data.disableCaching();
    	data.setFileName(fileName);
		super.setResponseHeaders(data, attributes);		
    }

}
