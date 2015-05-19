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
package ro.nextreports.server.web.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree.State;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.Folder;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.WindowsTheme;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.EntityComparator;
import ro.nextreports.server.util.I18NUtil;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.chart.ChartSection;
import ro.nextreports.server.web.common.event.AjaxUpdateEvent;
import ro.nextreports.server.web.common.event.AjaxUpdateListener;
import ro.nextreports.server.web.common.event.BulkMenuUpdateEvent;
import ro.nextreports.server.web.common.menu.MenuPanel;
import ro.nextreports.server.web.common.table.AjaxCheckTablePanel;
import ro.nextreports.server.web.common.table.FakeSortableDataAdapter;
import ro.nextreports.server.web.common.table.SortableDataAdapter;
import ro.nextreports.server.web.core.event.SelectEntityEvent;
import ro.nextreports.server.web.core.menu.EntityBulkMenuPanel;
import ro.nextreports.server.web.core.menu.EntityMenuPanel;
import ro.nextreports.server.web.core.menu.EntityTreeMenuPanel;
import ro.nextreports.server.web.core.section.EntitySection;
import ro.nextreports.server.web.core.section.SectionContext;
import ro.nextreports.server.web.core.section.SectionContextUtil;
import ro.nextreports.server.web.core.section.SectionManager;
import ro.nextreports.server.web.core.table.ActionsColumn;
import ro.nextreports.server.web.core.table.CreatedByColumn;
import ro.nextreports.server.web.core.table.CreationDateColumn;
import ro.nextreports.server.web.core.table.LastUpdatedByColumn;
import ro.nextreports.server.web.core.table.LastUpdatedDateColumn;
import ro.nextreports.server.web.core.table.NameColumn;
import ro.nextreports.server.web.core.table.TypeColumn;
import ro.nextreports.server.web.core.tree.EntityTreeProvider;
import ro.nextreports.server.web.datasource.DataSourceSection;
import ro.nextreports.server.web.report.ReportSection;
import ro.nextreports.server.web.schedule.SchedulerSection;

/**
 * @author Decebal Suiu
 */
public class EntityBrowserPanel extends StackPanel implements AjaxUpdateListener {

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(EntityBrowserPanel.class);

    protected LocationPanel locationPanel;
    protected StatusBarPanel statusBarPanel;
    protected MenuPanel bulkMenuPanel;
    protected MenuPanel treeMenuPanel;
    protected MenuPanel menuPanel;
    protected EntityTree tree;
    protected ISortableDataProvider<Entity, String> dataProvider;
    protected AjaxCheckTablePanel<Entity> tablePanel;

    protected String sectionId;

    @SpringBean
    protected StorageService storageService;

    @SpringBean
    protected SectionManager sectionManager;

    public EntityBrowserPanel(String id, String sectionId) {
        super(id);

        this.sectionId = sectionId;

        setModel(new EntityBrowserModel(sectionId));

        locationPanel = new LocationPanel("locationPanel", sectionId);
        locationPanel.setOutputMarkupId(true);
        add(locationPanel);

        statusBarPanel = new StatusBarPanel("statusBarPanel", sectionId);
        statusBarPanel.setOutputMarkupId(true);
        add(statusBarPanel);

        tree = createTree(getRootPath());
        tree.setOutputMarkupId(true);
        add(tree);
        addTreeLinks();

        dataProvider = getEntityDataProvider();
        tablePanel = createTablePanel(dataProvider);
        tablePanel.setOutputMarkupId(true);
        
        initWorkspace(tablePanel);

        addTableLinks();
        
        addBulkLinks();
    }

    public void onAjaxUpdate(AjaxUpdateEvent event) {
        if (event instanceof SelectEntityEvent) {
            SelectEntityEvent selectEntityEvent = (SelectEntityEvent) event;
            selectEntity(selectEntityEvent.getEntity(), event.getTarget());
        } else if (event instanceof BulkMenuUpdateEvent) {
        	event.getTarget().add(bulkMenuPanel);
        	event.getTarget().add(menuPanel);
    	}
    }

    @Override
    protected void onBeforeRender() {
        selectEntity(getModelObject(), null);
        
        super.onBeforeRender();
    }

    protected EntityTree createTree(String rootPath) {
    	ITreeProvider<Entity> treeProvider = new EntityTreeProvider(rootPath) {

			private static final long serialVersionUID = 1L;

			@Override
			protected boolean acceptEntityAsChild(Entity entity) {
				return (entity instanceof ro.nextreports.server.domain.Folder);
			}

			@Override
			protected List<Entity> getChildren(String id) throws NotFoundException {
				// sort
				List<Entity> children = super.getChildren(id);
				Collections.sort(children, new EntityComparator());
				
				return children;
			}
    		
    	};
    	
        return new EntityTree("tree", treeProvider);
    }

