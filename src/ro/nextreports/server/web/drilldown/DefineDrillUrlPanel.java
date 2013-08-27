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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;

import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.DrillDownEntity;
import ro.nextreports.server.domain.Entity;


public class DefineDrillUrlPanel extends Panel {

	public DefineDrillUrlPanel(String id, final DrillDownEntity drillEntity, Entity entity) {
		
		super(id, new CompoundPropertyModel<DrillDownEntity>(drillEntity));

		Label urlLabel = new Label("urlLabel", getString("Url"));		
		add(urlLabel);

		TextField<String> urlField = new TextField<String>("url");		
		add(urlField);
		
		Label infoLabel = new Label("infoLabel", getString("ActionContributor.Drill.url"));		
		add(infoLabel);
		
		Label columnLabel = new Label("columnLabel", getString("ActionContributor.Drill.column"));                    
		add(columnLabel);
                    
        TextField<Integer> columnField = new TextField<Integer>("column");                    
        add(columnField);
        
        if (drillEntity.getIndex() == 0) {
        	if (entity instanceof Chart) {
        		columnLabel.setVisible(false);            	
        		columnField.setVisible(false);            		 
        	}
        } else {
        	if (DrillDownUtil.getLastDrillType(entity) == DrillDownEntity.CHART_TYPE) {
        		columnLabel.setVisible(false);            	
        		columnField.setVisible(false);            		   
        	} 
        }

	}

}
