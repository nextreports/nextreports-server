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

package ro.nextreports.server.web.core.audit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.IteratorUtils;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.engine.exporter.util.TableData;
import ro.nextreports.server.web.dashboard.table.TableObjectComparator;

/**
 * @author Mihai Dinca-Panaitescu
 */
public class AuditDataProvider extends SortableDataProvider<List<Object>, String> {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(AuditDataProvider.class);

    private TableData data;
    private int sortPos;
    private int sortDir;

    public AuditDataProvider(TableData data) {
        this.data = data;
    }   

    @SuppressWarnings("unchecked")
    @Override
	public Iterator<List<Object>> iterator(long first, long count) {
    	if (getSort() != null) {      
    		sortDir = getSort().isAscending() ? 1 : -1;
        	sortPos = getPropertyPosition(getSort().getProperty());
    	}	
        try {
			return getRows().subList((int) first, (int) (first + count)).iterator();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return IteratorUtils.EMPTY_ITERATOR;
		}
    }

    @Override
    public IModel<List<Object>> model(List<Object> object) {
        return new Model(new ArrayList(object));
    }

    @Override
    public long size() {
        try {
			return getRows().size();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return 0;
		}
    } 
	
	public List<String> getHeader() {
        return data.getHeader();
    }
	
    private List<List<Object>> getRows() {        
        Collections.sort(data.getData(), new Comparator<List<Object>>() {
			public int compare(List<Object> o1, List<Object> o2) {
				if ((o1.get(sortPos) == null) && (o2.get(sortPos) == null)) {
					return 0;
				} else if (o1.get(sortPos) == null) {
					return sortDir;
				} else if (o2.get(sortPos) == null) {
					return -sortDir;
				} else {
					return sortDir * new TableObjectComparator().compare(o1.get(sortPos), o2.get(sortPos));
				}								
			}
		});
        return data.getData();
    }    
    
    private int getPropertyPosition(String property) {
    	List<String> header = new ArrayList<String>();
		try {
			header = getHeader();
			for (int i=0, size=header.size(); i<size; i++) {
	    		if (header.get(i).equals(property)) {
	    			return i;
	    		}
	    	}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}    	
    	return -1;
    }
    
}
