package ro.nextreports.server.web.analysis.feature.create;

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import ro.nextreports.server.domain.Analysis;
import ro.nextreports.server.domain.AnalysisDeclaredColumn;

public class DeclaredColumnObjectDataProvider extends SortableDataProvider<AnalysisDeclaredColumn, String> {
	
	private IModel<Analysis>  model;	
	
	public DeclaredColumnObjectDataProvider(IModel<Analysis> model) {
		Injector.get().inject(this);
		this.model = model;		
	}

	@Override
	public Iterator<? extends AnalysisDeclaredColumn> iterator(long first, long count) {		
		return getDeclaredColumnObjects().subList((int)first, (int)(first + Math.min(count, size()))).iterator();
	}

	@Override
	public IModel<AnalysisDeclaredColumn> model(AnalysisDeclaredColumn columnObject) {		
		return new Model<AnalysisDeclaredColumn>(columnObject);
	}

	@Override
	public long size() {		
		return getDeclaredColumnObjects().size();
	}

	private List<AnalysisDeclaredColumn> getDeclaredColumnObjects() {
		return model.getObject().getDeclaredColumns();
	}	
}
