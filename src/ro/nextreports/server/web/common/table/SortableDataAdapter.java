/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.nextreports.server.web.common.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.beans.support.SortDefinition;

/**
 * @author Decebal Suiu
 */
public class SortableDataAdapter<T> extends SortableDataProvider<T> {
	
	private static final long serialVersionUID = 1L;

	private IDataProvider<T> provider;
	private Map<String, Comparator<T>> comparators;

	@SuppressWarnings("unchecked")
	public SortableDataAdapter(IDataProvider<T> provider) {
		this(provider, Collections.EMPTY_MAP);
	}
	
	public SortableDataAdapter(IDataProvider<T> provider, Map<String, Comparator<T>> comparators) {
		this.provider = provider;
		this.comparators = comparators;
	}

	public Iterator<T> iterator(int first, int count) {
		int size = provider.size();
		List<T> resources = new ArrayList<T>(size);
		Iterator<? extends T> iter = provider.iterator(0, size);
		while (iter.hasNext()) {
			resources.add(iter.next());
		}

		if (comparators != null) {
			SortParam sortParam = getSort();
			if (sortParam != null) {
				String sortProperty = sortParam.getProperty();
				if (sortProperty != null) {
					Comparator<T> comparator = comparators.get(sortProperty);
					if (comparator != null) {
						Collections.sort(resources, comparator);
						if (getSort().isAscending() == false) {
							Collections.reverse(resources);
						}
					} else {
						SortDefinition sortDefinition = new MutableSortDefinition(sortProperty, true, getSort().isAscending());
						PropertyComparator.sort(resources, sortDefinition);
					}						
				}
			}
		}
		
		return Collections.unmodifiableList(resources.subList(first, first + count)).iterator();
	}

	@SuppressWarnings("unchecked")
	public IModel<T> model(Object object) {
		return provider.model((T) object);
	}

	public int size() {
		return provider.size();
	}

	@Override
	public void detach() {
		provider.detach();
	}

}
