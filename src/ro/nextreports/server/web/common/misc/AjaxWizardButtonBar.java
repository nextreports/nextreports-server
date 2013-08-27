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

import org.apache.wicket.extensions.wizard.Wizard;
import org.apache.wicket.extensions.wizard.IWizardModel;
import org.apache.wicket.extensions.wizard.IWizardStep;
import org.apache.wicket.extensions.wizard.WizardButtonBar;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.ajax.AjaxRequestTarget;

//
// Created by IntelliJ IDEA.
// User: mihai.panaitescu
// Date: 22-Oct-2009
// Time: 11:08:48

//
public class AjaxWizardButtonBar extends WizardButtonBar {

    private static final long serialVersionUID = 1L;
    private AjaxWizardFinishButton finishButton;

    public AjaxWizardButtonBar(String id, final Wizard wizard) {
        super(id, wizard);

        addOrReplace(new AjaxWizardButton("next", wizard, "next") {
            @Override
            protected void onClick(AjaxRequestTarget target, Form form) {
                IWizardModel wizardModel = getWizardModel();
                IWizardStep step = wizardModel.getActiveStep();
                // let the step apply any state step.applyState();
                // if the step completed after applying the state, move the model onward
                if (step.isComplete()) {
                    wizardModel.next();
                } else {
                    error(getLocalizer().getString("org.apache.wicket.extensions.wizard.NextButton.step.did.not.complete", this));
                }
                target.add(wizard);
            }

            protected void onError(AjaxRequestTarget target, Form<?> form) {
                //@todo how to get feedbackPanel only
                target.add(wizard);
            }

            public final boolean isEnabled() {
                return getWizardModel().isNextAvailable();
            }
        });

        AjaxWizardButton prevButton = new AjaxWizardButton("previous", wizard, "prev") {
            @Override
            protected void onClick(AjaxRequestTarget target, Form form) {
                getWizardModel().previous();
                target.add(wizard);
            }

            protected void onError(AjaxRequestTarget target, Form<?> form) {
                //@todo how to get feedbackPanel only
                target.add(wizard);
            }

            public final boolean isEnabled() {
                return getWizardModel().isPreviousAvailable();
            }

        };
        //no validation is done clicking previous
        prevButton.setDefaultFormProcessing(false);
        addOrReplace(prevButton);


        AjaxWizardButton cancelButton = new AjaxWizardButton("cancel", wizard, "cancel") {
            @Override
            protected void onClick(AjaxRequestTarget target, Form form) {
                //getWizardModel().cancel();
                onCancel(target);
                target.add(wizard);
            }

            protected void onError(AjaxRequestTarget target, Form<?> form) {
                //@todo how to get feedbackPanel only
                target.add(wizard);
            }

            public final boolean isEnabled() {
                return getWizardModel().isCancelVisible();
            }
        };
        //no validation is done clicking cancel
        cancelButton.setDefaultFormProcessing(false);
        addOrReplace(cancelButton);
        
        finishButton = new AjaxWizardFinishButton("finish", wizard);
        addOrReplace(finishButton);
    }

    public void onCancel(AjaxRequestTarget target) {
    }
    
    public void setFinishSteps(int[] finishSteps) {
		finishButton.setFinishSteps(finishSteps);
	}

}
