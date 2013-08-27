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

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import ro.nextreports.server.web.common.misc.GenericComponent;

/**
 * @author Decebal Suiu
 */
public class GenericPanel<T> extends Panel implements GenericComponent<T> {
	
	private static final long serialVersionUID = 1L;

	public GenericPanel(String id) {
		super(id);
	}

	public GenericPanel(String id, IModel<T> model) {
		super(id, model);
	}

	@SuppressWarnings("unchecked")
	public final IModel<T> getModel() {
		return (IModel<T>) getDefaultModel();
	}

	@SuppressWarnings("unchecked")
	public final T getModelObject() {
		return (T) getDefaultModelObject();
	}

	public final void setModel(IModel<T> model) {
		setDefaultModel(model);
	}

	public final void setModelObject(T object) {
//		setDefaultModelObject(object); // this method compare the old object (deleted in some case) with the new object
		getModel().setObject(object);
	}

}
