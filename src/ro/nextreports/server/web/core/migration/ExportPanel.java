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
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.DashboardState;
import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Group;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.domain.Settings;
import ro.nextreports.server.domain.User;
import ro.nextreports.server.domain.UserWidgetParameters;
import ro.nextreports.server.domain.WidgetState;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.NextServerApplication;
import ro.nextreports.server.web.common.behavior.SimpleTooltipBehavior;
import ro.nextreports.server.web.common.form.FormPanel;
import ro.nextreports.server.web.core.BasePage;
import ro.nextreports.server.web.dashboard.EntityWidget;

public class ExportPanel extends Panel {

	@SpringBean
	private StorageService storageService;

	private String exportPath = "";
	private ArrayList<String> list = new ArrayList<String>();

	@SuppressWarnings("unchecked")
	public ExportPanel(String id) {
		super(id);

		final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		feedbackPanel.setOutputMarkupId(true);
		add(feedbackPanel);

		Form<Void> exportForm = new Form<Void>("exportForm");
		add(exportForm);

		final Model<ArrayList<String>> choiceModel = new Model<ArrayList<String>>();
		final ListMultipleChoice listChoice = new ListMultipleChoice("listChoice", choiceModel,
				new PropertyModel<String>(this, "list"));
		listChoice.setMaxRows(10);
		listChoice.setOutputMarkupId(true);
		exportForm.add(listChoice);

		AjaxLink addLink = new AjaxLink<Void>("addElement") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				ModalWindow dialog = findParent(BasePage.class).getDialog();
				dialog.setTitle(getString("Settings.migration.export.entity.add"));
				dialog.setInitialWidth(350);
				dialog.setUseInitialHeight(false);

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
						while (entities.hasNext()) {
							Entity entity = entities.next();
							String path = entity.getPath();
							path = path.substring(StorageConstants.NEXT_SERVER_ROOT.length() + 1);
							if (!list.contains(path)) {
								list.add(path);
							}
						}
						ModalWindow.closeCurrent(target);
						target.add(listChoice);
					}

				};
				dialog.setContent(new FormPanel<Void>(dialog.getContentId(), addEntityPanel, true) {

					private static final long serialVersionUID = 1L;

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setOkButtonValue(getString("add"));
					}

				});
				dialog.show(target);
			}

		};
		addLink.add(new SimpleTooltipBehavior(getString("Settings.migration.export.entity.add")));
		exportForm.add(addLink);

		AjaxSubmitLink removeLink = new AjaxSubmitLink("removeElement", exportForm) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				for (String sel : choiceModel.getObject()) {
					for (Iterator<?> it = list.iterator(); it.hasNext();) {
						if (sel.equals(it.next())) {
							it.remove();
						}
					}
				}
				if (target != null) {
					target.add(listChoice);
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(form);
			}

		};
		removeLink.add(new SimpleTooltipBehavior(getString("Settings.migration.export.entity.remove")));
		exportForm.add(removeLink);

		TextField<String> exportField = new TextField<String>("exportPath",
				new PropertyModel<String>(this, "exportPath"));
		exportField.setLabel(new Model<String>(getString("Settings.migration.export.path")));
		exportForm.add(exportField);

		exportForm.add(new AjaxSubmitLink("export") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

				// we do not use TextField validation with setRequired because
				// we have a submit button (remove entity)
				// which must work without validation
				if ((exportPath == null) || "".endsWith(exportPath.trim())) {
					error(getString("Settings.migration.export.path.select"));
					target.add(feedbackPanel);
					return;
				}

				NextServerApplication.setMaintenance(true);

				MigrationObject mo = new MigrationObject();
				List<DataSource> dsList = new ArrayList<DataSource>();
				List<Report> reportList = new ArrayList<Report>();
				List<Chart> chartList = new ArrayList<Chart>();
				List<DashboardState> dashboards = new ArrayList<DashboardState>();
				List<SchedulerJob> schedulers = new ArrayList<SchedulerJob>();
				mo.setDataSources(dsList);
				mo.setReports(reportList);
				mo.setCharts(chartList);
				mo.setDashboards(dashboards);
				mo.setSchedulers(schedulers);

				FileOutputStream fos = null;
				try {

					if (list.size() == 0) {
						error(getString("Settings.migration.export.entity.select"));
					} else {

						for (String pathM : list) {
							populateLists(pathM, mo);
						}

						XStream xstream = new XStream(new DomDriver());
						SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy_hh_mm");
						fos = new FileOutputStream(
								exportPath + File.separator + "migration-" + sdf.format(new Date()) + ".xml");
						xstream.toXML(mo, fos);

						info(getString("Settings.migration.export.info"));
					}
				} catch (Throwable t) {
					error(t.getMessage());
				} finally {
					NextServerApplication.setMaintenance(false);
					if (fos != null) {
						try {
							fos.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

				target.add(feedbackPanel);

			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(feedbackPanel);
			}

		});

		//
		exportForm.add(new AjaxSubmitLink("exportAll") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

				// we do not use TextField validation with setRequired because
				// we have a submit button (remove entity)
				// which must work without validation
				if ((exportPath == null) || "".endsWith(exportPath.trim())) {
					error(getString("Settings.migration.export.path.select"));
					target.add(feedbackPanel);
					return;
				}

				NextServerApplication.setMaintenance(true);

				MigrationObject mo = new MigrationObject();
				List<DataSource> dsList = new ArrayList<DataSource>();
				List<Report> reportList = new ArrayList<Report>();
				List<Chart> chartList = new ArrayList<Chart>();
				List<DashboardState> dashboards = new ArrayList<DashboardState>();
				List<SchedulerJob> schedulers = new ArrayList<SchedulerJob>();
				//
				List<User> users = new ArrayList<User>();
				List<Group> groups = new ArrayList<Group>();
				Settings settings = new Settings();
				List<UserWidgetParameters> userWidgetParameters = new ArrayList<UserWidgetParameters>();
				//
				mo.setDataSources(dsList);
				mo.setReports(reportList);
				mo.setCharts(chartList);
				mo.setDashboards(dashboards);
				mo.setSchedulers(schedulers);
				//
				mo.setUsers(users);
				mo.setGroups(groups);
				mo.setSettings(settings);
				mo.setUserWidgetParameters(userWidgetParameters);

				FileOutputStream fos = null;
				try {

					Entity[] m = storageService.getEntitiesByClassNameWithoutSecurity(StorageConstants.DATASOURCES_ROOT,
							DataSource.class.getName());
					System.err.println(DataSource.class.getName() + " ->" + m.length);
					mo.getDataSources().addAll((List) Arrays.asList(m));

					m = storageService.getEntitiesByClassNameWithoutSecurity(StorageConstants.REPORTS_ROOT,
							Report.class.getName());
					System.err.println(Report.class.getName() + " ->" + m.length);
					mo.getReports().addAll((List) Arrays.asList(m));
					//
					m = storageService.getEntitiesByClassNameWithoutSecurity(StorageConstants.CHARTS_ROOT,
							Chart.class.getName());
					System.err.println(Chart.class.getName() + " ->" + m.length);
					mo.getCharts().addAll((List) Arrays.asList(m));
					//
					m = storageService.getEntitiesByClassNameWithoutSecurity(StorageConstants.SCHEDULER_ROOT,
							SchedulerJob.class.getName());
					System.err.println(SchedulerJob.class.getName() + " ->" + m.length);
					mo.getSchedulers().addAll((List) Arrays.asList(m));
					//
					m = storageService.getEntitiesByClassNameWithoutSecurity(StorageConstants.DASHBOARDS_ROOT,
							DashboardState.class.getName());
					System.err.println(DashboardState.class.getName() + " ->" + m.length);
					mo.getDashboards().addAll((List) Arrays.asList(m));
					//
					m = storageService.getEntitiesByClassNameWithoutSecurity(StorageConstants.SECURITY_ROOT,
							User.class.getName());
					System.err.println(User.class.getName() + " ->" + m.length);
					mo.getUsers().addAll((List) Arrays.asList(m));
					//
					m = storageService.getEntitiesByClassNameWithoutSecurity(StorageConstants.SECURITY_ROOT,
							Group.class.getName());
					System.err.println(Group.class.getName() + " ->" + m.length);
					mo.getGroups().addAll((List) Arrays.asList(m));
					//
					settings = storageService.getSettings();
					mo.setSettings(settings);
					//
					m = storageService.getEntitiesByClassNameWithoutSecurity(StorageConstants.USERS_DATA_ROOT,
							UserWidgetParameters.class.getName());
					System.err.println(UserWidgetParameters.class.getName() + " ->" + m.length);
					mo.getUserWidgetParameters().addAll((List) Arrays.asList(m));
					//

					XStream xstream = new XStream(new DomDriver());
					SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy_hh_mm");
					fos = new FileOutputStream(
							exportPath + File.separator + "migration-" + sdf.format(new Date()) + ".xml");
					xstream.toXML(mo, fos);

					info(getString("Settings.migration.export.info"));
				} catch (Throwable t) {
					error(t.getMessage());
				} finally {
					NextServerApplication.setMaintenance(false);
					if (fos != null) {
						try {
							fos.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

				target.add(feedbackPanel);

			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(feedbackPanel);
			}

		});
	}

	private void populateLists(String pathM, MigrationObject mo) throws NotFoundException {
		if (pathM.startsWith(StorageConstants.DATASOURCES_FOLDER_NAME)) {
			DataSource ds = (DataSource) storageService
					.getEntity(StorageConstants.NEXT_SERVER_ROOT + StorageConstants.PATH_SEPARATOR + pathM);
			mo.getDataSources().add(ds);
		} else if (pathM.startsWith(StorageConstants.REPORTS_FOLDER_NAME)) {
			Report report = (Report) storageService
					.getEntity(StorageConstants.NEXT_SERVER_ROOT + StorageConstants.PATH_SEPARATOR + pathM);
			mo.getReports().add(report);
		} else if (pathM.startsWith(StorageConstants.CHARTS_FOLDER_NAME)) {
			Chart chart = (Chart) storageService
					.getEntity(StorageConstants.NEXT_SERVER_ROOT + StorageConstants.PATH_SEPARATOR + pathM);
			mo.getCharts().add(chart);
		} else if (pathM.startsWith(StorageConstants.SCHEDULER_FOLDER_NAME)) {
			SchedulerJob job = (SchedulerJob) storageService
					.getEntity(StorageConstants.NEXT_SERVER_ROOT + StorageConstants.PATH_SEPARATOR + pathM);
			mo.getSchedulers().add(job);
			if (job.getReport() != null) {
				mo.getReports().add(job.getReport());
			}

		} else if (pathM.startsWith(StorageConstants.DASHBOARDS_FOLDER_NAME)) {
			DashboardState dashboard = (DashboardState) storageService
					.getEntity(StorageConstants.NEXT_SERVER_ROOT + StorageConstants.PATH_SEPARATOR + pathM);
			mo.getDashboards().add(dashboard);

			for (WidgetState ws : dashboard.getWidgetStates()) {
				String entityId = ws.getInternalSettings().get(EntityWidget.ENTITY_ID);
				Entity entity = storageService.getEntityById(entityId);
				if (entity instanceof Report) {
					mo.getReports().add((Report) entity);
				} else if (entity instanceof Chart) {
					mo.getCharts().add((Chart) entity);
				}
			}
		}
	}
}
