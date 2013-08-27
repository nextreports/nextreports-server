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
package ro.nextreports.server.web.dashboard.pivot;

import java.io.File;

import org.apache.wicket.request.resource.ByteArrayResource;

import ro.nextreports.server.pivot.DefaultPivotModel;
import ro.nextreports.server.pivot.NextPivotDataSource;
import ro.nextreports.server.pivot.PivotModel;
import ro.nextreports.server.util.FileUtil;
import ro.nextreports.server.web.dashboard.table.TableDataExporter;
import ro.nextreports.server.web.pivot.PivotUtil;

import ro.nextreports.engine.exporter.util.TableData;

public class PivotResource extends ByteArrayResource {

    private static final long serialVersionUID = 1L;

	private PivotWidget pivotWidget;

    public PivotResource(PivotWidget pivotWidget) {
        super("excel/ms-excel");                
        this.pivotWidget = pivotWidget;        
    }

    @Override
	protected byte[] getData(Attributes attributes) {
		try {
			NextPivotDataSource dataSource = new NextPivotDataSource(pivotWidget);
			PivotModel pivotModel = new DefaultPivotModel(dataSource);
			PivotUtil.readPivotPropertiesFromWidget(pivotModel, pivotWidget);
			pivotModel.calculate();
			
			TableData data = PivotUtil.getTableData(pivotModel);									
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
    	data.setFileName("pivot.xls");
		super.setResponseHeaders(data, attributes);		
    }

}
