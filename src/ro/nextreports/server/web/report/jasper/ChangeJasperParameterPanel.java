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
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.jcrom.JcrDataProviderImpl;
import org.jcrom.JcrFile;

import ro.nextreports.server.domain.JasperContent;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.report.ReportConstants;
import ro.nextreports.server.report.jasper.JasperParameterSource;
import ro.nextreports.server.report.jasper.JasperReportsUtil;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.StorageUtil;
import ro.nextreports.server.web.common.form.AdvancedForm;
import ro.nextreports.server.web.common.panel.NextFeedbackPanel;
import ro.nextreports.server.web.core.EntityBrowserPanel;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

//
public class ChangeJasperParameterPanel extends Panel {

    private Report report;
    private JasperParameterSource parameter;

    @SpringBean
    private StorageService storageService;

    @SpringBean
    private ReportService reportService;

    public ChangeJasperParameterPanel(String id, Report report, JasperParameterSource parameter) {
        super(id);

        this.report = report;
        this.parameter = parameter;

        AdvancedForm form = new ChangeForm("form");
        add(form);
        NextFeedbackPanel feedbackPanel = new NextFeedbackPanel("feedback", form);
        feedbackPanel.setOutputMarkupId(true);
        form.add(feedbackPanel);

        setOutputMarkupId(true);
    }

    private class ChangeForm extends AdvancedForm<JasperParameterSource> {

        public ChangeForm(String id) {
            super(id);

            setOutputMarkupId(true);
            setModel(new CompoundPropertyModel<JasperParameterSource>(parameter));
            add(new Label("name", report.getName() + " : " + parameter.getName()));

            List<String> types = Arrays.asList(JasperParameterSource.SINGLE, JasperParameterSource.COMBO,
                    JasperParameterSource.LIST);
            IChoiceRenderer<String> renderer = new ChoiceRenderer<String>() {

				@Override
				public Object getDisplayValue(String object) {
					return getString("ActionContributor.EditParameters." + object.toString().toLowerCase());
				}

				@Override
				public String getIdValue(String object, int index) {
					return object.toString();
				}
            	
            };
            final DropDownChoice<String> choice = new DropDownChoice<String>("type", types, renderer);

            final TextArea selectArea = new TextArea<String>("select");
            selectArea.setOutputMarkupId(true);

            choice.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                protected void onUpdate(AjaxRequestTarget target) {
                    selectType(selectArea);
                    target.add(selectArea);
                }

            });

            CheckBox chkBox = new CheckBox("mandatory");
            add(chkBox);

            selectType(selectArea);
            add(choice);
            add(selectArea);

            add(new AjaxLink("cancel") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    back(target);
                }
            });

            add(new AjaxButton("submit", this) {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    try {
                        saveParameters();
                        back(target);
                    } catch (Exception e) {
                        e.printStackTrace();
                        form.error(e.getMessage());
                    }
                }
                protected void onError(AjaxRequestTarget target, Form<?> form) {
                    target.add(form);
                }

            });
        }

        private void selectType(TextArea selectArea) {
            if (JasperParameterSource.SINGLE.equals(parameter.getType())) {
                parameter.setSelect("");
                selectArea.setEnabled(false);
            } else {
                selectArea.setEnabled(true);
            }
        }

//        @Override
//        public void onSubmit() {
//            try {
//                saveParameters();
//                back();
//            } catch (Exception e) {
//                e.printStackTrace();
//                error(e.getMessage());
//            }
//        }

        private void back(AjaxRequestTarget target) {
            //setResponsePage(new EditJasperParametersPage(report));
            EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
            panel.backwardWorkspace(target);
        }

        private void done(AjaxRequestTarget target) {
            EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
            panel.restoreWorkspace(target);
        }

    }

    private String getParamFile(String file) {
        int index = file.indexOf(ReportConstants.JASPER_REPORT_EXTENSION);
        if (index == -1) {
            return null;
        }
        return file.substring(0, index) + ReportConstants.JASPER_PARAM_FILE_EXTENSION;
    }

    @SuppressWarnings("unchecked")
    private void saveParameters() throws Exception {

        JasperContent jc = (JasperContent) report.getContent();
        Map<String, Serializable> map = reportService.getReportUserParametersForEdit(report);
        List<JasperParameterSource> params = new ArrayList<JasperParameterSource>();
        for (Serializable ser : map.values()) {
            JasperParameterSource jp = (JasperParameterSource) ser;
            if (jp.getName().equals(parameter.getName())) {
                jp.setType(parameter.getType());
                jp.setSelect(parameter.getSelect());                
                jp.setMandatory(parameter.isMandatory());
                // when we modify the select , we modify also the valueClassName (class of the ID from select)
                jp.setValueClassName(JasperReportsUtil.getValueClassName(storageService, report.getDataSource(), jp.getSelect()));
            }
            params.add(jp);
        }

        String paramFile = getParamFile(jc.getMaster().getName());

        XStream xstream = new XStream(new DomDriver());
        xstream.alias("param", JasperParameterSource.class);
        String fileContent = xstream.toXML(params);

        JcrFile parametersFile = new JcrFile();
        parametersFile.setName(paramFile);
        parametersFile.setPath(StorageUtil.createPath(jc.getPath(), parametersFile.getName()));
        parametersFile.setMimeType("text/xml");
        parametersFile.setLastModified(Calendar.getInstance());
        parametersFile.setDataProvider(new JcrDataProviderImpl(fileContent.getBytes()));
        jc.setParametersFile(parametersFile);

        report.setContent(jc);
        storageService.modifyEntity(report);

    }

}
