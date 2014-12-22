package ro.nextreports.server.web.analysis;

import org.apache.wicket.model.IModel;

import ro.nextreports.server.domain.Analysis;
import ro.nextreports.server.web.analysis.model.AnalysisPopupMenuModel;
import ro.nextreports.server.web.common.menu.MenuPanel;

public class AnalysisPopupMenuPanel extends MenuPanel {

	private static final long serialVersionUID = 1L;
	
	public AnalysisPopupMenuPanel(String id, IModel<Analysis> model) {
		super(id, new AnalysisPopupMenuModel(model));
	}

}
