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

import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.distribution.Destination;
import ro.nextreports.server.distribution.DistributionException;
import ro.nextreports.server.distribution.Distributor;
import ro.nextreports.server.distribution.DistributorFactory;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.common.form.FormContentPanel;
import ro.nextreports.server.web.core.validation.JcrNameValidator;


/**
 * User: mihai.panaitescu
 * Date: 28-Sep-2010
 * Time: 15:11:25
 */
public abstract class AbstractDestinationPanel extends FormContentPanel {

	private static final long serialVersionUID = 1L;
	
	protected Destination destination;    
	
	private static final Logger LOG = LoggerFactory.getLogger(AbstractDestinationPanel.class);

    public AbstractDestinationPanel(String id, Destination destination) {
        super(id);
        this.destination = destination;
        setOutputMarkupId(true);
        addComponents();
    }

    private void addComponents() {
        add(new Label("name", getString("name")));
        
        TextField<String> displayNameField = new TextField<String>("nameField", new PropertyModel<String>(destination, "name"));
        displayNameField.setLabel(new Model<String>( getString("name")));
        displayNameField.setRequired(true);
        displayNameField.add(new JcrNameValidator());
        add(displayNameField);

        initComponents();
    }

    @Override
	public void onOk(AjaxRequestTarget target) {
		super.onOk(target);
		
		if (NextServerSession.get().isDemo()) {
			error(getString("ActionContributor.Run.destination.demo"));
			target.add(getFeedbackPanel());
			return;
		}
		
        MarkupContainer p = AbstractDestinationPanel.this.getParent();
        while (!(p instanceof DestinationsPanel)) {
            p = p.getParent();
        }
        List<Destination> destinations = ((DestinationsPanel) p).getDestinations();
        for (Destination d : destinations) {
            if  (d.equals(destination)) {
                // edit
                continue;
            }
            if (d.getName().equals(destination.getName())) {                
                error(getString("ActionContributor.Run.destination.unique") + " : '" + destination.getName() + "'");
                target.add(getFeedbackPanel());
                return;
            }
        }

		onSave(target);
	}

    @Override
	public void onApply(AjaxRequestTarget target) {
    	if (NextServerSession.get().isDemo()) {
			error(getString("ActionContributor.Run.destination.demoTest"));			
		} else {
			try {
				testDestination(destination);
				info(getString("ActionContributor.Run.destination.test"));
			} catch (DistributionException e) {
				LOG.error(e.getMessage(), e);
				error(getDisplayMessage(e));
			}
		}		
		target.add(getFeedbackPanel());
	}

	@Override
	public void onCancel(AjaxRequestTarget target) {
		super.onCancel(target);
		onClose(target);
	}

    protected abstract void initComponents();
    
	protected void onSave(AjaxRequestTarget target) {
    }

    protected void onClose(AjaxRequestTarget target) {
    }
    
    protected String getDisplayMessage(DistributionException e) {
    	return ExceptionUtils.getRootCause(e).getMessage();
    }
    
    private void testDestination(Destination destination) throws DistributionException {
    	Distributor distributor = DistributorFactory.getDistributor(destination.getType());
    	distributor.test(destination);
    }

}
