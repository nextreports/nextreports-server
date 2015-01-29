package ro.nextreports.server.web.analysis;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.html.panel.Panel;

import ro.nextreports.server.domain.Entity;

public class AnalysisBrowserPanel extends GenericPanel<Entity> {
	
	private static final long serialVersionUID = 1L;

	private AnalysisNavigationPanel analysisNavigationPanel;
	private AnalysisPanel analysisPanel;

    private WebMarkupContainer workContainer;
	
    public AnalysisBrowserPanel(String id) {
		super(id);
		
		setOutputMarkupId(true);
		
		workContainer = new WebMarkupContainer("workContainer");
        workContainer.setOutputMarkupId(true);		
        
        analysisNavigationPanel = new AnalysisNavigationPanel("navigation");
		add(analysisNavigationPanel);
		
		analysisPanel = new AnalysisPanel("work");
        workContainer.add(analysisPanel);
        add(workContainer);
    }
    
    public AnalysisNavigationPanel getAnalysisNavigationPanel() {
		return analysisNavigationPanel;
	}

	public AnalysisPanel getAnalysisPanel() {
    	return analysisPanel;
    }

    public void setWorkspace(Panel panel, AjaxRequestTarget target) {
        workContainer.replace(panel);
        target.add(workContainer);
    }
    
}
