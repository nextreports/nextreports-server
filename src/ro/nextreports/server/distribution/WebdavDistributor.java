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
package ro.nextreports.server.distribution;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;

import ro.nextreports.server.domain.RunReportHistory;
import ro.nextreports.server.domain.WebdavDestination;

import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import com.googlecode.sardine.util.SardineException;

/**
 * @author Decebal Suiu
 */
public class WebdavDistributor implements Distributor {

	public WebdavDistributor() {
	}
		
	public void distribute(File file, Destination destination, DistributionContext context) throws DistributionException {
		WebdavDestination webdavDestination = (WebdavDestination) destination;

		if (webdavDestination.getChangedFileName() != null) {
			file = DistributorUtil.getFileCopy(file, webdavDestination.getChangedFileName());
		}

		Sardine sardine = createSardine(webdavDestination);
		InputStream input = null;
		try {
			input = new FileInputStream(file);
			String webdav = toWebdav(file.getName(), webdavDestination);
			sardine.put(webdav, input);
			if (webdavDestination.getChangedFileName() != null) {	
				Files.delete(file.toPath());
			}
		} catch (Exception e) {
			throw new DistributionException(e);
		} finally {
			DistributorUtil.deleteFileCopy(webdavDestination.getChangedFileName(), file);
		}
	}

	public void afterDistribute(RunReportHistory history, DistributionContext context) {
	}

    public boolean isTestable() {
    	return true;
    }

	public void test(Destination destination) throws DistributionException {
		WebdavDestination webdavDestination = (WebdavDestination) destination;
		
		Sardine sardine = createSardine(webdavDestination);
		String webdav = toWebdav("", webdavDestination);
		try {
			sardine.getResources(webdav);
		} catch (SardineException e) {
			throw new DistributionException(e);
		}
	}
	
	private String toWebdav(String fileName, WebdavDestination destination) {
		StringBuilder sb = new StringBuilder();
		if (destination.isHttps()) {
			sb.append("https://");
		} else {
			sb.append("http://");
		}
		sb.append(destination.getHost());
		sb.append(':');
		sb.append(destination.getPort());
		sb.append('/');
		String path = new File(destination.getFolder(), fileName).getPath();
		sb.append(path);
		
		return sb.toString();
	}

	private Sardine createSardine(WebdavDestination webdavDestination) throws DistributionException {
		String username = webdavDestination.getUsername();
		try {
			if (username == null) {
				return SardineFactory.begin();
			} else { 
				String password = webdavDestination.getPassword();
				return SardineFactory.begin(username, password);
			}
		} catch (SardineException e) {
			throw new DistributionException(e);
		}
	}

}
