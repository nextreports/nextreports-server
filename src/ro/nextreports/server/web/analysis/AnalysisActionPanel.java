package ro.nextreports.server.web.analysis;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import ro.nextreports.server.domain.Analysis;

public class AnalysisActionPanel extends Panel {
	
	private static final long serialVersionUID = 1L;

    public AnalysisActionPanel(String id, final IModel<Object> model) {
        super(id, model);

        //setRenderBodyOnly(true);
        add(new AnalysisPopupMenuPanel("menuPanel", model));
    }

}
