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
package ro.nextreports.server.web.report;

import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import ro.nextreports.engine.queryexec.QueryParameter;


public abstract class DynamicParameterRuntimePanel extends ParameterRuntimePanel {
	
	public DynamicParameterRuntimePanel(String id) {
        this(id, true);
    }

	
	 public DynamicParameterRuntimePanel(String id, boolean runNow) {
	    super(id, runNow);	       	        
	 }
		
	@SuppressWarnings("unchecked")
    protected void createItem(final ListItem<QueryParameter> item) {
        super.createItem(item);

        // add dynamic label and checkbox only for scheduler
        final QueryParameter parameter = item.getModelObject();
        boolean hasDefaultSource = (parameter.getDefaultSource() != null) && (parameter.getDefaultSource().trim().length() > 0);
        boolean hasSource = (parameter.getSource() != null) && (parameter.getSource().trim().length() > 0);

        final IModel dynamicModel = new PropertyModel(runtimeModel.getParameters(), parameter.getName() + ".dynamic");

        enableItem(item, dynamicModel, null);

        final CheckBox dynamicChkBox = new CheckBox("dynamicChkBox", dynamicModel);
        dynamicChkBox.setVisible(!runNow && (hasDefaultSource || hasSource));
        dynamicChkBox.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
                enableItem(item, dynamicModel, target);
            }
        });
        item.add(dynamicChkBox.setOutputMarkupId(true));

        Label dynamicLabel = new Label("dynamicLabel", getString("DynamicParameterRuntimePanel.dynamic"));
        dynamicLabel.setVisible(!runNow && (hasDefaultSource || hasSource));
        item.add(dynamicLabel.setOutputMarkupId(true));

    }

    private void enableItem(ListItem<QueryParameter> item, IModel dynamicModel, AjaxRequestTarget target) {
        Iterator it = item.iterator();
        while (it.hasNext()) {
            Component component = (Component) it.next();
            if (component.isVisible()) {
            	if (!component.getId().startsWith("dynamic")) {
            		component.setEnabled(!(Boolean) dynamicModel.getObject());
            		if (target != null) {
            			target.add(component);
            		}
            	}            	            	            		            	
            }
        }
    }	
    
    public boolean hasDynamicParameter() {
    	for (QueryParameter parameter : paramList) {
    		 boolean hasDefaultSource = (parameter.getDefaultSource() != null) && (parameter.getDefaultSource().trim().length() > 0);
    	     boolean hasSource = (parameter.getSource() != null) && (parameter.getSource().trim().length() > 0);
    		 if (!runNow && (hasDefaultSource || hasSource)) {
    			 return true;
    		 }
    	}
    	return false;
    }

}
