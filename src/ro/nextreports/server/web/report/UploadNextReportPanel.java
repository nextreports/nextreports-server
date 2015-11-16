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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
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
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.lang.Bytes;
import org.jcrom.JcrDataProviderImpl;
import org.jcrom.JcrFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.engine.ReleaseInfoAdapter;
import ro.nextreports.engine.util.ReportUtil;
import ro.nextreports.engine.util.converter.ConverterChain;
import ro.nextreports.engine.util.converter.ConverterException;
import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.NextContent;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.report.ReportConstants;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.StorageUtil;
import ro.nextreports.server.web.common.form.AdvancedForm;
import ro.nextreports.server.web.common.panel.NextFeedbackPanel;
import ro.nextreports.server.web.core.EntityBrowserPanel;
import ro.nextreports.server.web.core.validation.DuplicationEntityValidator;
import ro.nextreports.server.web.core.validation.JcrNameValidator;

public class UploadNextReportPanel extends Panel {

    private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(UploadNextReportPanel.class);

    private FileUploadField uploadField;
    public Report report;
    private String parentPath;
    private boolean update;
    private FeedbackPanel feedback;
    private MultiFileUploadField imagesUploadField;
    private FileUploadField templateUploadField;

    @SpringBean
    private StorageService storageService;

    public UploadNextReportPanel(String id, String parentPath) {
        super(id);

        this.update = false;
        this.parentPath = parentPath;

        report = new Report();
        report.setType(ReportConstants.NEXT);

        Form form = new UploadForm("form", true);
        add(form);
        feedback = new NextFeedbackPanel("feedback", form);
        feedback.setOutputMarkupId(true);
        form.add(feedback);

        setOutputMarkupId(true);
    }

    public UploadNextReportPanel(String id, Report report) {
        super(id);

        this.update = true;
        this.parentPath = StorageUtil.getParentPath(report.getPath());

        this.report = report;

        Form form = new UploadForm("form", false);
        add(form);
        feedback = new NextFeedbackPanel("feedback", form);
        feedback.setOutputMarkupId(true);
        form.add(feedback);

        setOutputMarkupId(true);
    }

    private class UploadForm extends AdvancedForm<Report> {

        private final List<FileUpload> imagesUploads = new ArrayList<FileUpload>();

