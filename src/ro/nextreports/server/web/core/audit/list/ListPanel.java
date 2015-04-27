package ro.nextreports.server.web.core.audit.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.engine.exporter.util.TableData;
import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.Analysis;
import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.DashboardState;
import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.schedule.ScheduleConstants;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.common.form.FormContentPanel;
import ro.nextreports.server.web.common.form.FormPanel;
import ro.nextreports.server.web.core.audit.rights.AuditRights;

public class ListPanel extends FormContentPanel<AuditList> {
	
	private static final Logger LOG = LoggerFactory.getLogger(ListPanel.class);		
	
	@SpringBean
    private StorageService storageService;
	
	private AuditList auditList;
	
	public ListPanel() {
		super(FormPanel.CONTENT_ID);
		
		setOutputMarkupId(true);
		
		auditList = new AuditList();				      
        setModel(new CompoundPropertyModel<AuditList>(auditList)); 
        
        IChoiceRenderer<String> entitiesRenderer = new ChoiceRenderer<String>() {
			@Override
			public Object getDisplayValue(String object) {				
				return getString("Section.Audit.Entity." + object);
			}		
			
			public String getIdValue(String object, int index) {
				return object;
			}
		};
        final DropDownChoice<String> typeChoice = new DropDownChoice<String>("entityType", new Model(new ArrayList(AuditRights.ENTITIES)), entitiesRenderer);		
		typeChoice.setRequired(true);
		typeChoice.setNullValid(false);		
		typeChoice.setOutputMarkupId(true);
		add(typeChoice);
	}   
	
	public AuditList getAuditList() {
		return auditList;
	}
	
	protected TableData getResults(AuditList auditList) {			
		
		List<Entity> entities = new ArrayList<Entity>();
		TableData result = new TableData();
	
		if (auditList.getEntityType().equals(AuditRights.ENTITY_DATA_SOURCES)) {
			try {
				entities.addAll(Arrays.asList(storageService.getEntitiesByClassNameWithoutSecurity(
						StorageConstants.DATASOURCES_ROOT, DataSource.class.getName())));				
			} catch (NotFoundException e) {
				LOG.error(e.getMessage(),  e);
			}
		} else if (auditList.getEntityType().equals(AuditRights.ENTITY_REPORTS)) {
			try {
				entities.addAll(Arrays.asList(storageService.getEntitiesByClassNameWithoutSecurity(
						StorageConstants.REPORTS_ROOT, Report.class.getName())));
			} catch (NotFoundException e) {
				LOG.error(e.getMessage(),  e);
			}
		} else if (auditList.getEntityType().equals(AuditRights.ENTITY_CHARTS)) {
			try {
				entities.addAll(Arrays.asList(storageService.getEntitiesByClassNameWithoutSecurity(
						StorageConstants.CHARTS_ROOT, Chart.class.getName())));
			} catch (NotFoundException e) {
				LOG.error(e.getMessage(),  e);
			}
		} else if (auditList.getEntityType().equals(AuditRights.ENTITY_SCHEDULERS)) {
			try {
				entities.addAll(Arrays.asList(storageService.getEntitiesByClassNameWithoutSecurity(
						StorageConstants.SCHEDULER_ROOT, SchedulerJob.class.getName())));
			} catch (NotFoundException e) {
				LOG.error(e.getMessage(),  e);
			}
		} else if (auditList.getEntityType().equals(AuditRights.ENTITY_DASHBOARDS)) {
			try {
				entities.addAll(Arrays.asList(storageService.getEntitiesByClassNameWithoutSecurity(
						StorageConstants.DASHBOARDS_ROOT, DashboardState.class.getName())));
			} catch (NotFoundException e) {
				LOG.error(e.getMessage(),  e);
			}
		} else if (auditList.getEntityType().equals(AuditRights.ENTITY_ANALYSIS)) {
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
				return o1.getPath().compareTo(o2.getPath());				
			}			
		});
		
		if (auditList.getEntityType().equals(AuditRights.ENTITY_DATA_SOURCES) ||
		    auditList.getEntityType().equals(AuditRights.ENTITY_REPORTS) ||
		    auditList.getEntityType().equals(AuditRights.ENTITY_CHARTS) ||
		    auditList.getEntityType().equals(AuditRights.ENTITY_DASHBOARDS)) {
				List<String> header = Arrays.asList(getString("Section.Audit.Rights.name"), 
						getString("Section.Audit.Rights.path"), 												
						getString("ActionContributor.Search.entityCreated"),
						getString("ActionContributor.Search.entityCreation"),
						getString("ActionContributor.Search.entityModified"),
						getString("ActionContributor.Search.entityModification"));
				result.setHeader(header);
				for (Iterator<Entity> it = entities.iterator(); it.hasNext();) {
					Entity entity = it.next();
					List<Object> row = new ArrayList<Object>();
					row.add(entity.getName());
					row.add(entity.getPath());
					row.add(entity.getCreatedBy());
					row.add(entity.getCreatedDate());
					row.add(entity.getLastUpdatedBy());
					row.add(entity.getLastUpdatedDate());
					result.getData().add(row);
				}		
		} else if (auditList.getEntityType().equals(AuditRights.ENTITY_SCHEDULERS)) {
			List<String> header = Arrays.asList(getString("Section.Audit.Rights.name"),
					getString("ActionContributor.Search.entityType"),
					getString("Section.Audit.Run.report"),
					getString("ActionContributor.Search.entityActive"),
					getString("ActionContributor.Search.entityNextRun"),				
					getString("ActionContributor.Run.export"));
			result.setHeader(header);
			for (Iterator<Entity> it = entities.iterator(); it.hasNext();) {
				SchedulerJob entity = (SchedulerJob)it.next();
				List<Object> row = new ArrayList<Object>();
				row.add(entity.getName());
				row.add(entity.getTime().getType());
				row.add(entity.getReport().getPath());
				row.add(isActive(entity));				
				row.add(entity.getNextRun());
				row.add(entity.getReportRuntime().getOutputType());
				result.getData().add(row);
			}			
		} else if (auditList.getEntityType().equals(AuditRights.ENTITY_ANALYSIS)) {
			List<String> header = Arrays.asList(getString("Section.Audit.Rights.name"),
					getString("Table"),
					getString("freeze"),
					getString("ActionContributor.Search.entityCreated"),
					getString("ActionContributor.Search.entityCreation"),
					getString("ActionContributor.Search.entityModified"),
					getString("ActionContributor.Search.entityModification"));
			result.setHeader(header);
			for (Iterator<Entity> it = entities.iterator(); it.hasNext();) {
				Analysis entity = (Analysis)it.next();
				List<Object> row = new ArrayList<Object>();
				row.add(entity.getName());
				row.add(entity.getTableName());
				row.add(entity.isFreezed());
				row.add(entity.getCreatedBy());
				row.add(entity.getCreatedDate());
				row.add(entity.getLastUpdatedBy());
				row.add(entity.getLastUpdatedDate());				
				result.getData().add(row);
			}			
		} 
							
		return result;
	}
	
	private boolean isActive(SchedulerJob job) {
		boolean active;
		Date now = new Date();
		if (ScheduleConstants.ONCE_TYPE.equals(job.getTime().getType())) {
            active = job.getTime().getRunDate().compareTo(now) >= 0;
        } else {
            active = (job.getTime().getStartActivationDate().compareTo(now) <= 0) &&
                    (job.getTime().getEndActivationDate().compareTo(now) >= 0);
        }
		return active;
	}

}
