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

import java.io.File;

import org.apache.wicket.extensions.ajax.markup.html.form.upload.UploadProgressBar;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.lang.Bytes;

import ro.nextreports.server.web.NextServerSession;

/**
 * @author Decebal Suiu
 */
public class LicenseErrorPage extends WebPage {

	private static final long serialVersionUID = 1L;

	public LicenseErrorPage(PageParameters parameters)  {
		String error = parameters.get("errorMessage").toString();
		
        FeedbackPanel uploadFeedback = new FeedbackPanel("uploadFeedback");
        add(uploadFeedback);

        add(new Label("curentUser", NextServerSession.get().getUsername()));
        add(new Label("realName", NextServerSession.get().getRealName()));
        add(new Link<String>("logout") {

			private static final long serialVersionUID = 1L;

			@Override
            public void onClick() {
                NextServerSession.get().signOut();
                setResponsePage(getApplication().getHomePage());
            }
            
        });

        Form<Void> uploadLicenseForm = new UploadLicenseForm("licenseForm");          
        uploadLicenseForm.add(new UploadProgressBar("progress", uploadLicenseForm));
        if (!NextServerSession.get().isAdmin()) {
            error = "Please contact your administrator : " + error;
            uploadLicenseForm.setVisible(false);
        } 
        add(new Label("error", error));
        add(uploadLicenseForm);
    }
    
	private String getUploadFolder() {
		return System.getProperty("user.dir");
	}

    private void checkFileExists(File newFile) {
        if (newFile.exists()) {
            // try to delete the file
            if (!Files.remove(newFile)) {
                throw new IllegalStateException("Unable to overwrite " + newFile.getAbsolutePath());
            }
        }
    }

    private class UploadLicenseForm extends Form<Void> {

		private static final long serialVersionUID = 1L;
		
		private FileUploadField fileUploadField;
    	
		public UploadLicenseForm(String id) {
			super(id);
			
			// set this form to multipart mode (allways needed for uploads!)
            setMultiPart(true);

            // add one file input field
            add(fileUploadField = new FileUploadField("fileInput"));

            // set maximum size to 100K
            setMaxSize(Bytes.kilobytes(100));
		}
    	
		@Override
        protected void onSubmit() {
            FileUpload upload = fileUploadField.getFileUpload();
            if (upload != null) {
                File licenseFile = new File(getUploadFolder(), "license.dat"/*upload.getClientFileName()*/);

                // check new file, delete if it allready existed
                checkFileExists(licenseFile);
                try {
                    // Save to new file
                    licenseFile.createNewFile();
                    upload.writeTo(licenseFile);

                    LicenseErrorPage.this.info("Uploaded file: " + upload.getClientFileName());
                    setResponsePage(getApplication().getHomePage());
                } catch (Exception e) {
                    throw new IllegalStateException("Unable to write file");
                }
            }
		}
		
    }
    
}
