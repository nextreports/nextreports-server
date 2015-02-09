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
import java.io.OutputStream;
import java.net.MalformedURLException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

import org.apache.commons.io.IOUtils;

import ro.nextreports.server.domain.RunReportHistory;
import ro.nextreports.server.domain.SmbDestination;


/**
 * @author Decebal Suiu
 */
public class SmbDistributor implements Distributor {

	public SmbDistributor() {
	}
			
	public void distribute(File file, Destination destination, DistributionContext context) throws DistributionException {
		SmbDestination smbDestination = (SmbDestination) destination;
		
		OutputStream output = null;
		InputStream input = null;
		try {
			if (smbDestination.getChangedFileName() != null) {				
				file = DistributorUtil.getFileCopy(file, smbDestination.getChangedFileName());
			}
			SmbFile smbFile = toSmbFile(file.getName(), smbDestination);
			output = new SmbFileOutputStream(smbFile);
			input = new FileInputStream(file);
			IOUtils.copy(input, output);
//			IOUtils.copyLarge(input, output); // file over 2GB
		} catch (Exception e) {
			throw new DistributionException(e);
		} finally {
			IOUtils.closeQuietly(output);
			IOUtils.closeQuietly(input);
		}
	}

    public void afterDistribute(RunReportHistory history, DistributionContext context) {        
    }

    public boolean isTestable() {
    	return true;
    }

    public void test(Destination destination) throws DistributionException {
		SmbDestination smbDestination = (SmbDestination) destination;
		try {
			SmbFile smbFile = toSmbFile("", smbDestination);
			smbFile.exists();
		} catch (Exception e) {
			throw new DistributionException(e);
		}
	}

	private String toSmb(String fileName, SmbDestination destination) {
		StringBuilder sb = new StringBuilder();
		sb.append("smb://");
		sb.append(destination.getHost());
		sb.append(':');
		sb.append(destination.getPort());
		sb.append('/');
		String path = new File(destination.getFolder(), fileName).getPath();
		sb.append(path);
		
		return sb.toString();
	}
	
	private SmbFile toSmbFile(String fileName, SmbDestination destination) throws MalformedURLException {
		String uri = toSmb(fileName, destination);
		SmbFile smbFile;
		if (destination.getPassword() == null) {
			smbFile = new SmbFile(uri);
		} else {
			NtlmPasswordAuthentication passwordAuthentication = new NtlmPasswordAuthentication(destination.getDomain(), 
					destination.getUsername(), destination.getPassword());
			smbFile = new SmbFile(uri, passwordAuthentication);
		}
		
		return smbFile;
	}

}
