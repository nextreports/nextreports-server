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
package ro.nextreports.server.web.drilldown;

import java.util.List;

import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.DrillDownEntity;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Report;


public class DrillDownUtil {
	
	public static byte getLastDrillType(Entity entity) {
    	List<DrillDownEntity> drillDownEntities = null;
    	if (entity instanceof Report) {
    		drillDownEntities =((Report)entity).getDrillDownEntities();
    	} else if(entity instanceof Chart) {
    		drillDownEntities = ((Chart)entity).getDrillDownEntities();
    	}
    	if (drillDownEntities == null) {
    		return DrillDownEntity.UNDEFINED_TYPE;
    	} else {
    		if (drillDownEntities.size() == 0) {
    			return  DrillDownEntity.UNDEFINED_TYPE;
    		} else {
    			DrillDownEntity drillEntity = drillDownEntities.get(drillDownEntities.size()-1);
    			return drillEntity.getType();    			
    		}
    	}
    }
	
	public static int getCurrentDrillIndex(Entity entity) {
    	int index = 0;
    	List<DrillDownEntity> drillEntities = null;
    	if (entity instanceof Report) {
    		drillEntities = ((Report)entity).getDrillDownEntities();
    	} else if (entity instanceof Chart) {
    		drillEntities = ((Chart)entity).getDrillDownEntities();
    	}
    	if (drillEntities != null) {
            index = drillEntities.size();
        }
    	return index;
    } 

}
