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
package ro.nextreports.server.cache.ehcache;

import ro.nextreports.server.cache.Cache;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 * @author Decebal Suiu
 */
public class EhCache implements Cache {
	
	private Ehcache ehcache;

	public EhCache(Ehcache ehcache) {
		this.ehcache = ehcache;
	}

	public String getName() {
		return ehcache.getName();
	}

	public void put(Object key, Object value) {
		getEhCache().put(new Element(key, value));
	}

	public Object get(Object key) {
		Element element = getEhCache().get(key);
		if (element != null) {
			return element.getValue();
		}
		
		return null;
	}

	public boolean hasElement(Object key) {
		return getEhCache().get(key) != null;
	}
	
	public void remove(Object key) {
		if (hasElement(key)) {
			getEhCache().remove(key);
		}
	}

	public void clear() {
		getEhCache().removeAll();
	}

	public Ehcache getEhCache() {
		return ehcache;
	}
	
}
