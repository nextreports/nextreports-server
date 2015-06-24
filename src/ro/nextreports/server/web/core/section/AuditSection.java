package ro.nextreports.server.web.core.section;

import org.apache.wicket.markup.html.panel.Panel;

import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.core.audit.AuditPanel;

public class AuditSection implements Section {
	
	public static final String ID = AuditSection.class.getName();

	@Override
	public String getId() {		
		return ID;
	}

	@Override
	public String getTitle() {		
		return "Audit";
	}

	@Override
	public String getIcon() {
		//return "images/audit.png";
		return "binoculars";
	}

	@Override
	public Panel createView(String viewId) {
		return new AuditPanel(viewId);
	}

	@Override
	public boolean isVisible() {		
		return NextServerSession.get().isAdmin();
	}

}
