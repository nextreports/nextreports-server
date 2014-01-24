package ro.nextreports.server.web.core.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.core.EntityModel;

/**
 * @author Decebal Suiu
 */
public class EntityTreeProvider implements ITreeProvider<Entity> {

	private static final long serialVersionUID = 1L;

	private String rootPath;
	
	@SpringBean
	private StorageService storageService;
	
	public EntityTreeProvider(String rootPath) {
		this.rootPath = rootPath;
		
		Injector.get().inject(this);
	}
	
	@Override
	public Iterator<? extends Entity> getRoots() {
		List<Entity> roots = new ArrayList<Entity>();
		try {
			roots.add(storageService.getEntity(rootPath));
		} catch (NotFoundException e) {
			throw new WicketRuntimeException(e);
		}
		
		return roots.iterator();
	}

	@Override
	public boolean hasChildren(Entity entity) {
		// TODO performance wicket-6
		try {
			return !getChildren(entity.getId()).isEmpty();
		} catch (NotFoundException e) {
			throw new WicketRuntimeException(e);
		}
	}

	@Override
	public Iterator<? extends Entity> getChildren(Entity entity) {
		// TODO performance wicket-6
		try {
			return getChildren(entity.getId()).iterator();
		} catch (NotFoundException e) {
			throw new WicketRuntimeException(e);
		}
	}

	@Override
	public IModel<Entity> model(Entity object) {
		return new EntityModel(object);
	}

	@Override
	public void detach() {
		// do nothing
	}

	protected List<Entity> getChildren(String id) throws NotFoundException {
		Entity[] entities = storageService.getEntityChildrenById(id);
		List<Entity> children = new ArrayList<Entity>();
		for (Entity entity : entities) {
			if (acceptEntityAsChild(entity)) {
//				System.out.println("add " + entity.getId() + " | " + entity.getPath());
				children.add(entity);
			}
		}
		
		return children;
	}
	
	protected boolean acceptEntityAsChild(Entity entity) {
		return true;
	}

}
