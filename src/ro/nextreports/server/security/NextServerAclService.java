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
package ro.nextreports.server.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;

import ro.nextreports.server.dao.AclDao;
import ro.nextreports.server.dao.SecurityDao;
import ro.nextreports.server.dao.StorageDao;


/**
 * @author Decebal Suiu
 */
public class NextServerAclService implements AclService {

	private AclDao aclDao;
    private SecurityDao securityDao;
    private StorageDao storageDao;

	public void setAclDao(AclDao aclDao) {
		this.aclDao = aclDao;
	}

    public void setSecurityDao(SecurityDao securityDao) {
        this.securityDao = securityDao;
    }        

    public void setStorageDao(StorageDao storageDao) {
		this.storageDao = storageDao;
	}

	public List<ObjectIdentity> findChildren(ObjectIdentity parentIdentity) {
		throw new UnsupportedOperationException();
	}

	public Acl readAclById(ObjectIdentity object) throws NotFoundException {
	    return readAclById(object, null);
	}

	@Override
	public Acl readAclById(ObjectIdentity object, List<Sid> sids) throws NotFoundException {
		List<ObjectIdentity> objects = new ArrayList<ObjectIdentity>();
		objects.add(object);
		Map<ObjectIdentity, Acl> map = readAclsById(objects, sids);
		if (map.size() == 0) {
			throw new NotFoundException("Acl not find for " + object);
		}

		return map.get(object);
	}

	@Override
	public Map<ObjectIdentity, Acl> readAclsById(List<ObjectIdentity> objects) throws NotFoundException {
		return readAclsById(objects, null);
	}

	@Override
	public Map<ObjectIdentity, Acl> readAclsById(List<ObjectIdentity> objects, List<Sid> sids) throws NotFoundException {
		Map<ObjectIdentity, Acl> acls = new HashMap<ObjectIdentity, Acl>();
		for (ObjectIdentity objectIdentity : objects) {
			acls.put(objectIdentity, new NextServerAcl(objectIdentity, aclDao, securityDao, storageDao));
		}

		return acls;
	}

}
