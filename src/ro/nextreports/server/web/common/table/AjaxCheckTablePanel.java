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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import ro.nextreports.server.web.common.event.BulkMenuUpdateEvent;
import ro.nextreports.server.web.common.panel.GenericPanel;


/**
 * User: mihai.panaitescu
 * Date: 26-May-2010
 * Time: 17:13:33
 */
public class AjaxCheckTablePanel<T> extends Panel {

	private static final long serialVersionUID = 1L;

	private static final String TABLE_ID = "table";

    private BaseTable<T> dataTable;
    private AjaxCheckBox headerAjaxCheckBox;
    
    private List<T> selectedObjects;

    public AjaxCheckTablePanel(String id, List<IColumn<T>> columns, ISortableDataProvider<T> dataProvider, int rowsPerPage) {
        super(id);
        
        this.setOutputMarkupId(true);
        
        selectedObjects = new ArrayList<T>();
        
        columns.add(0, new CheckableTableColumn(new Model<String>(), "id"));
        dataTable = new BaseTable<T>(TABLE_ID, columns, dataProvider, rowsPerPage) {

			private static final long serialVersionUID = 1L;

			@Override
            protected Item<T> newRowItem(String id, int index, IModel<T> model) {
                return newRowTableItem(model, new HighlitableDataItem(id, index, model));
            }

        };
        dataTable.setItemReuseStrategy(ReuseIfModelsEqualStrategy.getInstance());
        add(dataTable);
    }

    public List<T> getSelected() {
    	return selectedObjects;
    }

    public BaseTable<T> getDataTable() {
        return dataTable;
    }

    public void unselectAll() {
        selectedObjects.clear();
    }

    protected Item<T> newRowTableItem(IModel<T> entityIModel, Item<T> item) {
        return item;
    }
    
    private void onCheckChanged(HighlitableDataItem item, AjaxRequestTarget target) {
    	// refresh row
    	item.toggleHighlite();
    	target.add(item);

    	// refresh first column header
    	target.add(headerAjaxCheckBox);
    	
    	// refresh bulk menu
    	updateSelectedObjects();
        new BulkMenuUpdateEvent(this, target).fire();
    }

    private void onHeaderCheckChanged(final boolean selectAll, final AjaxRequestTarget target) {
    	// refresh rows affected by changing
    	dataTable.visitChildren(HighlitableDataItem.class, new IVisitor<HighlitableDataItem, Void>() {

			@Override
			public void component(HighlitableDataItem object, IVisit<Void> visit) {
    			if (object.isHighlite() != selectAll) {
    				object.toggleHighlite();
    				target.add(object);
    			}
			}

    	});

    	// refresh bulk menu
    	updateSelectedObjects();
        new BulkMenuUpdateEvent(this, target).fire();
    }

    private void updateSelectedObjects() {
    	selectedObjects.clear();
    	dataTable.visitChildren(HighlitableDataItem.class, new IVisitor<HighlitableDataItem, Void>() {

			@Override
			public void component(HighlitableDataItem object, IVisit<Void> visit) {
    			if (object.isHighlite()) {
    				selectedObjects.add(object.getModelObject());
    			}

				visit.dontGoDeeper();
			}

    	});
    }
    
    protected boolean isCheckable(IModel<T> rowModel) {
    	return true;
    }
    
    private class CheckableTableColumn extends PropertyColumn<T> {

		private static final long serialVersionUID = 1L;

		public CheckableTableColumn(IModel<String> displayModel, String propertyExpressions) {
            super(displayModel, propertyExpressions);
        }

        @SuppressWarnings("unchecked")
		@Override
		public void populateItem(final Item<ICellPopulator<T>> item, String componentId, IModel<T> rowModel) {
        	final HighlitableDataItem highlitableDataItem = item.findParent(HighlitableDataItem.class);
        	IModel<Boolean> checkBoxModel = new LoadableDetachableModel<Boolean>() {

				private static final long serialVersionUID = 1L;

				@Override
				protected Boolean load() {
					return highlitableDataItem.isHighlite();
				}
        		
        	};
        	if (isCheckable(rowModel)) {
        		item.add(new CheckBoxColumnPanel(componentId, checkBoxModel));
        		item.add(AttributeModifier.replace("class", "checkboxColumn"));
        	} else {
        		item.add(new EmptyPanel(componentId));
        	}
		}                

