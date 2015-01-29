package ro.nextreports.server.web.analysis.feature.sort;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class SortObjectDataProvider extends SortableDataProvider<SortObject, String> {
	
	private ArrayList<String> sortProperty;
	private ArrayList<Boolean> ascending;
		
	public SortObjectDataProvider(IModel<ArrayList<String>> propertyModel, IModel<ArrayList<Boolean>> orderModel) {
		Injector.get().inject(this);
		sortProperty = propertyModel.getObject();
		ascending = orderModel.getObject();
	}

	@Override
	public Iterator<? extends SortObject> iterator(long first, long count) {		
		return getSortObjects().subList((int)first, (int)(first + Math.min(count, size()))).iterator();
	}

	@Override
	public IModel<SortObject> model(SortObject sortObject) {		
		return new Model<SortObject>(sortObject);
	}

	@Override
	public long size() {		
		return getSortObjects().size();
	}

	private List<SortObject> getSortObjects() {
		List<SortObject> sortObjects = new ArrayList<SortObject>();		
		for (int i=0, size=sortProperty.size(); i<size; i++) {
			SortObject so = new SortObject();
			so.setColumn(sortProperty.get(i));
			so.setOrder(ascending.get(i));
			sortObjects.add(so);
		}
		return sortObjects;
	}	
}
