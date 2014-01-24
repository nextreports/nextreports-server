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

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import org.apache.wicket.markup.html.form.upload.MultiFileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.lang.Bytes;
import org.jcrom.JcrDataProviderImpl;
import org.jcrom.JcrFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.JasperContent;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.exception.ReportEngineException;
import ro.nextreports.server.report.ReportConstants;
import ro.nextreports.server.report.jasper.util.JasperReportSaxParser;
import ro.nextreports.server.report.jasper.util.JasperUtil;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.StorageUtil;
import ro.nextreports.server.web.common.form.AdvancedForm;
import ro.nextreports.server.web.common.panel.NextFeedbackPanel;
import ro.nextreports.server.web.core.EntityBrowserPanel;
import ro.nextreports.server.web.core.validation.DuplicationEntityValidator;
import ro.nextreports.server.web.core.validation.JcrNameValidator;

public class UploadJasperReportPanel extends Panel {

    private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(UploadJasperReportPanel.class);

    private FileUploadField masterUploadField;
    private MultiFileUploadField subreportsUploadField;
    private FileUploadField parametersUploadField;
    private MultiFileUploadField imagesUploadField;
    public Report report;
    private String parentPath;
    private boolean update;
    private FeedbackPanel feedback;

    @SpringBean
    private StorageService storageService;

    private PageParameters searchPageParameters;

    public UploadJasperReportPanel(String id, String parentPath) {
        super(id);

        this.update = false;
        this.parentPath = parentPath;

        report = new Report();
        report.setType(ReportConstants.JASPER);

        Form form = new UploadForm("form", true);
        add(form);
        feedback = new NextFeedbackPanel("feedback", form);
        feedback.setOutputMarkupId(true);
        form.add(feedback);

        setOutputMarkupId(true);
    }

    public UploadJasperReportPanel(String id, Report report) {
        super(id);

        this.update = true;
        this.parentPath = StorageUtil.getParentPath(report.getPath());

        this.report = report;
        report.setType(ReportConstants.JASPER);

        Form form = new UploadForm("form", false);
        add(form);
        feedback = new NextFeedbackPanel("feedback", form);
        feedback.setOutputMarkupId(true);
        form.add(feedback);

        setOutputMarkupId(true);
    }

    private JasperContent getReportContent(FileUpload upload,
                                           Collection<FileUpload> subreportsFileUpload, FileUpload paramUpload,
                                           Collection<FileUpload> imgUpload) throws IOException, ReportEngineException {
        JasperContent reportContent = new JasperContent();
        reportContent.setName("content");
        reportContent.setPath(StorageUtil.createPath(report.getPath(), "content"));
        try {
            List<JcrFile> jasperFiles = new ArrayList<JcrFile>();
            JcrFile masterFile = new JcrFile();
            masterFile.setName(upload.getClientFileName());
            masterFile.setPath(StorageUtil.createPath(reportContent.getPath(), masterFile.getName()));
            masterFile.setMimeType("text/xml");
            masterFile.setLastModified(Calendar.getInstance());
            masterFile.setDataProvider(new JcrDataProviderImpl(upload.getBytes()));

            JasperReportSaxParser parser = new JasperReportSaxParser();
            parser.process(upload.getBytes());
            String language = parser.getLanguage();
            if ((language != null) && !"java".equals(language)) {
                throw new ReportEngineException("Report language is '" + language + "'. Only reports with 'java' may be added.");
            }

            jasperFiles.add(masterFile);
            for (FileUpload subUpload : subreportsFileUpload) {
                JcrFile subreportFile = new JcrFile();
                subreportFile.setName(subUpload.getClientFileName());
                subreportFile.setPath(StorageUtil.createPath(reportContent.getPath(), subreportFile.getName()));
                subreportFile.setMimeType("text/xml");
                subreportFile.setLastModified(Calendar.getInstance());
                subreportFile.setDataProvider(new JcrDataProviderImpl(subUpload.getBytes()));

                parser.process(subUpload.getBytes());
                language = parser.getLanguage();
                if ((language != null) && !"java".equals(language)) {
                    throw new ReportEngineException("Report language is '" + language + "'. Only reports with 'java' may be added.");
                }

                jasperFiles.add(subreportFile);
            }
            reportContent.setJasperFiles(jasperFiles);

            if (paramUpload != null) {
                JcrFile parametersFile = new JcrFile();
                parametersFile.setName(paramUpload.getClientFileName());
                parametersFile.setPath(StorageUtil.createPath(reportContent.getPath(), parametersFile.getName()));
                parametersFile.setMimeType("text/xml");
                parametersFile.setLastModified(Calendar.getInstance());
                parametersFile.setDataProvider(new JcrDataProviderImpl(paramUpload.getBytes()));
                reportContent.setParametersFile(parametersFile);
            }

            List<JcrFile> imageFiles = new ArrayList<JcrFile>();
            for (FileUpload iu : imgUpload) {
                JcrFile imageFile = new JcrFile();
                imageFile.setName(iu.getClientFileName());
                imageFile.setPath(StorageUtil.createPath(reportContent.getPath(), imageFile.getName()));
                imageFile.setMimeType(iu.getContentType());
                imageFile.setLastModified(Calendar.getInstance());
                imageFile.setDataProvider(new JcrDataProviderImpl(iu.getBytes()));
                imageFiles.add(imageFile);
            }
            reportContent.setImageFiles(imageFiles);
        } catch (Exception e) {
            if (e instanceof ReportEngineException) {
                throw (ReportEngineException)e;
            }
            e.printStackTrace();
            LOG.error(e.getMessage(), e);
            error(e.getMessage());
        }

        return reportContent;
    }