        @SuppressWarnings("unchecked")
        public UploadForm(String id, boolean create) {
            super(id);

            String title;
            if (create) {
               title = getString("ActionContributor.UploadNext.name");
            } else {
               title = getString("ActionContributor.UploadNext.update");
            }
            add(new Label("title", title));

            setModel(new CompoundPropertyModel<Report>(report));
            setOutputMarkupId(true);
            setMultiPart(true);
            setMaxSize(Bytes.megabytes(storageService.getSettings().getUploadSize())); 
            //add(new UploadProgressBar("progress", this));

            final TextField<String> name = new TextField<String>("name") {
                @Override
				public boolean isEnabled() {
					return !update;
				}
            };
            name.add(new JcrNameValidator());
            name.setRequired(true);
            name.setLabel(new Model<String>(getString("ActionContributor.UploadNext.reportName")));
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
            ChoiceRenderer<DataSource> pathRenderer = new ChoiceRenderer<DataSource>("path") {
                public Object getDisplayValue(DataSource dataSource) {
                    return dataSource.getPath().substring(StorageConstants.DATASOURCES_ROOT.length());
                }
            };
            final DropDownChoice<DataSource> choice = new DropDownChoice<DataSource>("dataSource", datasources, pathRenderer);
            choice.setRequired(true);
            choice.setLabel(new Model<String>(getString("ActionContributor.UploadNext.datasource")));
            add(choice);

            TextArea<String> description = new TextArea<String>("description");
            add(description);

            uploadField = new FileUploadField("file", new Model(new ArrayList<FileUpload>()));
            uploadField.setRequired(true);
            uploadField.setLabel(new Model<String>(getString("ActionContributor.UploadNext.file")));
            // In html 5 a new security "feature" has introduced. From this reason in javascript you have a c:/fakepath apeares.
            // See http://stackoverflow.com/questions/6365858/use-jquery-to-get-the-file-inputs-selected-filename-without-the-path
            /*
            uploadField.add(new AjaxEventBehavior("onchange") {
            	
                 protected void onEvent(AjaxRequestTarget target) {
                     Request request = RequestCycle.get().getRequest();
                     String filename = request.getParameter("filename");
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

                 public CharSequence getCallbackUrl(boolean onlyTargetActivePage) {
                     CharSequence callBackUrl = super.getCallbackUrl(onlyTargetActivePage);
                     return callBackUrl + "&filename=' + this.value + '";
                 }

             });
             */

            add(uploadField);
            imagesUploadField = new MultiFileUploadField("imageFile", new PropertyModel(this, "imagesUploads"), 3);
            add(imagesUploadField);        
            
            templateUploadField = new FileUploadField("templateFile", new Model(new ArrayList<FileUpload>()));            
            templateUploadField.setLabel(new Model<String>(getString("ActionContributor.UploadNext.template")));
            add(templateUploadField);

            add(new AjaxLink("cancel") {

                @Override
                public void onClick(AjaxRequestTarget target) {
//                    if (ActionUtil.isFromSearch()) {
//                        setResponsePage(new SearchEntityPage(null));
//                    } else {
//                        setResponsePage(HomePage.class);
//                    }
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
//                    target.addComponent(feedback);
                	target.add(form);
                }
            };
            setDefaultButton(submitButton);
            add(submitButton);
        }


        // !IMPORTANT : in IE FileUpload.getClientFileName() contains full path
        // that's why we will use FilenameUtils.getName
        private void upload(AjaxRequestTarget target) {
//        public void onSubmit() {
        	LOG.info("*** UPLOAD started ----------------------------------------------- ");
            FileUpload upload = uploadField.getFileUpload();
            Collection<FileUpload> imgUpload = imagesUploadField.getModelObject();
            if (!update) {
            	LOG.info("*** UPLOAD: Parent path = '" + parentPath + "'");
            	LOG.info("*** UPLOAD: Report name = '" + report.getName() + "'");
                report.setPath(StorageUtil.createPath(parentPath, report.getName()));
            }
            FileUpload templateUpload = templateUploadField.getFileUpload(); 
            try {
            	NextContent reportContent = new NextContent();
            	reportContent.setName("content");
            	reportContent.setPath(StorageUtil.createPath(report.getPath(), "content"));
            	LOG.info("*** UPLOAD: Report Content Path = '" + reportContent.getPath() + "'");
            	JcrFile xmlFile = new JcrFile();
            	xmlFile.setName(FilenameUtils.getName(upload.getClientFileName()));
            	LOG.info("*** UPLOAD: XML file name = '" + xmlFile.getName() + "'");
            	xmlFile.setLastModified(Calendar.getInstance());
            	xmlFile.setPath(StorageUtil.createPath(reportContent.getPath(), xmlFile.getName()));
            	LOG.info("*** UPLOAD: XML file path = '" + xmlFile.getPath() + "'");
            	xmlFile.setMimeType("text/xml");
            	
            	// convert xml if needed before upload
            	String xml = null;
                try {        	
                	xml = ConverterChain.applyFromInputStream(upload.getInputStream());                	
                } catch (ConverterException ex) {                    	
                	ex.printStackTrace();
                	LOG.error(ex.getMessage());
                } 
            	
                if (xml != null) {
                	xmlFile.setDataProvider(new JcrDataProviderImpl(xml.getBytes("UTF-8")));
                }	
            	reportContent.setNextFile(xmlFile);

                List<JcrFile> imageFiles = new ArrayList<JcrFile>();
                for (FileUpload iu : imgUpload) {
                    JcrFile imageFile = new JcrFile();
                    imageFile.setName(FilenameUtils.getName(iu.getClientFileName()));
                    imageFile.setPath(StorageUtil.createPath(reportContent.getPath(), imageFile.getName()));
                    LOG.info("*** UPLOAD: image file path = '" + imageFile.getPath() + "'");
                    imageFile.setMimeType(iu.getContentType());
                    imageFile.setLastModified(Calendar.getInstance());
                    imageFile.setDataProvider(new JcrDataProviderImpl(iu.getBytes()));
                    imageFiles.add(imageFile);
                }
                reportContent.setImageFiles(imageFiles);
                
                if (templateUpload != null) {
                	JcrFile templateFile = new JcrFile();
                    templateFile.setName(FilenameUtils.getName(templateUpload.getClientFileName()));
                    templateFile.setPath(StorageUtil.createPath(reportContent.getPath(), templateFile.getName()));
                    LOG.info("*** UPLOAD: template file path = '" + templateFile.getPath() + "'");
                    templateFile.setMimeType(templateUpload.getContentType());
                    templateFile.setLastModified(Calendar.getInstance());
                    templateFile.setDataProvider(new JcrDataProviderImpl(templateUpload.getBytes()));
                    reportContent.setTemplateFile(templateFile);
                }

                report.setContent(reportContent);
                report = NextUtil.renameImagesAsUnique(report);
                
                byte[] reportBytes = NextUtil.getNextReportBytes(storageService.getSettings(), report);                                               
                byte status = ReportUtil.isValidReportVersion(reportBytes);
                if (ReportUtil.REPORT_INVALID_OLDER == status) {
                    error("Cannot publish a report version older than 2.0.");
                } else if (ReportUtil.REPORT_INVALID_NEWER == status) {
                    error("Cannot publish a report version newer than " + ReleaseInfoAdapter.getVersionNumber());
                } else {
                	ro.nextreports.engine.Report rep = NextUtil.getNextReport(storageService.getSettings(), report);
                    report.setSpecialType(rep.getLayout().getReportType());                	
                    if (update) {
                        // IMPORTANT : see ReportModifiedAdvice
                        storageService.modifyEntity(report);                        
                    } else {
                        storageService.addEntity(report);                        
                    }
                    info(getString("ActionContributor.UploadNext.success"));
                    //setResponsePage(HomePage.class);
                    EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
                    panel.backwardWorkspace(target);
                }
                LOG.info("*** UPLOAD ended ----------------------------------------------- ");
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error(e.getMessage(), e);
                error(getString("ActionContributor.UploadNext.failed") + " : " + e.getMessage());
            }
        }

    }
}
