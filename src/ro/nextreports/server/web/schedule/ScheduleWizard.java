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

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.wizard.IWizard;
import org.apache.wicket.extensions.wizard.Wizard;
import org.apache.wicket.extensions.wizard.WizardModel;
import org.apache.wicket.extensions.wizard.WizardStep;
import org.apache.wicket.extensions.wizard.WizardModel.ICondition;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.odlabs.wiquery.core.events.Event;
import org.odlabs.wiquery.core.events.MouseEvent;
import org.odlabs.wiquery.core.events.WiQueryEventBehavior;
import org.odlabs.wiquery.core.javascript.JsScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.distribution.Destination;
import ro.nextreports.server.domain.MailServer;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.ReportRuntimeParameterModel;
import ro.nextreports.server.domain.ReportRuntimeTemplate;
import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.domain.Settings;
import ro.nextreports.server.domain.ShortcutType;
import ro.nextreports.server.licence.ModuleLicence;
import ro.nextreports.server.licence.NextServerModuleLicence;
import ro.nextreports.server.report.AbstractReportRuntimeParameterModel;
import ro.nextreports.server.report.ReportConstants;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.service.AnalysisService;
import ro.nextreports.server.service.DataSourceService;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.SchedulerUtil;
import ro.nextreports.server.util.StorageUtil;
import ro.nextreports.server.web.common.event.AjaxUpdateEvent;
import ro.nextreports.server.web.common.event.AjaxUpdateListener;
import ro.nextreports.server.web.common.event.ChangeShortcutTemplateEvent;
import ro.nextreports.server.web.common.event.ChangeValuesTemplateEvent;
import ro.nextreports.server.web.common.form.AdvancedForm;
import ro.nextreports.server.web.common.jgrowl.JGrowlAjaxBehavior;
import ro.nextreports.server.web.common.misc.AjaxWizardButtonBar;
import ro.nextreports.server.web.common.panel.NextFeedbackPanel;
import ro.nextreports.server.web.core.EntityBrowserPanel;
import ro.nextreports.server.web.core.HomePage;
import ro.nextreports.server.web.core.section.SectionManager;
import ro.nextreports.server.web.core.validation.DuplicationEntityValidator;
import ro.nextreports.server.web.core.validation.JcrNameValidator;
import ro.nextreports.server.web.integration.ReportsPage;
import ro.nextreports.server.web.report.NextRuntimePanel;
import ro.nextreports.server.web.report.ReportRuntimeModel;
import ro.nextreports.server.web.report.jasper.JasperRuntimePanel;
import ro.nextreports.server.web.schedule.batch.BatchDefinitionPanel;
import ro.nextreports.server.web.schedule.destination.DestinationsPanel;
import ro.nextreports.server.web.schedule.time.JobPanel;
import ro.nextreports.engine.queryexec.QueryParameter;
import ro.nextreports.engine.util.ParameterUtil;

