package ro.nextreports.server.web.analysis.model;

import java.util.List;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Analysis;
import ro.nextreports.server.service.AnalysisService;

public class AnalysisModel extends LoadableDetachableModel<List<Analysis>> {

	private static final long serialVersionUID = 1L;
	
	@SpringBean
	private AnalysisService analysisService;
	
	public AnalysisModel() {
		Injector.get().inject(this);
	}
	
	@Override
	protected List<Analysis> load() {
		return analysisService.getMyAnalysis();							
	}

}
