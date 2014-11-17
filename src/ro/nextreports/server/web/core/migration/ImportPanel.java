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
package ro.nextreports.server.web.core.migration;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.DashboardState;
import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.domain.DrillDownEntity;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.WidgetState;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.FileUtil;
import ro.nextreports.server.web.NextServerApplication;
import ro.nextreports.server.web.NextServerConfiguration;
import ro.nextreports.server.web.dashboard.EntityWidget;

import ro.nextreports.engine.util.ObjectCloner;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class ImportPanel extends Panel {
	
	@SpringBean
	private StorageService storageService;
	
	@SpringBean
	private DashboardService dashboardService;

	private String backupPath = "";
	private FileUploadField uploadField;	
	private final String NAME_SEPARATOR = "_IMP_";
	// keep a map of newly added entities paths (path, entity) (these are the path before modification if any)
	private Map<String, Entity> addedPaths = new HashMap<String, Entity>();
	private MigrationObject mo;
	
	private static final Logger LOG = LoggerFactory.getLogger(ImportPanel.class);

	public ImportPanel(String id) {
		super(id);
				
		final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		feedbackPanel.setOutputMarkupId(true);
		add(feedbackPanel);
		
		Form<Void> importForm = new Form<Void>("importForm");
		add(importForm);

		uploadField = new FileUploadField("file");
		uploadField.setRequired(true);
		uploadField.setLabel(new Model<String>(getString("Settings.migration.import.file")));		
		importForm.add(uploadField);
		
		TextField<String> backupField = new TextField<String>("backup", new PropertyModel<String>(this, "backupPath"));
		backupField.setRequired(true);
		backupField.setLabel(new Model<String>(getString("Settings.migration.import.backup.path")));
		importForm.add(backupField);

		importForm.add(new AjaxSubmitLink("import") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				
				NextServerApplication.setMaintenance(true);
				
				Date start = new Date();
				long sd = start.getTime();
				
				LOG.info("### Backup: start");
				try {
					String home = NextServerConfiguration.get().getConfiguration().getString("repository.home");
					LOG.info("\t\trepository home = " + home);
					FileUtil.backupRepository(new File(home), new File(backupPath + File.separator + "data"), new BackupFileFilter());
															
				} catch (Throwable t) {
					NextServerApplication.setMaintenance(false);
					error(t.getMessage());
					LOG.error(t.getMessage(), t);
					t.printStackTrace();					
					return;
				} 
				long bd = new Date().getTime();
				LOG.info("### Backup: end in " + (bd-sd)/1000 + " seconds");
				
				LOG.info("### Starting import at " +  start + " ...");				
				
				FileUpload upload = uploadField.getFileUpload();
				String fileName = (upload == null) ? "" : upload.getClientFileName();
				LOG.info("    migration file name = " + fileName);

				if (!"".equals(fileName)) {					
					try {						
						String content =  new String(upload.getBytes());
						
						XStream xstream = new XStream(new DomDriver());
						mo = (MigrationObject) xstream.fromXML(content);
																		
						importDataSources();												
						importReports();		
						importCharts();
						importDrillDownEntities();
						importDashboards();											
						
						info(getString("Settings.migration.import.info"));
						
					} catch (Throwable t) {
						error(t.getMessage());
						LOG.error(t.getMessage(), t);
						t.printStackTrace();
					} 
				}
				
				Date end = new Date();
				long ed = end.getTime();
				LOG.info("### End import at " +  end + " took " + (ed - bd)/1000 + " seconds.");	
				
				NextServerApplication.setMaintenance(false);
				
				target.add(feedbackPanel);							
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(feedbackPanel); 
			}

		});
	}
	
	private void importDashboards() {
		for (DashboardState dashboard : mo.getDashboards()) {
			importEntity(dashboard);			
		}
	}
	
	// drill down relatinons must be set after all reports and charts were created!
	private void importDrillDownEntities() throws Exception {
		
		// import drill down reports
		for (Report report : mo.getReports()) {
			boolean hasDrill = false;
			for (DrillDownEntity dd : report.getDrillDownEntities()) {								
				DataSource ds = null; 								
				hasDrill = true;
				Entity entity = ObjectCloner.deepCopy(dd.getEntity());
				if (entity instanceof Report) {
					((Report)entity).setDrillDownEntities(new ArrayList<DrillDownEntity>());
					ds = ((Report)entity).getDataSource();
				} else if (entity instanceof Chart) {
					((Chart)entity).setDrillDownEntities(new ArrayList<DrillDownEntity>());
					ds = ((Chart)entity).getDataSource();
				}
				
				// import the data source
				Entity newDSEntity = importEntity(ds);
				if (newDSEntity.getId() != null) {
					ds.setId(newDSEntity.getId());
					ds.setName(newDSEntity.getName());
					ds.setPath(newDSEntity.getPath());
				}
				
				Entity newEntity = importEntity(entity);
				if (newEntity.getId() != null) {
					dd.getEntity().setId(newEntity.getId());
					dd.getEntity().setName(newEntity.getName());
					dd.getEntity().setPath(newEntity.getPath());
				}
			}
			// save report with drill down relations
			if (hasDrill) {
				storageService.modifyEntity(report);
			}
		}

		// import drill down charts
		for (Chart chart : mo.getCharts()) {
			boolean hasDrill = false;
			for (DrillDownEntity dd : chart.getDrillDownEntities()) {
				DataSource ds = null; 			
				hasDrill = true;
				Entity entity = ObjectCloner.deepCopy(dd.getEntity());				
				if (entity instanceof Report) {
					((Report)entity).setDrillDownEntities(new ArrayList<DrillDownEntity>());
					ds = ((Report)entity).getDataSource();
				} else if (entity instanceof Chart) {
					((Chart)entity).setDrillDownEntities(new ArrayList<DrillDownEntity>());
					ds = ((Chart)entity).getDataSource();
				}
				
				// import the data source
				Entity newDSEntity = importEntity(ds);
				if (newDSEntity.getId() != null) {
					ds.setId(newDSEntity.getId());
					ds.setName(newDSEntity.getName());
					ds.setPath(newDSEntity.getPath());
				}
				
				Entity newEntity = importEntity(entity);
				if (newEntity.getId() != null) {
					dd.getEntity().setId(newEntity.getId());
					dd.getEntity().setName(newEntity.getName());
					dd.getEntity().setPath(newEntity.getPath());					
				}
			}
			// save chart with drill down relations
			if (hasDrill) {
				storageService.modifyEntity(chart);
			}
		}
	}
	
	private void importCharts() throws Exception {
		for (Chart chart : mo.getCharts()) {		
			// import the data source
			DataSource ds = chart.getDataSource();
			Entity newEntity = importEntity(ds);
			if (newEntity.getId() != null) {
				ds.setId(newEntity.getId());
				ds.setName(newEntity.getName());
				ds.setPath(newEntity.getPath());
			}
						
			// import chart : must modify chart id for widget states that contain the chart
			List<WidgetState> widgetStates = findWidgetStates(chart);
			Chart c = ObjectCloner.deepCopy(chart);
			c.setDrillDownEntities(new ArrayList<DrillDownEntity>());
			newEntity = importEntity(c);			
			chart.setId(newEntity.getId());
			chart.setName(newEntity.getName());
			chart.setPath(newEntity.getPath());
			
			if (newEntity.getId() != null) {
				for (WidgetState ws : widgetStates) {
					ws.getInternalSettings().put(EntityWidget.ENTITY_ID, newEntity.getId());					
				}
			}
			
		}
	}
	
	private void importReports() throws Exception {
		for (Report report : mo.getReports()) {		
			// import the data source
			DataSource ds = report.getDataSource();
			Entity newEntity = importEntity(ds);
			if (newEntity.getId() != null) {
				ds.setId(newEntity.getId());
				ds.setName(newEntity.getName());
				ds.setPath(newEntity.getPath());
			}
												
			// import report : must modify report id for widget states that contain the report
			List<WidgetState> widgetStates = findWidgetStates(report);
			Report r = ObjectCloner.deepCopy(report);
			r.setDrillDownEntities(new ArrayList<DrillDownEntity>());
			newEntity = importEntity(r);
			report.setId(newEntity.getId());
			report.setName(newEntity.getName());
			report.setPath(newEntity.getPath());
			if (newEntity.getId() != null) {
				for (WidgetState ws : widgetStates) {
					ws.getInternalSettings().put(EntityWidget.ENTITY_ID, newEntity.getId());					
				}
			}
		}
	}
	
	private void importDataSources() {
		for (DataSource ds : mo.getDataSources()) {																					
			importEntity(ds);
		}
	}
	
	private List<WidgetState> findWidgetStates(Entity entity) {
		List<WidgetState> result = new ArrayList<WidgetState>();
		for (DashboardState dashboard : mo.getDashboards()) {
			for (WidgetState ws : dashboard.getWidgetStates()) {
				String entityId = ws.getInternalSettings().get(EntityWidget.ENTITY_ID);
				if (entityId.equals(entity.getId())) {
					result.add(ws);
				}
			}
		}
		return result;
	}
	
	private Entity importEntity(Entity importedEntity) {
		LOG.info("    import " + importedEntity.getPath());
		String id = null;
		try {												
			Entity entity = null;
			try {
				entity = storageService.getEntityById(importedEntity.getId());
			} catch (NotFoundException ex) {
				// not found
			}
			if (entity != null) {
				// update
				LOG.info("\tentity found : " + entity.getPath());																	
				storageService.modifyEntity(importedEntity);				
				// for simple entities cache is cleared in modifyEntity
				// but for DashboardState we must take care for all internal WidgetStates
				clearCache(entity);
								
				LOG.info("\tentity was updated");
			} else {
				LOG.info("\tnew entity");
				String initialPath = importedEntity.getPath();
				int index = initialPath.lastIndexOf(StorageConstants.PATH_SEPARATOR);
				String parentPath = initialPath.substring(0, index);
				if (storageService.entityExists(initialPath)) {
					if (!addedPaths.keySet().contains(initialPath)) {
						LOG.info("\tWARNING : another entity with the same name exists. Name will be changed");
						// change name
						String newName = importedEntity.getName() + NAME_SEPARATOR + new Date().getTime();
						String newPath = parentPath + StorageConstants.PATH_SEPARATOR + newName;
						importedEntity.setName(newName);
						importedEntity.setPath(newPath);
					} else {
						LOG.info("\tskip entity because it was previously added : " + importedEntity.getPath());
						return addedPaths.get(initialPath);
					}
				} else {					
					storageService.createFolders(parentPath);					
				}
								
				// add 		
				importedEntity.setLastUpdatedBy(null);
				importedEntity.setLastUpdatedDate(null);				
				id = storageService.addEntity(importedEntity, true);
				importedEntity.setId(id);
				addedPaths.put(initialPath, importedEntity);
				
				LOG.info("\tentity was added : " + importedEntity.getPath());
				// add rights ?
			}			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return importedEntity;
	}	
	
	private void clearCache(Entity oldEntity) {
		if (oldEntity instanceof DashboardState) {
			// when an entire dashboard state is updated, widget states are recreated with other ids
			// so we have to clear cache for children
			// widget ids are modified -> iframes for those widgets will not work anymore!
			for (WidgetState ws : ((DashboardState)oldEntity).getWidgetStates()) {
				storageService.clearEntityCache(ws.getId());
				storageService.clearEntityCache(ws.getInternalSettings().get(EntityWidget.ENTITY_ID));
				// clear cache for widgets that have expirationTime > 0 
				dashboardService.resetCache(ws.getInternalSettings().get(EntityWidget.ENTITY_ID));						
			}						
			storageService.clearEntityCache(oldEntity.getId());													
		}
	}
		
}

