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
package ro.nextreports.server.web.common.misc;

import java.util.Iterator;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.wizard.IWizard;
import org.apache.wicket.extensions.wizard.IWizardModel;
import org.apache.wicket.extensions.wizard.IWizardStep;
import org.apache.wicket.extensions.wizard.Wizard;
import org.apache.wicket.markup.html.form.Form;


public class AjaxWizardFinishButton extends AjaxWizardButton {
	
	private static final long serialVersionUID = 1L;
	private int[] steps = new int[0];	

	/**
	 * Construct.
	 * 
	 * @param id    The component id
	 * @param wizard  The wizard
	 */
	public AjaxWizardFinishButton(String id, IWizard wizard) {
		super(id, wizard, "finish");
	}

	/**
	 * @see org.apache.wicket.Component#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		IWizardStep activeStep = getWizardModel().getActiveStep();
		Iterator<IWizardStep> iterator = getWizardModel().stepIterator();
		int index = 0;
		if (activeStep != null) {
			while (iterator.hasNext()) {
				IWizardStep step = iterator.next();
				if (activeStep.equals(step)) {
					break;
				}
				index++;
			}				
			for (int i : steps) {
				if (index == i) {
					return true;
				}
			}			
		}
		return (activeStep != null && getWizardModel().isLastStep(activeStep));
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.WizardButton#onClick()
	 */
	@Override
	public void onClick(AjaxRequestTarget target, Form form) {
		IWizardModel wizardModel = getWizardModel();
		IWizardStep step = wizardModel.getActiveStep();

		// let the step apply any state
		step.applyState();

		// if the step completed after applying the state, notify the wizard
		if (step.isComplete()) {			
			getWizardModel().finish();			
		} else {
			error(getLocalizer().getString("org.apache.wicket.extensions.wizard.FinishButton.step.did.not.complete", this));
		}
		target.add((Wizard)getWizard());
	}
	
	@Override
	protected void onError(AjaxRequestTarget target, Form<?> form) {        
        target.add((Wizard)getWizard());
    }
	
	public void setFinishSteps(int[] steps) {
		if (steps == null) {
			return;
		}
		this.steps = steps;
	}
		
}
