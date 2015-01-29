package ro.nextreports.server.web.analysis.model;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Analysis;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.analysis.AnalysisSection;
import ro.nextreports.server.web.core.section.SectionContext;
import ro.nextreports.server.web.core.section.SectionContextConstants;

public class SelectedAnalysisModel extends LoadableDetachableModel<Analysis> {
	
	private static final long serialVersionUID = 1L;
	
	@SpringBean
	private StorageService storageService;
	
	public SelectedAnalysisModel() {
		Injector.get().inject(this);
	}
	
	@Override
	protected Analysis load() {		
		SectionContext sectionContext = NextServerSession.get().getSectionContext(AnalysisSection.ID);
		String id = sectionContext.getData().getString(SectionContextConstants.SELECTED_ANALYSIS_ID);	
		if (id == null) {
			return null;
		}
		try {
			return (Analysis)storageService.getEntityById(id);
		} catch (NotFoundException e) {						
			return null;
		}		
	}		

}
