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
package ro.nextreports.server.web.common.table;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.settings.SettingsBean;
import ro.nextreports.server.web.common.panel.AbstractImageAjaxLinkPanel;


public class ImageLinkPropertyColumn<T> extends AbstractColumn<T, String> {
	
	private static final long serialVersionUID = 1L;
	
	@SpringBean
	private SettingsBean settings;

    public ImageLinkPropertyColumn(IModel<String> displayModel) {
		super(displayModel);
		
		Injector.get().inject(this);
	}	

    @Override
	public void populateItem(Item<ICellPopulator<T>> item, String componentId, final IModel<T> rowModel) {
        item.add(new AbstractImageAjaxLinkPanel(componentId) {

			private static final long serialVersionUID = 1L;

			@Override
            public String getImageName() {                
                return "images/" + getLinkImageName();                
            }

            public String getDisplayString() {
                return "";
            }

            public void onClick(AjaxRequestTarget target) {
                onImageClick(rowModel.getObject(), target);
            }
        });
    }

    @Override
    public String getCssClass() {
        return "boolean";
    }

    public void onImageClick(T object, AjaxRequestTarget target) {
    }
    
    public String getLinkImageName() {
    	return "info.png";
    }

}
