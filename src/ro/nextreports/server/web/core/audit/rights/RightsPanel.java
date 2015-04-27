package ro.nextreports.server.web.core.audit.rights;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.markup.html.form.palette.component.Recorder;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.engine.exporter.util.TableData;
import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.AclEntry;
import ro.nextreports.server.domain.Analysis;
import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.DashboardState;
import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Group;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.domain.User;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.PermissionUtil;
import ro.nextreports.server.web.common.form.FormContentPanel;
import ro.nextreports.server.web.common.form.FormPanel;
import ro.nextreports.server.web.common.misc.ExtendedPalette;
import ro.nextreports.server.web.common.renderer.StringChoiceRenderer;

public class RightsPanel extends FormContentPanel<AuditRights> {
	
	private static final Logger LOG = LoggerFactory.getLogger(RightsPanel.class);
	
	@SpringBean
    private StorageService storageService;
	
	@SpringBean
    private SecurityService securityService;
	
	private AuditRights auditRights;
	
	public RightsPanel() {
		super(FormPanel.CONTENT_ID);
		
		setOutputMarkupId(true);
		
		auditRights = new AuditRights();				      
        setModel(new CompoundPropertyModel<AuditRights>(auditRights));       
        
        IChoiceRenderer<String> renderer = new ChoiceRenderer<String>() {
			@Override
			public Object getDisplayValue(String object) {				
				return getString("AclEntryPanel." + object.toLowerCase());
			}			
		};
        final DropDownChoice<String> typeChoice = new DropDownChoice<String>("type", AuditRights.TYPES, renderer);		
        typeChoice.setRequired(true);
        typeChoice.setNullValid(false);		
        typeChoice.setOutputMarkupPlaceholderTag(true);
		add(typeChoice);	
		
		final DropDownChoice<String> nameChoice = new DropDownChoice<String>("name", getNames(auditRights.getType()));		
		nameChoice.setRequired(true);
		nameChoice.setNullValid(false);		
		nameChoice.setOutputMarkupId(true);
		add(nameChoice);
		
		typeChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {    				                                   
                nameChoice.setChoices(getNames(auditRights.getType()));
                target.add(nameChoice);                                    
			}			
		});     
						
		IChoiceRenderer<String> entitiesRenderer = new ChoiceRenderer<String>() {
			@Override
			public Object getDisplayValue(String object) {				
				return getString("Section.Audit.Entity." + object);
			}		
			
			public String getIdValue(String object, int index) {
				return object;
			}
		};
		ExtendedPalette<String> entitiesPalette = new ExtendedPalette<String>("entities", new PropertyModel(auditRights, "entities"), 
				new Model(new ArrayList(AuditRights.ENTITIES)), entitiesRenderer, 6, false) {
			
			@Override
	        protected Recorder<String> newRecorderComponent() {
	            Recorder<String> recorder = super.newRecorderComponent();	          
	            recorder.setRequired(true);	    
	            recorder.setLabel(Model.of(getString("Section.Audit.Rights.entities")));
	            return recorder;
	        }
		};				
		add(entitiesPalette);
		
		ExtendedPalette<String> rightsPalette = new ExtendedPalette<String>("rights", new PropertyModel(auditRights, "rights"), 
				new Model(new ArrayList(AuditRights.RIGHTS)), new StringChoiceRenderer(), 6, false) {
			
			@Override
	        protected Recorder<String> newRecorderComponent() {
	            Recorder<String> recorder = super.newRecorderComponent();	          
	            recorder.setRequired(true);	       
	            recorder.setLabel(Model.of(getString("Section.Audit.Rights.rights")));
	            return recorder;
	        }
		};		
		add(rightsPalette);
    
	}
	
	public AuditRights getAuditRights() {
		return auditRights;
	}
	
	private List<String> getNames(String type) {
		List<String> names = new ArrayList<String>();
		if (type.equals(AuditRights.USER_TYPE)) {
	      	User[] users = securityService.getUsers();
	      	for (User user : users) {
	      		if (!user.isAdmin()) {
	      			names.add(user.getUsername());
	      		}
            }
        } else {
        	Group[] groups = securityService.getGroups();     
        	for (Group group : groups) {
        		names.add(group.getGroupname());
        	}
        }
		Collections.sort(names, new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				return Collator.getInstance().compare(s1, s2);
			}			
		});
		return names;
	}
	
	protected TableData getResults(AuditRights auditRights) {			
		
		List<Entity> entities = new ArrayList<Entity>();
	
		if (auditRights.getEntities().contains(AuditRights.ENTITY_DATA_SOURCES)) {
			try {
				entities.addAll(Arrays.asList(storageService.getEntitiesByClassNameWithoutSecurity(
						StorageConstants.DATASOURCES_ROOT, DataSource.class.getName())));
			} catch (NotFoundException e) {
				LOG.error(e.getMessage(),  e);
			}
		}
		if (auditRights.getEntities().contains(AuditRights.ENTITY_REPORTS)) {
			try {
				entities.addAll(Arrays.asList(storageService.getEntitiesByClassNameWithoutSecurity(
						StorageConstants.REPORTS_ROOT, Report.class.getName())));
			} catch (NotFoundException e) {
				LOG.error(e.getMessage(),  e);
			}
		}
		if (auditRights.getEntities().contains(AuditRights.ENTITY_CHARTS)) {
			try {
				entities.addAll(Arrays.asList(storageService.getEntitiesByClassNameWithoutSecurity(
						StorageConstants.CHARTS_ROOT, Chart.class.getName())));
			} catch (NotFoundException e) {
				LOG.error(e.getMessage(),  e);
			}
		}
		if (auditRights.getEntities().contains(AuditRights.ENTITY_SCHEDULERS)) {
			try {
				entities.addAll(Arrays.asList(storageService.getEntitiesByClassNameWithoutSecurity(
						StorageConstants.SCHEDULER_ROOT, SchedulerJob.class.getName())));
			} catch (NotFoundException e) {
				LOG.error(e.getMessage(),  e);
			}
		}
		if (auditRights.getEntities().contains(AuditRights.ENTITY_DASHBOARDS)) {
			try {
				entities.addAll(Arrays.asList(storageService.getEntitiesByClassNameWithoutSecurity(
						StorageConstants.DASHBOARDS_ROOT, DashboardState.class.getName())));
			} catch (NotFoundException e) {
				LOG.error(e.getMessage(),  e);
			}
		}
		if (auditRights.getEntities().contains(AuditRights.ENTITY_ANALYSIS)) {
			try {
				entities.addAll(Arrays.asList(storageService.getEntitiesByClassNameWithoutSecurity(
						StorageConstants.ANALYSIS_ROOT, Analysis.class.getName())));
			} catch (NotFoundException e) {
				LOG.error(e.getMessage(),  e);
			}
		}
		Collections.sort(entities, new Comparator<Entity>() {
			@Override
			public int compare(Entity o1, Entity o2) {
				if (o1.getClass().getName().equals(o2.getClass().getName())) {
					return o1.getPath().compareTo(o2.getPath());
				} else {
					return o1.getClass().getName().compareTo(o2.getClass().getName());
				}
			}			
		});
		
		TableData result = new TableData();
		List<String> header = Arrays.asList(getString("Section.Audit.Rights.category"), 
				getString("Section.Audit.Rights.name"), 
				getString("Section.Audit.Rights.permissions"), 
				getString("Section.Audit.Rights.path"));
		result.setHeader(header);
		for (Iterator<Entity> it = entities.iterator(); it.hasNext();) {
			Entity entity = it.next();
			String rights = getRights(auditRights, entity);
			if (!"".equals(rights)) {
				List<Object> row = new ArrayList<Object>();
				row.add(getCategory(entity.getClass().getName()));
				row.add(entity.getName());
				row.add(rights);
				row.add(entity.getPath());							
				result.getData().add(row);
			}
		}
		
		return result;
	}
	
	private String getRights(AuditRights auditRights, Entity entity) {		
		byte aclType;
		if (auditRights.getType().equals(AuditRights.USER_TYPE)) {
			aclType = AclEntry.USER_TYPE;
		} else {
			aclType = AclEntry.GROUP_TYPE;
		}
		AclEntry[] aclEntries = securityService.getGrantedById(entity.getId());
		
		for (AclEntry acl : aclEntries) {
			if ((acl.getType() == aclType) && (acl.getName().equals(auditRights.getName()))) {
				String permissions = PermissionUtil.toString(acl.getPermissions());
				for (String right : auditRights.getRights()) {
					if (!permissions.contains(right)) {
						return "";
					}
				}
				return permissions;				
			}
		}	
		return "";
	}
	
	private String getCategory(String entityClass) {
		String type = "";	
		if (entityClass.endsWith("DataSource")) {
			type = AuditRights.ENTITY_DATA_SOURCES;
		} else if (entityClass.endsWith("Report")) {
			type = AuditRights.ENTITY_REPORTS;
		} else if (entityClass.endsWith("Chart")) {
			type = AuditRights.ENTITY_CHARTS;
		} else if (entityClass.endsWith("SchedulerJob")) {
			type = AuditRights.ENTITY_SCHEDULERS;
		} else if (entityClass.endsWith("DashboardState")) {
			type = AuditRights.ENTITY_DASHBOARDS;
		} else if (entityClass.endsWith("Analysis")) {
			type = AuditRights.ENTITY_ANALYSIS;
		}		
		return getString("Section.Audit.Entity." + type);
	}
	
}
