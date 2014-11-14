package ro.nextreports.server.web.action.dashboard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.core.BasePage;
import ro.nextreports.server.web.core.action.ActionAjaxLink;
import ro.nextreports.server.web.dashboard.Dashboard;
import ro.nextreports.server.web.dashboard.DashboardBrowserPanel;
import ro.nextreports.server.web.dashboard.DashboardUtil;
import ro.nextreports.server.web.dashboard.ModifyDashboardPanel;
import ro.nextreports.server.web.dashboard.Widget;

public class ModifyActionLink extends ActionAjaxLink {
	
	private DashboardActionContext actionContext;
	
	@SpringBean
    private DashboardService dashboardService;
	
	@SpringBean
    private StorageService storageService;

    public void setDashboardService(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }        

	public void setStorageService(StorageService storageService) {
		this.storageService = storageService;
	}

	public ModifyActionLink(DashboardActionContext actionContext) {
		super(actionContext);
		this.actionContext = actionContext;
		Injector.get().inject(this);
	}

	public void executeAction(AjaxRequestTarget target) {
		Dashboard board = null;
		try {
			// refresh widgets
			board = dashboardService.getDashboard(actionContext.getEntity().getId());
		} catch (NotFoundException e) {
			e.printStackTrace();
		}				 
		final Dashboard d = board; 
    	final ModalWindow dialog = findParent(BasePage.class).getDialog();
    	final DashboardBrowserPanel dp = findParent(DashboardBrowserPanel.class);
        dialog.setTitle(new StringResourceModel("DashboardPopupMenuModel.modifyTitle", null).getString());
        dialog.setInitialWidth(350);
        dialog.setUseInitialHeight(false);
                        
        final ModifyDashboardPanel modifyDashboardPanel = new ModifyDashboardPanel(dialog.getContentId(), 
        		new Model<Dashboard>(d)) {

			private static final long serialVersionUID = 1L;

			@Override
            public void onModify(AjaxRequestTarget target) {
                ModalWindow.closeCurrent(target);
                d.setColumnCount(getColumnCount());  
                d.setTitle(getTitle());
                dashboardService.modifyDashboard(d);
                String titleSelected = DashboardUtil.getDashboard(DashboardUtil.getSelectedDashboardId(), dashboardService).getTitle();
                if (titleSelected.equals(getTitle())) {
                	target.add(dp);
                }
            }
			
			@Override
			public boolean onVerify(AjaxRequestTarget target) {			
				if (!d.getTitle().equals(getTitle())) {
					if (storageService.entityExists( StorageConstants.DASHBOARDS_ROOT + "/" + NextServerSession.get().getUsername() + "/" + getTitle())) {
						error(new StringResourceModel("DashboardPopupMenuModel.modifyExists", null).getString());
						return false;
					}
				}
				 for (Widget widget : d.getWidgets()) {	     							 	
                    	if (getColumnCount() < widget.getColumn()+1) {	                        		
                    		error(new StringResourceModel("DashboardPopupMenuModel.modifyAsk", null).getString());                        		   
                            return false;
                    	}
                    }
				return true;
			}

            @Override
            public void onCancel(AjaxRequestTarget target) {
                ModalWindow.closeCurrent(target);
            }

        };
        dialog.setContent(modifyDashboardPanel);
        dialog.show(target);         
	}
	
	@Override
	public boolean isVisible() {
		if (actionContext.isDashboardLink()) {
			return false;
		}		
		return true;
	}
		

}
