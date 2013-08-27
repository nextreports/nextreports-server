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
package ro.nextreports.server.web.core.validation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;

public class ImageValidator extends AbstractFormValidator {
	
	private final FileUploadField fileUpload;
    
	private int width = 0;
	private int height = 0;
    private String extension;

    /**
     * Create a validator with width and height constraints.
     */
    public ImageValidator(FileUploadField fileUpload, int width, int height) {
    	if (fileUpload == null) {
            throw new IllegalArgumentException("argument formComponent cannot be null");
        }
    	this.fileUpload = fileUpload;
        this.width = width;
        this.height = height;
    }

    /**
     * Create a validator with width,height and extension constraints.
     * @param width required width in pixels
     * @param height required height in pixels
     * @param extension required extension (without period)
     */
    public ImageValidator(FileUploadField fileUpload, int width, int height, String extension) {
        this(fileUpload, width, height);
        this.extension = "." + extension.toLowerCase();
    }

    public void validate(Form<?> form) {        
        if (fileUpload.getFileUpload() != null) {
            try {
                BufferedImage image = ImageIO.read(fileUpload.getFileUpload().getInputStream());
                if (image == null) {
                	error(fileUpload, "ImageValidator.nullError");
                } else if (extension != null && fileUpload.getFileUpload().getClientFileName().toLowerCase().endsWith(extension.toLowerCase())) {                	
                    error(fileUpload, "ImageValidator.formatError");
                } else if ((width > 0 && image.getWidth() > width) || (height > 0 && image.getHeight() > height)) {
                	 Map<String, Object> params = new HashMap<String, Object>();
                     params.put("width", image.getWidth());
                     params.put("height", image.getHeight());
                     params.put("maxWidth", width);
                     params.put("maxHeight", height);
                     error(fileUpload, "ImageValidator.sizeError", params);
                } 
            } catch (IOException e) {
                error(fileUpload, "ImageValidator.ioError");
            }
        } 
    }

	@Override
	public FormComponent<?>[] getDependentFormComponents() {
		return new FormComponent[]{fileUpload};
	}
	
} 
