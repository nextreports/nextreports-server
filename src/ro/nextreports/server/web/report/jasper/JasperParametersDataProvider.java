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
package ro.nextreports.server.web.report.jasper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Report;
import ro.nextreports.server.report.jasper.JasperParameterSource;
import ro.nextreports.server.service.ReportService;

public class JasperParametersDataProvider extends SortableDataProvider<JasperParameterSource, String> {

	private static final long serialVersionUID = 1L;
	
	private Report report;
    private transient List<JasperParameterSource> parameters;

    @SpringBean
    private ReportService reportService;

    public JasperParametersDataProvider(Report report) {
        this.report = report;
        
        Injector.get().inject(this);
    }

    @Override
    public Iterator<JasperParameterSource> iterator(long first, long count) {
        return getParameters().iterator();
    }

    @Override
    public IModel<JasperParameterSource> model(JasperParameterSource p) {
        return new Model<JasperParameterSource>(p);
    }

    @Override
    public long size() {
        return getParameters().size();
    }

    @Override
    public void detach() {
        parameters = null;
    }

    private List<JasperParameterSource> getParameters() {
        if (parameters == null) {
            try {
                parameters = getReportParameters();
            } catch (Exception e) {
                // TODO
                throw new RuntimeException(e);
            }
        }
        return parameters;
    }

    private List<JasperParameterSource> getReportParameters() throws Exception {
        ArrayList<JasperParameterSource> result = new ArrayList<JasperParameterSource>();
        Map<String, Serializable> map = reportService.getReportUserParametersForEdit(report);
        for (Serializable ser : map.values()) {
            result.add((JasperParameterSource) ser);
        }
        
        return result;
    }

}
