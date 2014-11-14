package ro.nextreports.server.web.action.dashboard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.common.menu.MenuPanel;
import ro.nextreports.server.web.common.misc.AjaxConfirmLink;
import ro.nextreports.server.web.core.action.ActionContext;
import ro.nextreports.server.web.core.section.SectionContext;
import ro.nextreports.server.web.core.section.SectionContextConstants;
import ro.nextreports.server.web.dashboard.DashboardBrowserPanel;
import ro.nextreports.server.web.dashboard.DashboardSection;

public class SetDefaultActionLink extends AjaxConfirmLink {
			
	@SpringBean
    private DashboardService dashboardService;

    public void setDashboardService(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
    
    private ActionContext actionContext;
	
	public SetDefaultActionLink(ActionContext actionContext) {
		super(MenuPanel.LINK_ID, new StringResourceModel("DashboardPopupMenuModel.defaultAsk", null, new Object[] { actionContext.getEntity().getName() }).getString());				
		this.actionContext = actionContext;		
		Injector.get().inject(this);
	}

	public void executeAction(AjaxRequestTarget target) {
		String id = actionContext.getEntity().getId();                   	
        dashboardService.setDefaultDashboard(id);                    
        SectionContext sectionContext = NextServerSession.get().getSectionContext(DashboardSection.ID);                        
        sectionContext.getData().put(SectionContextConstants.SELECTED_DASHBOARD_ID, id);                    
        target.add(findParent(DashboardBrowserPanel.class));
	}

	@Override
	public void onClick(AjaxRequestTarget target) {
		executeAction(target);		
	}

}