    protected AjaxCheckTablePanel<Entity> createTablePanel(ISortableDataProvider<Entity, String> dataProvider) {
        return new AjaxCheckTablePanel<Entity>("work", createTableColumns(), dataProvider, getEntitiesPerPage()) {
        	
			private static final long serialVersionUID = 1L;

			@Override
            protected Item<Entity> newRowTableItem(IModel<Entity> entityIModel, Item<Entity> item) {
                // select report from scheduler job action or dashboard GoToReport
                selectEntity(entityIModel.getObject(), item, ReportSection.ID);
                // select chart from dashboard GoTo
                selectEntity(entityIModel.getObject(), item, ChartSection.ID);
                // select data source from audit GoTo
                selectEntity(entityIModel.getObject(), item, DataSourceSection.ID);
                // select scheduler from audit GoTo
                selectEntity(entityIModel.getObject(), item, SchedulerSection.ID);
                
                return item;
            }
        	
        };
    }
    
    protected int getEntitiesPerPage() {
    	return Integer.MAX_VALUE;
    }
    
    protected ISortableDataProvider<Entity, String> getEntityDataProvider()  {
    	return new SortableDataAdapter<Entity>(new EntityDataProvider(getModel()));
    }

    protected List<IColumn<Entity, String>> createTableColumns() {
        List<IColumn<Entity, String>> columns = new ArrayList<IColumn<Entity, String>>();
        columns.add(new NameColumn() {
        	
            private static final long serialVersionUID = 1L;

            @Override
			public void onEntitySelection(Entity entity, AjaxRequestTarget target) {
                selectEntity(entity, target);
            }
            
        });
        columns.add(new ActionsColumn());
        columns.add(new TypeColumn());
        columns.add(new CreatedByColumn());
        columns.add(new CreationDateColumn());
        columns.add(new LastUpdatedByColumn());
        columns.add(new LastUpdatedDateColumn());

        return columns;
    }

    private void addBulkLinks() {
    	bulkMenuPanel = new EntityBulkMenuPanel("bulkMenuPanel", tablePanel.getSelected(), sectionId);
    	bulkMenuPanel.setOutputMarkupId(true);
    	bulkMenuPanel.setOutputMarkupPlaceholderTag(true);
        add(bulkMenuPanel);
    }

    private void addTreeLinks() {
        treeMenuPanel = new EntityTreeMenuPanel("treeMenuPanel", tree);
        treeMenuPanel.setOutputMarkupId(true);
        add(treeMenuPanel);
    }

    private void addTableLinks() {
    	menuPanel = new EntityMenuPanel("menuPanel", getModel(), sectionId) {
    		
			private static final long serialVersionUID = 1L;

			@Override
    		public boolean isVisible() {
    			return !bulkMenuPanel.isVisible();
    		}
    		
    	};
    	menuPanel.setOutputMarkupPlaceholderTag(true);
        menuPanel.setOutputMarkupId(true);
        add(menuPanel);
    }

    private void onNodeClicked(Entity entity, AjaxRequestTarget target) {
    	// set the new selected entity
        setCurrentPath(entity.getPath());
        
        // TODO
        String count = "unknown";
        try {
        	if (dataProvider instanceof FakeSortableDataAdapter) {
        		// right now only SecurityBrowserPanel uses pagination and there is no need for security on entities (users are seen by admin)
        		count = String.valueOf(storageService.countEntityChildrenById(entity.getId()));        		
        	} else {        		
        		count = String.valueOf(dataProvider.size());        		
        	}
        } catch (NotFoundException ex) {
        	LOG.error(ex.getMessage(), ex);
        }
        SectionContextUtil.setCurrentEntityChildren(sectionId, count);
        SectionContextUtil.setLookFor(sectionId, null);

        if (target != null) {
            target.add(tablePanel);
            target.add(locationPanel);
            target.add(bulkMenuPanel);
            target.add(menuPanel);
            target.add(statusBarPanel);
        }
        
        tablePanel.unselectAll();
        restoreWorkspace(target);
    }