		@Override
        public Component getHeader(String componentId) {
        	IModel<Boolean> checkBoxHeaderModel = new LoadableDetachableModel<Boolean>() {

				private static final long serialVersionUID = 1L;

				private boolean selected;
				
				@Override
				protected Boolean load() {
					selected = false;
		        	dataTable.visitChildren(HighlitableDataItem.class, new IVisitor<HighlitableDataItem, Void>() {

						@Override
						public void component(HighlitableDataItem object, IVisit<Void> visit) {
		        			if (!object.isHighlite()) {
		        				selected = false;
		        				return;
		        			}
		        			
		        			selected = true;
		        			
		    				visit.dontGoDeeper();
						}

		        	});

		        	return selected;
				}
        		
        	};

            return new CheckBoxHeaderPanel(componentId, checkBoxHeaderModel);
        }

    }

    abstract class CheckBoxPanel extends GenericPanel<Boolean> {

		private static final long serialVersionUID = 1L;

        public CheckBoxPanel(String id) {
			super(id);
			this.createComponents();
        }

        public CheckBoxPanel(String id, IModel<Boolean> model) {
			super(id, model);
			this.createComponents();
		}

		protected abstract void createComponents();

    }

    class CheckBoxHeaderPanel extends CheckBoxPanel {

		private static final long serialVersionUID = 1L;
		
        public CheckBoxHeaderPanel(String id, IModel<Boolean> model) {
            super(id, model);
        }

        protected void createComponents() {
            headerAjaxCheckBox = new AjaxCheckBox("select", getModel()) {

        		private static final long serialVersionUID = 1L;
        		
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                	onHeaderCheckChanged(getModelObject(), target);
                }

            };
            add(headerAjaxCheckBox);
        }

    }

    /*
    class CheckedHeaderModel extends AbstractCheckBoxModel {

		private static final long serialVersionUID = 1L;
		
        @Override
        public boolean isSelected() {
        	dataTable.visitChildren(HighlitableDataItem.class, new IVisitor<HighlitableDataItem>() {

    			public Object common(HighlitableDataItem common) {
        			if (!common.isHighlite()) {
        				return false;
        			}
        			
    				return IVisitor.CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER;
    			}

        	});

        	return true;
        }

		@Override
		public void select() {
			// TODO
		}

		@Override
		public void unselect() {
			// TODO
		}
        
    }
    */

    class CheckBoxColumnPanel extends CheckBoxPanel {

		private static final long serialVersionUID = 1L;
		
        public CheckBoxColumnPanel(String id, IModel<Boolean> model) {
            super(id, model);
        }

        protected void createComponents() {
            AjaxCheckBox ajaxCheckBox = new AjaxCheckBox("select", getModel()) {
            	
        		private static final long serialVersionUID = 1L;

                @SuppressWarnings("unchecked")
				@Override
                protected void onUpdate(AjaxRequestTarget target) {
                	HighlitableDataItem clickedItem = findParent(HighlitableDataItem.class);
                	onCheckChanged(clickedItem, target);
                }

            };
            add(ajaxCheckBox);
        }
        
    }

    /*
    class CheckedColumnModel extends AbstractCheckBoxModel {

		private static final long serialVersionUID = 1L;
		
		private HighlitableDataItem item;

        public CheckedColumnModel(HighlitableDataItem item) {
        	this.item = item;
        }

        @Override
        public boolean isSelected() {
            return item.isHighlite();
        }

        @Override
        public void select() {
        	// TODO
        }

        @Override
        public void unselect() {
            // TODO
        }

    }
    */
    
    class HighlitableDataItem extends Item<T> {

		private static final long serialVersionUID = 1L;
		
		private boolean highlite;

        public HighlitableDataItem(String id, int index, IModel<T> model) {
            super(id, index, model);
            
            setOutputMarkupId(true);
			if (isCheckable(model)) {
				add(new AttributeModifier("class", Model.of("tr-checked")) {

					private static final long serialVersionUID = 1L;

					@Override
					public boolean isEnabled(Component component) {
						return isHighlite();
					}

				});
			}
        }

        public void toggleHighlite() {
            highlite = !highlite;
        }
        
        public void setHighlite(boolean highlite) {
            this.highlite = highlite;
        }

        public boolean isHighlite() {
            return highlite;
        }
        
    }

}
