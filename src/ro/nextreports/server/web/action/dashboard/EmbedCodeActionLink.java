package ro.nextreports.server.web.action.dashboard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.web.core.BasePage;
import ro.nextreports.server.web.core.action.ActionAjaxLink;
import ro.nextreports.server.web.core.action.ActionContext;
import ro.nextreports.server.web.dashboard.DashboardEmbedCodePanel;

public class EmbedCodeActionLink extends ActionAjaxLink {
	
	private ActionContext actionContext;
	
	@SpringBean
    private SecurityService securityService;

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

	public EmbedCodeActionLink(ActionContext actionContext) {
		super(actionContext);
		this.actionContext = actionContext;
		Injector.get().inject(this);
	}

	public void executeAction(AjaxRequestTarget target) {
		Entity entity = getActionContext().getEntity();
		ModalWindow dialog = findParent(BasePage.class).getDialog();

		dialog.setTitle(new StringResourceModel("WidgetPopupMenu.embeddedCode", null).getString());
		dialog.setInitialWidth(550);
		dialog.setUseInitialHeight(false);

		dialog.setContent(new DashboardEmbedCodePanel(dialog.getContentId(), entity.getId()));
		dialog.show(target);
	}

}
