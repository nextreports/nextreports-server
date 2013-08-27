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

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;

/**
 * @author Decebal Suiu
 */
public class SimpleLink extends WebComponent {

	private static final long serialVersionUID = 1L;
	
	private String url;
	private String text;
	private boolean openUrlInNewWindow;

	public SimpleLink(String id, String url) {
		this(id, url, url);
	}

	public SimpleLink(String id, String url, boolean openUrlInNewWindow) {
		this(id, url, url, openUrlInNewWindow);
	}

	public SimpleLink(String id, String url, String text) {
		this(id, url, text, false);
	}
	
	public SimpleLink(String id, String url, String text, boolean openUrlInNewWindow) {
		super(id);
		
		this.url = url;
		this.text = text;
		this.openUrlInNewWindow = openUrlInNewWindow;
	}

	@Override
	public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
		replaceComponentTagBody(markupStream, openTag, text);
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		tag.setName("a");  
		tag.put("href", url);
        tag.put("class", "link");
        if (openUrlInNewWindow) {
			tag.put("target", "_blank");
		}
	}

}
