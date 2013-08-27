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
package ro.nextreports.server.web.core.settings;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.service.StorageService;


public class LogoResource extends ByteArrayResource {
	
	private static final long serialVersionUID = 1L;

	@SpringBean
    private StorageService storageService;
	
	private boolean scale = false;
	public static final int IMG_WIDTH = 300;
	public static final int IMG_HEIGHT = 61;
	
	public LogoResource() {
		this(false);
	}
	
	public LogoResource(boolean scale) {
		super("image/png");		
		
		this.scale = scale;
		Injector.get().inject(this);
	}
	    
	/*
	public int getWidth() {
		try {
			BufferedImage originalImage = ImageIO.read(getResourceStream().getInputStream());
			return originalImage.getWidth();
		} catch (Exception e) {			
			e.printStackTrace();
			return -1;
		} 
	}
	
	public int getHeight() {
		try {
			
			BufferedImage originalImage = ImageIO.read(getResourceStream().getInputStream());
			return originalImage.getHeight();
		} catch (Exception e) {			
			e.printStackTrace();
			return -1;
		} 
	}
	*/

    @Override
	protected byte[] getData(Attributes attributes) {
        byte[] data = storageService.getLogoImage();
        if (!scale) {
        	return data;
        }
        
		try {
			InputStream is = new ByteArrayInputStream(data);
			BufferedImage originalImage = ImageIO.read(is);						
			int width = IMG_WIDTH; 
			if (originalImage.getWidth() < IMG_WIDTH) {
				width = originalImage.getWidth();
			}
			int height = IMG_HEIGHT;
			if (originalImage.getHeight() < IMG_HEIGHT) {
				height = originalImage.getHeight();
			}
			BufferedImage scaledImage = scaleImage(originalImage, width, height);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(scaledImage, "png", os);
			
			return os.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
    }

    @Override
	protected void setResponseHeaders(ResourceResponse data, Attributes attributes) {
    	// TODO wicket 1.5
    	data.disableCaching();
		super.setResponseHeaders(data, attributes);
    	// http://palisade.plynt.com/issues/2008Jul/cache-control-attributes/
//    	response.setHeader("Cache-Control", "no-cache, max-age=0, no-store, must-revalidate");
    }

	private BufferedImage scaleImage(BufferedImage originalImage, int width, int height) {
		int type = (originalImage.getType() == 0) ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
		BufferedImage resizedImage = new BufferedImage(width, height, type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
		g.dispose();
		g.setComposite(AlphaComposite.Src);		 
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		return resizedImage;
	}
	
}
