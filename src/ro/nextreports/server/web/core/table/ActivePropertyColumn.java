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
package ro.nextreports.server.web.core.table;

import java.util.Date;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.schedule.ScheduleConstants;
import ro.nextreports.server.settings.SettingsBean;
import ro.nextreports.server.web.NextServerApplication;
import ro.nextreports.server.web.common.panel.AbstractImagePanel;
import ro.nextreports.server.web.themes.ThemesManager;


public class ActivePropertyColumn extends AbstractColumn<Entity, String> {
	
	private static final long serialVersionUID = 1L;
	
	@SpringBean
	private SettingsBean settings;

    public ActivePropertyColumn() {
        super(new Model<String>(new StringResourceModel("ActionContributor.Search.entityActive", null).getString()));
        
        Injector.get().inject(this);
    }

    public void populateItem(Item<ICellPopulator<Entity>> item,
                             String componentId, final IModel<Entity> rowModel) {

        SchedulerJob job = (SchedulerJob) rowModel.getObject();
        final boolean active;
        Date now = new Date();
        if (ScheduleConstants.ONCE_TYPE.equals(job.getTime().getType())) {
            active = job.getTime().getRunDate().compareTo(now) >= 0;
        } else {
            active = (job.getTime().getStartActivationDate().compareTo(now) <= 0) &&
                    (job.getTime().getEndActivationDate().compareTo(now) >= 0);
        }

        item.add(new AbstractImagePanel(componentId) {

			private static final long serialVersionUID = 1L;

			@Override
            public String getImageName() {
                if (active) {
                	String theme = settings.getSettings().getColorTheme();
                    return "images/" + ThemesManager.getTickImage(theme, (NextServerApplication)getApplication());
                } else {
                    return "images/delete.gif";
                }
            }

        });
    }

    @Override
    public String getCssClass() {
        return "boolean";
    }
    
}
