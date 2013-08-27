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
package ro.nextreports.server.web.common.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * @author Decebal Suiu
 */
public abstract class AbstractAjaxLinkPanel extends Panel {

	private static final long serialVersionUID = 116634505227682256L;

	public AbstractAjaxLinkPanel(String id) {
		super(id);
		add(getLink());
	}

	public abstract String getDisplayString();

	public abstract void onClick(AjaxRequestTarget target);

	@SuppressWarnings("serial")
	protected Link getLink() {
		Link link = new AjaxFallbackLink("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				AbstractAjaxLinkPanel.this.onClick(target);
			}

		};
//		link.add(new SimpleAttributeModifier("class", "links"));
		link.add(new Label("label", getDisplayString()));

		return link;
	}

}
