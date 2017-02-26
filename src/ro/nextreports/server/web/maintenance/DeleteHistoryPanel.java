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
package ro.nextreports.server.web.maintenance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.datetime.StyleDateConverter;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.calendar.DateField;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.engine.util.DateUtil;
import ro.nextreports.server.domain.DateRange;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.RunReportHistory;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.common.behavior.AlertBehavior;
import ro.nextreports.server.web.common.misc.AjaxSubmitConfirmLink;

//
public class DeleteHistoryPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean
	private ReportService reportService;

	@SpringBean
	private StorageService storageService;

	private Date time;
	private Date tillTime;
	private DateField tillTimeField;
	private FeedbackPanel feedbackPanel;

	private static String DAY_TYPE = "Day";
	private static String FROM_DAY_TYPE = "DayRange";

	private String type = DAY_TYPE;

	private String reportPath = null;

	@SuppressWarnings("unchecked")
	public DeleteHistoryPanel(String id, final Report report) {
		super(id);

		if (report != null) {
			reportPath = report.getPath();
		}

		Form<RunReportHistory> form = new Form<RunReportHistory>("form");

		feedbackPanel = new FeedbackPanel("feedback");
		feedbackPanel.setOutputMarkupId(true);
		form.add(feedbackPanel);

		List<String> types = new ArrayList<String>();
		types.add(DAY_TYPE);
		types.add(FROM_DAY_TYPE);
		IChoiceRenderer<String> renderer = new ChoiceRenderer<String>() {

			@Override
			public Object getDisplayValue(String object) {
				return getString("ActionContributor.RunHistory." + object);
			}

			@Override
			public String getIdValue(String object, int index) {
				return object;
			}

		};
		DropDownChoice<String> typeDropDownChoice = new DropDownChoice<String>("type",
				new PropertyModel<String>(this, "type"), types, renderer);
		typeDropDownChoice.setOutputMarkupPlaceholderTag(true);
		typeDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				setDateRange(target);
			}

		});
		form.add(typeDropDownChoice);

		time = new Date();
		DateField timeField = new DateField("time", new PropertyModel(this, "time")) {

			protected DateTextField newDateTextField(String s, PropertyModel propertyModel) {

				final DateTextField dateField = DateTextField.withConverter(s, propertyModel,
						new StyleDateConverter("S-", false));
				dateField.add(new AjaxFormComponentUpdatingBehavior("onchange") {
					protected void onUpdate(AjaxRequestTarget target) {
						// @todo wicket 1.5 does not update model for DateField
						// and DateTimeField
						// https://issues.apache.org/jira/browse/WICKET-4496
						// use this as an workaround
						time = (Date) dateField.getDefaultModelObject();

						setDateRange(target);
					}
				});
				dateField.setLabel(new Model<String>(getString("ActionContributor.RunHistory.day")));
				return dateField;
			}

			protected DatePicker newDatePicker() {
				return new DatePicker() {
					private static final long serialVersionUID = 1L;

					@Override
					protected void configure(final Map<String, Object> widgetProperties, final IHeaderResponse response,
							final Map<String, Object> initVariables) {
						super.configure(widgetProperties, response, initVariables);
					}

					@Override
					protected boolean enableMonthYearSelection() {
						return true;
					}
				};
			}
		};
		timeField.setRequired(true);
		timeField.add(new AttributeModifier("class", "timeField"));
		form.add(timeField);

		tillTime = new Date();
		tillTimeField = new DateField("tillTime", new PropertyModel(this, "tillTime")) {
			protected DateTextField newDateTextField(String s, PropertyModel propertyModel) {
				final DateTextField dateField = DateTextField.withConverter(s, propertyModel,
						new StyleDateConverter("S-", false));
				dateField.add(new AjaxFormComponentUpdatingBehavior("onchange") {
					protected void onUpdate(AjaxRequestTarget target) {
						// @todo wicket 1.5 does not update model for DateField
						// and DateTimeField
						// https://issues.apache.org/jira/browse/WICKET-4496
						// use this as an workaround
						tillTime = (Date) dateField.getDefaultModelObject();

						setDateRange(target);
					}
				});
				dateField.setLabel(new Model<String>(getString("ActionContributor.RunHistory.TillDay")));
				return dateField;
			}

			protected DatePicker newDatePicker() {
				return new DatePicker() {
					private static final long serialVersionUID = 1L;

					@Override
					protected void configure(final Map<String, Object> widgetProperties, final IHeaderResponse response,
							final Map<String, Object> initVariables) {
						super.configure(widgetProperties, response, initVariables);
					}

					@Override
					protected boolean enableMonthYearSelection() {
						return true;
					}
				};
			}
		};
		tillTimeField.setVisible(false);
		tillTimeField.setOutputMarkupId(true);
		tillTimeField.setOutputMarkupPlaceholderTag(true);
		tillTimeField.add(new AttributeModifier("class", "timeField"));
		form.add(tillTimeField);

		AjaxSubmitConfirmLink deleteByRangeLink = new AjaxSubmitConfirmLink("deleteRangeLink",
				getString("deleteEntities")) {
			public void onSubmit(AjaxRequestTarget target, Form<?> form) {
				try {
					DateRange range = null;
					if (DAY_TYPE.equals(type)) {
						tillTime = new Date();
						range = new DateRange(DateUtil.floor(time), DateUtil.ceil(time));
					} else {
						if (DateUtil.after(time, tillTime)) {
							error(getString("ActionContributor.RunHistory.rangeInvalid"));
							target.add(feedbackPanel);
							target.add(tillTimeField);
							return;
						}
						range = new DateRange(DateUtil.floor(time), DateUtil.ceil(tillTime));
					}
					long deleted = reportService.deleteRunHistoryForRange(reportPath, range, false);

					info(getString("ActionContributor.RunHistory.deleteByRangeDone") + " {" + deleted + "}");

					target.add(feedbackPanel);
				} catch (Exception e) {
					e.printStackTrace();
					add(new AlertBehavior(e.getMessage()));
					target.add(this);
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(form);
			}

		};
		form.add(deleteByRangeLink);

		if (NextServerSession.get().isDemo()) {
			deleteByRangeLink.setVisible(false);
		}

		add(form);
	}

	private void setDateRange(AjaxRequestTarget target) {
		if (DAY_TYPE.equals(type)) {
			tillTime = new Date();
			tillTimeField.setVisible(false);
		} else {
			tillTimeField.setVisible(true);
			if (DateUtil.after(time, tillTime)) {
				error(getString("ActionContributor.RunHistory.rangeInvalid"));
				target.add(feedbackPanel);
				target.add(tillTimeField);
				return;
			}
		}
		target.add(tillTimeField);
		target.add(feedbackPanel);
	}

}
