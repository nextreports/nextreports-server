package ro.nextreports.server.web.action.dashboard;

import ro.nextreports.server.web.core.action.DefaultActionContext;

public class DefaultDashboardActionContext extends DefaultActionContext implements DashboardActionContext {
	
	private boolean isDashboardLink;

	@Override
	public boolean isDashboardLink() {		
		return isDashboardLink;
	}

	public void setDashboardLink(boolean isDashboardLink) {
		this.isDashboardLink = isDashboardLink;
	}		

}
