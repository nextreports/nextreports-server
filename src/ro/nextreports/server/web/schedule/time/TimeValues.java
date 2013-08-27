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
package ro.nextreports.server.web.schedule.time;

import java.io.Serializable;
import java.util.ArrayList;
//
// Created by IntelliJ IDEA.
// User: mihai.panaitescu
// Date: 20-May-2009
// Time: 11:49:18

//
public class TimeValues implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String INTERVAL_TYPE = "Interval";
    public static final String DISCRETE_TYPE = "Discrete";

    private String intervalType;
    private String startTime;
    private String endTime;
    private ArrayList<String> discreteValues = new ArrayList<String>();

    public TimeValues(){
    }

    public String getIntervalType() {
        return intervalType;
    }

    public void setIntervalType(String intervalType) {
        this.intervalType = intervalType;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {        
        this.endTime = endTime;
    }

    public ArrayList<String> getDiscreteValues() {
        return discreteValues;
    }

    public void setDiscreteValues(ArrayList<String> discreteValues) {        
        this.discreteValues = discreteValues;
    }

    public boolean isDiscrete() {
    	return DISCRETE_TYPE.equals(intervalType);
    }

    public boolean isInterval() {
    	return INTERVAL_TYPE.equals(intervalType);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((discreteValues == null) ? 0 : discreteValues.hashCode());
		result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
		result = prime * result + ((intervalType == null) ? 0 : intervalType.hashCode());
		result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TimeValues other = (TimeValues) obj;
		if (discreteValues == null) {
			if (other.discreteValues != null)
				return false;
		} else if (!discreteValues.equals(other.discreteValues))
			return false;
		if (endTime == null) {
			if (other.endTime != null)
				return false;
		} else if (!endTime.equals(other.endTime))
			return false;
		if (intervalType == null) {
			if (other.intervalType != null)
				return false;
		} else if (!intervalType.equals(other.intervalType))
			return false;
		if (startTime == null) {
			if (other.startTime != null)
				return false;
		} else if (!startTime.equals(other.startTime))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TimeValues [intervalType=" + intervalType + ", startTime=" + startTime + ", endTime=" + endTime
				+ ", discreteValues=" + discreteValues + "]";
	}
	
	
     
    
}
