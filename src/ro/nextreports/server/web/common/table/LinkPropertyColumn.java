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
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

public abstract class LinkPropertyColumn<T> extends PropertyColumn<T, String> {
		
	private static final long serialVersionUID = 1L;
	private IModel labelModel;

	@SuppressWarnings("unchecked")
	public LinkPropertyColumn(IModel displayModel, IModel labelModel) {
		super(displayModel, null);
		
		this.labelModel = labelModel;
	}

	@SuppressWarnings("unchecked")
	public LinkPropertyColumn(IModel displayModel, String sortProperty,
			String propertyExpression) {
		super(displayModel, sortProperty, propertyExpression);
	}

	@SuppressWarnings("unchecked")
	public LinkPropertyColumn(IModel displayModel, String propertyExpressions) {
		super(displayModel, propertyExpressions);
	}

	@Override
	public void populateItem(Item item, String componentId, IModel model) {
		item.add(new LinkPanel(item, componentId, model));
	}
	
	public abstract void onClick(Item item, String componentId, IModel model, AjaxRequestTarget ajaxRequestTarget);

	public class LinkPanel extends Panel {
		
		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unchecked")
		public LinkPanel(final Item item, final String componentId,	final IModel model) {
			super(componentId);

			AjaxLink link = new AjaxLink("link") {
								
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget ajaxRequestTarget) {
					LinkPropertyColumn.this.onClick(item, componentId, model, ajaxRequestTarget);
					
				}
			};			

			add(link);

			IModel tmpLabelModel = labelModel;
			if (labelModel == null) {
				tmpLabelModel = createLabelModel(model);
			}

			link.add(new Label("label", tmpLabelModel));
		}
	}
}