public class ScheduleWizard extends Wizard {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleWizard.class);

    @SpringBean
    protected StorageService storageService;

    @SpringBean
    protected DataSourceService dataSourceService;

    @SpringBean
    protected ReportService reportService;
    
    @SpringBean
    protected AnalysisService analysisService;

    @SpringBean
    protected SectionManager sectionManager;
    
    @SpringBean
    private ModuleLicence moduleLicence;

    private SchedulerJob schedulerJob;
    private ReportRuntimeModel runtimeModel;
    private boolean edit = false;
    private boolean runNow = false;
    private ICondition notRunNowCondition = new NotRunNowCondition();
    private ICondition editNotRunNowCondition = new EditNotRunNowCondition();
    private ICondition batchCondition = new BatchCondition();
    private ICondition mailServerDefinedCondition = new MailServerDefinedCondition();
    private AjaxWizardButtonBar buttonBar;
    private int[] finishSteps = new int[0];
    private TemplatePanel templatePanel;

    //private String INFO = "After 'Finish' is clicked the running process is started and it can be seen in the Monitor section";
    private String INFO = getString("ActionContributor.Run.info");

    public ScheduleWizard(String id, Report report, boolean runNow) {
        super(id);

        schedulerJob = new SchedulerJob();
        schedulerJob.setReport(report);
        this.runNow = runNow;

        initWizard();

        add(AttributeModifier.replace("class", "wizardScheduler"));
    }

    public ScheduleWizard(String id, SchedulerJob job) {
        super(id);

        schedulerJob = job;
        edit = true;

        initWizard();
    }

    private void initWizard() {
        setDefaultModel(new CompoundPropertyModel<ScheduleWizard>(this));
        
        WizardModel model = new WizardModel();
        model.add(new ScheduleNameStep(), editNotRunNowCondition);
        model.add(new ScheduleRuntimeStep());
        model.add(new ScheduleJobStep(), notRunNowCondition);
        if (moduleLicence.isValid(NextServerModuleLicence.BATCH_MODULE)) {
        	model.add(new ScheduleBatchStep(), batchCondition);
        }
        model.add(new ScheduleDestinationsStep());
                        
        if (runNow) {
        	finishSteps = new int[] {1,2,3};
        } else {
        	finishSteps = new int[] {2,3,4};
        }        

        // initialize the wizard with the wizard model we just built
        init(model);
    }

    // Job step and Destination step are allowed to finish 
    protected Component newButtonBar(String s) {      	
        buttonBar = new AjaxWizardButtonBar(s, this) {
            public void onCancel(AjaxRequestTarget target) {
                EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
                panel.backwardWorkspace(target);
            }
        };
        buttonBar.setFinishSteps(finishSteps);
        return buttonBar;
    }

    public SchedulerJob getSchedulerJob() {
        return schedulerJob;
    }

    @Override
    public void onFinish() {       	    	
    	
		// a scheduled alarm / indicator / display must have at least an alert
		if (schedulerJob.getReport().isAlarmType() || schedulerJob.getReport().isIndicatorType() || schedulerJob.getReport().isDisplayType()) {
			if (schedulerJob.getDestinations().isEmpty()) {
				error(getString("ActionContributor.Run.destination.error.alert"));
				return;
			}
		}
						    	
    	String globalMessage;
    	if (runNow) {
    		globalMessage = getString("ActionContributor.Run.running");
    	} else {
    		globalMessage = getString("ActionContributor.Run.scheduledMessage");
    	}   
    	
    	schedulerJob.setCreator(SecurityContextHolder.getContext().getAuthentication().getName());
    	
    	if (ReportConstants.ETL_FORMAT.equals(runtimeModel.getExportType())) {
    		// test to create user node under analysis
    		analysisService.checkAnalysisPath();
    	}
    	
        schedulerJob.setRuntimeModel(runtimeModel);
        
        if (schedulerJob.getBatchDefinition() != null) {
			String batchParameter = schedulerJob.getBatchDefinition().getParameter();
			if (batchParameter != null) {	
				// batch parameter must not be set as dynamic, if it is dynamic we reset it.
				schedulerJob.getReportRuntime().resetDynamic(batchParameter);
				
				// all children dependent parameters to batch parameter must be dynamic
				ro.nextreports.engine.Report nextReport = NextUtil.getNextReport(storageService.getSettings(), schedulerJob.getReport());					
				Map<String, QueryParameter> map = ParameterUtil.getChildDependentParameters(nextReport, batchParameter);				
				schedulerJob.getReportRuntime().setDynamic(new ArrayList<String>(map.keySet()));
			}
		}
        
        if (!runNow) {
            schedulerJob.setPath(StorageConstants.SCHEDULER_ROOT + "/" + schedulerJob.getName());
            SchedulerUtil.updateSchedulerTime(schedulerJob.getTime());            
            schedulerJob.getTime().setPath(StorageUtil.createPath(schedulerJob, schedulerJob.getTime().getName()));
            schedulerJob.getReportRuntime().setPath(StorageUtil.createPath(schedulerJob, schedulerJob.getReportRuntime().getName()));
            schedulerJob.setRunNow(false);

            for (Destination destination : schedulerJob.getDestinations()) {
                // temporary path created with an UUID
                if (destination.getPath().indexOf(StorageConstants.PATH_SEPARATOR) == -1) {
                    // add new destination
                    destination.setPath(schedulerJob.getPath() + StorageConstants.PATH_SEPARATOR +
                        StorageConstants.DESTINATIONS + StorageConstants.PATH_SEPARATOR +  destination.getName());                    
                }                
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("job = " + schedulerJob);
        }

        try {
        	
        	if (runtimeModel.isSaveTemplate()) {
        		//System.out.println("**** save template : " + runtimeModel.getTemplateName());
        		
        		// save values template
        		Report report = schedulerJob.getReport();
        		ReportRuntimeTemplate template = new ReportRuntimeTemplate();
        		template.setName(runtimeModel.getTemplateName());
        		template.setReportRuntime(schedulerJob.getReportRuntime());     
        		template.setShortcutType(runtimeModel.getShortcutType());

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
                path += StorageConstants.PATH_SEPARATOR + "templates" + StorageConstants.PATH_SEPARATOR + template.getName();        
                template.setPath(path);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Create values template '" + template.getPath() + "'");
                }                            

                try {
                    String id = storageService.addEntity(template);
                    schedulerJob.setTemplate((ReportRuntimeTemplate)storageService.getEntityById(id));
                } catch (Exception e) {
                    e.printStackTrace();
                    LOG.error(e.getMessage(), e);
                }        		        		
        	} else {        		
        		ReportRuntimeTemplate template = templatePanel.getTemplate();        		
                schedulerJob.setTemplate(templatePanel.getTemplate());                              		
        	}
        	
            if (edit) {
                // IMPORTANT : see SchedulerJobModifieddvice
                storageService.modifyEntity(schedulerJob);
            } else if (runNow) {       
            	schedulerJob.setRunNow(true);
                reportService.runReport(schedulerJob);
                //sectionManager.setSelectedSectionId(MonitorSection.ID);
            } else {
                // IMPORTANT : see SchedulerJobAddedAdvice
                storageService.addEntity(schedulerJob);
                sectionManager.setSelectedSectionId(SchedulerSection.ID);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error(e.getMessage(), e);
            error(e.getMessage());
        } finally {
            // some values for jasper parameters are updated in run method, so we log here
            if (LOG.isDebugEnabled()) {
                LOG.debug(schedulerJob.getReportRuntime().toString());
            }
        }

        // just put a feedback message to be shown by JGrowl
        getSession().getFeedbackMessages().add(new FeedbackMessage(null, globalMessage, JGrowlAjaxBehavior.INFO_FADE));        
        if (getPage() instanceof ReportsPage) {
        	setResponsePage(ReportsPage.class);
        } else {
        	setResponsePage(HomePage.class);
        }
    }

    private final class ScheduleNameStep extends WizardStep {

        public ScheduleNameStep() {
            super();
            RequiredTextField<String> textField = new RequiredTextField<String>("schedulerJob.name");
            textField.setLabel(new Model<String>(getString("name")));
            textField.add(new JcrNameValidator());
            add(textField);
            add(new DuplicationEntityValidator(textField, StorageConstants.SCHEDULER_ROOT));
        }

        @Override
        public String getTitle() {
            return getString("ActionContributor.Run.schedule") + " : " + schedulerJob.getReport().getName();
        }

        @Override
        public String getSummary() {
            return getString("ActionContributor.Run.schedulerName");
        }

        @Override
    	public Component getHeader(final String id, final Component parent, final IWizard wizard) {
    		Component c = super.getHeader(id, parent, wizard);
    		c.get("summary").setEscapeModelStrings(false);
    		return c;
    	}
        
        public String getName() {
            return schedulerJob.getName();
        }

    }

    private final class ScheduleRuntimeStep extends WizardStep implements AjaxUpdateListener {
    	
    	private WebMarkupContainer container;    	

        public ScheduleRuntimeStep() {
            super();            
            container = new WebMarkupContainer("container");
            container.setOutputMarkupId(true);
            
            IModel<String> toggleImageModel = new LoadableDetachableModel<String>() {

                private static final long serialVersionUID = 1L;

                @Override
                protected String load() {
                    String imagePath = "images/down-gray.png";
                    if (runtimeModel.isCollapsed()) {
                        imagePath = "images/up-gray.png";
                    }
                    
                    return imagePath;
                }

            };
            ContextImage toggle = new ContextImage("toggle", toggleImageModel);
            container.add(toggle);
            
            toggle.add(new WiQueryEventBehavior(new Event(MouseEvent.CLICK) {

    			private static final long serialVersionUID = 1L;

    			@Override
    			public JsScope callback() {
    				return JsScope.quickScope(getJsCode());
    			}
    			
    			String collapse = getString("collapse");
    			String expand = getString("expand");
    			
    			private CharSequence getJsCode() {    				
    				StringBuilder buffer = new StringBuilder();
    				buffer.append("var content = $(this).siblings('.runtimePanel').find('tr.parameters');");
    				buffer.append("if (content.css('display') == 'none') {");
    				buffer.append("content.slideDown(400);");
    				buffer.append("$(this).attr('src',  \"../images/down-gray.png\");");
    				buffer.append("$(this).attr('title',  \"" + collapse +  "\");");
    				buffer.append("} else {");
    				buffer.append("content.slideUp(200);");
    				buffer.append("$(this).attr('src', \"../images/up-gray.png\");");
    				buffer.append("$(this).attr('title',  \"" + expand  + "\");");
    				buffer.append("}");    								
    				return buffer.toString();
    			}    			
    		}));            
            
            if (edit) {
                runtimeModel = SchedulerUtil.getStoredRuntimeModel(storageService, schedulerJob, reportService, dataSourceService);
            } else {
                runtimeModel = schedulerJob.createRuntimeModel();
            }

            String type = schedulerJob.getReport().getType();
            if (ReportConstants.JASPER.equals(type)) {            	
                container.add(new JasperRuntimePanel("runtimePanel", schedulerJob.getReport(), runtimeModel).setOutputMarkupPlaceholderTag(true));
            } else {
                container.add(new NextRuntimePanel("runtimePanel", schedulerJob.getReport(), runtimeModel, runNow).setOutputMarkupPlaceholderTag(true));
            }
            templatePanel = new TemplatePanel("templatePanel", schedulerJob.getReport(), runtimeModel);
            if (edit) {
            	templatePanel.setTemplate(schedulerJob.getTemplate());
            }
            templatePanel.setOutputMarkupId(true);
            add(templatePanel);
            add(container);
        }

        @Override
        public String getTitle() {
            String action = runNow ? getString("run") : getString("schedule");
            return action + " " +  getString("Report") + " : " + schedulerJob.getReport().getName();
        }

        @Override
        public String getSummary() {
            String action = runNow ? "" : getString("Section.Scheduler.name");
            String result = getString("select") + " " + action + " " + getString("ActionContributor.Run.parameters");
            if (runNow) {
                result = result + "<br>" + INFO;
            }
            return result;
        }
        
        @Override
    	public Component getHeader(final String id, final Component parent, final IWizard wizard) {
    		Component c = super.getHeader(id, parent, wizard);
    		c.get("summary").setEscapeModelStrings(false);
    		return c;
    	}
        
        // replace entire panel to have all dependent parameters compute their values
        public void onAjaxUpdate(AjaxUpdateEvent event) {   
        	boolean changed = false;
            if (event instanceof ChangeValuesTemplateEvent) {
            	ChangeValuesTemplateEvent changeEvent = (ChangeValuesTemplateEvent) event;                	
            	runtimeModel = SchedulerUtil.getStoredRuntimeModel(storageService, changeEvent.getReport(), changeEvent.getReportRuntime(), reportService, dataSourceService);
            	runtimeModel.setShortcutType(changeEvent.getShortcutType());            	
            	if (templatePanel != null) {
            		templatePanel.setRuntimeModel(runtimeModel);
            	}
            	String type = schedulerJob.getReport().getType();
                if (ReportConstants.JASPER.equals(type)) {
                    container.replace(new JasperRuntimePanel("runtimePanel", schedulerJob.getReport(), runtimeModel));
                } else {
                    container.replace(new NextRuntimePanel("runtimePanel", schedulerJob.getReport(), runtimeModel, runNow));
                }     
                changed = true;            	  
            } else if (event instanceof ChangeShortcutTemplateEvent) {            	
            	ChangeShortcutTemplateEvent shortcutEvent = (ChangeShortcutTemplateEvent)event;            	
            	if (shortcutEvent.getShortcutType().equals(ShortcutType.NONE)) {
            		// no selection
            		return;
            	}
            	Date[] dates = shortcutEvent.getShortcutType().getTimeShortcutType().getDates();		            	
				HashMap<String, ReportRuntimeParameterModel>  params = runtimeModel.getParameters();								
				AbstractReportRuntimeParameterModel startModel = (AbstractReportRuntimeParameterModel)params.get(QueryParameter.INTERVAL_START_DATE_NAME);
				if (startModel != null) {
					Object start = startModel.getRawValue();
					if (start instanceof Date) {						
						startModel.setRawValue(dates[0]);						
					} else if (start instanceof Timestamp) {
						startModel.setRawValue(new Timestamp(dates[0].getTime()));
					} else if (start instanceof Time) {
						startModel.setRawValue(new Time(dates[0].getTime()));
					}
				}
				AbstractReportRuntimeParameterModel endModel = (AbstractReportRuntimeParameterModel)params.get(QueryParameter.INTERVAL_END_DATE_NAME);
				if (endModel != null) {
					Object end = endModel.getRawValue();
					if (end instanceof Date) {
						endModel.setRawValue(dates[1]);						
					} else if (end instanceof Timestamp) {
						endModel.setRawValue(new Timestamp(dates[1].getTime()));
					} else if (end instanceof Time) {
						endModel.setRawValue(new Time(dates[1].getTime()));
					}
				}				
				changed = true;					
				schedulerJob.setTemplate(templatePanel.getTemplate());
            }            
            runtimeModel.setCollapsed(changed);
            event.getTarget().add(container);            
            if (runtimeModel.isCollapsed()) {
            	event.getTarget().appendJavaScript("jQuery('.runtimePanel').find('tr.parameters').css({'display' : 'none' });");
            }	
            
        }

    }

    private final class ScheduleJobStep extends WizardStep {

        public ScheduleJobStep() {
            super();

            if (edit) {
                SchedulerUtil.restoreSchedulerTime(schedulerJob.getTime());
            }
            add(new JobPanel("jobPanel", schedulerJob));
        }

        @Override
        public String getTitle() {
            return getString("ActionContributor.Run.schedule") + " : " + schedulerJob.getReport().getName();
        }

        @Override
        public String getSummary() {
            return getString("ActionContributor.Run.time");
        }
        
        @Override
    	public Component getHeader(final String id, final Component parent, final IWizard wizard) {
    		Component c = super.getHeader(id, parent, wizard);
    		c.get("summary").setEscapeModelStrings(false);
    		return c;
    	}

    }
    
    private final class ScheduleBatchStep extends WizardStep {

        public ScheduleBatchStep() {
            super();
            add(new BatchDefinitionPanel("batchPanel", schedulerJob));
        }

        @Override
        public String getTitle() {
        	return getString("ActionContributor.Run.schedule") + " : " + schedulerJob.getReport().getName();
        }

        @Override
        public String getSummary() {
        	return getString("ActionContributor.Run.batch");
        }
        
        @Override
    	public Component getHeader(final String id, final Component parent, final IWizard wizard) {
    		Component c = super.getHeader(id, parent, wizard);
    		c.get("summary").setEscapeModelStrings(false);
    		return c;
    	}

    }

    private final class ScheduleDestinationsStep extends WizardStep {

        public ScheduleDestinationsStep() {
            super();
            add(new DestinationsPanel("destinationsPanel", schedulerJob));
        }

        @Override
        public String getTitle() {
            String action = runNow ? getString("run") : getString("schedule");
            return action + getString("Report") + "  : " + schedulerJob.getReport().getName();
        }

        @Override
        public String getSummary() {
            String action = runNow ? "" : getString("Section.Scheduler.name");
            String result = getString("select") + " " + action + " " + getString("ActionContributor.Run.destinations");
            if (runNow) {
                result = result + "<br>" + INFO;
            }
            return result;
        }
        
        @Override
    	public Component getHeader(final String id, final Component parent, final IWizard wizard) {
    		Component c = super.getHeader(id, parent, wizard);
    		c.get("summary").setEscapeModelStrings(false);
    		return c;
    	}

    }

    private final class NotRunNowCondition implements ICondition {

        public boolean evaluate() {
            return !runNow;
        }

    }

    private final class EditNotRunNowCondition implements ICondition {

        public boolean evaluate() {
            return !runNow && !edit;
        }

    }
    
    private final class BatchCondition implements ICondition {
    	
    	public boolean evaluate() {
    		String type = schedulerJob.getReport().getType();
    		return ReportConstants.NEXT.equals(type);
    	}	
    }

    private final class MailServerDefinedCondition implements ICondition {
        
        public boolean evaluate() {
        	Settings settings = storageService.getSettings();    
        	MailServer mailServer = settings.getMailServer();    		          
            return (mailServer != null) && !"".equals(mailServer.getIp());
        }
        
    }

    // create form for scheduler (to validate components)
    protected <E> Form<E> newForm(String s) {
        return new AdvancedForm<E>(s);
    }

    // overwrite feedback panel so we will show to it only the messages that do not come from validation
    // (like errors, or information)
    protected FeedbackPanel newFeedbackPanel(String s) {
        return new NextFeedbackPanel(s, getForm());
    }

}
