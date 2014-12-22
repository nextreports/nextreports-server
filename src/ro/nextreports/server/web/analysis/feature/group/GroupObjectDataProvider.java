package ro.nextreports.server.web.analysis.feature.group;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class GroupObjectDataProvider extends SortableDataProvider<String, String> {
	
	private LinkedList<String> groups;	
		
	public GroupObjectDataProvider(IModel<LinkedList<String>> groupModel) {
		Injector.get().inject(this);
		groups = groupModel.getObject();		
	}

	@Override
	public Iterator<? extends String> iterator(long first, long count) {		
		return getGroupObjects().subList((int)first, (int)(first + Math.min(count, size()))).iterator();
	}

	@Override
	public IModel<String> model(String groupObject) {		
		return new Model<String>(groupObject);
	}

	@Override
	public long size() {		
		return getGroupObjects().size();
	}

	private List<String> getGroupObjects() {
		List<String> groupObjects = new ArrayList<String>();
		for (int i=0, size=groups.size(); i<size; i++) {			
			groupObjects.add(groups.get(i));
		}
		return groupObjects;
	}	
}

