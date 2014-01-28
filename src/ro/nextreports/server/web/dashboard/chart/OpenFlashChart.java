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
package ro.nextreports.server.web.dashboard.chart;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

import org.apache.wicket.IResourceListener;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.handler.resource.ResourceRequestHandler;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.core.UrlUtil;

/**
 * http://cwiki.apache.org/WICKET/open-flash-chart-and-wicket.html
 */
public class OpenFlashChart extends GenericPanel<String> implements IResourceListener {
	
	private static final long serialVersionUID = 1L;
	
//	private ByteArrayResource jsonResource;
//	private String jsonUrl;
	private String width;
	private String height;
	private SWFObject swf;
	
	private boolean detachedPage;
	
	@SpringBean
	private StorageService storageService;

	public OpenFlashChart(String id, String width, String height, IModel<String> jsonModel) {
		super(id, jsonModel);
		
		this.width = width;
		this.height = height;
		
		// if I call this metod in onResourceRequested() I will obtain a small time for rendering
		// because json string will be created only on flash request
//		createJsonResource();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(OpenFlashChart.class, "saveChartImage.js")));
	}

	@Override
	public void onResourceRequested() {
		//System.out.println("OpenFlashChart.onResourceRequested()");
		//System.out.println("requestUrl = " + RequestCycle.get().getRequest().getUrl());
		//System.out.println("... " + this);
		IResource jsonResource = createJsonResource();
//		IResource.Attributes attrs = new IResource.Attributes(RequestCycle.get().getRequest(), RequestCycle.get().getResponse(), null);
//		jsonResource.respond(attrs);
		IRequestHandler requestHandler = new ResourceRequestHandler(jsonResource, null);
		requestHandler.respond(getRequestCycle());
	}
	
	@Override
	public boolean isVisible() {
		return getModelObject() != null;
	}
	
	@Override
	protected boolean getStatelessHint() {
		return false;
	}

	@Override
	protected void onInitialize() {
		//System.out.println("OpenFlashChart.onInitialize()");
		super.onInitialize();
		
		String swfURL = toAbsolutePath(urlFor(new PackageResourceReference(OpenFlashChart.class, "open-flash-chart.swf"), null).toString());
		
		// see http://ofc2dz.com/OFC2/downloads/ofc2Downloads.html
		// http://ofc2dz.com/OFC2/examples/MiscellaneousPatches.html (Passing the Char Parameter "ID" when saving images (23-Feb-2009))
		// for embedded charts in html pages we also put some randomness at the end (if we have charts from different dashboards id is chart1 for all)
		swfURL = swfURL.concat("?id=").concat(getMarkupId()).concat("&nocache=").concat(UUID.randomUUID().toString());;
		//System.out.println("swfURL = " + swfURL);
		swf = new SWFObject(swfURL, width, height, "9.0.0");
		add(swf);
	}

	@Override
	protected void onBeforeRender() {
		//System.out.println("OpenFlashChart.onBeforeRender()");
		//createJsonResource();
		
		String jsonUrl = getUrlForJson();
		//System.out.println("jsonUrl = " + jsonUrl);
		swf.addParameter("data-file", jsonUrl);
        swf.addParameter("wmode", "transparent");
        
        super.onBeforeRender();
	}
	
	private IResource createJsonResource() {
		//System.out.println("OpenFlashChart.createJsonResource()");
		String jsonData = getJsonData();
		//System.out.println("jsonData = " + jsonData);
		IResource jsonResource = new ByteArrayResource("text/plain", jsonData.getBytes()) {

			private static final long serialVersionUID = 1L;			
						
			// These headers are needed for IE 
			//
			// Pragma & Cache-Control are needed for https (otherwise a #2032 Error will be thrown)
			// see http://dwairi.wordpress.com/2009/01/15/open-flash-chart-ie-and-ssl/
			//
			// Use no-store for Cache-Control & Expires to force IE to not cache flash (otherwise refresh actions &
			// drill-down are not working)
			// see http://www.cfcoffee.co.uk/index.cfm/2010/1/24/IE-and-XML-issue-over-SSL
			//
			@Override
			protected void setResponseHeaders(ResourceResponse data, Attributes attributes) {
				// TODO wicket 1.5
				//data.getHeaders().addHeader("Pragma", "public"); 
				//data.getHeaders().addHeader("Cache-Control", "no-store, must-revalidate");
				//data.getHeaders().addHeader("Expires", "-1");	        
				data.disableCaching();
				super.setResponseHeaders(data, attributes);
			}
			
		};
		
		return jsonResource;
	}

	private String getUrlForJson() { 
//		CharSequence dataPath = RequestCycle.get().urlFor(OpenFlashChart.this, IResourceListener.INTERFACE);
//		CharSequence dataPath = RequestCycle.get().urlFor(new ComponentRenderingRequestHandler(this));
//		System.out.println(jsonResource);
//		CharSequence dataPath = urlFor(new ResourceRequestHandler(jsonResource, null));
		/*
		ResourceReference resourceReference = new ResourceReference("jsonData-" + getMarkupId()) {

			private static final long serialVersionUID = 1L;

			@Override
			public IResource getResource() {
				return jsonResource;
			}
			
		};
		
		// register resource reference
		if (resourceReference.canBeRegistered()) {
			getApplication().getResourceReferenceRegistry().registerResourceReference(resourceReference);
		}
		
//		CharSequence dataPath = urlFor(new ResourceReferenceRequestHandler(resourceReference));
		 */
		
		
		CharSequence dataPath = urlFor(IResourceListener.INTERFACE, null);

		try {
			dataPath = URLEncoder.encode(dataPath.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Error encoding dataPath for Chart Json data file.", e);
		}
		
		return toAbsolutePath(dataPath.toString());				
	}
	
	private String getJsonData() {
		return getModelObject();
	}
	
	private String toAbsolutePath(String path) {
		// server behind Apache, firewall etc
		StringBuffer base = UrlUtil.getAppBaseUrl(storageService);
		// @todo there is a problem with a detached chart seen in a WidgetZoomPage.class
		// this method is called for swf object and for json data
		// in case of detached chart, json data url does not contain "wicket/"
		// path contains "wicket/" for swf object url
		// path contains "widgetList" in dashboards
		// path contains "tabs-panel" when we run chart		
		// path contains "widget%3F" if we have iframe code in an external html
		//if (!path.contains("widgetList") && !path.contains("wicket/") && !path.contains("tabs-panel") && !path.contains("widget%3F")) {
		if (detachedPage) {
			base.append("wicket/");
		}
		String url = base.append(path).toString();		
		return url;
	}
	
	public void setDetachedPage(boolean detachedPage) {
		this.detachedPage = detachedPage;
	}

}
