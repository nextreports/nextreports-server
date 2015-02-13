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
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Analysis;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.AnalysisUtil;
import ro.nextreports.server.web.analysis.util.DatabaseUtil;
import ro.nextreports.server.web.common.behavior.SimpleTooltipBehavior;
import ro.nextreports.server.web.common.form.FormContentPanel;
import ro.nextreports.server.web.common.form.FormPanel;

public class ColumnsPanel extends FormContentPanel<Analysis> {
	
	private List<String> choices;
	public ArrayList<String> columns = new ArrayList<String>();
	private ColumnsOrderBehavior orderBehavior;
	
	@SpringBean
    private StorageService storageService;   
		
	public ColumnsPanel(IModel<Analysis> model) {		
		super(FormPanel.CONTENT_ID);
		
		//System.out.println("@@@@@ " + model.getObject());
		
		setRenderBodyOnly(false);
		setOutputMarkupId(true);
		
		ContextImage urlImage = new ContextImage("infoImage","images/information.png");        
        urlImage.add(new SimpleTooltipBehavior(AnalysisUtil.getAnalysisInfo(model.getObject(), 5, storageService.getSettings())));
        add(urlImage);
	 
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
				target.add(ColumnsPanel.this);
			}
	    });	
	    
	    add(new AjaxLink<Analysis>("selectNone") {						
			@Override
			public void onClick(AjaxRequestTarget target) {
				columns = new ArrayList<String>();								
				target.add(ColumnsPanel.this);
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
