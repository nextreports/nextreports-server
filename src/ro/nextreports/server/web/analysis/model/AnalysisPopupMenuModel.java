package ro.nextreports.server.web.analysis.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Analysis;
import ro.nextreports.server.domain.Link;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.AnalysisService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.action.analysis.DefaultAnalysisActionContext;
import ro.nextreports.server.web.analysis.AnalysisSection;
import ro.nextreports.server.web.common.menu.MenuItem;
import ro.nextreports.server.web.common.menu.MenuPanel;
import ro.nextreports.server.web.core.action.ActionContext;
import ro.nextreports.server.web.core.action.ActionContributor;
import ro.nextreports.server.web.core.action.DefaultActionContext;
import ro.nextreports.server.web.core.section.SectionManager;

public class AnalysisPopupMenuModel extends LoadableDetachableModel<List<MenuItem>> {
	
	private static final long serialVersionUID = 1L;
	
	@SpringBean
	private SectionManager sectionManager;

	@SpringBean
	private StorageService storageService;
	
	@SpringBean
	private AnalysisService analysisService;
			
	private IModel<Object> model;
	
	public AnalysisPopupMenuModel(IModel<Object> model) {
		this.model = model;
	}
	
	@Override
	protected List<MenuItem> load() {
		Injector.get().inject(this);
        List<MenuItem> menuItems = new ArrayList<MenuItem>();
        Object analysis = model.getObject();
        
        AnalysisSection analysisSection = (AnalysisSection)sectionManager.getSection(AnalysisSection.ID);
        List<ActionContributor> popupContributors = analysisSection.getPopupContributors();
        if (popupContributors != null) {
        	for (ActionContributor contributor : popupContributors) {    
        			if (contributor.isVisible()) {
	        			AbstractLink link = contributor.getLink(createActionContext(analysis));
	        			if (link.isVisible()) {
	        				menuItems.add(new MenuItem(link, contributor.getActionName(),  contributor.getActionImage()));
	        			}
        			}
        	}        	
        }
                       
        //MenuItem menuItem = new MenuItem("images/" + ThemesManager.getActionImage(storageService.getSettings().getColorTheme()), null);
        MenuItem menuItem = new MenuItem("images/actions.png", null);
        menuItem.setMenuItems(menuItems);
        
        return Arrays.asList(menuItem);
	}		

	private ActionContext createActionContext(Object analysis) {	
		final String analysisId = getAnalysisId(analysis);
        final String title = getTitle(analysis);
		
    	DefaultAnalysisActionContext actionContext = new DefaultAnalysisActionContext();
        actionContext.setLinkId(MenuPanel.LINK_ID); 

        Analysis a = null;
        try {        	
            a = (Analysis) storageService.getEntityById(analysisId);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        actionContext.setEntity(a);        
        actionContext.setAnalysisLink(isLink(analysis));
        return actionContext;
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
		return title;
	}

	private boolean isLink(Object object) {
		return (object instanceof Link);
	}
   
}
