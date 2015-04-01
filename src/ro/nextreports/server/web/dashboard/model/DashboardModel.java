package ro.nextreports.server.web.dashboard.model;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.web.dashboard.Dashboard;
import ro.nextreports.server.web.dashboard.DashboardUtil;

public class DashboardModel extends LoadableDetachableModel<Dashboard> {
	
	private static final long serialVersionUID = 1L;
	
	@SpringBean
	private DashboardService dashboardService;
	private String dashboardId;
	
	public DashboardModel(String dashboardId) {
		Injector.get().inject(this);
		this.dashboardId = dashboardId;
	}
	
	@Override
	protected Dashboard load() {		
		return DashboardUtil.getDashboard(dashboardId, dashboardService);		
	}		

}
