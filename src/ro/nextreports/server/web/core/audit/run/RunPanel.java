package ro.nextreports.server.web.core.audit.run;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.datetime.StyleDateConverter;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.yui.calendar.DateField;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.engine.exporter.util.TableData;
import ro.nextreports.engine.util.DateUtil;
import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.DateRange;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.RunReportHistory;
import ro.nextreports.server.domain.User;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.StorageUtil;
import ro.nextreports.server.web.common.behavior.SimpleTooltipBehavior;
import ro.nextreports.server.web.common.form.FormContentPanel;
import ro.nextreports.server.web.common.form.FormPanel;
import ro.nextreports.server.web.core.audit.InnerReport;
import ro.nextreports.server.web.core.audit.rights.AuditRights;
import ro.nextreports.server.web.core.migration.AddEntityPanel;
import ro.nextreports.server.web.core.migration.MigrationEntityType;

public class RunPanel extends FormContentPanel<AuditRun> {
	
	private static final Logger LOG = LoggerFactory.getLogger(RunPanel.class);
	
	@SpringBean
    private StorageService storageService;
	
	@SpringBean
    private ReportService reportService;

	@SpringBean
	private SecurityService securityService;

	private AuditRun auditRun;
	
	private TextField pathField;
	
	private ModalWindow runDialog;
	
	public RunPanel() {
		super(FormPanel.CONTENT_ID);
		
		setOutputMarkupId(true);
		
		auditRun = new AuditRun();				      
        setModel(new CompoundPropertyModel<AuditRun>(auditRun));      
        
        runDialog = new ModalWindow("runDialog");
        add(runDialog);
		
		final DropDownChoice<String> ownerChoice = new DropDownChoice<String>("owner", getOwners());			
		ownerChoice.setOutputMarkupId(true);
		add(ownerChoice);
		
		IChoiceRenderer<String> renderer = new ChoiceRenderer<String>() {
			@Override
			public Object getDisplayValue(String object) {				
				return getString("Section.Audit.Run.status." + object.toLowerCase());
			}			
		};		
		final DropDownChoice<String> statusChoice = new DropDownChoice<String>("status", AuditRun.STATUS_LIST, renderer);					
		statusChoice.setNullValid(false);	
		statusChoice.setOutputMarkupId(true);
		add(statusChoice);
		
		DateField startField = new DateField("startDate") {
        	
            protected DateTextField newDateTextField(String s, PropertyModel propertyModel) {            	
                DateTextField dateField = DateTextField.withConverter(s, propertyModel,new StyleDateConverter("S-", false));
                dateField.setLabel(new Model<String>(getString("Section.Audit.Run.start")));                
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
        startField.setRequired(true);        
        add(startField); 
        
        
        DateField endField = new DateField("endDate") {        	
            protected DateTextField newDateTextField(String s, PropertyModel propertyModel) {
                DateTextField dateField = DateTextField.withConverter(s, propertyModel, new StyleDateConverter("S-", false));
                dateField.setLabel(new Model<String>(getString("Section.Audit.Run.end")));                
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
        endField.setRequired(true);
        add(endField);    
        
        pathField = new TextField("path");
        pathField.setOutputMarkupPlaceholderTag(true);
        add(pathField);
        
        AjaxLink addLink = new AjaxLink<Void>("addPath") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {	            
	            runDialog.setTitle(getString("Settings.migration.export.entity.add"));
	            runDialog.setInitialWidth(350);
	            runDialog.setUseInitialHeight(false);

				AddEntityPanel addEntityPanel = new AddEntityPanel() {

					private static final long serialVersionUID = 1L;

					@Override
					public void onOk(AjaxRequestTarget target) {
						Iterator<Entity> entities = getEntities();
						if (!entities.hasNext()) {
							error(getString("Settings.migration.export.entity.select"));
							target.add(getFeedbackPanel());
							return;
						}

						Entity entity = entities.next();
						String path = entity.getPath();
						path = path.substring(StorageConstants.NEXT_SERVER_ROOT.length() + 1);						
						auditRun.setPath(path);						
						runDialog.close(target);
						target.add(pathField);
					}

				};
				addEntityPanel.setRoot(MigrationEntityType.REPORT);
				auditRun.setTree(addEntityPanel.getTreeModel());
				FormPanel formPanel = new FormPanel<Void>(runDialog.getContentId(), addEntityPanel, true) {

					private static final long serialVersionUID = 1L;

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setOkButtonValue(getString("select"));
					}

	            };
	            runDialog.setContent(formPanel);
				runDialog.show(target);
			}
						
		};
		addLink.add(new SimpleTooltipBehavior(getString("Section.Audit.Run.select")));
		add(addLink);
		
	}	
	private List<String> getOwners() {
		List<String> owners = new ArrayList<String>();

		User[] users = securityService.getUsers();
		for (User user : users) {
			owners.add(user.getUsername());
		}

		Collections.sort(owners, new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				return Collator.getInstance().compare(s1, s2);
			}
		});
		return owners;
	}
	
