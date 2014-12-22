package ro.nextreports.server.web.report;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

//@todo analysis remove
public class AnalysisRuntimePanel extends Panel {		

	public AnalysisRuntimePanel(String id, ReportRuntimeModel model) {
		super(id);

		Label label = new Label("analysisLabel", getString("Analysis.run.name"));
		add(label);
		
		TextField<String> analysisText = new TextField<String>("analysisText", new PropertyModel(model, "analysisName"));    	
    	add(analysisText);
    	
    	AjaxLink analysisLink = new AjaxLink("analysisLink") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				// TODO Auto-generated method stub
				
			}
    		
    	}; 
    	add(analysisLink);
	}	
	

}
