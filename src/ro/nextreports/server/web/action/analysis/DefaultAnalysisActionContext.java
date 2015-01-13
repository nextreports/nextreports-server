package ro.nextreports.server.web.action.analysis;

import ro.nextreports.server.web.core.action.DefaultActionContext;

public class DefaultAnalysisActionContext extends DefaultActionContext implements AnalysisActionContext {
	
	private boolean isAnalysisLink;

	@Override
	public boolean isAnalysisLink() {		
		return isAnalysisLink;
	}

	public void setAnalysisLink(boolean isAnalysisLink) {
		this.isAnalysisLink = isAnalysisLink;
	}		

}