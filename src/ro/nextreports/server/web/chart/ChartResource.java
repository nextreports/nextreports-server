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
package ro.nextreports.server.web.chart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.request.resource.ByteArrayResource;
import org.apache.wicket.request.resource.ContentDisposition;

import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.ChartContent;
import ro.nextreports.server.util.FileUtil;


/**
 * User: mihai.panaitescu
 * Date: 20-Jan-2010
 * Time: 13:47:39
 */
public class ChartResource extends ByteArrayResource {

    private static final long serialVersionUID = 1L;
    
	private List<Chart> charts;

    public ChartResource(Chart chart) {
        super("application/zip");
        
        this.charts = new ArrayList<Chart>();
        this.charts.add(chart);
    }

    public ChartResource(List<Chart> charts) {
        super("application/zip");
        
        this.charts = charts;
    }

    @Override
	protected byte[] getData(Attributes attributes) {
        List<String> files = new ArrayList<String>();
        List<byte[]> contents = new ArrayList<byte[]>();

        for (Chart chart : charts) {
            ChartContent chartContent = chart.getContent();
            files.add(chartContent.getChartFile().getName());
            contents.add(chartContent.getChartFile().getDataProvider().getBytes());
        }

        byte[] zip = null;
        try {
            zip = FileUtil.zip(files, contents);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return zip;
    }

    @Override
	protected void setResponseHeaders(ResourceResponse data, Attributes attributes) {    	
    	data.disableCaching();    	
    	data.setContentDisposition(ContentDisposition.ATTACHMENT);    	
    	String name;
        if (charts.size() == 1) {
            name = charts.get(0).getName();
        } else {
            name = "downloaded_charts";
        }
        data.setFileName(name + ".zip");         
    	super.setResponseHeaders(data, attributes);   
		 	                       
	}

}
