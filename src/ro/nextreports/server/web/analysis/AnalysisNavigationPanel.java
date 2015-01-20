package ro.nextreports.server.web.analysis;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.WindowClosedCallback;
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
import ro.nextreports.server.domain.Link;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.AnalysisService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.analysis.model.AnalysisAndLinksModel;
import ro.nextreports.server.web.analysis.model.SelectedAnalysisModel;
import ro.nextreports.server.web.common.behavior.SimpleTooltipBehavior;
import ro.nextreports.server.web.common.form.FormPanel;
import ro.nextreports.server.web.core.BasePage;
import ro.nextreports.server.web.core.section.SectionContext;
import ro.nextreports.server.web.core.section.SectionContextConstants;
import ro.nextreports.server.web.security.SecurityUtil;

public class AnalysisNavigationPanel extends Panel {

	private static final Logger LOG = LoggerFactory.getLogger(AnalysisNavigationPanel.class);

	@SpringBean
	private AnalysisService analysisService;
	
	@SpringBean
	private StorageService storageService;
	
	private String addedId = null;

	public AnalysisNavigationPanel(String id) {
		super(id);

		setOutputMarkupPlaceholderTag(true);
		
		addToolbar();

		WebMarkupContainer container = new WebMarkupContainer("navigation");
		ListView<Object> listView = new ListView<Object>("analysisList", new AnalysisAndLinksModel()) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<Object> item) {
				Tab tab = new Tab("analysis", item.getModel(), item.getIndex());
				item.add(tab);

				item.add(new AnalysisActionPanel("actions", item.getModel()));

				Object analysis = item.getModelObject();
				String analysisId = getAnalysisId(analysis);
				if (getSelectedAnalysisId().equals(analysisId)) {
					item.add(AttributeModifier.append("class", "selected"));
				}
				item.setOutputMarkupId(true);
			}

		};
		listView.setOutputMarkupId(true);

		container.add(listView);
		add(container);

		// we select default analysis only at first login, then we may select other analysis 
		// and we want that analysis to remain selected when we move between UI tabs
		SectionContext sectionContext = NextServerSession.get().getSectionContext(AnalysisSection.ID);		
		if (sectionContext.getData().get(SectionContextConstants.SELECTED_ANALYSIS_ID) == null) {

			String analysisId = "";
			
			List<Object> analysis = listView.getModelObject();
			if (analysis.size() > 0) {
				analysisId = getAnalysisId(analysis.get(0));
				sectionContext.getData().put(SectionContextConstants.SELECTED_ANALYSIS_ID, analysisId);
			}														

		}
	}
	
	private void addToolbar() {    	    	
    	
    	add(new AjaxLink<Void>("addAnalysis") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
                final ModalWindow dialog = findParent(BasePage.class).getDialog();
                dialog.setTitle(getString("AnalysisNavigationPanel.add"));
                dialog.setInitialWidth(350);
                dialog.setUseInitialHeight(false);                
                
				final AddAnalysisPanel panel = new AddAnalysisPanel() {
					@Override
					public void onOk(AjaxRequestTarget target) {						
						Analysis analysis = new Analysis();
						analysis.setName("Analysis " + UUID.randomUUID());
						analysis.setTableName(SecurityUtil.getLoggedUsername()+"-"+getSelectedTable());
						String path = analysisService.getAnalysisPath(analysis, SecurityUtil.getLoggedUsername());
						analysis.setPath(path);
						analysis.setRowsPerPage(20);
						addedId = analysisService.addAnalysis(analysis);						
						dialog.close(target);												
					}
				};
                dialog.setContent(new FormPanel<Void>(dialog.getContentId(), panel, true) {

					private static final long serialVersionUID = 1L;

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setOkButtonValue(getString("add"));
					}
	            	
	            });
                dialog.setWindowClosedCallback(new WindowClosedCallback() {					

					@Override
					public void onClose(AjaxRequestTarget target) {
						if (addedId != null) {														
							AnalysisBrowserPanel browserPanel = findParent(AnalysisBrowserPanel.class);
							SectionContext sectionContext = NextServerSession.get().getSectionContext(AnalysisSection.ID);
							sectionContext.getData().put(SectionContextConstants.SELECTED_ANALYSIS_ID, addedId);
							browserPanel.getAnalysisPanel().changeDataProvider(new SelectedAnalysisModel(), target);							
							target.add(browserPanel);
							addedId = null;
						}
					}
                	
                });
                dialog.show(target);
			}
			
		});
    }

	class Tab extends Fragment {

		private static final long serialVersionUID = 1L;

		public Tab(String id, final IModel<Object> model, int index) {
			super(id, "tab", AnalysisNavigationPanel.this);

			setOutputMarkupId(true);

			final Object analysis = model.getObject();

			add(createTitleLink(analysis, index));
		}
	}

	private AjaxLink createTitleLink(final Object analysis, int index) {

		String title = getTitle(analysis);

		AjaxLink<Void> titleLink = new AjaxLink<Void>("titleLink") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				 SectionContext sectionContext =  NextServerSession.get().getSectionContext(AnalysisSection.ID);
				 sectionContext.getData().put(SectionContextConstants.SELECTED_ANALYSIS_ID, getAnalysisId(analysis));
				 AnalysisBrowserPanel browserPanel = findParent(AnalysisBrowserPanel.class);
				 Analysis a = null;
				 if (isLink(analysis)) {
					 try {
						a = (Analysis)storageService.getEntityById(getAnalysisId(analysis));
					} catch (NotFoundException e) {					
						e.printStackTrace();
						LOG.error(e.getMessage(),e);
					}
				 } else {
					 a = (Analysis)analysis;
				 }
				 browserPanel.getAnalysisPanel().changeDataProvider(new Model<Analysis>(a), target);
				 target.add(browserPanel);

			}

		};

		IModel<String> linkImageModel = new LoadableDetachableModel<String>() {

			private static final long serialVersionUID = 1L;

			@Override
			protected String load() {
				String imagePath = "images/analysis.png";
				if (isLink(analysis)) {					
                    imagePath = "images/analysis_link.png";
                } else if (((Analysis)analysis).isFreezed()) {
                	imagePath = "images/analysis_freeze.png";
                }
				return imagePath;
			}

		};
		final ContextImage link = new ContextImage("titleImage", linkImageModel);
		titleLink.add(link);

		titleLink.add(new Label("title", title));
		titleLink.add(new SimpleTooltipBehavior(getTitle(analysis)));
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
	
	private String getAnalysisId(Object object) {
        if (isLink(object)) {
            return ((Link) object).getReference();
        }       
        return ((Analysis) object).getId();            
    }    
   	
	private String getTitle(Object object) {
    	String title;
        if (isLink(object)) {
            title = ((Link) object).getName();
        } else {
        	title = ((Analysis) object).getName();
        }        
        // TODO i18n maybe for DashboardService.MY_DASHBOARD_NAME        
        return title;             
    }
	
	private boolean isLink(Object object) {
    	return (object instanceof Link);
    }
    

}
