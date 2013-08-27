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

import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.JasperContent;
import ro.nextreports.server.domain.NextContent;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.report.ExternalParameter;
import ro.nextreports.server.report.ReportConstants;
import ro.nextreports.server.report.jasper.JasperParameter;
import ro.nextreports.server.report.jasper.JasperParameterSource;
import ro.nextreports.server.report.jasper.JasperReportsUtil;
import ro.nextreports.server.report.jasper.util.JasperUtil;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.service.StorageService;

import ro.nextreports.engine.queryexec.QueryParameter;

//
public class ParamViewDataProvider extends SortableDataProvider<ParamView> {

	private static final long serialVersionUID = 1L;
	
	private Report report;
    private Chart chart;
    private transient List<ParamView> params;

    @SpringBean
    private ReportService reportService;
    
    @SpringBean
    private StorageService storageService;

    public ParamViewDataProvider(Report report) {
        this.report = report;
        Injector.get().inject(this);
    }

    public ParamViewDataProvider(Chart chart) {
        this.chart = chart;
        Injector.get().inject(this);
    }

    public Iterator<? extends ParamView> iterator(int first, int count) {
        return getParams().iterator();
    }

    public IModel<ParamView> model(ParamView version) {
        return new Model<ParamView>(version);
    }

    public int size() {
        return getParams().size();
    }

    public void detach() {
        params = null;
    }

    private List<ParamView> getParams() {
        if (params == null) {
            try {
                if (report != null) {
                    params = getParamViews(report);
                } else {
                    params = getParamViews(chart);
                }
            } catch (Exception e) {
                // TODO
                throw new RuntimeException(e);
            }
        }
        return params;
    }

    public List<ParamView> getParamViews(Report report) {

        List<ParamView> views = new ArrayList<ParamView>();
        if (ReportConstants.NEXT.equals(report.getType())) {
            java.util.List<QueryParameter> parameters = NextUtil.getNextReport(storageService.getSettings(), (NextContent) report.getContent()).getParameters();
            for (QueryParameter qp : parameters) {                
                views.add(createParamView(qp));
            }

        } else if (ReportConstants.JASPER.equals(report.getType())) {
            try {
                JasperContent jr = (JasperContent) report.getContent();
                List<JasperParameterSource> list;
//                JcrFile paramEntry = jr.getParametersDescription();
                List<JasperParameterSource> sourceParams = JasperUtil.getParameterSources(jr);
                if ((sourceParams.size() == 0)) {
                    list = loadJasperParameters(report);
                } else {
                    list = sourceParams;
                }
                for (JasperParameterSource sp : list) {
                    ParamView pv = new ParamView();
                    pv.setName(sp.getName());
                    pv.setClassName(sp.getValueClassName());
                    pv.setType(sp.getType());
                    pv.setSource(sp.getSelect());
                    views.add(pv);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return views;
    }

    public List<ParamView> getParamViews(Chart chart) {
        List<ParamView> views = new ArrayList<ParamView>();
        java.util.List<QueryParameter> parameters = NextUtil.getChart(chart.getContent()).getReport().getParameters();
        for (QueryParameter qp : parameters) {
            views.add(createParamView(qp));
        }
        return views;
    }

    private ParamView createParamView(QueryParameter qp) {
        ParamView pv = new ParamView();
        pv.setName(getParameterName(qp));
        pv.setClassName(qp.getValueClassName());
        pv.setType(qp.getSelection());
        pv.setSource(qp.getSource());
        pv.setDefaultSource(qp.getDefaultSource());
        return pv;
    }

    private List<JasperParameterSource> loadJasperParameters(Report report) {
        List<JasperParameterSource> list = new ArrayList<JasperParameterSource>();
        try {

            Map<String, Serializable> map = reportService.getReportUserParameters(report, new ArrayList<ExternalParameter>());

            for (String key : map.keySet()) {
                JasperParameter jp = (JasperParameter) map.get(key);
                JasperParameterSource sp = new JasperParameterSource(jp.getName());
                sp.setValueClassName(JasperReportsUtil.getValueClassName(storageService, report.getDataSource(), jp));                
                list.add(sp);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    private String getParameterName(QueryParameter parameter) {
        String name = parameter.getRuntimeName();
        if ((name == null) || name.trim().equals("")) {
            name = parameter.getName();
        }
        return name;
    }

}
