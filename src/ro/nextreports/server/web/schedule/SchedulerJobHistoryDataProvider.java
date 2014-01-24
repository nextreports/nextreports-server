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
package ro.nextreports.server.web.schedule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.RunReportHistory;
import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.service.StorageService;

public class SchedulerJobHistoryDataProvider extends SortableDataProvider<RunReportHistory, String> {

	private static final long serialVersionUID = 1L;
	
	private SchedulerJob job;
	private transient List<RunReportHistory> histories;

    @SpringBean
    private StorageService storageService;

    public SchedulerJobHistoryDataProvider(SchedulerJob job) {
    	Injector.get().inject(this);
    	this.job = job;
    }

    @Override
	public Iterator<? extends RunReportHistory> iterator(long first, long count) {
		return getHistories().iterator();
	}

    @Override
	public IModel<RunReportHistory> model(RunReportHistory history) {
		return new Model<RunReportHistory>(history);
	}

    @Override
	public long size() {
		return getHistories().size();
	}

	@Override
	public void detach() {
		histories = null;
	}

    private List<RunReportHistory> getHistories() {
        if (histories == null) {
        	try {
				histories = getSchedulerHistory();
			} catch (Exception e) {
				// TODO
				throw new RuntimeException(e);
			}
        }

        return histories;
    }

   private List<RunReportHistory> getSchedulerHistory() throws Exception {
	   // TODO performance (execute a query in jcr)
       List<RunReportHistory> list = new ArrayList<RunReportHistory>();
       Entity[] array = storageService.getEntitiesByClassName(job.getReport().getPath(), RunReportHistory.class.getName());
       for (Entity entity : array) {
    	   RunReportHistory runHistory = (RunReportHistory) entity;
    	   if (runHistory.getRunnerId().equals(job.getId())) {
               list.add((RunReportHistory) entity);
    	   }
       }
       
        return list;
    }

}
