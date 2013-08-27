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
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.ReportRuntimeTemplate;
import ro.nextreports.server.domain.ShortcutType;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.report.util.ReportUtil;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.StorageUtil;
import ro.nextreports.server.web.common.event.ChangeShortcutTemplateEvent;
import ro.nextreports.server.web.common.event.ChangeValuesTemplateEvent;
import ro.nextreports.server.web.core.BasePage;
import ro.nextreports.server.web.core.validation.FoundEntityValidator;
import ro.nextreports.server.web.report.ReportRuntimeModel;

import ro.nextreports.engine.util.TimeShortcutType;

public class TemplatePanel extends Panel {
	
	@SpringBean
	private ReportService reportService;
	
	@SpringBean
	private StorageService storageService;
	
	private static final Logger LOG = LoggerFactory.getLogger(TemplatePanel.class);

	private Report report;
	private ReportRuntimeTemplate template = null;
	private ReportRuntimeModel runtimeModel;
	private FoundEntityValidator validator;
	private DropDownChoice<ShortcutType> shortcutChoice;
	private TextField<Integer> timeUnitsField;
	private DropDownChoice<String> timeTypeChoice;
	private String timeType = "Days";
	private WebMarkupContainer shortcutLastContainer;

	public TemplatePanel(String id, final Report report, ReportRuntimeModel runtimeModel) {
		super(id);
		this.report = report;
		this.runtimeModel = runtimeModel;
		if (this.runtimeModel.getShortcutType() == null) {
			this.runtimeModel.setShortcutType(ShortcutType.NONE);
		}
		timeType = TimeShortcutType.getTypeName(runtimeModel.getShortcutType().getTimeType());
		init();
	}

