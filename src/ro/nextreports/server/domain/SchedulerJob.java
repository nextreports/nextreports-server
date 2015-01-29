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
package ro.nextreports.server.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrReference;

import ro.nextreports.server.distribution.Destination;
import ro.nextreports.server.util.StorageUtil;
import ro.nextreports.server.web.report.ReportRuntimeModel;


/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: May 20, 2008
 * Time: 12:25:49 PM
 */
public class SchedulerJob extends Entity {

	private static final long serialVersionUID = 1L;

	@JcrReference
    private Report report;

    @JcrChildNode(createContainerNode = false)
    private ReportRuntime reportRuntime;

    @JcrChildNode(createContainerNode = false)
    private SchedulerTime time;

    @JcrChildNode(createContainerNode = false)
    private List<Destination> destinations;
    
    @JcrReference
    private ReportRuntimeTemplate template;

    private boolean running; // will be updated by SchedulerJobReadAdvice

    private Date nextRun; // will be updated by SchedulerJobReadAdvice

	private int runTime; // will be updated by DefaultSchedulerService
	
	private boolean runNow; // true if we do Run, false for Schedule
	
	private String creator;
	
    public SchedulerJob() {
        super();
        
        reportRuntime = new ReportRuntime();
        destinations = new ArrayList<Destination>();
        time = new SchedulerTime();
        runNow = false;
    }

	public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public ReportRuntime getReportRuntime() {
		return reportRuntime;
	}

	public void setReportRuntime(ReportRuntime reportRuntime) {
		this.reportRuntime = reportRuntime;
	}

    public SchedulerTime getTime() {
        return time;
    }

    public void setTime(SchedulerTime schedulerTime) {
        this.time = schedulerTime;
    }

    public List<Destination> getDestinations() {
		return destinations;
	}

	public void setDestinations(List<Destination> destinations) {
		this.destinations = destinations;
	}

	public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
        
    public boolean isRunNow() {
		return runNow;
	}

	public void setRunNow(boolean runNow) {
		this.runNow = runNow;
	}		

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	@Override
    public boolean allowPermissions() {
        return true;
    }

    public Date getNextRun() {
        return nextRun;
    }

    public void setNextRun(Date nextRun) {        
        this.nextRun = nextRun;
    }

    public int getRunTime() {
		return runTime;
	}

	public void setRunTime(int runTime) {
		this.runTime = runTime;
	}		

	public ReportRuntimeTemplate getTemplate() {
		return template;
	}

	public void setTemplate(ReportRuntimeTemplate template) {
		this.template = template;
	}

	public ReportRuntimeModel createRuntimeModel() {
        ReportRuntimeModel runtimeModel = new ReportRuntimeModel();
        runtimeModel.setExportLayout(reportRuntime.getLayoutType());
        runtimeModel.setExportType(reportRuntime.getOutputType());
        runtimeModel.setHeaderPerPage(reportRuntime.isHeaderPerPage());
        runtimeModel.setParameters(new HashMap<String, ReportRuntimeParameterModel>());
        
        return runtimeModel;
    }

    public void setRuntimeModel(ReportRuntimeModel runtimeModel) {
        if (reportRuntime == null) {
        	reportRuntime = new ReportRuntime();
        	reportRuntime.setName("reportRuntime");
        	reportRuntime.setPath(StorageUtil.createPath(getPath(), reportRuntime.getName()));
        }

        reportRuntime.setOutputType(runtimeModel.getExportType());
        reportRuntime.setLayoutType(runtimeModel.getExportLayout());
        reportRuntime.setHeaderPerPage(runtimeModel.isHeaderPerPage());
        reportRuntime.clearParametersValues();
        HashMap<String, ReportRuntimeParameterModel> parameters = runtimeModel.getParameters();
        for (String parameterName : parameters.keySet()) {
            ReportRuntimeParameterModel runtimeParameterModel = parameters.get(parameterName);
            reportRuntime.addParameterValue(parameterName, runtimeParameterModel.getDisplayName(), (Serializable) runtimeParameterModel.getProcessingValue(), runtimeParameterModel.isDynamic());
        }        
    }

    @Override
    public String toString() {
        return "SchedulerJob{" +
                "report=" + report +
                ", reportRuntime=" + reportRuntime +
                ", time=" + time +
                ", destinations=" + destinations +
                ", running=" + running +
                ", nextRun=" + nextRun +
                ", runTime=" + runTime +
                '}';
    }
}
