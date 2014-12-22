package ro.nextreports.server.web.analysis;

import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.domain.Analysis;
import ro.nextreports.server.service.AnalysisService;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.analysis.model.AnalysisModel;
import ro.nextreports.server.web.common.behavior.SimpleTooltipBehavior;
import ro.nextreports.server.web.core.section.SectionContext;
import ro.nextreports.server.web.core.section.SectionContextConstants;

public class AnalysisNavigationPanel extends Panel {

	private static final Logger LOG = LoggerFactory.getLogger(AnalysisNavigationPanel.class);

	@SpringBean
	private AnalysisService analysisService;

	public AnalysisNavigationPanel(String id) {
		super(id);

		setOutputMarkupPlaceholderTag(true);

		WebMarkupContainer container = new WebMarkupContainer("navigation");
		ListView<Analysis> listView = new ListView<Analysis>("analysisList", new AnalysisModel()) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<Analysis> item) {
				Tab tab = new Tab("analysis", item.getModel(), item.getIndex());
				item.add(tab);

				item.add(new AnalysisActionPanel("actions", item.getModel()));

				Analysis analysis = item.getModelObject();
				if (getSelectedAnalysisId().equals(analysis.getId())) {
					item.add(AttributeModifier.append("class", "selected"));
				}
				item.setOutputMarkupId(true);
			}

		};
		listView.setOutputMarkupId(true);

		container.add(listView);
		add(container);

		// we select default analysis only at first login, then we may select
		// other analysis
		// and we want that analysis to remain selected when we move between UI
		// tabs
		SectionContext sectionContext = NextServerSession.get().getSectionContext(AnalysisSection.ID);		
		if (sectionContext.getData().get(SectionContextConstants.SELECTED_ANALYSIS_ID) == null) {

			String analysisId = "";
			
			List<Analysis> analysis = listView.getModelObject();
			if (analysis.size() > 0) {
				analysisId = analysis.get(0).getId();
				sectionContext.getData().put(SectionContextConstants.SELECTED_ANALYSIS_ID, analysisId);
			}														

		}
	}

	class Tab extends Fragment {

		private static final long serialVersionUID = 1L;

		public Tab(String id, final IModel<Analysis> model, int index) {
			super(id, "tab", AnalysisNavigationPanel.this);

			setOutputMarkupId(true);

			final Analysis analysis = model.getObject();

			add(createTitleLink(analysis, index));
		}
	}

	private AjaxLink createTitleLink(final Analysis analysis, int index) {

		String title = analysis.getName();

		AjaxLink<Void> titleLink = new AjaxLink<Void>("titleLink") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				 SectionContext sectionContext =  NextServerSession.get().getSectionContext(AnalysisSection.ID);
				 sectionContext.getData().put(SectionContextConstants.SELECTED_ANALYSIS_ID, analysis.getId());
				 AnalysisBrowserPanel browserPanel = findParent(AnalysisBrowserPanel.class);
				 browserPanel.getAnalysisPanel().changeDataProvider(new Model<Analysis>(analysis), target);
				 target.add(browserPanel);

			}

		};

		IModel<String> linkImageModel = new LoadableDetachableModel<String>() {

			private static final long serialVersionUID = 1L;

			@Override
			protected String load() {
				String imagePath = "images/analysis.png";
				return imagePath;
			}

		};
		final ContextImage link = new ContextImage("titleImage", linkImageModel);
		titleLink.add(link);

		titleLink.add(new Label("title", title));
		titleLink.add(new SimpleTooltipBehavior(analysis.getName()));
		return titleLink;
	}

	private String getSelectedAnalysisId() {
		SectionContext sectionContext = NextServerSession.get().getSectionContext(AnalysisSection.ID);
		String result = sectionContext.getData().getString(SectionContextConstants.SELECTED_ANALYSIS_ID);
		if (result == null) {
			result = "";
		}
		return result;
	}
	
	@Override
    public boolean isVisible() {
		Map<String, String> preferences = NextServerSession.get().getPreferences();
		if (!preferences.containsKey("analysis.navigationToggle")) {
			return true;
		}		
		return Boolean.parseBoolean(preferences.get("analysis.navigationToggle"));
    }
    

}
