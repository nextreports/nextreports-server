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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxPreprocessingCallDecorator;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;

/**
 * @author Decebal Suiu
 */
public abstract class AjaxConfirmLink<T> extends AjaxLink<T> {

	private static final long serialVersionUID = 3034440259588339615L;

	protected String message;
	
	public AjaxConfirmLink(String id) {
		super(id);
	}

	public AjaxConfirmLink(String id, String message) {
		super(id);
		this.message = message;
	}

	public AjaxConfirmLink(String id, IModel<T> object) {
		super(id, object);
	}

	public AjaxConfirmLink(String id, IModel<T> object, String message) {
		super(id, object);
		this.message = message;
	}

    public boolean showDialog() {
        return true;
    }	

	public String getMessage() {
		return message;
	}

	@Override
	protected IAjaxCallDecorator getAjaxCallDecorator() {
		return new ConfirmAjaxCallDecorator(super.getAjaxCallDecorator());
	}
	
    protected CharSequence decorateOnSuccessScript(Component c, CharSequence script) {
		return script;
	}

	class ConfirmAjaxCallDecorator extends AjaxPreprocessingCallDecorator {

		private static final long serialVersionUID = 2155228645806565335L;

		public ConfirmAjaxCallDecorator(IAjaxCallDecorator delegate) {
			super(delegate);
		}

		@Override
		public CharSequence preDecorateScript(CharSequence script) {
			if (showDialog()) {
				// doesn't work if you have ' chars in the message
				String message = getMessage().replaceAll("'", "\"");
				String extraJs = "if (!confirm('" + message + "')) return false; ";
				return extraJs + script;
			}
			
			return script;
		}
		
		@Override
		public CharSequence decorateOnSuccessScript(Component c, CharSequence script) {
			return AjaxConfirmLink.this.decorateOnSuccessScript(c, script);
		}				

	}
	
    
	
}
