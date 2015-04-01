package ro.nextreports.server.web.dashboard;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.core.UrlUtil;

public class DashboardEmbedCodePanel extends Panel {

	private static final long serialVersionUID = 1L;
	private FeedbackPanel feedbackPanel;
	private ErrorLoadableDetachableModel model;
	
	@SpringBean
	StorageService storageService;

	public DashboardEmbedCodePanel(String id, final String dashboardId) {
		super(id);						
						
		model = new ErrorLoadableDetachableModel(dashboardId);
		final Label codeLabel = new Label("code", model);
		codeLabel.setEscapeModelStrings(false);		
		codeLabel.setOutputMarkupId(true);
		add(codeLabel);					
				
		feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);
							
	}	
	
	private String getCode(String dashboardId, boolean error) {
		if (error) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		String url = UrlUtil.getAppBaseUrl(storageService).
		    append("dashboard?dashboardId=").append(dashboardId).		   
		    toString();
		sb.append(url);
				
		return sb.toString();
	}
	
	private class ErrorLoadableDetachableModel extends LoadableDetachableModel<String> {
		
		private static final long serialVersionUID = 1L;
		
		private boolean error = false;
		private String dashboardId;
		
		public ErrorLoadableDetachableModel(String dashboardId) {
			super();
			
			this.dashboardId = dashboardId;
		}
		
		public void setError(boolean error) {
			this.error = error;
		}
		
		@Override
		protected String load() {				
			return getCode(dashboardId, error);
		}		
		
	}

}
