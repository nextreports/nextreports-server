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
package ro.nextreports.server.web.core;

import java.util.UUID;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.Folder;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.StorageUtil;
import ro.nextreports.server.web.core.BasePage;


/**
 * @author Decebal Suiu
 */
public class AddFoldersPage extends BasePage {

	@SpringBean
	private StorageService storageService; 
	
	public AddFoldersPage() {
		super();
		
		add(new AddFoldersForm("form"));
		add(new FeedbackPanel("feedback"));
	}
		
	class AddFoldersForm extends Form {

		private String parentPath;
		private int instances;
		
		@SuppressWarnings("unchecked")
		public AddFoldersForm(String id) {
			super(id);
			
			add(new TextField<String>("parentPath", new PropertyModel<String>(this, "parentPath")).setRequired(true));
			add(new TextField("instances", new PropertyModel<String>(this, "instances"), Integer.class).setRequired(true));
		}

		@Override
		protected void onSubmit() {
			addFolders();
		}

		private void addFolders() {
			for (int i = 0; i < instances; i++) {
				try {
					String folderName = UUID.randomUUID().toString();
					String path = StorageUtil.createPath(StorageConstants.REPORTS_ROOT + "/" + parentPath, folderName);
					Folder folder = new Folder(folderName, path);
					storageService.addEntity(folder);
				} catch (Exception e) {
					// TODO
					e.printStackTrace();
				}
			}
		}

	}
		
}
