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

import ro.nextreports.server.distribution.Destination;
import ro.nextreports.server.distribution.Distributor;
import ro.nextreports.server.distribution.DistributorFactory;
import ro.nextreports.server.web.common.form.FormContentPanel;
import ro.nextreports.server.web.common.form.FormPanel;

/**
 * @author Decebal Suiu
 */
public class DestinationFormPanel extends FormPanel {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public DestinationFormPanel(String id, FormContentPanel contentPanel, Destination destination) {
		super(id, contentPanel);
				
		Distributor distributor = DistributorFactory.getDistributor(destination.getType());
		if (distributor.isTestable()) {
			setApplyButtonValue("Test");
			getApplyButton().setVisible(true);
		}
	}

}
