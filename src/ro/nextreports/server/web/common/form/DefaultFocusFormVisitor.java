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
package ro.nextreports.server.web.common.form;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import ro.nextreports.server.web.common.behavior.DefaultFocusBehavior;


/**
 * @author Decebal Suiu
 */
public class DefaultFocusFormVisitor implements IVisitor<Component, Void>, Serializable {

	private static final long serialVersionUID = 1L;

	private final Set<FormComponent<?>> visited = new HashSet<FormComponent<?>>();
	private boolean found = false;

	public void component(Component object, IVisit<Void> visit) {
		if (!visited.contains(object) && (object instanceof FormComponent) && !(object instanceof Button)) {
			final FormComponent<?> fc = (FormComponent<?>) object;
			visited.add(fc);
			if (!found && fc.isEnabled() && fc.isVisible()
					&& (fc instanceof DropDownChoice || fc instanceof AbstractTextComponent)) {
				found = true;
				fc.add(new DefaultFocusBehavior());
			}
		}
	}

}
