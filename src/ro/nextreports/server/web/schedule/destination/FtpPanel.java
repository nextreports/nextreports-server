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
package ro.nextreports.server.web.schedule.destination;

import org.apache.commons.lang.StringUtils;

import ro.nextreports.server.distribution.DistributionException;
import ro.nextreports.server.domain.FtpDestination;

import it.sauronsoftware.ftp4j.FTPException;


/**
 * User: mihai.panaitescu
 * Date: 28-Sep-2010
 * Time: 15:38:28
 */
public class FtpPanel extends TransferPanel {

	private static final long serialVersionUID = 1L;

	public FtpPanel(String id, FtpDestination destination) {
        super(id, destination);
    }

	@Override
	protected String getDisplayMessage(DistributionException e) {
		String message = super.getDisplayMessage(e);
		Throwable cause = e.getCause();
		if (cause instanceof FTPException) {
			if (StringUtils.isEmpty(message)) {
				message = "" + ((FTPException) cause).getCode();
			} else {
				message += ((FTPException) cause).getCode();
			}
		}
		
		return message;
	}
	
}
