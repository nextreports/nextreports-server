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
package ro.nextreports.server.web.security;

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.AclEntry;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.web.report.ParamView;

/**
 * @author Decebal Suiu
 */
public class AclEntryDataProvider extends SortableDataProvider<AclEntry, String> {

	private static final long serialVersionUID = 1L;
	
	private String entityId;
	private transient List<AclEntry> aclEntries;    

    @SpringBean
    private SecurityService securityService;

    public AclEntryDataProvider(String entityId) {
    	Injector.get().inject(this);
    	this.entityId = entityId;
    }

    @Override
	public Iterator<? extends AclEntry> iterator(long first, long count) {
		return getAclEntries().iterator();
	}

	public IModel<ParamView> model(ParamView version) {
		return new Model<ParamView>(version);
	}

	@Override
	public long size() {
		return getAclEntries().size();
	}

	@Override
	public void detach() {
		aclEntries = null;
	}

	public IModel<AclEntry> model(AclEntry aclEntry) {
		return new Model<AclEntry>(aclEntry);
	}

    private List<AclEntry> getAclEntries() {
        if (aclEntries == null) {
        	try {
        		AclEntry[] aclEntries = securityService.getGrantedById(entityId);
                Arrays.sort(aclEntries, new Comparator<AclEntry>() {
                	
                    public int compare(AclEntry o1, AclEntry o2) {
                         if (o1.getType() == o2.getType()) {
                             return Collator.getInstance().compare(o1.getName(), o2.getName());
                         } else {
                             return o1.getType() - o2.getType();
                         }
                    }
                    
                });
                this.aclEntries = Arrays.asList(aclEntries);
			} catch (Exception e) {
				// TODO
				throw new RuntimeException(e);
			}
        }
        
        return aclEntries;
    }

}
