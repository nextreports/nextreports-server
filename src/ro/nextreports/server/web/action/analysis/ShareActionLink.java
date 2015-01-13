package ro.nextreports.server.web.action.analysis;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.util.PermissionUtil;
import ro.nextreports.server.util.ServerUtil;
import ro.nextreports.server.web.analysis.AnalysisBrowserPanel;
import ro.nextreports.server.web.analysis.AnalysisPanel;
import ro.nextreports.server.web.core.action.ActionAjaxLink;
import ro.nextreports.server.web.core.action.ActionContext;
import ro.nextreports.server.web.security.SecurityPanel;

public class ShareActionLink extends ActionAjaxLink {

	private ActionContext actionContext;

	@SpringBean
	private SecurityService securityService;

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public ShareActionLink(ActionContext actionContext) {
		super(actionContext);
		this.actionContext = actionContext;
		Injector.get().inject(this);
	}

	public void executeAction(AjaxRequestTarget target) {
		Entity entity = getActionContext().getEntity();
		final AnalysisBrowserPanel panel = findParent(AnalysisBrowserPanel.class);
		panel.setWorkspace(new SecurityPanel("work", entity) {
			protected void onCancel(AjaxRequestTarget target) {
				panel.setWorkspace(new AnalysisPanel("work"), target);
			}
		}, target);
	}

	@Override
	public boolean isVisible() {
		return hasSecurityPermission(actionContext.getEntity().getId());
	}

	private boolean hasSecurityPermission(String analysisId) {
		try {
			return securityService.hasPermissionsById(ServerUtil.getUsername(), PermissionUtil.getSecurity(), analysisId);
		} catch (NotFoundException e) {
			// TODO
			e.printStackTrace();
		}
		return false;
	}

}
