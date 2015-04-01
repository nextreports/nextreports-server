package ro.nextreports.server.web.integration;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.IFrameSettings;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.PermissionUtil;
import ro.nextreports.server.web.dashboard.DashboardPanel;
import ro.nextreports.server.web.dashboard.WidgetErrorView;
import ro.nextreports.server.web.dashboard.model.DashboardModel;
import ro.nextreports.server.web.security.SecurityUtil;

public class DashboardWebPage extends WebPage {

	private static final long serialVersionUID = 1L;
	
	@SpringBean
    private StorageService storageService;
	
	@SpringBean
    private DashboardService dashboardService;
	
	@SpringBean
    private SecurityService securityService;

	public DashboardWebPage(PageParameters pageParameters) {
		super(pageParameters);
		
		String dashboardId = pageParameters.get("dashboardId").toString();
		
		IFrameSettings iframeSettings = storageService.getSettings().getIframe();
		if ((iframeSettings == null) || (iframeSettings.isUseAuthentication() && (SecurityUtil.getLoggedUser() == null)) ) {
			add(new WidgetErrorView("panel", null, new Exception("You are not allowed to see iframe if you are not logged!")));
			return;
		}
		
		if (iframeSettings.isUseAuthentication()) {			
			try {				
				String user = SecurityUtil.getLoggedUsername();
				String owner = dashboardService.getDashboardOwner(dashboardId);
				if (!owner.equals(user)) {
					boolean hasRead = securityService.hasPermissionsById(user, PermissionUtil.getRead(), dashboardId);
					if (!hasRead) {
						add(new WidgetErrorView("panel", null, new Exception("You do not have rights to see this iframe!")));
						return;
					}
				}
			} catch (NotFoundException e) {
				add(new WidgetErrorView("panel", null, new Exception("Could not load iframe: " + e.getMessage())));
				return;
			}
		}
		
		add(new DashboardPanel("panel", new DashboardModel(dashboardId), false));
	}
	
}