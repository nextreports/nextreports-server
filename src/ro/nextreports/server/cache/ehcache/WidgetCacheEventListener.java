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

import ro.nextreports.server.domain.DashboardState;
import ro.nextreports.server.domain.WidgetState;
import ro.nextreports.server.service.DashboardService;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

/**
 * @author Decebal Suiu
 */
public class WidgetCacheEventListener implements CacheEventListener {
			
	private DashboardService dashboardService;		
			
	public void setDashboardService(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	public void notifyElementRemoved(Ehcache cache, Element element) throws CacheException {
		resetParentCache(cache, element);
	}

	public void notifyElementPut(Ehcache cache, Element element) throws CacheException {
		resetParentCache(cache, element);
	}

	public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException {
		resetParentCache(cache, element);
	}

	public void notifyElementExpired(Ehcache cache, Element element) {		
	}

	public void notifyElementEvicted(Ehcache cache, Element element) {		
	}

	public void notifyRemoveAll(Ehcache cache) {		
	}

	public void dispose() {		
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	private void resetParentCache(Ehcache cache, Element element) {
		// we must clear parent DashboardState, otherwise dashboard's refresh is not done
		if (element.getObjectValue() instanceof WidgetState) {
			DashboardState dashboardState = dashboardService.getDashboardState((WidgetState)element.getObjectValue());			
			if (dashboardState != null) {
				cache.remove(dashboardState.getId());
			}
		}
	}

}
