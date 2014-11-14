package ro.nextreports.server.web.action.dashboard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.common.menu.MenuPanel;
import ro.nextreports.server.web.common.misc.AjaxConfirmLink;
import ro.nextreports.server.web.core.section.SectionContext;
import ro.nextreports.server.web.core.section.SectionContextConstants;
import ro.nextreports.server.web.dashboard.DashboardBrowserPanel;
import ro.nextreports.server.web.dashboard.DashboardSection;

public class DeleteActionLink extends AjaxConfirmLink {
	
	private DashboardActionContext actionContext;
	
	@SpringBean
    private DashboardService dashboardService;
		
    public void setDashboardService(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }        
	
	public DeleteActionLink(DashboardActionContext actionContext) {
		super(MenuPanel.LINK_ID, new StringResourceModel("DashboardPopupMenuModel.deleteAsk", null, 
					new Object[] { actionContext.getEntity().getName() }).getString());
		this.actionContext = actionContext;
		Injector.get().inject(this);
	}

	public void executeAction(AjaxRequestTarget target) {
		String id = actionContext.getEntity().getId();                	
        try {
			dashboardService.removeDashboard(id);
		} catch (NotFoundException e) {
			// TODO
			e.printStackTrace();						
		}

        if (id.equals(getSelectedDashboardId())) {
            SectionContext sectionContext = NextServerSession.get().getSectionContext(DashboardSection.ID);
            String _id = dashboardService.getMyDashboards().get(0).getId();
            sectionContext.getData().put(SectionContextConstants.SELECTED_DASHBOARD_ID, _id);
        }

        target.add(findParent(DashboardBrowserPanel.class));   
	}
	
	@Override
	public boolean isVisible() {
		if (actionContext.isDashboardLink() || DashboardService.MY_DASHBOARD_NAME.equals(actionContext.getEntity().getName())) {
			return false;
		}		
		return true;
	}
	
	@Override
	public void onClick(AjaxRequestTarget target) {
		executeAction(target);		
	}

	private String getSelectedDashboardId() {
        SectionContext sectionContext = NextServerSession.get().getSectionContext(DashboardSection.ID);
        return sectionContext.getData().getString(SectionContextConstants.SELECTED_DASHBOARD_ID);
    }

}
