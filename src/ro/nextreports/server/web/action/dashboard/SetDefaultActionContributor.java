package ro.nextreports.server.web.action.dashboard;

import java.util.List;

import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.StringResourceModel;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.web.core.action.AbstractActionContributor;
import ro.nextreports.server.web.core.action.ActionContext;

public class SetDefaultActionContributor extends AbstractActionContributor {

	public static final String ID = SetDefaultActionContributor.class.getName();

	// does not matter for dashboards
	// Dashboards panel contains Dashboard and Link elements (which are not entities)
	public boolean support(List<Entity> entities) {
		return true;
	}

	public String getActionImage() {
		return "images/star.png";
	}

	public String getActionName() {
		return new StringResourceModel("DashboardPopupMenuModel.default", null).getString();
	}

	public String getId() {
		return ID;
	}

	public AbstractLink getLink(final ActionContext actionContext) {
		return new SetDefaultActionLink(actionContext);
	}

}
