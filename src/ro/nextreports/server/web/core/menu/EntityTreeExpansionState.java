package ro.nextreports.server.web.core.menu;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;

import ro.nextreports.server.domain.Entity;


public class EntityTreeExpansionState implements Set<Entity>, Serializable {
	
    private static final long serialVersionUID = 1L;

    private static MetaDataKey<EntityTreeExpansionState> KEY = new MetaDataKey<EntityTreeExpansionState>() {
        private static final long serialVersionUID = 1L;
    };

    private Set<String> ids = new HashSet<String>();

    private boolean inverse;

    public void expandAll() {
        ids.clear();
        inverse = true;
    }

    public void collapseAll() {
        ids.clear();
        inverse = false;
    }

    @Override
    public boolean add(Entity entity) {
        if (inverse) {
            return ids.remove(entity.getId());
        } else {
            return ids.add(entity.getId());
        }
    }

    @Override
    public boolean remove(Object o) {
        Entity entity = (Entity)o;
        if (inverse) {
            return ids.add(entity.getId());
        } else {
            return ids.remove(entity.getId());
        }
    }

    @Override
    public boolean contains(Object o) {
    	Entity entity = (Entity)o;
        if (inverse) {
            return !ids.contains(entity.getId());
        } else {
            return ids.contains(entity.getId());
        }
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <A> A[] toArray(A[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Entity> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends Entity> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the expansion for the session.
     * 
     * @return expansion
     */
    public static EntityTreeExpansionState get() {
    	EntityTreeExpansionState expansion = Session.get().getMetaData(KEY);
        if (expansion == null) {
            expansion = new EntityTreeExpansionState();
            Session.get().setMetaData(KEY, expansion);
        }
        return expansion;
    }
}