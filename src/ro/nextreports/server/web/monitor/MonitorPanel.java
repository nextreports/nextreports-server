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
package ro.nextreports.server.web.monitor;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.quartz.JobDetail;
import org.quartz.Scheduler;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.ReportJobInfo;
import ro.nextreports.server.domain.RunReportHistory;
import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.domain.SchedulerTime;
import ro.nextreports.server.domain.Settings;
import ro.nextreports.server.schedule.ReportJobInfoDataProvider;
import ro.nextreports.server.schedule.RunReportJob;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.service.SchedulerService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.SchedulerUtil;
import ro.nextreports.server.util.StorageUtil;
import ro.nextreports.server.web.common.behavior.ConfirmBehavior;
import ro.nextreports.server.web.common.behavior.SimpleTooltipBehavior;
import ro.nextreports.server.web.common.panel.AbstractImageAjaxLinkPanel;
import ro.nextreports.server.web.common.table.BaseTable;
import ro.nextreports.server.web.common.table.BooleanImagePropertyColumn;
import ro.nextreports.server.web.common.table.DateColumn;
import ro.nextreports.server.web.common.table.SortableDataAdapter;
import ro.nextreports.server.web.core.table.NextRunDateColumn;
import ro.nextreports.server.web.report.RunHistoryPanel;
import ro.nextreports.server.web.schedule.ActiveSchedulerJobDataProvider;

/**
 * @author decebal
 */