	private void init() {
		
		validator = createValidator();
		
		add(new Label("templateLabel", getString("ActionContributor.Run.template.name")));

		ChoiceRenderer<ReportRuntimeTemplate> nameRenderer = new ChoiceRenderer<ReportRuntimeTemplate>("name") {
        	
			private static final long serialVersionUID = 1L;

			@Override
			public Object getDisplayValue(ReportRuntimeTemplate template) {				
				if (template == null) {
					return getString("nullValid");
				}
                return template.getName();
            }
			
        };
        
		final DropDownChoice<ReportRuntimeTemplate> templateChoice = new DropDownChoice<ReportRuntimeTemplate>("template", 
				new PropertyModel<ReportRuntimeTemplate>(this, "template"), new LoadableDetachableModel<List<ReportRuntimeTemplate>>() {

					@Override
					protected List<ReportRuntimeTemplate> load() {
						List<ReportRuntimeTemplate> templates = new ArrayList<ReportRuntimeTemplate>();
				        try {
				        	templates = reportService.getReportTemplates(report.getPath());				        	
				        } catch (Exception e) {
				            e.printStackTrace();
				            LOG.error(e.getMessage(), e);
				            error(e.getMessage());
				        } 
				        return templates;
					}}, nameRenderer);	
		
		templateChoice.setNullValid(true);
		
		
		templateChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			protected void onUpdate(AjaxRequestTarget target) {			
				if (template != null) {					
					new ChangeValuesTemplateEvent(templateChoice, report, template.getReportRuntime(), template.getShortcutType(), target).fire();
					runtimeModel.setShortcutType(template.getShortcutType());					
					target.add(shortcutChoice);
					
					// when a template is changed then the shortcut is changed
					ShortcutType shortcutType = runtimeModel.getShortcutType();					
					new ChangeShortcutTemplateEvent(shortcutChoice, shortcutType, target).fire();
					
					timeType = TimeShortcutType.getTypeName(runtimeModel.getShortcutType().getTimeType());
					boolean visible = (runtimeModel.getShortcutType().getType() == -1);
					shortcutLastContainer.setVisible(visible);								
					target.add(shortcutLastContainer);					
				} else {
					runtimeModel.setShortcutType(ShortcutType.NONE);					
					shortcutLastContainer.setVisible(false);
					target.add(shortcutChoice);
					target.add(shortcutLastContainer);		
				}
			}
		});
		add(templateChoice);
		
		final TextField<String> nameField = new TextField<String>("name",  new PropertyModel<String>(this, "runtimeModel.templateName"));
		nameField.setRequired(false);
		nameField.setEnabled(false);
		nameField.setOutputMarkupId(true);
		nameField.setLabel(Model.of(getString("ActionContributor.Run.template.info")));
		add(nameField);
		
		add(new Label("saveTemplateLabel", getString("ActionContributor.Run.template.save")));
		final CheckBox saveTemplate = new CheckBox("saveTemplate", new PropertyModel<Boolean>(this, "runtimeModel.saveTemplate"));
		saveTemplate.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			protected void onUpdate(AjaxRequestTarget target) {
				boolean save = runtimeModel.isSaveTemplate();
				nameField.setEnabled(save);
				nameField.setRequired(save);
				if (save) {					
					nameField.add(validator);
				} else {
					nameField.remove(validator);
				}
				target.add(nameField);
			}
		});
		add(saveTemplate);	
		
		WebMarkupContainer shortcutContainer = new WebMarkupContainer("shortcutContainer");
		boolean visible = ReportUtil.hasIntervalParameters(storageService.getSettings(), report);
		shortcutContainer.setVisible(visible);
		add(shortcutContainer);
		
		shortcutContainer.add(new Label("withShortcut", getString("ActionContributor.Run.template.interval")));
		
		ChoiceRenderer<ShortcutType> shortcutRenderer = new ChoiceRenderer<ShortcutType>("name") {
        	
			private static final long serialVersionUID = 1L;

			@Override
			public Object getDisplayValue(ShortcutType shortcut) {
               return getString("ActionContributor.Run.template.interval." + ShortcutType.getName(shortcut.getType()));
            }
			
        };
		shortcutChoice = new DropDownChoice<ShortcutType>("shortcutType", 
				new PropertyModel<ShortcutType>(this, "runtimeModel.shortcutType"),			
				ShortcutType.getTypes(), shortcutRenderer);
		shortcutChoice.setNullValid(false);
		shortcutChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			protected void onUpdate(AjaxRequestTarget target) {			
				ShortcutType shortcutType = runtimeModel.getShortcutType();
				new ChangeShortcutTemplateEvent(shortcutChoice, shortcutType, target).fire();	
				boolean visible = (runtimeModel.getShortcutType().getType() == -1);
				shortcutLastContainer.setVisible(visible);		
				target.add(shortcutLastContainer);				
			}
		});		
		shortcutChoice.setOutputMarkupId(true);
		shortcutContainer.add(shortcutChoice);
		
		shortcutLastContainer = new WebMarkupContainer("shortcutLastContainer");
		shortcutLastContainer.setVisible(false);
		shortcutLastContainer.setOutputMarkupPlaceholderTag(true);
		shortcutContainer.add(shortcutLastContainer);
		
		timeUnitsField = new TextField<Integer>("timeUnits",  new PropertyModel<Integer>(this, "runtimeModel.shortcutType.timeUnits"));
		timeUnitsField.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {				
				new ChangeShortcutTemplateEvent(shortcutChoice, runtimeModel.getShortcutType(), target).fire();							
			}
		});
		timeUnitsField.setRequired(false);		
		shortcutLastContainer.add(timeUnitsField);
		
		ChoiceRenderer<String> timeTypeRenderer = new ChoiceRenderer<String>() {
        	
			private static final long serialVersionUID = 1L;

			@Override
			public Object getDisplayValue(String type) {
               return getString(type.toLowerCase());
            }
			
        };
		timeTypeChoice = new DropDownChoice<String>("timeType", new PropertyModel<String>(this, "timeType"), TimeShortcutType.getTypeNames(), timeTypeRenderer);		
		timeTypeChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			protected void onUpdate(AjaxRequestTarget target) {			
				runtimeModel.getShortcutType().setTimeType(TimeShortcutType.getTypeUnit(timeType));				
				new ChangeShortcutTemplateEvent(shortcutChoice, runtimeModel.getShortcutType(), target).fire();								
			}
		});						
		shortcutLastContainer.add(timeTypeChoice);
		
		if (runtimeModel.getShortcutType().getType() == -1) {
			shortcutLastContainer.setVisible(true);
		}
		
		add(new AjaxLink<Void>("removeTemplate") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
                ModalWindow dialog = findParent(BasePage.class).getDialog();
                dialog.setTitle(getString("ActionContributor.Run.template.remove"));
                dialog.setInitialWidth(350);                                                
                dialog.setInitialHeight(200);
                dialog.setResizable(true);
                
                final ManageTemplatesPanel panel = new ManageTemplatesPanel(dialog.getContentId(), report) {

					private static final long serialVersionUID = 1L;

					@Override
                    public void onDelete(AjaxRequestTarget target, List<ReportRuntimeTemplate> selected) {
                        ModalWindow.closeCurrent(target);                        
                        if (selected.size() == 0) {
                        	return;
                        }
                        List<String> ids = new ArrayList<String>();
                        for (ReportRuntimeTemplate rt : selected) {
                        	ids.add(rt.getId());
                        }
                        try {
							storageService.removeEntitiesById(ids);
						} catch (NotFoundException e) {							
							e.printStackTrace();
							LOG.error(e.getMessage(), e);
							error(e.getMessage());
						}
                        template = null;
                        target.add(templateChoice);
                    }

                    @Override
                    public void onCancel(AjaxRequestTarget target) {
                        ModalWindow.closeCurrent(target);
                    }

                };
                dialog.setContent(panel);
                dialog.show(target);
			}
			
		});
	}

	public void setRuntimeModel(ReportRuntimeModel runtimeModel) {
		this.runtimeModel = runtimeModel;
	}		
	
	private FoundEntityValidator createValidator() {
		String path = null;
		try {
			if (!StorageUtil.isVersion(report)) {				
				path = storageService.getEntityById(report.getId()).getPath();
			} else {
				String versionId = StorageUtil.getVersionableId(report);				
				path = storageService.getEntityById(versionId).getPath();
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(e.getMessage(), e);
		}
        path += StorageConstants.PATH_SEPARATOR + "templates";  
		return new FoundEntityValidator(path, getString("ActionContributor.Run.template.exists"));
	}		
	
	public void refreshShortcut(AjaxRequestTarget target) {		
		target.add(shortcutChoice);		
		target.add(shortcutLastContainer);		
	}

	public ReportRuntimeTemplate getTemplate() {
		return template;
	}

	public void setTemplate(ReportRuntimeTemplate template) {
		this.template = template;
		if (template != null) {
			this.runtimeModel.setShortcutType(template.getShortcutType());
			this.timeType = TimeShortcutType.getTypeName(runtimeModel.getShortcutType().getTimeType());
			boolean visible = (runtimeModel.getShortcutType().getType() == -1);
			shortcutLastContainer.setVisible(visible);	
		} else {
			this.runtimeModel.setShortcutType(ShortcutType.NONE);	
			shortcutLastContainer.setVisible(false);
		}
	}				

}