    private class UploadForm extends AdvancedForm<Report> {

        private final List<FileUpload> subreportsUploads = new ArrayList<FileUpload>();
        private final List<FileUpload> imagesUploads = new ArrayList<FileUpload>();

        @SuppressWarnings("unchecked")
        public UploadForm(String id, boolean create) {
            super(id);

            String title;
            if (create) {
               title = getString("ActionContributor.UploadJasper.name");
            } else {
               title = getString("ActionContributor.UploadJasper.update");
            }
            add(new Label("title", title));

            setModel(new CompoundPropertyModel<Report>(report));
            setOutputMarkupId(true);
            setMultiPart(true);
            setMaxSize(Bytes.megabytes(1)); // ?!
            //add(new UploadProgressBar("progress", this));

            final TextField<String> name = new TextField<String>("name") {
                @Override
				public boolean isEnabled() {
					return !update;
				}
            };
            name.add(new JcrNameValidator());
            name.setRequired(true);
            name.setLabel(new Model<String>(getString("ActionContributor.UploadJasper.reportName")));
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
            Collections.sort(datasources, new Comparator<DataSource>() {
                public int compare(DataSource o1, DataSource o2) {
                	return Collator.getInstance().compare(o1.getPath(),o2.getPath());
                }
            });
            ChoiceRenderer<DataSource> pathRenderer = new ChoiceRenderer<DataSource>("path")  {
                public Object getDisplayValue(DataSource dataSource) {
                    return dataSource.getPath().substring(StorageConstants.DATASOURCES_ROOT.length());
                }
            };
            final DropDownChoice<DataSource> choice = new DropDownChoice<DataSource>("dataSource", datasources, pathRenderer);
            choice.setRequired(true);
            choice.setLabel(new Model<String>(getString("ActionContributor.UploadJasper.datasource")));
            add(choice);

            TextArea<String> description = new TextArea<String>("description");
            add(description);

            subreportsUploadField = new MultiFileUploadField("subreportFile", new PropertyModel(this, "subreportsUploads"), 5);
            masterUploadField = new FileUploadField("masterFile", new Model(new ArrayList<FileUpload>()));
            masterUploadField.setRequired(true);
            masterUploadField.setLabel(new Model<String>(getString("ActionContributor.UploadJasper.file")));
            masterUploadField.add(new AjaxEventBehavior("onchange") {
            	
				private static final long serialVersionUID = 1L;

				@Override
				protected void onEvent(AjaxRequestTarget target) {
                     Request request = RequestCycle.get().getRequest();
                     String filename = request.getRequestParameters().getParameterValue("filename").toString();
                     String text = getModel().getObject().getName();
                     if ((text == null) || "".equals(text.trim())) {
                         int index = filename.lastIndexOf(".");
                         if (index > 0) {
                             filename = filename.substring(0,  index);
                         }
                         getModel().getObject().setName(filename);
                     }
                     target.add(name);
                 }

				// TODO wicket-6
				@Override
				public CharSequence getCallbackScript() {
					CharSequence callbackScrip = super.getCallbackScript();
                    return callbackScrip + "&filename=' + this.value + '";
                }
				
             });
            parametersUploadField = new FileUploadField("parameterFile", new Model(new ArrayList<FileUpload>()));
            imagesUploadField = new MultiFileUploadField("imageFile", new PropertyModel(this, "imagesUploads"), 3);
            add(masterUploadField);
            add(subreportsUploadField);
            add(parametersUploadField);
            add(imagesUploadField);

            add(new AjaxLink("cancel") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
                    panel.backwardWorkspace(target);
                }
                
            });

             // add a button that can be used to submit the form via ajax
            AjaxButton submitButton = new AjaxButton("submit", this) {
            	
				private static final long serialVersionUID = 1L;

				@Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    upload(target);
                    target.add(feedback);
                }

                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form) {
                    target.add(form);
                }
                
            };
            setDefaultButton(submitButton);
            add(submitButton);
        }

        private void upload(AjaxRequestTarget target) {
            FileUpload upload = masterUploadField.getFileUpload();
            FileUpload paramUpload = parametersUploadField.getFileUpload();
            Collection<FileUpload> subUpload = subreportsUploadField.getModelObject();
            Collection<FileUpload> imgUpload = imagesUploadField.getModelObject();
            if (!update) {
                report.setPath(StorageUtil.createPath(parentPath, report.getName()));
            }
            try {                
                JasperContent reportContent = getReportContent(upload, subUpload, paramUpload, imgUpload);
                report.setContent(reportContent);
                report = JasperUtil.renameImagesAsUnique(report);
                if (update) {
                    // IMPORTANT : see ReportModifiedAdvice
                    storageService.modifyEntity(report);                    
                } else {
                    storageService.addEntity(report);                    
                }                
                info(getString("ActionContributor.UploadJasper.success"));
                EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
                panel.backwardWorkspace(target);
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error(e.getMessage(), e);
                error(getString("ActionContributor.UploadJasper.failed") + " : " + e.getMessage());
            }
        }

    }

}
