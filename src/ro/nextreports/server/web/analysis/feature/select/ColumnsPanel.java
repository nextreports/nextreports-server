package ro.nextreports.server.web.analysis.feature.select;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import ro.nextreports.server.domain.Analysis;
import ro.nextreports.server.web.analysis.util.DatabaseUtil;
import ro.nextreports.server.web.common.form.FormContentPanel;
import ro.nextreports.server.web.common.form.FormPanel;

public class ColumnsPanel extends FormContentPanel<Analysis> {
	
	private List<String> choices;
	public ArrayList<String> columns = new ArrayList<String>();
	private ColumnsOrderBehavior orderBehavior;
		
	public ColumnsPanel(IModel<Analysis> model) {		
		super(FormPanel.CONTENT_ID);
		
		System.out.println("@@@@@ " + model.getObject());
		
		setRenderBodyOnly(false);
		setOutputMarkupId(true);
	 
		columns = model.getObject().getSelectedColumns();
		choices = new LinkedList<String>();				
		choices.addAll(model.getObject().getColumns());
		
		add(new Label("info", new StringResourceModel("ColumnsPanel.info", null, null)));
		
		final CheckBoxMultipleChoice<String> listChoice = new CheckBoxMultipleChoice<String>("columns", new PropertyModel(this, "columns"), choices,
				new ChoiceRenderer<String>() {
					@Override
					public Object getDisplayValue(String fullColumnName) {
						return DatabaseUtil.getColumnAlias(fullColumnName);
					}								
		});
		listChoice.setOutputMarkupId(true);
		// add class to allow for changing layout from vertical to horizontal
		listChoice.setPrefix("<li class=\"analysisChoice\">");
		listChoice.setSuffix("</li>"); 
	    add(listChoice);		
	    
	    
	    add(new AjaxLink<Analysis>("selectAll") {						
			@Override
			public void onClick(AjaxRequestTarget target) {
				columns = new ArrayList<String>(choices);
				System.out.println("*** select all = " + choices);				
				target.add(listChoice);
			}
	    });	
	    
	    add(new AjaxLink<Analysis>("selectNone") {						
			@Override
			public void onClick(AjaxRequestTarget target) {
				columns = new ArrayList<String>();								
				target.add(listChoice);
			}
	    });	
	    
	    add(orderBehavior = new ColumnsOrderBehavior() {
	        @Override
	        public void onResponse(int oldIndex, int newIndex, AjaxRequestTarget target) {	            
	            moveElement(choices, oldIndex, newIndex);	            
	            target.add(ColumnsPanel.this);
	        }
	    });
	}
	
	public List<String> getColumns() {
		return columns;
	}

	public void setColumns(ArrayList<String> columns) {
		this.columns = columns;
	}
	
	public List<String> getChoices() {
		return choices;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
	  response.render(OnDomReadyHeaderItem.forScript(
			  "var oldIndex, newIndex; " +
	          "$(function() { $(\"#sortable\").sortable({ " +
			  		"start : function(event, ui){ " + 
		  				" oldIndex = ui.item.index();" +
        			"}, " +
        			"update : function(event, ui){ " + 
        				" newIndex = ui.item.index(); " +
        			    orderBehavior.getJavascript() +
        			"}" +
    			"} );  $(\"#sortable\").disableSelection(); });"));
	}
	
	private void moveElement(List<String> list, int oldPos, int newPos) {
		if (oldPos == newPos) {
			return;
		}
		String oldElement = new String(list.get(oldPos));
		list.remove(oldPos);
		list.add(newPos, oldElement);
	}

}