    protected void selectEntity(Entity entity, AjaxRequestTarget target) {        
        if (entity == null) {
    		entity = getRoot();
    	}
    	
        if (LOG.isDebugEnabled()) {
            LOG.debug("Select entity '" + entity.getPath() + "'");
        }

        tree.expandsParents(entity, target);
        if (target != null) {
            tree.updateBranch(entity, target);
        }

        onNodeClicked(entity, target);
    }
    
    private String getCurrentPath() {
    	return SectionContextUtil.getCurrentPath(sectionId);
    }
    
    private void setCurrentPath(String path) {
    	SectionContextUtil.setCurrentPath(sectionId, path);
    }
    
    private Entity getCurrentEntity() {
    	String path = getCurrentPath();
    	try {
			return storageService.getEntity(path);
		} catch (NotFoundException e) {
			throw new WicketRuntimeException(e);
		}
    }
    
    private String getRootPath() {
    	return getSection().getRootPath();
    }
    
    private EntitySection getSection() {
    	return (EntitySection) sectionManager.getSection(sectionId);
    }
    
    private Entity getRoot() {
		try {
			return storageService.getEntity(getRootPath());
		} catch (NotFoundException e) {
			throw new WicketRuntimeException(e);
		}
    }

    // select entity from GoTo actions
    private void selectEntity(Entity entity, Item<Entity> item, String sectionId) {
    	SectionContext sectionContext = NextServerSession.get().getSectionContext(sectionId);
    	if (sectionContext == null) {
    		return;
    	}

        String entityPath = SectionContextUtil.getSelectedEntityPath(sectionId);
        if ((entityPath != null) && entity.getPath().equals(entityPath)) {
            item.add(AttributeModifier.replace("class", "tr-selected"));
            // reset
            SectionContextUtil.setSelectedEntityPath(sectionId, null);
        }
    }
    
    class EntityTree extends NestedTree<Entity> {

    	private static final long serialVersionUID = 1L;

    	public EntityTree(String id, ITreeProvider<Entity> provider) {
    		super(id, provider);
    		
    		add(new WindowsTheme());
        }

    	@Override
    	protected Component newContentComponent(String id, IModel<Entity> model) {
    		return new Folder<Entity>(id, this, model) {

    			private static final long serialVersionUID = 1L;

    			@Override
    			protected boolean isClickable() {
    				return true;
    			}

    			@Override
    			protected void onClick(AjaxRequestTarget target) {
    				// I don't want the default behavior (collapse node if it's expanded)
//    				super.onClick(target);
    				Entity entity = getModelObject();
    				if (tree.getState(entity) == State.COLLAPSED) {
    					tree.expand(entity);
    				} else {
        	        	tree.updateNode(entity, target);    					
    				}
    				
    	        	// unselect the (old) selected entity (force repaint)
    	        	tree.updateNode(getCurrentEntity(), target);    	        	
    				
    				onNodeClicked(entity, target);
    			}

                @Override
				protected String getOtherStyleClass(Entity t) {
					return getClosedStyleClass();
				}

				@Override
                protected boolean isSelected() {
                	return getCurrentPath().equals(getModelObject().getPath());
                }

    			@Override
    			protected IModel<?> newLabelModel(IModel<Entity> model) {
    				return Model.of(getNodeLabel(model.getObject()));
    			}
    			
    		};
    	}

		protected String getNodeLabel(Entity entity) {		
    		if (I18NUtil.nodeNeedsInternationalization(entity.getName())) {
    			return getString("node."+ entity.getName());
    		}
    		
    		return entity.getName();
    	}
    	
        protected void expandsParents(Entity entity, AjaxRequestTarget target) {
            String path = entity.getPath();
            String[] tokens = path.split(StorageConstants.PATH_SEPARATOR);

            Entity root = getRoot();

            Stack<Entity> stack = new Stack<Entity>();
            stack.add(root);

            String rootPath = root.getPath();
            String[] rootTokens = rootPath.split(StorageConstants.PATH_SEPARATOR);

            Entity tmp = root;
            for (int i = rootTokens.length; i < tokens.length; i++) {
                tmp = getChild(tmp, tokens[i]);
                stack.add(tmp);
            }
            
            for (Entity item : stack) {
            	tree.expand(item);
            	tree.updateNode(item, target);
            }
        }

		private Entity getChild(Entity node, String name) {
			Entity[] children;
			try {
				children = storageService.getEntityChildren(node.getPath());
			} catch (NotFoundException e) {
				throw new WicketRuntimeException(e);
			}
			
            for (Entity tmp : children) {
                if (name.equals(tmp.getName())) {
                    return tmp;
                }
            }

            return null;
        }

    }
    
}
