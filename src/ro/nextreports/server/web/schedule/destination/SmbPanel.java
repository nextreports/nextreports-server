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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;

import ro.nextreports.server.domain.SmbDestination;

/**
 * User: mihai.panaitescu
 * Date: 28-Sep-2010
 * Time: 16:31:25
 */
public class SmbPanel extends TransferPanel {

	private static final long serialVersionUID = 1L;

	public SmbPanel(String id, SmbDestination destination) {
        super(id, destination);
    }

    protected void initComponents() {
        super.initComponents();

        add(new Label("domain", getString("ActionContributor.Run.destination.domain")));
        TextField<String> domainField = new TextField<String>("domainField",
                new PropertyModel<String>(destination, "domain"));
        add(domainField);
    }
    
}
