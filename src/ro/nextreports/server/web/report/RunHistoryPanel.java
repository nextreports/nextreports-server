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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.datetime.PatternDateConverter;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.extensions.yui.calendar.DateField;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.CheckGroupSelector;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.RangeValidator;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ro.nextreports.engine.util.DateUtil;
import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.DateRange;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.RunReportHistory;
import ro.nextreports.server.domain.User;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.PermissionUtil;
import ro.nextreports.server.util.ServerUtil;
import ro.nextreports.server.util.StorageUtil;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.common.behavior.AlertBehavior;
import ro.nextreports.server.web.common.behavior.SimpleTooltipBehavior;
import ro.nextreports.server.web.common.misc.AjaxSubmitConfirmLink;
import ro.nextreports.server.web.common.misc.SimpleLink;
import ro.nextreports.server.web.common.table.BaseTable;
import ro.nextreports.server.web.common.table.BooleanImageLinkPropertyColumn;
import ro.nextreports.server.web.common.table.DateColumn;
import ro.nextreports.server.web.common.table.ImageLinkPropertyColumn;
import ro.nextreports.server.web.common.table.SortableDataAdapter;
import ro.nextreports.server.web.core.BasePage;
import ro.nextreports.server.web.core.EntityBrowserPanel;
import ro.nextreports.server.web.report.RunHistoryPanel.CheckBoxHeaderPanel;
import ro.nextreports.server.web.report.RunHistoryPanel.CheckBoxPanel;

