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

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.ReportRuntimeTemplate;
import ro.nextreports.server.service.ReportService;


public class TemplatesDataProvider extends SortableDataProvider<ReportRuntimeTemplate> {

	private static final long serialVersionUID = 1L;
	
	private Report report;
	private transient List<ReportRuntimeTemplate> templates;

    @SpringBean
    private ReportService reportService;

    public TemplatesDataProvider(Report report) {
    	Injector.get().inject(this);
    	this.report = report;
    }

	public Iterator<? extends ReportRuntimeTemplate> iterator(int first, int count) {
		return getTemplates().iterator();
	}

	public IModel<ReportRuntimeTemplate> model(ReportRuntimeTemplate template) {
		return new Model<ReportRuntimeTemplate>(template);
	}

	public int size() {
		return getTemplates().size();
	}

	@Override
	public void detach() {
		templates = null;
	}

    private List<ReportRuntimeTemplate> getTemplates() {
        if (templates == null) {
        	try {
        		templates = getReportTemplates();
			} catch (Exception e) {
				// TODO
				throw new RuntimeException(e);
			}
        }

        return templates;
    }

   private List<ReportRuntimeTemplate> getReportTemplates() throws Exception {
       return reportService.getReportTemplates(report.getPath());
    }

}
