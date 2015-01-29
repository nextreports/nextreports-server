package ro.nextreports.server.web.analysis.feature.paging;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.validator.RangeValidator;

import ro.nextreports.server.domain.Analysis;
import ro.nextreports.server.web.common.form.FormContentPanel;
import ro.nextreports.server.web.common.form.FormPanel;

public class PaginatePanel extends FormContentPanel<Analysis> {
	
	public PaginatePanel(IModel<Analysis> model) {		
		super(FormPanel.CONTENT_ID);
		
		add(new Label("info", new StringResourceModel("PaginatePanel.info", null, null)));
		
		add(new Label("rows",  new StringResourceModel("PaginatePanel.rows", this, null)));
		TextField<Integer> rowsText = new TextField<Integer>("rowsText", new PropertyModel<Integer>(model.getObject(), "rowsPerPage"));
		rowsText.add(RangeValidator.range(1, 500));
		rowsText.setLabel(new StringResourceModel("PaginatePanel.rows", this, null));
 		add(rowsText);		 		 		
	}	
}
