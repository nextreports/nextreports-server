package ro.nextreports.server.web.action.schedule;

import javax.annotation.Resource;

import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.StringResourceModel;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.util.PermissionUtil;
import ro.nextreports.server.util.ServerUtil;
import ro.nextreports.server.web.core.action.ActionContext;
import ro.nextreports.server.web.core.action.SingleActionContributor;

public class CloneActionContributor extends SingleActionContributor {
	
	public static final String ID = CloneActionContributor.class.getName();

    @Resource
    private SecurityService securityService;

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public boolean support(Entity entity) {
        if (entity instanceof SchedulerJob) {
            try {
                if (securityService.hasPermissionsById(ServerUtil.getUsername(),
                        PermissionUtil.getRead(), entity.getId())) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        
        return false;
    }

    public String getActionImage() {
        return "images/schedule_clone.png";
    }

    public String getActionName() {
        return new StringResourceModel("ActionContributor.Clone.name", null).getString();
    }
    
    public String getId() {
    	return ID;
    }

    public AbstractLink getLink(ActionContext context) {
        return new CloneActionLink(context);
    }

}
