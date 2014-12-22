package ro.nextreports.server.web.analysis;

import java.util.List;

import org.apache.wicket.markup.html.panel.Panel;

import ro.nextreports.server.web.core.action.ActionContributor;
import ro.nextreports.server.web.core.section.AbstractSection;

public class AnalysisSection extends AbstractSection {

	public static final String ID = AnalysisSection.class.getName();
	
	protected List<ActionContributor> popupContributors;
		
	public String getId() {
		return ID;
	}

	public String getTitle() {
		return "Analysis";
	}

	public String getIcon() {
		return "images/analysis.png";
	}	

	public Panel createView(String viewId) {
		return new AnalysisBrowserPanel(viewId);
	}
	
	public List<ActionContributor> getPopupContributors() {
		return popupContributors;
	}

	public void setPopupContributors(List<ActionContributor> popupContributors) {
		this.popupContributors = popupContributors;
	}

	public int getPopupContributorCount() {
		if ((popupContributors == null) || (popupContributors.size() == 0)) {
			return 0;
		}
		
		return popupContributors.size();
	}


}