//
public class RunHistoryPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean
    private ReportService reportService;

    @SpringBean
    private StorageService storageService;

    @SpringBean
    private SecurityService securityService;

    private RunReportHistoryDataProvider runHistoryDataProvider;
    private DataTable<RunReportHistory, String> runHistoryTable;

    private CheckGroup<RunReportHistory> group;
    private Date time;   
    private Date tillTime;
    private DateField tillTimeField;
    private FeedbackPanel feedbackPanel;
    
    private static String DAY_TYPE = "Day";
    private static String FROM_DAY_TYPE = "DayRange";
    
    private String type = DAY_TYPE;    
    
    private int rowsPerPage = 10;

    private transient List<RunReportHistory> marked = new ArrayList<RunReportHistory>();

    @SuppressWarnings("unchecked")
	public RunHistoryPanel(String id, final Report report) {
        super(id);                
                       
        group = new CheckGroup<RunReportHistory>("group", marked);

        String reportPath = null;
        if (report != null) {
            reportPath = report.getPath();
        }
        runHistoryDataProvider = new RunReportHistoryDataProvider(reportPath);
        runHistoryTable = createRunHistoryTable(runHistoryDataProvider);
        runHistoryTable.setOutputMarkupId(true);

        /*
        int updateInterval = NextServerApplication.get().getConfiguration().getInt("ui.updateInterval", 0);
        if (updateInterval > 0) {
        	runHistoryTable.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(updateInterval)));
        }
        */

        Form<RunReportHistory> form = new Form<RunReportHistory>("form");     
        
        feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        form.add(feedbackPanel);
        
        List<String> types = new ArrayList<String>();
		types.add(DAY_TYPE);
		types.add(FROM_DAY_TYPE);		
		IChoiceRenderer<String> renderer = new ChoiceRenderer<String>() {

			@Override
			public Object getDisplayValue(String object) {
				return getString("ActionContributor.RunHistory." + object);
			}

			@Override
			public String getIdValue(String object, int index) {
				return object;
			}
			
		};
		DropDownChoice<String> typeDropDownChoice = new DropDownChoice<String>("type", new PropertyModel<String>(this, "type"), types, renderer);
		typeDropDownChoice.setOutputMarkupPlaceholderTag(true);
		typeDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {				
				setDateRange(target);            	
			}
			
		});
		form.add(typeDropDownChoice);
        
        time = new Date();
        DateField timeField = new DateField("time", new PropertyModel(this, "time")) {
        	
            protected DateTextField newDateTextField(String s, PropertyModel propertyModel) {
                final DateTextField dateField = DateTextField.withConverter(s, propertyModel,	new PatternDateConverter("MM/dd/yyyy", true));
                dateField.add(new AjaxFormComponentUpdatingBehavior("onchange") {
                    protected void onUpdate(AjaxRequestTarget target) {
                      // @todo wicket 1.5 does not update model for DateField and DateTimeField
                      // https://issues.apache.org/jira/browse/WICKET-4496	
                      // use this as an workaround	
                      time = (Date)dateField.getDefaultModelObject();
                      
                      setDateRange(target);                  	  
                   }
                });   
                dateField.setLabel(new Model<String>(getString("ActionContributor.RunHistory.day")));                
                return dateField;
            }  
            
            protected DatePicker newDatePicker() {
        		return new DatePicker() {
        			private static final long serialVersionUID = 1L;

        			@Override
        			protected void configure(final Map<String, Object> widgetProperties,
        				final IHeaderResponse response, final Map<String, Object> initVariables) {
        				super.configure(widgetProperties, response, initVariables);        				
        			}

					@Override
					protected boolean enableMonthYearSelection() {
						return true;
					}        			        			
        		};
        	}
        };
        timeField.setRequired(true);
        timeField.add(new AttributeModifier("class", "timeField"));
        form.add(timeField);    
        
        tillTime = new Date();
        tillTimeField = new DateField("tillTime", new PropertyModel(this, "tillTime")) {
        	
            protected DateTextField newDateTextField(String s, PropertyModel propertyModel) {
                final DateTextField dateField = DateTextField.withConverter(s, propertyModel, new PatternDateConverter("MM/dd/yyyy", true));
                dateField.add(new AjaxFormComponentUpdatingBehavior("onchange") {
                    protected void onUpdate(AjaxRequestTarget target) {
                      // @todo wicket 1.5 does not update model for DateField and DateTimeField
                      // https://issues.apache.org/jira/browse/WICKET-4496		
                      // use this as an workaround                    	
                      tillTime = (Date)dateField.getDefaultModelObject();
                      
                      setDateRange(target);                  	  
                   }
                });   
                dateField.setLabel(new Model<String>(getString("ActionContributor.RunHistory.TillDay")));                
                return dateField;
            }     
            
            protected DatePicker newDatePicker() {
        		return new DatePicker() {
        			private static final long serialVersionUID = 1L;

        			@Override
        			protected void configure(final Map<String, Object> widgetProperties,
        				final IHeaderResponse response, final Map<String, Object> initVariables) {
        				super.configure(widgetProperties, response, initVariables);        				
        			}

					@Override
					protected boolean enableMonthYearSelection() {
						return true;
					}        			        			
        		};
        	}
        };
        tillTimeField.setVisible(false);   
        tillTimeField.setOutputMarkupId(true);
        tillTimeField.setOutputMarkupPlaceholderTag(true);
        tillTimeField.add(new AttributeModifier("class", "timeField"));
        form.add(tillTimeField);    

        group.add(runHistoryTable);
        form.add(group);
        
        Label rowsLabel = new Label("rowsLabel", getString("ActionContributor.RunHistory.rows"));
        form.add(rowsLabel);
        TextField<Integer> rowsTextField = new TextField<Integer>("rows", new PropertyModel(this, "rowsPerPage"));
        rowsTextField.setRequired(true);
        rowsTextField.add(RangeValidator.minimum(5));
        rowsTextField.add(RangeValidator.maximum(1000));
        rowsTextField.setLabel(Model.of(getString("ActionContributor.RunHistory.rows")));
        form.add(rowsTextField);
        
        AjaxButton rowsLink = new AjaxButton("rowsLink"){         
            
            @Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(form);
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {						
				runHistoryTable.setItemsPerPage(rowsPerPage);						
				target.add(feedbackPanel);
                target.add(runHistoryTable);    				
			}
        };        
        form.add(rowsLink);        
        

        AjaxSubmitConfirmLink submitLink = new AjaxSubmitConfirmLink("deleteLink", getString("deleteEntities")) {

			private static final long serialVersionUID = 1L;

			public void onSubmit(AjaxRequestTarget target, Form<?> form) {
				if (marked == null) {				
					return;
				}
                for (RunReportHistory h : marked) {
                    try {
                        storageService.removeEntityById(h.getId());
                    } catch (Exception e) {
                        e.printStackTrace();
                        add(new AlertBehavior(e.getMessage()));
                        target.add(this);
                    }
                }
                if (marked.size() > 0) {
                    target.add(runHistoryTable);
                }
            }

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(form);
			}

        };
        form.add(submitLink);

        WebMarkupContainer buttonsPanel = new WebMarkupContainer("buttonsPanel") {

			@Override
			public boolean isVisible() {
				return (report != null);
			}
        	
        };
        if (NextServerSession.get().isDemo()) {
            submitLink.setVisible(false);            
        }
        
        buttonsPanel.add(new AjaxLink("cancel") {
        	
            @Override
            public void onClick(AjaxRequestTarget target) {
//                if (ActionUtil.isFromSearch()) {
//                    setResponsePage(new SearchEntityPage(null));
//                } else {
//                    setResponsePage(HomePage.class);
//                }
                EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
                panel.backwardWorkspace(target);
            }
            
        });
        form.add(buttonsPanel);

        add(form);
    }
    
    private void setDateRange(AjaxRequestTarget target) {
    	if (DAY_TYPE.equals(type))  {
    		tillTime = new Date();
    		tillTimeField.setVisible(false);
			runHistoryDataProvider.setDateRange(new DateRange(DateUtil.floor(time), DateUtil.ceil(time)));
		} else {
			tillTimeField.setVisible(true);
			if (DateUtil.after(time, tillTime)) {
				error(getString("ActionContributor.RunHistory.rangeInvalid"));
				target.add(feedbackPanel);
				target.add(tillTimeField);
				return;
			} 
			runHistoryDataProvider.setDateRange(new DateRange(DateUtil.floor(time),  DateUtil.ceil(tillTime)));			
		}
    	target.add(tillTimeField);
    	target.add(runHistoryTable);
    	target.add(feedbackPanel);
    }

	public DataTable<RunReportHistory, String> getRunHistoryTable() {
		return runHistoryTable;
	}

	protected DataTable<RunReportHistory, String> createRunHistoryTable(RunReportHistoryDataProvider dataProvider) {
    	SortableDataProvider<RunReportHistory, String> sortableDataProvider = new SortableDataAdapter<RunReportHistory>(dataProvider);
    	sortableDataProvider.setSort("endDate", SortOrder.DESCENDING);
        return new BaseTable<RunReportHistory>("runHistoryTable", createHistoryTableColumns(), sortableDataProvider, rowsPerPage);
    }

    protected List<IColumn<RunReportHistory, String>> createHistoryTableColumns() {
        List<IColumn<RunReportHistory, String>> columns = new ArrayList<IColumn<RunReportHistory, String>>();

        columns.add(new AbstractColumn<RunReportHistory, String>(new Model<String>(getString("ActionContributor.EditParameters.parameterSelect"))) {

            public void populateItem(Item<ICellPopulator<RunReportHistory>> item, String componentId, IModel<RunReportHistory> rowModel) {
                try {
                    if (securityService.hasPermissionsById(ServerUtil.getUsername(),
                            PermissionUtil.getDelete(), rowModel.getObject().getId())) {
                        item.add(new CheckBoxPanel(componentId, rowModel, item));
                    } else {
                        item.add(new Label(componentId));
                    }
                    item.add(new AttributeModifier("width", "30px"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public Component getHeader(String s) {
                return new CheckBoxHeaderPanel(s);
            }

        });

        columns.add(new PropertyColumn<RunReportHistory, String>(new Model<String>(getString("Report")), "path", "path") {

            @Override
			public void populateItem(Item<ICellPopulator<RunReportHistory>> item, String componentId, IModel<RunReportHistory> rowModel) {
                String path = rowModel.getObject().getPath();                                
                String relativePath = path.substring(StorageConstants.REPORTS_ROOT.length(), path.indexOf("/runHistory"));
                String name = StorageUtil.getName(relativePath);
                Label label = new Label(componentId, name);
                label.add(new SimpleTooltipBehavior(relativePath));
				item.add(label);
			}

        });

        columns.add(new AbstractColumn<RunReportHistory, String>(new Model<String>(getString("ActionContributor.DataSource.url"))) {

            public void populateItem(Item<ICellPopulator<RunReportHistory>> item, String componentId,
                                     final IModel<RunReportHistory> rowModel) {
                String url = rowModel.getObject().getUrl();
                if ((url == null) || url.equals("")) {
                    item.add(new Label(componentId));
                    return;
                }

                // dynamic url
                String fileName = url.substring(url.lastIndexOf("/") + 1);
                String dynamicUrl = reportService.getReportURL(fileName);
                item.add(new SimpleLink(componentId, dynamicUrl, getString("view"), true));
            }

        });

        columns.add(new PropertyColumn<RunReportHistory, String>(new Model<String>(getString("DashboardNavigationPanel.owner")), "runnerId", "runnerId") {

            @Override
            protected IModel<?> createLabelModel(IModel<RunReportHistory> rowModel) {
                try {
                    // TODO optimization (getNameByUUID - no entity creation)
                    Entity entity = storageService.getEntityById(rowModel.getObject().getRunnerId());
                    String owner;
                    if (entity instanceof User) {
                        owner = entity.getName();
                    } else {
                        //SchedulerJob
                        owner = entity.getCreatedBy();
                    }
                    return new Model<String>(owner);
                } catch (NotFoundException ex) {
                    // if user or scheduler was deleted
                    return new Model<String>();
                }
            }

        });
        columns.add(new PropertyColumn<RunReportHistory, String>(new Model<String>(getString("ActionContributor.RunHistory.type")), "runnerType", "runnerType") {
        	@Override
            public void populateItem(Item<ICellPopulator<RunReportHistory>> item, String componentId, IModel<RunReportHistory> rowModel) {                
                item.add(new Label(componentId, getString("MonitorPanel.runnerType." + rowModel.getObject().getRunnerType())));
            }
        });
        columns.add(new DateColumn<RunReportHistory>(new Model<String>(getString("startDate")), "startDate", "startDate"));
        columns.add(new PropertyColumn<RunReportHistory, String>(new Model<String>(getString("duration")), "duration", "duration") {

            @Override
            public void populateItem(Item<ICellPopulator<RunReportHistory>> item, String componentId, IModel<RunReportHistory> rowModel) {
                super.populateItem(item, componentId, rowModel);
                item.add(new AttributeModifier("width", "70px"));
            }

            @Override
            protected IModel<?> createLabelModel(IModel<RunReportHistory> rowModel) {
                int runTime = rowModel.getObject().getDuration();
                String text = "";
                if (runTime >= 0) {
                    DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm:ss").withZone(DateTimeZone.UTC);
                    text = formatter.print(runTime * 1000);
                }

                return new Model<String>(text);
            }

        });
        columns.add(new DateColumn<RunReportHistory>(new Model<String>(getString("endDate")), "endDate", "endDate"));
        
        columns.add(new ImageLinkPropertyColumn<RunReportHistory>(new Model<String>(getString("Query"))) {

            @Override
            public void populateItem(Item<ICellPopulator<RunReportHistory>> item, String componentId, IModel<RunReportHistory> rowModel) {
                super.populateItem(item, componentId, rowModel);
                item.add(new AttributeModifier("width", "50px"));
                item.add(new SimpleTooltipBehavior(getString("Query")));
            }

            @Override
            public void onImageClick(RunReportHistory runHistory, AjaxRequestTarget target) {
                ModalWindow dialog = findParent(BasePage.class).getDialog();
                dialog.setTitle(getString("Query"));
                dialog.setInitialWidth(600);
                dialog.setInitialHeight(400);                
                dialog.setContent(new RunHistoryQueryPanel(dialog.getContentId(), runHistory));
                dialog.show(target);
            }
            
            @Override
            public String getLinkImageName() {
            	return "sql.png";
            }

        });
        
        columns.add(new BooleanImageLinkPropertyColumn<RunReportHistory>(new Model<String>(getString("success")), "success", "success") {

            @Override
            public void populateItem(Item<ICellPopulator<RunReportHistory>> item, String componentId, IModel<RunReportHistory> rowModel) {
                super.populateItem(item, componentId, rowModel);
                item.add(new AttributeModifier("width", "50px"));
                item.add(new SimpleTooltipBehavior(getString("details")));
            }

            @Override
            public void onImageClick(RunReportHistory runHistory, AjaxRequestTarget target) {
                ModalWindow dialog = findParent(BasePage.class).getDialog();
                dialog.setTitle(getString("details"));
                dialog.setInitialWidth(350);
                dialog.setInitialHeight(200);                
                dialog.setContent(new RunHistoryDetailPanel(dialog.getContentId(), runHistory));
                dialog.show(target);
            }

        });

        return columns;
    }

    class CheckBoxPanel extends Panel {
    	
        public CheckBoxPanel(String id, IModel<RunReportHistory> model, final Item<ICellPopulator<RunReportHistory>> item) {
            super(id, model);
            add(new Check<RunReportHistory>("select", model));
        }
        
    }

    class CheckBoxHeaderPanel extends Panel {
    	
        public CheckBoxHeaderPanel(String id) {
            super(id);
            CheckGroupSelector selector = new CheckGroupSelector("groupselector");
            group.add(selector);
            add(selector);
        }
        
    }

}
