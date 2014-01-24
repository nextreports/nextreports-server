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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.service.SchedulerService;

/**
 * User: mihai.panaitescu
 * Date: 11-Jun-2010
 * Time: 11:54:39
 */
public class ActiveSchedulerJobDataProvider extends SortableDataProvider<SchedulerJob, String> {

	private static final long serialVersionUID = 1L;

	private transient List<SchedulerJob> children;

    @SpringBean
    private SchedulerService schedulerService;

    public ActiveSchedulerJobDataProvider() {
    	Injector.get().inject(this);
    }

    @Override
	public Iterator<? extends SchedulerJob> iterator(long first, long count) {
		return getChildren().iterator();
	}

	@Override
	public IModel<SchedulerJob> model(SchedulerJob entity) {
		return new Model<SchedulerJob>(entity);
	}

	@Override
	public long size() {
		return getChildren().size();
	}

	@Override
	public void detach() {
		children = null;
	}

    private List<SchedulerJob> getChildren() {
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

    private List<SchedulerJob> loadChildren() throws Exception {
        SchedulerJob[] entities = schedulerService.getActiveSchedulerJobs();
        return Arrays.asList(entities);
    }

}
