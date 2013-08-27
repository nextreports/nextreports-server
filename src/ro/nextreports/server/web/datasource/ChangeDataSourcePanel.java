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
package ro.nextreports.server.web.datasource;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.common.form.AdvancedForm;
import ro.nextreports.server.web.common.panel.NextFeedbackPanel;
import ro.nextreports.server.web.core.EntityBrowserPanel;


//
public class ChangeDataSourcePanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	private static final Logger LOG = LoggerFactory.getLogger(ChangeDataSourcePanel.class);
	
    public List<Entity> entities;    
    private DataSource dataSource;

    @SpringBean
    private StorageService storageService;
    
    @SpringBean
    private DashboardService dashboardService;

    private NextFeedbackPanel feedbackPanel;

    public ChangeDataSourcePanel(String id, List<Entity> entities) {
        super(id);
        
        this.entities = entities;
        init();
    }    

    private void init() {
    	
    	AdvancedForm<Void> form = new ChangeForm("form");
        add(form);

        feedbackPanel = new NextFeedbackPanel("feedback", form);
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);

        setOutputMarkupId(true);
    }        

    @SuppressWarnings("unchecked")
    private class ChangeForm extends AdvancedForm<Void> {

		private static final long serialVersionUID = 1L;

		@SuppressWarnings("rawtypes")
		public ChangeForm(String id) {
            super(id);
            
            if (entities.size() == 1) {
            	if (entities.get(0) instanceof Report) {
            		setModel(new CompoundPropertyModel(entities.get(0)));
            	} else {
            		setModel(new CompoundPropertyModel(entities.get(0)));
            	}
            }	            
            setOutputMarkupId(true);

            String entityName;
            String name;
            
            if (entities.size() == 1) {
            	if (entities.get(0) instanceof Report) {
            		entityName = getString("Report");
            	} else {
            		entityName = getString("Chart");
            	}
            	name = entities.get(0).getName();
            } else {
            	if (entities.get(0) instanceof Report) {
            		entityName = getString("Section.Reports.name");
            	} else {
            		entityName = getString("Section.Charts.name");
            	}	
            	StringBuilder sb = new StringBuilder();
            	for (Entity entity : entities) {
            		sb.append(entity.getName()).append("\n");
            	} 
            	name = sb.toString();
            }
                                                       
            add(new Label("entityName", entityName));
            add(new MultiLineLabel("reportName", name));

            Entity[] dsEntities;
            try {
            	dsEntities = storageService.getEntitiesByClassName(StorageConstants.DATASOURCES_ROOT, DataSource.class.getName());
            } catch (Exception e) {
            	dsEntities = new Entity[0];
                e.printStackTrace();
                error(e.getMessage());
                LOG.error(e.getMessage(), e);
            }
            List<DataSource> datasources = new ArrayList<DataSource>();
            for (Entity entity : dsEntities) {
                datasources.add((DataSource) entity);
            }
            ChoiceRenderer<DataSource> pathRenderer = new ChoiceRenderer<DataSource>("path") {
            	
				private static final long serialVersionUID = 1L;

				@Override
				public Object getDisplayValue(DataSource dataSource) {
                    return dataSource.getPath().substring(StorageConstants.DATASOURCES_ROOT.length());
                }
				
            };
            DropDownChoice<DataSource> choice;
            
            if (entities.size() == 1) {
            	choice = new DropDownChoice<DataSource>("dataSource", datasources, pathRenderer);
            } else {
            	choice = new DropDownChoice<DataSource>("dataSource", new PropertyModel<DataSource>(this, "dataSource"), datasources, pathRenderer);
            }
            add(choice);

            add(new AjaxLink<Void>("cancel") {

				private static final long serialVersionUID = 1L;

				@Override
                public void onClick(AjaxRequestTarget target) {
                    back(target);
                }
				
            });

            add(new AjaxButton("submit", this) {
            	
				private static final long serialVersionUID = 1L;

				@Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    try {
                        if (entities.size() == 1) {
                        	Entity entity = entities.get(0);
                        	if (entity instanceof Report) {
                        		storageService.modifyEntity((Report)entity);
                        	} else {
                        		storageService.modifyEntity((Chart)entity);
                        	}
                            dashboardService.resetCache(entity.getId());
                        } else {
                        	boolean isReport = (entities.get(0) instanceof Report);
                        	List<String> entityIds = new ArrayList<String>();
                        	for (Entity entity : entities) {
                        		if (isReport) {
                        			((Report)entity).setDataSource(dataSource);
                        		} else {
                        			((Chart)entity).setDataSource(dataSource);
                        		}
                        		entityIds.add(entity.getId());
                        	}
                        	storageService.modifyEntities(entities);
                        	dashboardService.resetCache(entityIds);
                        }
                        back(target);
                    } catch (Exception e) {
                        e.printStackTrace();
                        error(e.getMessage());
                    }
                }

                protected void onError(AjaxRequestTarget target, Form<?> form) {
                    target.add(feedbackPanel);
                }
                
            });

        }

		// needed for wicket framework!
        public DataSource getDataSource() {
        	return dataSource;
        }
        
        public void setDataSource(DataSource dataSource) {
        	ChangeDataSourcePanel.this.dataSource = dataSource;
        }        
        
        private void back(AjaxRequestTarget target) {
            EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
            panel.backwardWorkspace(target);
        }

    }

}
