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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Folder;
import ro.nextreports.server.domain.Group;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.domain.User;
import ro.nextreports.server.report.ReportConstants;
import ro.nextreports.server.util.I18NUtil;
import ro.nextreports.server.util.StorageUtil;
import ro.nextreports.server.web.common.panel.AbstractImageAjaxLinkPanel;
import ro.nextreports.server.web.common.panel.AbstractImageLabelPanel;

/**
 * User: mihai.panaitescu
 * Date: 16-Sep-2010
 * Time: 13:11:51
 */
public abstract class NameColumn extends AbstractColumn<Entity, String> {

    private static final long serialVersionUID = 1L;

    public NameColumn() {
        super(new Model<String>(new StringResourceModel("ActionContributor.Search.entityName", null).getString()), "name");
    }

    public void populateItem(Item<ICellPopulator<Entity>> cellItem, String componentId, IModel<Entity> rowModel) {
        final Entity entity = rowModel.getObject();
        Component component;
        if (StorageUtil.isFolder(entity)) {
            component = new AbstractImageAjaxLinkPanel(componentId) {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    onEntitySelection(entity, target);
                }

                @Override
                public String getDisplayString() {
                	if (I18NUtil.nodeNeedsInternationalization(entity.getName())) {
            			return getString("node."+ entity.getName());
            		}
                    return entity.getName();
                }

                @Override
                public String getImageName() {
                    return NameColumn.getImage(entity);
                }

            };
        } else {
            component = new AbstractImageLabelPanel(componentId) {

                private static final long serialVersionUID = 1L;

                @Override
                public String getDisplayString() {
                    return entity.getName();
                }

                @Override
                public String getImageName() {
                    return NameColumn.getImage(entity);
                }

            };
        }
        cellItem.add(component);
        cellItem.add(AttributeModifier.append("class", "name-col"));
    }

    public static String getImage(Entity entity) {
        if (entity instanceof Folder) {
            return "images/folder-closed.gif";
        } else if (entity instanceof DataSource) {
            return "images/datasource.png";
        } else if (entity instanceof Report) {
            Report report = (Report) entity;
            if (ReportConstants.NEXT.equals(report.getType())) {    
            	if (report.isDrillDownable()) {
            		return "images/drill-report-next.png";
            	} else if (report.isTableType()) {
                	return "images/report-next-table.png";
                } else if (report.isAlarmType()) {
                	return "images/report-next-alarm.png";
                }  else if (report.isIndicatorType()) {
                	return "images/report-next-indicator.png";
                } else {
                	return "images/report-next.png";
                }
            } else if (ReportConstants.JASPER.equals(report.getType())) {
                return "images/report-jasper.png";
            } else {
                return "images/report.png";
            }
        } else if (entity instanceof Chart) {
            if ( ((Chart)entity).isDrillDownable() ) {
                return "images/drill.png"; 
            } else {
                return "images/chart.png";
            }
        } else if (entity instanceof User) {
            User user = (User) entity;
            if (user.isAdmin()) {
                return "images/user_admin.png";
            } else {
                return "images/user.png";
            }
        } else if (entity instanceof Group) {
            return "images/group.png";
        } else if (entity instanceof SchedulerJob) {
            return "images/schedule.png";
        }

        return "images/clear.gif"; // !?
    }

    @Override
    public String getCssClass() {
        return "name-col";
    }

    public abstract void onEntitySelection(Entity entity, AjaxRequestTarget target);

}
