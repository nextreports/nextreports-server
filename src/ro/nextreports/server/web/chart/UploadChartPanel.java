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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.lang.Bytes;
import org.jcrom.JcrDataProviderImpl;
import org.jcrom.JcrFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.engine.ReleaseInfoAdapter;
import ro.nextreports.engine.util.NextChartUtil;
import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.ChartContent;
import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.StorageUtil;
import ro.nextreports.server.web.common.form.AdvancedForm;
import ro.nextreports.server.web.common.panel.NextFeedbackPanel;
import ro.nextreports.server.web.core.EntityBrowserPanel;
import ro.nextreports.server.web.core.validation.DuplicationEntityValidator;
import ro.nextreports.server.web.core.validation.JcrNameValidator;

/**
 * @author Decebal Suiu
 */
public class UploadChartPanel extends Panel {

    private static final Logger LOG = LoggerFactory.getLogger(UploadChartPanel.class);

    private FileUploadField uploadField;
    public Chart chart;
    private String parentPath;
    private boolean update;
    private FeedbackPanel feedback;

    @SpringBean
    private StorageService storageService;

    public UploadChartPanel(String id, String parentPath) {
        super(id);

        this.update = false;
        this.parentPath = parentPath;

        chart = new Chart();

        Form form = new UploadForm("form", true);
        add(form);
        feedback = new NextFeedbackPanel("feedback", form);
        feedback.setOutputMarkupId(true);
        form.add(feedback);

        setOutputMarkupId(true);
    }

    public UploadChartPanel(String id, Chart chart) {
        super(id);

        this.update = true;
        this.parentPath = StorageUtil.getParentPath(chart.getPath());

        this.chart = chart;

        Form form = new UploadForm("form", false);
        add(form);
        feedback = new NextFeedbackPanel("feedback", form);
        feedback.setOutputMarkupId(true);
        form.add(feedback);

        setOutputMarkupId(true);
    }

    private class UploadForm extends AdvancedForm<Chart> {

        public UploadForm(String id, boolean create) {
            super(id);

            String title;
            if (create) {
               title = getString("ActionContributor.UploadChart.name");
            } else {
               title = getString("ActionContributor.UploadChart.update");
            }
            add(new Label("title", title));

            setModel(new CompoundPropertyModel<Chart>(chart));
            setOutputMarkupId(true);
            setMultiPart(true);
            setMaxSize(Bytes.megabytes(1)); // !?
            //add(new UploadProgressBar("progress", this));

            final TextField<String> name = new TextField<String>("name") {
            	
                @Override
				public boolean isEnabled() {
					return !update;
				}
                
            };
            name.add(new JcrNameValidator());
            name.setRequired(true);
            name.setLabel(new Model<String>(getString("ActionContributor.UploadChart.chartName")));
            name.setOutputMarkupId(true);
            add(name);

            if (!update) {
                add(new DuplicationEntityValidator(name, parentPath));
            }

            Entity[] entities;
            try {
                entities = storageService.getEntitiesByClassName(StorageConstants.DATASOURCES_ROOT, DataSource.class.getName());
            } catch (Exception e) {
                entities = new Entity[0];
                e.printStackTrace();
                LOG.error(e.getMessage(), e);
                error(e.getMessage());
            }
            List<DataSource> datasources = new ArrayList<DataSource>();
            for (Entity entity : entities) {
                datasources.add((DataSource) entity);
            }
            ChoiceRenderer<DataSource> pathRenderer = new ChoiceRenderer<DataSource>("path") {
            	
            	@Override
                public Object getDisplayValue(DataSource dataSource) {
                    return dataSource.getPath().substring(StorageConstants.DATASOURCES_ROOT.length());
                }
                
            };
            final DropDownChoice<DataSource> choice = new DropDownChoice<DataSource>("dataSource", datasources, pathRenderer);
            choice.setRequired(true);
            choice.setLabel(new Model<String>(getString("ActionContributor.UploadChart.datasource")));
            add(choice);

            TextArea<String> description = new TextArea<String>("description");
            add(description);
             
            uploadField = new FileUploadField("file", new Model(new ArrayList<FileUpload>()));
            uploadField.setRequired(true);
            uploadField.setLabel(new Model<String>(getString("ActionContributor.UploadChart.file")));
            uploadField.add(new AjaxEventBehavior("onchange") {
            	
                private static final long serialVersionUID = 1L;

				protected void onEvent(AjaxRequestTarget target) {
                    Request request = RequestCycle.get().getRequest();
                    String filename = request.getRequestParameters().getParameterValue("filename").toString();
                    String text = getModel().getObject().getName();
                    if ((text == null) || "".equals(text.trim())) {
                        int index = filename.lastIndexOf(".");
                        if (index > 0) {
                            filename = filename.substring(0, index);
                        }
                        getModel().getObject().setName(filename);
                    }
                    target.add(name);
                }

				// TODO wicket-6
				@Override
				public CharSequence getCallbackScript() {
					CharSequence callbackScript = super.getCallbackScript();
					return callbackScript + "&filename=' + this.value + '";
				}
				
            });
            add(uploadField);

            add(new AjaxLink("cancel") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
                    panel.backwardWorkspace(target);
                }
                
            });

            // add a button that can be used to submit the form via ajax
            AjaxButton submitButton = new AjaxButton("submit", this) {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    upload(target);
                    target.add(feedback);
                }

                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form) {
                    // repaint the feedback panel so errors are shown
//                    target.add(feedback);
                	target.add(form);
                }
            };
            setDefaultButton(submitButton);
            add(submitButton);
        }


        private void upload(AjaxRequestTarget target) {
//        public void onSubmit() {
            FileUpload upload = uploadField.getFileUpload();
            if (!update) {
                chart.setPath(StorageUtil.createPath(parentPath, chart.getName()));
            }
            try {
            	ChartContent chartContent = new ChartContent();
            	chartContent.setName("content");
            	chartContent.setPath(StorageUtil.createPath(chart.getPath(), "content"));
            	JcrFile xmlFile = new JcrFile();
            	xmlFile.setName(upload.getClientFileName());
            	xmlFile.setLastModified(Calendar.getInstance());
            	xmlFile.setPath(StorageUtil.createPath(chartContent.getPath(), xmlFile.getName()));
            	xmlFile.setMimeType("text/xml");
            	xmlFile.setDataProvider(new JcrDataProviderImpl(upload.getBytes()));
            	chartContent.setChartFile(xmlFile);

                chart.setContent(chartContent);

                byte status = NextChartUtil.isValidChartVersion(NextUtil.getChart(chartContent));                
                if (NextChartUtil.CHART_INVALID_NEWER == status) {
                    error("Cannot publish a chart version newer than " + ReleaseInfoAdapter.getVersionNumber());
                } else {
                    if (update) {
                        storageService.modifyEntity(chart);                        
                    } else {
                        storageService.addEntity(chart);                        
                    }
                    info(getString("ActionContributor.UploadNext.success"));
                    //setResponsePage(HomePage.class);
                    EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
                    panel.backwardWorkspace(target);
                }
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error(e.getMessage(), e);
                error(getString("ActionContributor.UploadNext.failed") + " : " + e.getMessage());
            }
        }

    }
}
