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
import java.util.HashMap;
import java.util.Map;

public class DrillEntityContext implements Serializable {
	
	private static final long serialVersionUID = 600212310823554025L;

    // values for settings of the parent root chart
    private Map<String, Object> settingsValues;
    // values for drill parameters
    private Map<String, Object> drillParameterValues;
    // a function for a drill chart / an entity id for a drill table report
    private String drillLink;    
    // has no other link to it (no onClick method is possible)
    private boolean isLast;      
    // clicked table column
    private int column;
    
    private String currentDrillEntityId;

    public DrillEntityContext() {
    	drillParameterValues = new HashMap<String, Object>();  
    }
    
    public DrillEntityContext(Map<String, Object> drillParameterValues, String drillLink) {
    	// TODO check in engine or in chart service
    	if (drillParameterValues == null) {
    		this.drillParameterValues = new HashMap<String, Object>();
    	} else {
    		this.drillParameterValues = drillParameterValues;
    	}
        this.drillLink = drillLink;
        this.settingsValues = new HashMap<String, Object>();
    }

    public Map<String, Object> getDrillParameterValues() {
        return drillParameterValues;
    }

    public String getDrillLink() {
        return drillLink;
    }

    public void setDrillLink(String drillLink) {
		this.drillLink = drillLink;
	}

    public Map<String, Object> getSettingsValues() {
        return settingsValues;
    }

    public void setSettingsValues(Map<String, Object> settingsValues) {
        if (settingsValues == null) {
            return;
        }
        this.settingsValues = settingsValues;
    }        

    public boolean isLast() {
		return isLast;
	}

	public void setLast(boolean isLast) {
		this.isLast = isLast;
	}		

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}
	
	public String getCurrentDrillEntityId() {
		return currentDrillEntityId;
	}

	public void setCurrentDrillEntityId(String currentDrillEntityId) {
		this.currentDrillEntityId = currentDrillEntityId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DrillEntityContext that = (DrillEntityContext) o;

        if (drillLink != null ? !drillLink.equals(that.drillLink) : that.drillLink != null)
            return false;
        if (drillParameterValues != null ? !drillParameterValues.equals(that.drillParameterValues) : that.drillParameterValues != null)
            return false;
        if (settingsValues != null ? !settingsValues.equals(that.settingsValues) : that.settingsValues != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = (settingsValues != null ? settingsValues.hashCode() : 0);
        result = 31 * result + (drillParameterValues != null ? drillParameterValues.hashCode() : 0);
        result = 31 * result + (drillLink != null ? drillLink.hashCode() : 0);
        return result;
    }

}
