package ro.nextreports.server.web.common.table;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.TextFilteredPropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import ro.nextreports.server.web.common.misc.AjaxConfirmLink;
import ro.nextreports.server.web.common.table.LinkPropertyColumn.LinkPanel;

public abstract class LinkTextFilteredPropertyColumn<T> extends TextFilteredPropertyColumn<T, String, String> {
	
	private static final long serialVersionUID = 1L;
	private IModel labelModel;
	private IModel<String> confirmModel;

	@SuppressWarnings("unchecked")
	public LinkTextFilteredPropertyColumn(IModel displayModel, IModel labelModel) {
		super(displayModel, null);
		this.labelModel = labelModel;
	}
	
	@SuppressWarnings("unchecked")
	public LinkTextFilteredPropertyColumn(IModel displayModel, IModel labelModel, IModel<String> confirmModel) {
		super(displayModel, null);
		this.labelModel = labelModel;
		this.confirmModel = confirmModel;
	}

	@SuppressWarnings("unchecked")
	public LinkTextFilteredPropertyColumn(IModel displayModel, String sortProperty,
			String propertyExpression) {
		super(displayModel, sortProperty, propertyExpression);
	}

	@SuppressWarnings("unchecked")
	public LinkTextFilteredPropertyColumn(IModel displayModel, String propertyExpressions) {
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

			AjaxLink link;
			if (confirmModel == null) {
				link = new AjaxLink("link") {

					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget ajaxRequestTarget) {
						LinkTextFilteredPropertyColumn.this.onClick(item, componentId, model, ajaxRequestTarget);

					}
				};
				add(link);
			} else {
			    link = new AjaxConfirmLink("link", confirmModel.getObject()) {

					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget ajaxRequestTarget) {
						LinkTextFilteredPropertyColumn.this.onClick(item, componentId, model, ajaxRequestTarget);

					}
				};
				add(link);
			}

			IModel tmpLabelModel = labelModel;
			if (labelModel == null) {
				tmpLabelModel = getDataModel(model);
			}

			link.add(new Label("label", tmpLabelModel));
		}
	}
}