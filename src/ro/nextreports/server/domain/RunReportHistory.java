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

import org.jcrom.annotations.JcrProperty;
import org.jcrom.annotations.JcrChildNode;

import ro.nextreports.server.report.util.ReportUtil;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.io.Serializable;


/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: Jun 4, 2008
 * Time: 2:46:22 PM
 */
public class RunReportHistory extends Entity {

	private static final long serialVersionUID = 1L;
	
	public static final String USER = "user";
    public static final String SCHEDULER = "scheduler";
    public static final String WEB_SERVICE = "webservice";

    @JcrProperty
    private Date startDate;

    @JcrProperty
    private Date endDate;
    
    @JcrProperty
    private int duration; // in seconds

    @JcrProperty
    private boolean error;

    @JcrProperty
    private String message;

    @JcrProperty
    private String url;
    
    @JcrProperty
    private String runnerId; // entity id

    @JcrProperty
    private String runnerType; // entity type (User or SchedulerJob)

    @JcrChildNode
	private List<ParameterValue> parametersValues;

    public RunReportHistory() {
        super();
        parametersValues = new ArrayList<ParameterValue>();
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public boolean isSuccess() {
    	return !isError();
    }
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getRunnerId() {
		return runnerId;
	}

	public void setRunnerId(String runnerId) {
		this.runnerId = runnerId;
	}

	public String getRunnerType() {
		return runnerType;
	}

	public void setRunnerType(String runnerType) {
		this.runnerType = runnerType;
	}

    private void clearParametersValues() {
		parametersValues.clear();
	}

    private void addParameterValue(String parameterName, String displayName, Serializable parameterValue) {
		ParameterValue internal = new ParameterValue();
		internal.setName(parameterName);
		internal.setRuntimeName(displayName);
		internal.setPath(getPath() + "/" + internal.getName());
		internal.setValue(parameterValue);
		parametersValues.add(internal);
	}

    public void setParametersValues(Map<String, Object> parametersValues, Map<String, String> displayNames) {
        clearParametersValues();
        for (String name : parametersValues.keySet()) {
        	String displayName = (displayNames == null) ? name : displayNames.get(name);
            addParameterValue(name, displayName, (Serializable)parametersValues.get(name));
        }
    }

    public Map<String, Object> getParametersValues() {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		for (ParameterValue parameterValue : parametersValues) {
			map.put(parameterValue.getName(), parameterValue.getValue());
		}
		return map;
	}
    
    public Map<String, String> getParametersDisplayNames() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		for (ParameterValue parameterValue : parametersValues) {
			map.put(parameterValue.getName(), parameterValue.getRuntimeName());
		}
		return map;
	}

    @Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("RunReportHistory[");
		buffer.append("duration = ").append(duration);
		buffer.append(", endDate = ").append(endDate);
		buffer.append(", error = ").append(error);
		buffer.append(", message = ").append(message);
		buffer.append(", runnerId = ").append(runnerId);
		buffer.append(", runnerType = ").append(runnerType);
		buffer.append(", startDate = ").append(startDate);
		buffer.append(", url = ").append(url);

        buffer.append(", parametersValues = [");
        for (ParameterValue pv : parametersValues) {
            buffer.append(ReportUtil.getParameterValueAsString(pv));
            buffer.append(",");
        }
        if (buffer.toString().endsWith(",")) {
            buffer.deleteCharAt(buffer.length() - 1);
        }
        buffer.append("]");
        
        buffer.append("]");
		
		return buffer.toString();
	}

}
