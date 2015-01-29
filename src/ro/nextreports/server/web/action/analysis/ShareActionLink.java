package ro.nextreports.server.web.action.analysis;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Link;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.PermissionUtil;
import ro.nextreports.server.util.ServerUtil;
import ro.nextreports.server.web.analysis.AnalysisBrowserPanel;
import ro.nextreports.server.web.analysis.AnalysisPanel;
import ro.nextreports.server.web.core.action.ActionAjaxLink;
import ro.nextreports.server.web.core.action.ActionContext;
import ro.nextreports.server.web.security.SecurityPanel;
import ro.nextreports.server.web.security.SecurityUtil;

public class ShareActionLink extends ActionAjaxLink {

	private ActionContext actionContext;

	@SpringBean
	private SecurityService securityService;
	
	@SpringBean
	private StorageService storageService;


	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	
	public void setStorageService(StorageService storageService) {
		this.storageService = storageService;
	}

	public ShareActionLink(ActionContext actionContext) {
		super(actionContext);
		this.actionContext = actionContext;
		Injector.get().inject(this);
	}

	public void executeAction(AjaxRequestTarget target) {
		Entity entity;
		try {
			entity = storageService.getEntityById(getAnalysisId());
			final AnalysisBrowserPanel panel = findParent(AnalysisBrowserPanel.class);
			panel.setWorkspace(new SecurityPanel("work", entity) {
				protected void onCancel(AjaxRequestTarget target) {
					panel.setWorkspace(new AnalysisPanel("work"), target);
				}
			}, target);
		} catch (NotFoundException e) {			
			e.printStackTrace();
		}		
	}

	@Override
	public boolean isVisible() {
		return SecurityUtil.hasPermission(securityService, PermissionUtil.getSecurity(), getAnalysisId());		
	}
	
	private String getAnalysisId() {
		Entity entity = actionContext.getEntity();
		String id;
		if (entity instanceof Link) {			 
		    id = ((Link)entity).getReference();       
		} else {
			id = entity.getId();
		}
		return id;
	}
	

}
