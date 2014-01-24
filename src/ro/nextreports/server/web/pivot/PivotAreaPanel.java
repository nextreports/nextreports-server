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
package ro.nextreports.server.web.pivot;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.odlabs.wiquery.ui.sortable.SortableBehavior;

import ro.nextreports.server.pivot.Aggregator;
import ro.nextreports.server.pivot.PivotField;
import ro.nextreports.server.pivot.PivotModel;

/**
 * @author Decebal Suiu
 */
public class PivotAreaPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private ListView<PivotField> values;
	private PivotField.Area area;
	
	public PivotAreaPanel(String id, PivotField.Area area) {
		super(id);

		this.area = area;
		
		add(new Label("name", area.getName().toUpperCase()));

		final ModalWindow modal = new ModalWindow("modal");
		modal.setTitle("Aggregator");		
		add(modal);
		
		WebMarkupContainer fieldsContainer = new WebMarkupContainer("fieldsContainer");
		fieldsContainer.setOutputMarkupId(true);
		fieldsContainer.setMarkupId("area-" + area.getName() + "-" + getSession().nextSequenceValue());
		add(fieldsContainer);
		
		values = new ListView<PivotField>("values") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<PivotField> item) {
				final IModel<PivotField> itemModel = item.getModel();
				PivotField pivotField = itemModel.getObject();
				String title = pivotField.getTitle();
				if (pivotField.getArea().equals(PivotField.Area.DATA)) {
					title += " (" + pivotField.getAggregator().getFunction().toUpperCase() + ")"; 
				}
				Label valueLabel = new Label("value", title);
				if (pivotField.isNumber()) {
					valueLabel.add(AttributeModifier.append("class", "label-info"));
//				} else {
//					valueLabel.add(AttributeModifier.append("class", "label-important"));
				}
				if (item.getModelObject().getArea().equals(PivotField.Area.DATA)) {
					valueLabel.add(new AjaxEventBehavior("onclick") {
	
						private static final long serialVersionUID = 1L;
	
						protected void onEvent(AjaxRequestTarget target) {
							final AggregatorPanel panel = new AggregatorPanel(modal.getContentId(), itemModel);
							modal.setUseInitialHeight(false);
							modal.setInitialWidth(200);							
							modal.setContent(panel);							
							/*
							modal.setWindowClosedCallback(new WindowClosedCallback() {
								
								private static final long serialVersionUID = 1L;

								public void onClose(AjaxRequestTarget target) {
									if (panel.isOkPressed()) {
										System.out.println(">>> " + itemModel.getObject().getAggregator());
									}
								}
								
							});
							*/
							modal.show(target);
						}
						
					});
					valueLabel.add(AttributeModifier.append("style", "cursor: pointer;"));
				}
				item.add(valueLabel);				
				item.setOutputMarkupId(true);
				item.setMarkupId("field-" + pivotField.getIndex());
			}
		};
		values.setOutputMarkupPlaceholderTag(true);
		fieldsContainer.add(values);

		// add dnd support
//		addSortableBehavior();
		
		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(PivotAreaPanel.class, "pivot.js")));
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		
		addSortableBehavior();
	}

	@Override
	protected void onBeforeRender() {
//		System.out.println("PivotAreaPanel.onBeforeRender() " + getMarkupId());
		IModel<List<PivotField>> model = new LoadableDetachableModel<List<PivotField>>() {

			private static final long serialVersionUID = 1L;

			@Override
			protected List<PivotField> load() {
				return getPivotModel().getFields(area);
			}
			
		};

		values.setModel(model);
		
		super.onBeforeRender();
	}

	private void addSortableBehavior() {
		StopSortableAjaxBehavior sortableAjaxBehavior = new StopSortableAjaxBehavior() {

			private static final long serialVersionUID = 1L;

			@Override
			public void onStop(Item[] items, AjaxRequestTarget target) {
				PivotModel pivotModel = getPivotModel();
				for (Item item : items) {
					PivotField pivotField = pivotModel.getField(item.fieldIndex);
					pivotField.setArea(PivotField.Area.getValue(item.areaName));
					pivotField.setAreaIndex(item.sortIndex);
				}
				send(getPage(), Broadcast.BREADTH, new AreaChangedEvent(target));
			}
			
		};
		SortableBehavior sortableBehavior = sortableAjaxBehavior.getSortableBehavior();
		sortableBehavior.setConnectWith(".fields-container");
		sortableBehavior.setHandle(".initiate");
		sortableBehavior.setCursor("move");
		sortableBehavior.setForcePlaceholderSize(true);
		sortableBehavior.setPlaceholder("pivot-placeholder");
		sortableBehavior.setOpacity(0.4f);
		
		get("fieldsContainer").add(sortableAjaxBehavior);
	}
	
	private PivotModel getPivotModel() {
		return findParent(PivotPanel.class).getPivotModel();
	}
	
	private class AggregatorPanel extends GenericPanel<PivotField> {

		private static final long serialVersionUID = 1L;

		private Aggregator aggregator;
//		private boolean okPressed;
		
		public AggregatorPanel(String id, final IModel<PivotField> model) {
			super(id, model);
			
//			okPressed = false;
			
			aggregator = model.getObject().getAggregator();
			
			List<Aggregator> aggregators = new ArrayList<Aggregator>();
			aggregators.add(Aggregator.get(Aggregator.SUM));
			aggregators.add(Aggregator.get(Aggregator.AVG));
			aggregators.add(Aggregator.get(Aggregator.MIN));
			aggregators.add(Aggregator.get(Aggregator.MAX));
			aggregators.add(Aggregator.get(Aggregator.COUNT));
			final DropDownChoice<Aggregator> aggregatorDownChoice = new DropDownChoice<Aggregator>("aggregator", 
					new PropertyModel<Aggregator>(this, "aggregator"), 
					aggregators,
					new ChoiceRenderer<Aggregator>("function"));
			aggregatorDownChoice.add(new OnChangeAjaxBehavior() {

				private static final long serialVersionUID = 1L;

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
				}
				
			});
			aggregatorDownChoice.setOutputMarkupId(true);
			add(aggregatorDownChoice);
			
			add(new AjaxLink<Void>("ok") {

				private static final long serialVersionUID = 1L;

				public void onClick(AjaxRequestTarget target) {
//					okPressed = true;
					getPivotModel().getField(model.getObject().getName()).setAggregator(aggregator);
					target.add(PivotAreaPanel.this);
					ModalWindow.closeCurrent(target);
				}
				
			});
		}
		
		/*
		public boolean isOkPressed() {
			return okPressed;
		}
		*/

	}
	
}
