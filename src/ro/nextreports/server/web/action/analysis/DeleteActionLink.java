package ro.nextreports.server.web.action.analysis;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Analysis;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.AnalysisService;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.analysis.AnalysisBrowserPanel;
import ro.nextreports.server.web.analysis.AnalysisSection;
import ro.nextreports.server.web.analysis.model.SelectedAnalysisModel;
import ro.nextreports.server.web.common.menu.MenuPanel;
import ro.nextreports.server.web.common.misc.AjaxConfirmLink;
import ro.nextreports.server.web.core.action.ActionContext;
import ro.nextreports.server.web.core.section.SectionContext;
import ro.nextreports.server.web.core.section.SectionContextConstants;

public class DeleteActionLink extends AjaxConfirmLink {
	
	private ActionContext actionContext;
	
	@SpringBean
    private AnalysisService analysisService;
		
    public void setAnalysisService(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }        
	
	public DeleteActionLink(ActionContext actionContext) {
		super(MenuPanel.LINK_ID, new StringResourceModel("AnalysisPopupMenuModel.deleteAsk", null, 
					new Object[] { actionContext.getEntity().getName() }).getString());
		this.actionContext = actionContext;
		Injector.get().inject(this);
	}

	public void executeAction(AjaxRequestTarget target) {
		String id = actionContext.getEntity().getId();                	
        try {
        	analysisService.removeAnalysis(id);
		} catch (NotFoundException e) {
			// TODO
			e.printStackTrace();						
		}

        if (id.equals(getSelectedAnalysisId())) {
            SectionContext sectionContext = NextServerSession.get().getSectionContext(AnalysisSection.ID);
            List<Analysis> list = analysisService.getMyAnalysis();
            if (list.size() > 0) {            		
            	String _id = list.get(0).getId();
            	sectionContext.getData().put(SectionContextConstants.SELECTED_ANALYSIS_ID, _id);
            }
        }

        AnalysisBrowserPanel panel = findParent(AnalysisBrowserPanel.class);
        panel.getAnalysisPanel().changeDataProvider(new SelectedAnalysisModel(), target);
        target.add(panel);   
	}		
	
	@Override
	public void onClick(AjaxRequestTarget target) {
		executeAction(target);		
	}

	private String getSelectedAnalysisId() {
        SectionContext sectionContext = NextServerSession.get().getSectionContext(AnalysisSection.ID);
        return sectionContext.getData().getString(SectionContextConstants.SELECTED_ANALYSIS_ID);
    }

}
