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
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import ro.nextreports.server.web.common.behavior.ErrorHighlightBehavior;
import ro.nextreports.server.web.common.behavior.ValidationMessageBehavior;


/**
 * @author Decebal Suiu
 */
public class AdvancedFormVisitor implements IVisitor<FormComponent<?>, Void>, Serializable {

	private static final long serialVersionUID = 1L;
	
	private Set<Component> visited = new HashSet<Component>();

	public void component(FormComponent<?> object, IVisit<Void> visit) {
		Palette<?> palette = object.findParent(Palette.class);
		Component comp;
		if (palette != null) {
			comp = palette;
		} else {
			comp = object;
		}
		if (!visited.contains(comp))	{
			visited.add(comp);
			
			/*
			if (isValidComponent(c)) {
				AdvancedFormComponentLabel label = new AdvancedFormComponentLabel(getLabelId(c), c);
				c.getParent().add(label);
				c.setLabel(new Model<String>(c.getId()));
			}
			*/
			
//			c.setComponentBorder(new RequiredBorder());
			comp.add(new ValidationMessageBehavior());
			comp.add(new ErrorHighlightBehavior());
		}
	}
	
	/*
	private String getLabelId(Component c) {
		return c.getId() + "Label";
	}
	
	private boolean isValidComponent(IFormVisitorParticipant fc) {
		return !(fc instanceof Button);
	}
	*/
	
}
