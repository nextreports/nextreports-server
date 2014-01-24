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
package ro.nextreports.server.web.report;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.DateRange;
import ro.nextreports.server.domain.RunReportHistory;
import ro.nextreports.server.service.ReportService;

import ro.nextreports.engine.util.DateUtil;

/**
 * @author Decebal Suiu
 */
public class RunReportHistoryDataProvider implements IDataProvider<RunReportHistory> {

	private static final long serialVersionUID = 1L;

	private transient Collection<RunReportHistory> children;

    private String reportPath;
    private DateRange dateRange;
	
    @SpringBean
    private ReportService reportService;
	
    public RunReportHistoryDataProvider(String reportPath) {
        this.reportPath = reportPath;
        this.dateRange =  createDateRange();
        
    	Injector.get().inject(this);
    }

    public RunReportHistoryDataProvider() {
        this(null);
    }
    
    @Override
    public Iterator<? extends RunReportHistory> iterator(long first, long count) {
		return getChildren().iterator();
	}

    @Override
	public IModel<RunReportHistory> model(RunReportHistory entity) {
		return new Model<RunReportHistory>(entity);
	}

	@Override
	public long size() {
		return getChildren().size();
	}

	@Override
	public void detach() {
		children = null;
	}

    private Collection<RunReportHistory> getChildren() {
        if (children == null) {
        	try {
				children = loadChildren();
			} catch (Exception e) {
				// TODO
				throw new RuntimeException(e);
			}
        }
        
        return children;
    }

    private Collection<RunReportHistory> loadChildren() throws Exception {    	
    	return reportService.getRunHistoryForRange(reportPath, dateRange);
    }
    
    public void setDateRange(DateRange dateRange) {
    	this.dateRange = dateRange;
    }
    
    private DateRange createDateRange() {
    	Calendar c = Calendar.getInstance();    	    	
    	Date start = DateUtil.floor(c.getTime());    	    	
    	Date end = DateUtil.ceil(c.getTime());    	    	
    	return new DateRange(start, end);
    }

}
