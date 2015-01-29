package ro.nextreports.server.web.analysis.feature.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import ro.nextreports.server.domain.AnalysisFilter;

public class FilterObjectDataProvider extends SortableDataProvider<AnalysisFilter, String> {
	
	private ArrayList<AnalysisFilter> filters;	
		
	public FilterObjectDataProvider(IModel<ArrayList<AnalysisFilter>> filterModel) {
		Injector.get().inject(this);
		this.filters = filterModel.getObject();		
	}

	@Override
	public Iterator<? extends AnalysisFilter> iterator(long first, long count) {		
		return getFilterObjects().subList((int)first, (int)(first + Math.min(count, size()))).iterator();
	}

	@Override
	public IModel<AnalysisFilter> model(AnalysisFilter filterObject) {		
		return new Model<AnalysisFilter>(filterObject);
	}

	@Override
	public long size() {		
		return getFilterObjects().size();
	}

	private List<AnalysisFilter> getFilterObjects() {
		return filters;
	}	
}
