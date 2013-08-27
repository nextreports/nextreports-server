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
import java.util.Date;

/**
 * @author Decebal Suiu
 */
public class ReportJobInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME_DELIMITER = "#delim#";

    private String jobName;
	private Date nextRun;
	private boolean running;
	private int runTime; // in seconds
	private Date startDate;
    private String runner;
    private String runnerKey;
    private String reportType;

    public String getJobName() {
		return jobName;
	}
	
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

    // till the delimiter (without the uuid)
    public String getBasicJobName() {
        if (jobName != null) {
            int index = jobName.indexOf(NAME_DELIMITER);
            if (index != -1) {
                return jobName.substring(0, index);
            }
        }
        return jobName;
    }

    public Date getNextRun() {
		return nextRun;
	}

	public void setNextRun(Date nextRun) {
		this.nextRun = nextRun;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public int getRunTime() {
		return runTime;
	}

	public void setRunTime(int runTime) {
		this.runTime = runTime;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startTime) {
		this.startDate = startTime;
	}

    public String getRunner() {
        return runner;
    }

    public void setRunner(String runner) {
        this.runner = runner;
    }

    public String getRunnerKey() {
        return runnerKey;
    }

    public void setRunnerKey(String runnerKey) {
        this.runnerKey = runnerKey;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }
    
}
