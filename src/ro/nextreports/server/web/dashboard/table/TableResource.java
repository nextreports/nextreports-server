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

import org.apache.wicket.injection.Injector;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.util.FileUtil;

import ro.nextreports.engine.exporter.util.TableData;

/**
 * User: mihai.panaitescu
 * Date: 12-Apr-2010
 * Time: 15:38:04
 */
public class TableResource extends ByteArrayResource {

    private static final long serialVersionUID = 1L;

	private String widgetId;

    @SpringBean
    private DashboardService dashboardService;

    public TableResource(String widgetId) {
        super("excel/ms-excel");        
        
        this.widgetId = widgetId;
        Injector.get().inject(this);
    }

    @Override
	protected byte[] getData(Attributes attributes) {
        try {
        	TableData data = dashboardService.getTableData(widgetId, null);            
            String file = new TableDataExporter().toExcel(data);
            return FileUtil.getBytes(new File(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return new byte[0];
    }

    @Override
	protected void setResponseHeaders(ResourceResponse data, Attributes attributes) {
    	// TODO wicket 1.5
    	data.disableCaching();
    	data.setFileName("table.xls");
		super.setResponseHeaders(data, attributes);
		/*
        response.setAttachmentHeader("table.xls");
        */
    }

}
