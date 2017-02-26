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
package ro.nextreports.server.web.maintenance;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.RunReportHistory;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.common.behavior.AlertBehavior;
import ro.nextreports.server.web.common.misc.AjaxSubmitConfirmLink;

//
public class ShrinkDataFolderPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean
	private StorageService storageService;

	@SuppressWarnings("unchecked")
	public ShrinkDataFolderPanel(String id, final Report report) {
		super(id);

		Form<RunReportHistory> form = new Form<RunReportHistory>("form");

		AjaxSubmitConfirmLink shrinkDataFolderLink = new AjaxSubmitConfirmLink("shrinkLink",
				getString("MaintenancePanel.shrink")) {
			public void onSubmit(AjaxRequestTarget target, Form<?> form) {
				try {
					storageService.shrinkDataFolder();

				} catch (Exception e) {
					e.printStackTrace();
					add(new AlertBehavior(e.getMessage()));
					target.add(this);
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(form);
			}

		};
		form.add(shrinkDataFolderLink);

		if (NextServerSession.get().isDemo()) {
			shrinkDataFolderLink.setVisible(false);
		}

		add(form);
	}

}
