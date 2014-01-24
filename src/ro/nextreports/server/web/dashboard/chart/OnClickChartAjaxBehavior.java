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

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;

import ro.nextreports.engine.chart.JsonExporter;

/**
 * @author Decebal Suiu
 */
public class OnClickChartAjaxBehavior extends AbstractDefaultAjaxBehavior {

	private static final long serialVersionUID = 1L;
	
	/** Value identifiant into the request */
	private static final String X_VALUE = "value";

	public OnClickChartAjaxBehavior() {
		super();
	}

	public String getOnClickJavaScript() {		
		return "Wicket.Ajax.get('" + getCallbackUrl() 
			+ "&" + X_VALUE + "=" + JsonExporter.X_VALUE 
			+ "', null, null, function() { return true; })";
	}
	
	@Override
	protected void respond(AjaxRequestTarget target) {
		String value = this.getComponent().getRequest().getRequestParameters().getParameterValue(X_VALUE).toString();		
		onClickChart(target, value);
	}

	public void onClickChart(AjaxRequestTarget target, String value) {
		System.out.println("!!!!!!!!!!!!!!!! Click on " + value);
	}

}
