package ro.nextreports.server.web.action.analysis;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.Analysis;
import ro.nextreports.server.service.AnalysisService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.analysis.AnalysisBrowserPanel;
import ro.nextreports.server.web.analysis.ModifyAnalysisPanel;
import ro.nextreports.server.web.core.BasePage;
import ro.nextreports.server.web.core.action.ActionAjaxLink;
import ro.nextreports.server.web.core.action.ActionContext;

public class ModifyActionLink extends ActionAjaxLink {
	
	private AnalysisActionContext actionContext;
	
	@SpringBean
    private AnalysisService analysisService;
	
	@SpringBean
    private StorageService storageService;

    public void setAnaliysisService(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }        

	public void setStorageService(StorageService storageService) {
		this.storageService = storageService;
	}

	public ModifyActionLink(AnalysisActionContext actionContext) {
		super(actionContext);
		this.actionContext = actionContext;
		Injector.get().inject(this);
	}

	public void executeAction(AjaxRequestTarget target) {
		
    	final ModalWindow dialog = findParent(BasePage.class).getDialog();
    	final AnalysisBrowserPanel dp = findParent(AnalysisBrowserPanel.class);
        dialog.setTitle(new StringResourceModel("ActionContributor.Rename.name", null).getString());
        dialog.setInitialWidth(350);
        dialog.setUseInitialHeight(false);
        final Analysis analysis = (Analysis)actionContext.getEntity();               
        
        final ModifyAnalysisPanel modifyAnalysisPanel = new ModifyAnalysisPanel(dialog.getContentId(), 
        		new Model<Analysis>(analysis)) {

			private static final long serialVersionUID = 1L;

			@Override
            public void onModify(AjaxRequestTarget target) {
                ModalWindow.closeCurrent(target);                
                analysis.setName(getTitle());
                storageService.modifyEntity(analysis);                
                target.add(dp);                
            }
			
			@Override
			public boolean onVerify(AjaxRequestTarget target) {			
				if (!analysis.getName().equals(getTitle())) {
					if (storageService.entityExists( StorageConstants.ANALYSIS_ROOT + "/" + NextServerSession.get().getUsername() + "/" + getTitle())) {
						error(new StringResourceModel("AnalysisPopupMenuModel.modifyExists", null).getString());
						return false;
					}
				}				
				return true;
			}

            @Override
            public void onCancel(AjaxRequestTarget target) {
                ModalWindow.closeCurrent(target);
            }

        };
        
        dialog.setContent(modifyAnalysisPanel);
        dialog.show(target);         
	}
	
	@Override
	public boolean isVisible() {
		if (actionContext.isAnalysisLink()) {
			return false;
		}		
		return true;
	}
			
}
