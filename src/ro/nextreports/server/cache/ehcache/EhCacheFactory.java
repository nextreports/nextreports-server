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

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.constructs.blocking.BlockingCache;

import org.springframework.beans.factory.annotation.Required;

import ro.nextreports.server.cache.Cache;
import ro.nextreports.server.cache.CacheFactory;


/**
 * @author Decebal Suiu
 */
public class EhCacheFactory implements CacheFactory {

	private CacheManager cacheManager;

	@Required
	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public Cache getCache(String name, int expirationTime) {
		Ehcache ehcache = cacheManager.getEhcache(name);
		if (ehcache == null) {			
			createCache(name, expirationTime);
			return getCache(name, expirationTime);
		}
		
		return new EhCache(ehcache);
	}

	public CacheManager getCacheManager() {
		return cacheManager;
	}
	
	protected void createCache(String name, int expirationTime) {
		synchronized (this.getClass()) {
			cacheManager.addCache(name);

			Ehcache cache = cacheManager.getEhcache(name);
			CacheConfiguration config = cache.getCacheConfiguration();
			config.setEternal(false);
			config.setTimeToLiveSeconds(expirationTime);
//		    config.setTimeToIdleSeconds(60);
//		    config.setMaxElementsInMemory(10000);
//		    config.setMaxElementsOnDisk(1000000);
		    
			BlockingCache blockingCache = new BlockingCache(cache);
			cacheManager.replaceCacheWithDecoratedCache(cache, blockingCache);
		}
	}
	
	public void resetCache(String name) {		
		cacheManager.removeCache(name);
	}

}
