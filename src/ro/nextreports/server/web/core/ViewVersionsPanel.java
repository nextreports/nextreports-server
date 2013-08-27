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
package ro.nextreports.server.web.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.VersionInfo;
import ro.nextreports.server.service.ChartService;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.PermissionUtil;
import ro.nextreports.server.util.ServerUtil;
import ro.nextreports.server.web.chart.ChartPanel;
import ro.nextreports.server.web.chart.ChartResource;
import ro.nextreports.server.web.common.behavior.AlertBehavior;
import ro.nextreports.server.web.common.menu.MenuItem;
import ro.nextreports.server.web.common.menu.MenuPanel;
import ro.nextreports.server.web.common.table.BaseTable;
import ro.nextreports.server.web.common.table.DateColumn;
import ro.nextreports.server.web.core.EntityBrowserPanel;
import ro.nextreports.server.web.core.EntityVersionDataProvider;
import ro.nextreports.server.web.report.ReportResource;
import ro.nextreports.server.web.schedule.ScheduleWizard;


//
public class ViewVersionsPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	private Report report;
    private Chart chart;
    private DataTable<VersionInfo> table;
    private EntityVersionDataProvider dataProvider;

    @SpringBean
    private StorageService storageService;

    @SpringBean
    private ReportService reportService;

    @SpringBean
    private ChartService chartService;
    
    @SpringBean
    private SecurityService securityService;

    public ViewVersionsPanel(String id, final Report report) {
        super(id);
        this.report = report;
        init(getString("Report"), report.getName());        
    }

    public ViewVersionsPanel(String id, final Chart chart) {
        super(id);
        this.chart = chart;
        init(getString("Chart"), chart.getName());
    }

    private void init(String entityName, String reportName) {
        add(new Label("legend", entityName + " " + getString("ActionContributor.Versions.name")));
        add(new Label("entityName", entityName));
        Label name = new Label("reportName", new Model<String>(reportName));
        add(name);

        addVersionsTable();

        add(new AjaxLink<Void>("cancel") {

			private static final long serialVersionUID = 1L;

			@Override
            public void onClick(AjaxRequestTarget target) {
                EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
                panel.backwardWorkspace(target);
            }
			
        });
    }

    private void addVersionsTable() {
        List<IColumn<VersionInfo>> columns = new ArrayList<IColumn<VersionInfo>>();
        columns.add(new AbstractColumn<VersionInfo>(new Model<String>(getString("name"))) {

			private static final long serialVersionUID = 1L;

			@Override
            public String getCssClass() {
                return "name-col";
            }

            public void populateItem(Item<ICellPopulator<VersionInfo>> item, String componentId,
                                     final IModel<VersionInfo> rowModel) {
                final VersionInfo version = rowModel.getObject();
                final String name = version.getName();
                item.add(new Label(componentId, new Model<String>(name)));
                item.add(AttributeModifier.replace("class", "name-col"));
            }
            
        });
        columns.add(new ActionsColumn());
        columns.add(new AbstractColumn<VersionInfo>(new Model<String>(getString("author"))) {
        	
			private static final long serialVersionUID = 1L;

			public void populateItem(Item<ICellPopulator<VersionInfo>> item, String componentId,
                                     final IModel<VersionInfo> rowModel) {
                final VersionInfo version = rowModel.getObject();
                final String author = version.getCreatedBy();
                item.add(new Label(componentId, new Model<String>(author)));
            }
			
        });
        columns.add(new DateColumn<VersionInfo>(new Model<String>(getString("date")), "createdDate", "createdDate" ));
        columns.add(new AbstractColumn<VersionInfo>(new Model<String>(getString("current"))) {
        	
			private static final long serialVersionUID = 1L;

			public void populateItem(Item<ICellPopulator<VersionInfo>> item, String componentId,
                                     final IModel<VersionInfo> rowModel) {
                final VersionInfo version = rowModel.getObject();
                final Boolean current = version.isBaseVersion();
                item.add(new Label(componentId, new Model<Boolean>(current)));
            }
			
        });        

        dataProvider = new EntityVersionDataProvider(getEntity().getId());
        table = new BaseTable<VersionInfo>("table", columns, dataProvider, 300);
        table.setOutputMarkupId(true);
        add(table);
    }

    private String getEntityPath() {
        return getEntity().getPath();
    }
    
    private Entity getEntity() {
    	if (report != null) {
            return report;
        } else {
            return chart;
        }
    }

    private class ActionsColumn extends AbstractColumn<VersionInfo> {

		private static final long serialVersionUID = 1L;

		public ActionsColumn() {
            super(new Model<String>("Actions"));
        }

        @Override
        public String getCssClass() {
            return "actions-col";
        }

        public void populateItem(Item<ICellPopulator<VersionInfo>> cellItem, String componentId, IModel<VersionInfo> model) {
            cellItem.add(new ActionPanel(componentId, model));
            cellItem.add(AttributeModifier.replace("class", "actions-col"));
        }

    }

    private class ActionPanel extends Panel {

		private static final long serialVersionUID = 1L;

		public ActionPanel(String id, final IModel<VersionInfo> model) {
            super(id, model);

            setRenderBodyOnly(true);

            MenuPanel menuPanel = new MenuPanel("menuPanel");
            add(menuPanel);

            MenuItem mi = new MenuItem("images/actions.png", null);
            menuPanel.addMenuItem(mi);

            AjaxLink<VersionInfo> runLink = new AjaxLink<VersionInfo>(MenuPanel.LINK_ID, model) {

				private static final long serialVersionUID = 1L;

				@Override
                public void onClick(AjaxRequestTarget target) {
                    VersionInfo version = model.getObject();
                    try {
                        if (report != null) {
                            Report reportVersion = (Report) storageService.getVersion(report.getId(), version.getName());
                            EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
                            ScheduleWizard wizard = new ScheduleWizard("work", reportVersion, true);
                            panel.forwardWorkspace(wizard, target);
                        } else {
                            Chart chartVersion = (Chart) storageService.getVersion(chart.getId(), version.getName());
                            String jsonData = chartService.getJsonData(chartVersion);
                            EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
                            ChartPanel chartPanel = new ChartPanel("work", jsonData);
                            panel.forwardWorkspace(chartPanel, target);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        add(new AlertBehavior(e.getMessage()));
                        target.add(this);
                    }
                }

				@Override
				public boolean isVisible() {
					boolean visible = false;
					try {
						visible = securityService.hasPermissionsById(ServerUtil.getUsername(),
	                        PermissionUtil.getExecute(), getEntity().getId());
					} catch (Exception e) {
		                e.printStackTrace();
		            }
					return visible;
				}
                
                
            };
            mi.addMenuItem(new MenuItem(runLink, getString("run"), "images/run.gif"));

            AjaxLink<VersionInfo> restoreLink = new AjaxLink<VersionInfo>(MenuPanel.LINK_ID, model) {

				private static final long serialVersionUID = 1L;

				@Override
                public void onClick(AjaxRequestTarget target) {
                    VersionInfo version = model.getObject();
                    try {
                        // IMPORTANT : see ReportRestoredAdvice
                        reportService.restoreReportVersion(getEntityPath(), version.getName());
                        target.add(table);
                    } catch (Exception e) {
                        e.printStackTrace();
                        add(new AlertBehavior(e.getMessage()));
                        target.add(this);
                    }
                }
                
                @Override
				public boolean isVisible() {
					boolean visible = false;
					try {
						visible = securityService.hasPermissionsById(ServerUtil.getUsername(),
	                        PermissionUtil.getWrite(), getEntity().getId());
					} catch (Exception e) {
		                e.printStackTrace();
		            }
					return visible;
				}
            };
            mi.addMenuItem(new MenuItem(restoreLink, getString("ActionContributor.Versions.setCurrent"), "images/report_restore.png"));

            //todo is there a possibility to take the version only when the link is clicked????
            try {
                final IResource download;
                if (report != null) {
                    Report reportVersion = (Report) storageService.getVersion(report.getId(), ((VersionInfo) model.getObject()).getName());
                    download = new ReportResource(reportVersion);
                } else {
                    Chart chartVersion = (Chart) storageService.getVersion(chart.getId(), ((VersionInfo) model.getObject()).getName());
                    download = new ChartResource(chartVersion);
                }
//                download.setCacheable(false);
                ResourceLink<Void> downloadLink = new ResourceLink<Void>(MenuPanel.LINK_ID, download);
                mi.addMenuItem(new MenuItem(downloadLink, getString("download"), "images/download.png"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            AjaxLink<VersionInfo> infoLink = new AjaxLink<VersionInfo>(MenuPanel.LINK_ID, model) {

				private static final long serialVersionUID = 1L;

				@Override
                public void onClick(AjaxRequestTarget target) {
                    VersionInfo version = model.getObject();
                    try {
                        ViewInfoPanel viewInfoPanel;
                        if (report != null) {
                            Report reportVersion = (Report) storageService.getVersion(report.getId(), version.getName());
                            viewInfoPanel = new ViewInfoPanel("work", reportVersion, report, version.getName());
                        } else {
                            Chart chartVersion = (Chart) storageService.getVersion(chart.getId(), version.getName());
                            viewInfoPanel = new ViewInfoPanel("work", chartVersion, chart, version.getName());
                        }
                        EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
                        panel.forwardWorkspace(viewInfoPanel, target);
                    } catch (Exception e) {
                        e.printStackTrace();
                        add(new AlertBehavior(e.getMessage()));
                        target.add(this);
                    }
                }
            };
            mi.addMenuItem(new MenuItem(infoLink, getString("ActionContributor.Info.name"), "images/info.png"));
        }
    }
    
}
