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

import org.apache.commons.io.IOUtils;

import ro.nextreports.server.domain.RunReportHistory;
import ro.nextreports.server.domain.SftpDestination;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * @author Decebal Suiu
 */
public class SftpDistributor implements Distributor {

	private boolean connected;
	private ChannelSftp channelSftp;

	public SftpDistributor() {
	}
			
	public void distribute(File file, Destination destination, DistributionContext context) throws DistributionException {
		SftpDestination sftpDestination = (SftpDestination) destination;
		
		if (!isConnected()) {
			connect(sftpDestination);
		}
		InputStream input = null;
        try {        	     	
			if (sftpDestination.getChangedFileName() != null) {				
				file = DistributorUtil.getFileCopy(file, sftpDestination.getChangedFileName());
			}
			input = new FileInputStream(file);   			
        	
        	String folder = sftpDestination.getFolder();
			if (folder != null) {
				channelSftp.cd(folder);
			}
        	channelSftp.put(input, file.getName(), ChannelSftp.OVERWRITE);
        } catch (Exception e) {
            throw new DistributionException(e);
        } finally {
        	IOUtils.closeQuietly(input);
        	disconnect();
        }
	}

    public void afterDistribute(RunReportHistory history, DistributionContext context) {
    }

    public boolean isTestable() {
    	return true;
    }

	public void test(Destination destination) throws DistributionException {
		SftpDestination sftpDestination = (SftpDestination) destination;
		
		if (!isConnected()) {
			connect(sftpDestination);
		}
	    try {
        	String folder = sftpDestination.getFolder();
			if (folder != null) {
				channelSftp.cd(folder);
			}
			channelSftp.pwd();
		} catch (SftpException e) {
			throw new DistributionException(e.getMessage());
        } finally {
        	disconnect();
        }
	}

	protected void connect(SftpDestination destination) throws DistributionException {
		if (connected) {
            throw new DistributionException("Already connected to sftp server"); // ?!
		}

		JSch jsch = new JSch();
		
		Session session;
		try {
			session = jsch.getSession(destination.getUsername(), destination.getHost(), destination.getPort());
		} catch (JSchException e) {
			throw new DistributionException(e);
		}
		
		SshUserInfo userInfo = new SshUserInfo();
		userInfo.setPassword(destination.getPassword());
		
		// username and password will be given via UserInfo interface.
		session.setUserInfo(userInfo);
		
		try {
			session.connect();		
			Channel channel = session.openChannel("sftp");
			channel.connect();
			connected = true;
		    channelSftp = (ChannelSftp) channel;
		} catch (JSchException e) {
			throw new DistributionException(e);
		}		
	}

	protected void disconnect() throws DistributionException {
		if (connected) {
            connected = false;
            channelSftp.disconnect();
        } else {
            throw new DistributionException("Not connected to sftp server"); // ?! or nothing
        }
	}

	protected boolean isConnected() throws DistributionException {
		return connected;
	}

}
