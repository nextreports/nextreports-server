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
import ro.nextreports.server.service.AnalysisService;
import ro.nextreports.server.service.StorageService;
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
			
	private IModel<Analysis> model;
	
	public AnalysisPopupMenuModel(IModel<Analysis> model) {
		this.model = model;
	}
	
	@Override
	protected List<MenuItem> load() {
		Injector.get().inject(this);
        List<MenuItem> menuItems = new ArrayList<MenuItem>();
        Analysis analysis = model.getObject();
        
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

	private ActionContext createActionContext(Analysis analysis) {		
    	DefaultActionContext actionContext = new DefaultActionContext();
        actionContext.setLinkId(MenuPanel.LINK_ID);                
        actionContext.setEntity(analysis);        
        return actionContext;
	}		                    
    
   
}