public class MonitorPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	@SpringBean
    private ReportService reportService;

    @SpringBean
    private SchedulerService schedulerService;
    
    @SpringBean
    private StorageService storageService;
    
    @SpringBean
    private Scheduler scheduler;

    private DataTable<ReportJobInfo, String> jobsTable;
    private DataTable<SchedulerJob, String> schedulerJobsTable;
    private DataTable<RunReportHistory, String> runHistoryTable;

    public MonitorPanel(String id) {
        super(id);

        jobsTable = createJobsTable(new ReportJobInfoDataProvider());
        jobsTable.setOutputMarkupId(true);
        add(jobsTable);

        schedulerJobsTable = createSchedulerJobsTable(new ActiveSchedulerJobDataProvider());
        schedulerJobsTable.setOutputMarkupId(true);
        add(schedulerJobsTable);

        RunHistoryPanel runHistoryPanel = new RunHistoryPanel("runHistoryPanel", null);
        runHistoryTable = runHistoryPanel.getRunHistoryTable();
        runHistoryTable.setOutputMarkupId(true);
        add(runHistoryPanel);
        
        Settings settings = storageService.getSettings();
		int updateInterval = settings.getUpdateInterval();
				        
        if (updateInterval > 0) {
        	jobsTable.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(updateInterval)));
            schedulerJobsTable.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(updateInterval)));
            runHistoryTable.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(updateInterval)));
        }
    }

    protected DataTable<ReportJobInfo, String> createJobsTable(ReportJobInfoDataProvider dataProvider) {
    	SortableDataProvider<ReportJobInfo, String> sortableDataProvider = new SortableDataAdapter<ReportJobInfo>(dataProvider);
    	sortableDataProvider.setSort("startDate", SortOrder.ASCENDING);
        return new BaseTable<ReportJobInfo>("jobsTable", createJobsTableColumns(), sortableDataProvider, Integer.MAX_VALUE);
    }

    protected DataTable<SchedulerJob, String> createSchedulerJobsTable(ActiveSchedulerJobDataProvider dataProvider) {
    	SortableDataProvider<SchedulerJob, String> sortableDataProvider = new SortableDataAdapter<SchedulerJob>(dataProvider);
    	sortableDataProvider.setSort("nextRun", SortOrder.ASCENDING);
        return new BaseTable<SchedulerJob>("schedulerJobsTable", createActiveSchedulerJobsTableColumns(), sortableDataProvider, Integer.MAX_VALUE);
    }

    protected List<IColumn<ReportJobInfo, String>> createJobsTableColumns() {
        List<IColumn<ReportJobInfo, String>> columns = new ArrayList<IColumn<ReportJobInfo, String>>();
        columns.add(new PropertyColumn<ReportJobInfo, String>(new Model<String>(getString("name")), "jobName", "jobName") {
        	
			private static final long serialVersionUID = 1L;

			@Override
			protected IModel<?> createLabelModel(IModel<ReportJobInfo> reportJobInfoIModel) {
                String basicName = reportJobInfoIModel.getObject().getBasicJobName();
                return new Model<String>(basicName.substring(StorageConstants.REPORTS_ROOT.length()));
            }
            
        });

        columns.add(new PropertyColumn<ReportJobInfo, String>(new Model<String>(getString("DashboardNavigationPanel.owner")), "runner", "runner") {
        	
        	private static final long serialVersionUID = 1L;
        	
            @Override
            public void populateItem(Item<ICellPopulator<ReportJobInfo>> item, String componentId, IModel<ReportJobInfo> rowModel) {
                super.populateItem(item, componentId, rowModel);
                item.add(AttributeModifier.replace("width", "150px"));
            }
            
        });
        
        columns.add(new DateColumn<ReportJobInfo>(new Model<String>(getString("startDate")), "startDate", "startDate") {
        	
        	private static final long serialVersionUID = 1L;
        	
            @Override
            public void populateItem(Item<ICellPopulator<ReportJobInfo>> item, String componentId, IModel<ReportJobInfo> rowModel) {
                super.populateItem(item, componentId, rowModel);
                item.add(AttributeModifier.replace("width", "120px"));
            }

        });
        columns.add(new PropertyColumn<ReportJobInfo, String>(new Model<String>(getString("MonitorPanel.runTime")), "runTime", "runTime") {

        	private static final long serialVersionUID = 1L;
        	
            @Override
            public void populateItem(Item<ICellPopulator<ReportJobInfo>> item, String componentId, IModel<ReportJobInfo> rowModel) {
                super.populateItem(item, componentId, rowModel);
                item.add(AttributeModifier.replace("width", "100px"));
            }

            @Override
            protected IModel<?> createLabelModel(IModel<ReportJobInfo> rowModel) {
                int runTime = rowModel.getObject().getRunTime();
                String text = "";
                if (runTime > 0) {
                    DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm:ss").withZone(DateTimeZone.UTC);
                    text = formatter.print(runTime * 1000);
                }

                return new Model<String>(text);
            }

        });

        columns.add(new AbstractColumn<ReportJobInfo, String>(new Model<String>(getString("MonitorPanel.stop"))) {

        	private static final long serialVersionUID = 1L;
        	
            public void populateItem(Item<ICellPopulator<ReportJobInfo>> item, String componentId,
                                     final IModel<ReportJobInfo> rowModel) {
                final String reportType = rowModel.getObject().getReportType();
                final String runnerKey = rowModel.getObject().getRunnerKey();

                item.add(new AbstractImageAjaxLinkPanel(componentId) {

                	private static final long serialVersionUID = 1L;
                	
                    @Override
                    public String getDisplayString() {
                        return "";
                    }

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        reportService.stopExport(runnerKey, reportType);
                        target.add(jobsTable);
                        target.add(runHistoryTable);
                    }

                    @Override
                    public String getImageName() {
                        return "images/stop.png";
                    }

                    @Override
					protected Link getLink() {
						Link link = super.getLink();
						link.add(new ConfirmBehavior(new Model<String>(
                                "Do you want to stop the execution?\\nBe aware that the cancelation process may take some time.")));
						return link;
					}
                });
            }

        });
        
        return columns;
    }

    protected List<IColumn<SchedulerJob, String>> createActiveSchedulerJobsTableColumns() {
        List<IColumn<SchedulerJob, String>> columns = new ArrayList<IColumn<SchedulerJob, String>>();
        columns.add(new PropertyColumn<SchedulerJob, String>(new Model<String>(getString("name")), "name", "name") {            
            @Override
            protected IModel<String> createLabelModel(IModel<SchedulerJob> jobInfoIModel) {
                return new Model<String>(jobInfoIModel.getObject().getName());
            }
        });

        columns.add(new PropertyColumn<SchedulerJob, String>(new Model<String>(getString("type")), "time.type", "time.type") {
            @Override
			public void populateItem(Item<ICellPopulator<SchedulerJob>> item, String componentId, IModel<SchedulerJob> rowModel) {
                SchedulerTime st = rowModel.getObject().getTime();
                Label label = new Label(componentId, getString("JobPanel.type." + st.getType()));
                label.add(new SimpleTooltipBehavior(SchedulerUtil.getTooltip(st)));
    			item.add(label);
			}
        });
        columns.add(new PropertyColumn<SchedulerJob, String>(new Model<String>(getString("Report")), "report.path", "report.path") {

            @Override
			public void populateItem(Item<ICellPopulator<SchedulerJob>> item, String componentId, IModel<SchedulerJob> rowModel) {
                String path = rowModel.getObject().getReport().getPath();
                String relativePath = StorageUtil.getPathWithoutRoot(path);
                String name = StorageUtil.getName(relativePath);
                Label label = new Label(componentId, name);
                label.add(new SimpleTooltipBehavior(relativePath));
    			item.add(label);
			}

        });
        columns.add(new BooleanImagePropertyColumn<SchedulerJob>(new Model<String>(getString("MonitorPanel.running")), "isRunning", "isRunning") {
        	@Override
            public void populateItem(Item<ICellPopulator<SchedulerJob>> item, String componentId, IModel<SchedulerJob> rowModel) {
                super.populateItem(item, componentId, rowModel);
                item.add(AttributeModifier.replace("width", "80px"));
            }
        });
        columns.add(new PropertyColumn<SchedulerJob, String>(new Model<String>(getString("MonitorPanel.runTime")), "runTime", "runTime") {

        	private static final long serialVersionUID = 1L;
        	
            @Override
            public void populateItem(Item<ICellPopulator<SchedulerJob>> item, String componentId, IModel<SchedulerJob> rowModel) {
                super.populateItem(item, componentId, rowModel);
                item.add(AttributeModifier.replace("width", "100px"));
            }

            @Override
            protected IModel<?> createLabelModel(IModel<SchedulerJob> rowModel) {
                int runTime = rowModel.getObject().getRunTime();
                String text = "";
                if (runTime > 0) {
                    DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm:ss").withZone(DateTimeZone.UTC);
                    text = formatter.print(runTime * 1000);
                }

                return new Model<String>(text);
            }

        });

        columns.add(new NextRunDateColumn<SchedulerJob>());
        
        columns.add(new AbstractColumn<SchedulerJob, String>(new Model<String>(getString("MonitorPanel.runNow"))) {

            public void populateItem(Item<ICellPopulator<SchedulerJob>> item, String componentId,
                                     final IModel<SchedulerJob> rowModel) {

            	item.add(AttributeModifier.replace("width", "80px"));
                item.add(new AbstractImageAjaxLinkPanel(componentId) {

                    @Override
                    public String getDisplayString() {
                        return "";
                    }

                    public boolean isVisible() {
                        return !rowModel.getObject().isRunning();
                    }

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        JobDetail job = schedulerService.getJobDetail(rowModel.getObject());
                        if (job != null) {                        	
                            reportService.runMonitorReport(job);                                                                                   
                        }
                    }

                    @Override
                    public String getImageName() {                        
                        return "images/run.gif";                        
                    }

					@Override
					protected Link getLink() {
						Link link = super.getLink();
						link.add(new ConfirmBehavior(new Model<String>(getString("MonitorPanel.runNowMessage"))));
						return link;
					}

                });
            }

        });


        columns.add(new AbstractColumn<SchedulerJob, String>(new Model<String>(getString("MonitorPanel.stop"))) {

            public void populateItem(Item<ICellPopulator<SchedulerJob>> item, String componentId,
                                     final IModel<SchedulerJob> rowModel) {


            	item.add(AttributeModifier.replace("width", "80px"));
                item.add(new AbstractImageAjaxLinkPanel(componentId) {

                    @Override
                    public String getDisplayString() {
                        return "";
                    }

                    public boolean isVisible() {
                        return rowModel.getObject().isRunning();
                    }

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        JobDetail job = schedulerService.getJobDetail(rowModel.getObject());
                        if (job != null) {
                            String reportType = (String) job.getJobDataMap().get(RunReportJob.REPORT_TYPE);
                            String runnerKey = (String) job.getJobDataMap().get(RunReportJob.RUNNER_KEY);
                            reportService.stopExport(runnerKey, reportType);
                            target.add(schedulerJobsTable);
                            target.add(runHistoryTable);
                        }
                    }

                    @Override
                    public String getImageName() {
                        if (isEnabled()) {
                            return "images/stop.png";
                        } else {
                            return "images/clear.gif";
                        }
                    }

					@Override
					protected Link getLink() {
						Link link = super.getLink();
						link.add(new ConfirmBehavior(new Model<String>(getString("MonitorPanel.stopMessage"))));
						return link;
					}

                });
            }

        });

        return columns;
    }

}
