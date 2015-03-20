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

import it.sauronsoftware.ftp4j.FTPClient;

import java.io.File;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.domain.FtpDestination;
import ro.nextreports.server.domain.RunReportHistory;


/**
 * @author Decebal Suiu
 */
public class FtpDistributor implements Distributor {

	private boolean connected;
	private FTPClient client;
	
	private static final Logger LOG = LoggerFactory.getLogger(FtpDistributor.class);
	
	public FtpDistributor() {
		client = new FTPClient();
	}
			
	public void distribute(File file, Destination destination, DistributionContext context) throws DistributionException {
		FtpDestination ftpDestination = (FtpDestination) destination;
		
		if (!isConnected()) {
			connect(ftpDestination);
		}
		File uploadFile = file;
		try {
			String folder = ftpDestination.getFolder();
			if (folder != null) {
				client.changeDirectory(folder);
			}			
			if (ftpDestination.getChangedFileName() != null) {			
				String fileName = DistributorUtil.replaceTemplates(ftpDestination.getChangedFileName(), context);
				uploadFile = DistributorUtil.getFileCopy(file, fileName);
			}
			client.upload(uploadFile);						
		} catch (Exception e) {
			throw new DistributionException(e);
		} finally {
			disconnect();
			DistributorUtil.deleteFileCopy(ftpDestination.getChangedFileName(), uploadFile);
		}
	}

    public void afterDistribute(RunReportHistory history, DistributionContext context) {
    }

    public boolean isTestable() {
    	return true;
    }
    
    public void test(Destination destination) throws DistributionException {
		FtpDestination ftpDestination = (FtpDestination) destination;
		
		if (!isConnected()) {
			connect(ftpDestination);
		}
	    try {
			String folder = ftpDestination.getFolder();
			if (folder != null) {
				client.changeDirectory(folder);
			}
			client.currentDirectory();
		} catch (Exception e) {
			throw new DistributionException(e.getMessage());
		} finally {
			disconnect();
		}
	}
	
	protected void connect(FtpDestination destination) throws DistributionException {
		if (connected) {
            throw new DistributionException("Already connected to ftp server"); // ?!
		}
		
		try {
			client.connect(destination.getHost(), destination.getPort());
			client.login(destination.getUsername(), destination.getPassword());
			connected = true;
		} catch (Exception e) {
			throw new DistributionException(e);
		}
	}
	
	protected void disconnect() throws DistributionException {
		if (connected) {
            connected = false;
    		try {
    			client.disconnect(true);
    		} catch (Exception e) {
    			throw new DistributionException(e);
    		}
        } else {
            throw new DistributionException("Not connected to ftp server"); // ?!
        }
	}

	protected boolean isConnected() throws DistributionException {
		return connected;
	}

}
