package ro.nextreports.server.web.analysis.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.service.AnalysisService;

public class AnalysisAndLinksModel extends LoadableDetachableModel<List<Object>> {

	private static final long serialVersionUID = 1L;
	
	@SpringBean
	private AnalysisService analysisService;
	
	public AnalysisAndLinksModel() {
		Injector.get().inject(this);
	}
	
	@Override
	protected List<Object> load() {						
		List<Object> entities = new ArrayList<Object>();
		entities.addAll(analysisService.getMyAnalysis());
		entities.addAll(analysisService.getAnalysisLinks());						
		return entities;
	}

}
