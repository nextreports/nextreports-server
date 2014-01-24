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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.DrillDownEntity;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.common.misc.AjaxConfirmLink;
import ro.nextreports.server.web.common.table.BaseTable;
import ro.nextreports.server.web.core.EntityBrowserPanel;

public class DrillDownEntityPanel extends Panel {

    private static final long serialVersionUID = 1L;
    
	private Entity entity;
    private DataTable<DrillDownEntity, String> table;
    private DrillDownEntitiesDataProvider dataProvider;
    private ModalWindow dialog;

    @SpringBean
    private StorageService storageService;

    public DrillDownEntityPanel(String id, final Entity entity) {
        super(id);
        
        this.entity = entity;
        
        init();
    }

    private void init() {
        add(new Label("legend", getString("ActionContributor.Drill.name")));
        add(new Label("entityName", new Model<String>(entity.getName())));

        addTable();

        dialog = new ModalWindow("dialog");
        add(dialog);

        final AjaxLink addLink = new AjaxLink("addDrill") {

            @Override
            public void onClick(AjaxRequestTarget target) {

                dialog.setTitle(getString("ActionContributor.Drill.addTitle"));                
                dialog.setInitialWidth(400);
                dialog.setUseInitialHeight(false);
                dialog.setContent(new AddDrillDownPanel(dialog.getContentId(), entity) {

                    @Override
                    public void onAddDrillDownEntity(AjaxRequestTarget target, Form form, DrillDownEntity drillEntity) {
                        try {
                            ModalWindow.closeCurrent(target);
                            List<DrillDownEntity> drillEntities = getDrillDownEntities(entity);
                            drillEntities.add(drillEntity);
                            storageService.modifyEntity(entity);
                            target.add(table);
                            refreshAddLink(target);
                        } catch (Exception e) {                            
                            e.printStackTrace();
                            form.error(e.getMessage());
                        }
                    }

                    @Override
                    public void onCancel(AjaxRequestTarget target) {
                        ModalWindow.closeCurrent(target);
                    }

                });
                dialog.show(target);
            }

            @Override
			public boolean isEnabled() {
            	List<DrillDownEntity> list =  getDrillDownEntities(entity);
            	if (list.size() == 0) {
            		return true;
            	} else {
            		DrillDownEntity last = list.get(list.size()-1);
            		return (last.getUrl() == null);
            	}
			}
                        
        };
        addLink.setOutputMarkupId(true);
        add(addLink);

        add(new AjaxConfirmLink("deleteDrill") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                try {
                    List<DrillDownEntity> drillEntities = getDrillDownEntities(entity);
                    if (drillEntities.size() > 0) {
                    	drillEntities.remove(drillEntities.size() - 1);                        
                        storageService.modifyEntity(entity);
                        target.add(table);
                        target.add(addLink);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    error(e.getMessage());
                }
            }

            @Override
            public String getMessage() {
                return getString("ActionContributor.Drill.deleteLast");
            }
        });

        add(new AjaxLink("cancel") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
                panel.backwardWorkspace(target);
            }
        });
    }
    
    private void refreshAddLink(AjaxRequestTarget target) {
    	target.add(get("addDrill"));
    }

    private void addTable() {
        List<IColumn<DrillDownEntity, String>> columns = new ArrayList<IColumn<DrillDownEntity, String>>();
        columns.add(new AbstractColumn<DrillDownEntity, String>(new Model<String>(getString("ActionContributor.Drill.index"))) {

            @Override
            public String getCssClass() {
                return "name-col";
            }

            public void populateItem(Item<ICellPopulator<DrillDownEntity>> item, String componentId,
                                     final IModel<DrillDownEntity> rowModel) {
                final DrillDownEntity drill = rowModel.getObject();
                final String index = String.valueOf(drill.getIndex());
                item.add(new Label(componentId, new Model<String>(index)));
                item.add(AttributeAppender.append("class", "name-col"));
            }
            
        });

        columns.add(new AbstractColumn<DrillDownEntity, String>(new Model<String>(getString("ActionContributor.Drill.target"))) {

            public void populateItem(Item<ICellPopulator<DrillDownEntity>> item, String componentId,
                                     final IModel<DrillDownEntity> rowModel) {
                final DrillDownEntity drill = rowModel.getObject();
                String path;
                if (drill.getEntity() == null) {
                	path = drill.getUrl();
                } else {
                	path = String.valueOf(drill.getEntity().getPath());
                }
                item.add(new Label(componentId, new Model<String>(path)));
            }
        });

        columns.add(new AbstractColumn<DrillDownEntity, String>(new Model<String>(getString("ActionContributor.Drill.parameter"))) {

            public void populateItem(Item<ICellPopulator<DrillDownEntity>> item, String componentId,
                                     final IModel<DrillDownEntity> rowModel) {
                final DrillDownEntity drill = rowModel.getObject();
                String param = String.valueOf(drill.getLinkParameter());
                if ("null".equals(param)) {
                	param = "-";
                }
                item.add(new Label(componentId, new Model<String>(param)));
            }
        });
        
        columns.add(new AbstractColumn<DrillDownEntity, String>(new Model<String>(getString("ActionContributor.Drill.type"))) {

    		public void populateItem(Item<ICellPopulator<DrillDownEntity>> item, String componentId,
                                 final IModel<DrillDownEntity> rowModel) {
    			final DrillDownEntity drill = rowModel.getObject();
    			String entityName = "N/A";
    			if (drill.getType() == DrillDownEntity.REPORT_TYPE) {
    				entityName = getString("Report");
    			} else if (drill.getType() == DrillDownEntity.CHART_TYPE) {
    				entityName = getString("Chart");
    			} else if (drill.getType() == DrillDownEntity.URL_TYPE) {
    				entityName = getString("Url");
    			} 
    			item.add(new Label(componentId, new Model<String>(entityName)));
    		}
    	});
        
        
        columns.add(new AbstractColumn<DrillDownEntity, String>(new Model<String>(getString("ActionContributor.Drill.column"))) {

        		public void populateItem(Item<ICellPopulator<DrillDownEntity>> item, String componentId,
                                     final IModel<DrillDownEntity> rowModel) {
        			final DrillDownEntity drill = rowModel.getObject();
        			String column = drill.getColumn() == 0 ? "-" : String.valueOf(drill.getColumn());
        			item.add(new Label(componentId, new Model<String>(column)));
        		}
        });
        

        dataProvider = new DrillDownEntitiesDataProvider(entity);
        table = new BaseTable<DrillDownEntity>("table", columns, dataProvider, 300);
        table.setOutputMarkupId(true);
        add(table);
    }
    
    public List<DrillDownEntity> getDrillDownEntities(Entity entity) {
    	if (entity instanceof Report) {
    		return ((Report)entity).getDrillDownEntities();
    	} else if(entity instanceof Chart) {
    		return ((Chart)entity).getDrillDownEntities();
    	} else {
    		throw new IllegalArgumentException("Invalid entity type.");
    	}
    }


}
