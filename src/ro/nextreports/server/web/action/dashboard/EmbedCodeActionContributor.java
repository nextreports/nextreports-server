package ro.nextreports.server.web.action.dashboard;

import java.util.List;

import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.StringResourceModel;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.web.core.action.AbstractActionContributor;
import ro.nextreports.server.web.core.action.ActionContext;

public class EmbedCodeActionContributor extends AbstractActionContributor {

	public static final String ID = EmbedCodeActionContributor.class.getName();
	
	public boolean support(List<Entity> entities) {
		return true;
	}

	public String getActionImage() {
		return "images/embed_code.png";
	}

	public String getActionName() {
		return new StringResourceModel("WidgetPopupMenu.embeddedCode", null).getString();
	}

	public String getId() {
		return ID;
	}

	public AbstractLink getLink(final ActionContext actionContext) {
		return new EmbedCodeActionLink((DashboardActionContext)actionContext);
	}

}
