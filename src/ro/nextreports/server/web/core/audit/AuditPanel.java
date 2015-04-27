package ro.nextreports.server.web.core.audit;

import org.apache.wicket.markup.html.panel.Panel;

public class AuditPanel extends Panel {
	
	public AuditPanel(String id) {
		super(id);
		add(new InnerReportsPanel("innerReports"));		
	}

}
