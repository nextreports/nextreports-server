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
package ro.nextreports.server.web.dashboard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Link;
import ro.nextreports.server.service.DashboardService;


public class SelectDashboardPanel  extends Panel {
	
	public static final String COPY_ACTION = "Copy";
	public static final String MOVE_ACTION = "Move";
	
	private String action = COPY_ACTION;
	private Object dashboard;
	
	public SelectDashboardPanel(String id) {
		super(id);
		
		final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);

		Form form = new Form("form");
		
		List<String> actions = new ArrayList<String>();
		actions.add(COPY_ACTION);
		actions.add(MOVE_ACTION);	
		IChoiceRenderer<String>  renderer = new ChoiceRenderer<String> () {
			public Object getDisplayValue(String  object) {
				return getString(object.toString().toLowerCase());
			}

			public String getIdValue(String object, int index) {    
				return object.toString();
			}
	    };
		DropDownChoice<String> actionDropDownChoice = new DropDownChoice<String>("action", new PropertyModel<String>(this, "action"), actions, renderer);		
		form.add(actionDropDownChoice);	
        
		final DropDownChoice<Object> dashboardDropDownChoice = new DropDownChoice<Object>("dashboards", 
				new PropertyModel<Object>(this, "dashboard"), new DashboardsModel(), new DashboardChoiceRenderer());
		dashboardDropDownChoice.setOutputMarkupPlaceholderTag(true);
		dashboardDropDownChoice.setOutputMarkupId(true);
		dashboardDropDownChoice.setRequired(true);
		dashboardDropDownChoice.setLabel(new Model<String>(getString("SelectDashboardPanel.dashboard")));
		form.add(dashboardDropDownChoice);
        
        form.add(new AjaxSubmitLink("move") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {				
				boolean move = MOVE_ACTION.equals(action);
				onAction(getDashboardId(dashboard), move , target);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(feedbackPanel); // show feedback message in feedback common
			}

		});
		form.add(new AjaxLink<Void>("cancel") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}

		});
		
		add(form);
		
	}	
	
	protected String getDashboardId(Object object) {
		if (object instanceof Link) {
			return ((Link) object).getReference();
		}
		return ((Dashboard) object).getId();
	}
	
	class DashboardChoiceRenderer extends ChoiceRenderer<Object> {

		private static final long serialVersionUID = 1L;

		@Override
		public Object getDisplayValue(Object object) {
			String title;
	        if (object instanceof Link) {
	            title = ((Link) object).getName() + " (link)";
	        } else {
	        	title = ((Dashboard) object).getTitle();
	        }
	        return title;
		}

	}
	
	class DashboardsModel extends LoadableDetachableModel<List<Object>> {

		@SpringBean
		private DashboardService dashboardService;
		
		public DashboardsModel() {
			Injector.get().inject(this);
		}
		
		@Override
		protected List<Object> load() {
			List<Object> entities = new ArrayList<Object>();
			entities.addAll(dashboardService.getMyDashboards());
			entities.addAll(dashboardService.getWritableDashboardLinks());
			
			String selectedId = DashboardUtil.getSelectedDashboardId();
			for (Iterator it = entities.iterator(); it.hasNext();) {
				Object obj = it.next();
				if (getDashboardId(obj).equals(selectedId)) {
					it.remove();
					break;
				}
			}
			
			return entities;
		}

	}

	
	protected void onAction(String toDashboardId, boolean move, AjaxRequestTarget target) {		
	}
	
	protected void onCancel(AjaxRequestTarget target) {		
	}


}