	public AuditRun getAuditRun() {
		auditRun.setEndDate(DateUtil.ceil(auditRun.getEndDate()));
		return auditRun;
	}
	
	protected TableData getResults(AuditRun auditRun) {
		TableData result = new TableData();
		try {
			String path = StorageConstants.REPORTS_ROOT;
			if (auditRun.getPath() != null) {
				path = StorageConstants.NEXT_SERVER_ROOT + StorageConstants.PATH_SEPARATOR + auditRun.getPath();
			}
			List<RunReportHistory> list = reportService.getRunHistoryForRange(path, new DateRange(auditRun.getStartDate(), auditRun.getEndDate()));			
			List<String> header = Arrays.asList(getString("Section.Audit.Run.owner"), 
					getString("Section.Audit.Run.report"), 
					getString("Section.Audit.Run.start"),
					getString("Section.Audit.Run.duration"),
					getString("Section.Audit.Run.end"),
					getString("Section.Audit.Run.status"),
					getString("Section.Audit.Rights.path"),
					getString("type"),
					getString("Url"));
			result.setHeader(header);
			Collections.sort(list, new Comparator<RunReportHistory>() {
				@Override
				public int compare(RunReportHistory o1, RunReportHistory o2) {					
					return o1.getStartDate().compareTo(o2.getStartDate());
				}
				
			});
			for (Iterator<RunReportHistory> it = list.iterator(); it.hasNext();) {
				RunReportHistory h = it.next();
				Entity entity = getEntity(h);
				if (entity != null) {
					String owner = getOwner(entity);
					if ( (auditRun.getOwner() == null) || owner.equals(auditRun.getOwner()) ) {					
						if (auditRun.getStatus().equals(AuditRun.STATUS_ALL) || auditRun.getStatus().equals(getStatus(h))) {
							List<Object> row = new ArrayList<Object>();
							row.add(owner);
							row.add(getName(h));
							row.add(h.getStartDate());
							row.add(getDuration(h));
							row.add(h.getEndDate());
							row.add(getStatus(h));
							row.add(getReportPath(h));
							row.add(h.getRunnerType());
							row.add(getUrl(h));
							result.getData().add(row);
						}
					}			
				}
			}
		} catch (NotFoundException e) {
			LOG.error(e.getMessage(), e);
		}
		return result;
	}
	
	private Entity getEntity(RunReportHistory h) {		
        Entity entity = null;        
		try {
			entity = storageService.getEntityById(h.getRunnerId());			
		} catch (NotFoundException e) {
			LOG.error(e.getMessage(), e);
		}        
        return entity;
	}
	
	private String getOwner(Entity entity) {		
        String owner = "";
        if (entity != null) {
			if (entity instanceof User) {
	            owner = entity.getName();
	        } else {
	            owner = entity.getCreatedBy();
	        }
        }
        return owner;
	}
	
	private String getStatus(RunReportHistory h) {
		return h.isSuccess() ? AuditRun.STATUS_SUCCESS : AuditRun.STATUS_FAILED;
	}
	
	private String getReportPath(RunReportHistory h) {
		String relativePath = h.getPath().substring(StorageConstants.REPORTS_ROOT.length(), h.getPath().indexOf("/runHistory"));
		return StorageConstants.REPORTS_ROOT + relativePath;
	}
	
	private String getName(RunReportHistory h) {
		String relativePath = h.getPath().substring(StorageConstants.REPORTS_ROOT.length(), h.getPath().indexOf("/runHistory"));		
        return StorageUtil.getName(relativePath);
	}
	
	private String getDuration(RunReportHistory h) {
		int runTime = h.getDuration();
        String duration = "";
        if (runTime >= 0) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm:ss").withZone(DateTimeZone.UTC);
            duration = formatter.print(runTime * 1000);
        }
        return duration;
	}
	
	private String getUrl(RunReportHistory h) {
		return h.getUrl();
	}
	
	protected ArrayList<Integer> getLinkColumns() {
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(8);
		return list;
	}
	
	protected String getTitle() {
		StringBuilder sb = new StringBuilder(InnerReport.RUN.name());
		sb.append("   ( ");
		sb.append(getString("Section.Audit.Run.owner"));
		sb.append(" = ");
		if (auditRun.getOwner() == null)  {
			sb.append(getString("Section.Audit.Run.status.all"));
		} else {
			sb.append(auditRun.getOwner());
		}			
		sb.append(" )");
		return sb.toString();
	}

}
