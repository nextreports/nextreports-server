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
package ro.nextreports.server.web.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.extensions.markup.html.form.palette.component.Recorder;
import org.apache.wicket.extensions.yui.calendar.DateField;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.engine.Report;
import ro.nextreports.engine.i18n.I18nLanguage;
import ro.nextreports.engine.i18n.I18nUtil;
import ro.nextreports.engine.queryexec.IdName;
import ro.nextreports.engine.queryexec.QueryParameter;
import ro.nextreports.engine.util.DateUtil;
import ro.nextreports.engine.util.ParameterUtil;
import ro.nextreports.engine.util.StringUtil;
import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.domain.ReportRuntimeParameterModel;
import ro.nextreports.server.report.next.NextRuntimeParameterModel;
import ro.nextreports.server.service.DataSourceService;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.ServerUtil;
import ro.nextreports.server.web.common.misc.ExtendedPalette;

/**
 * User: mihai.panaitescu
 * Date: 02-Feb-2010
 * Time: 13:17:03
 */
public abstract class ParameterRuntimePanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	public static final String USER_PARAM = "__USER__";
	
	@SpringBean
    protected StorageService storageService;
	
	@SpringBean
    protected ReportService reportService;

	@SpringBean
    protected DataSourceService dataSourceService;

    protected ParameterRuntimeModel runtimeModel;
    protected List<QueryParameter> paramList;
    private Map<String, QueryParameter> paramMap;
    private Map<QueryParameter, Component> paramComponentsMap;

    protected boolean runNow;    

    // In Next, for source parameters default values are not the entire objects (id, name), but only the ids
    // so we will have to look at selection if the default values can be found in the list of the parameter values
    // and only if we found them we add them to the model

    // all dependent parameters that must be initialized after completing default values
    private transient List<QueryParameter> depParameters = new ArrayList<QueryParameter>();
    // because dependent parameter values where not completed, also the default dependent values where not completed
    // and must be kept for further initialization
    private transient Map<QueryParameter, List<Serializable>> depDefValues = new HashMap<QueryParameter, List<Serializable>>();
    
    private static final Logger LOG = LoggerFactory.getLogger(ParameterRuntimePanel.class);

    public ParameterRuntimePanel(String id) {
        this(id, true);
    }

    public ParameterRuntimePanel(String id, boolean runNow) {
        super(id);
        this.runNow = runNow;
    }

    public ParameterRuntimePanel(String id, ParameterRuntimeModel runtimeModel) {
        super(id);
        this.runNow = true;
        init(runtimeModel);
    }

    public abstract void addWicketComponents();

    public abstract Report getNextReport();
    
    public abstract I18nLanguage getLocaleLanguage();

    public abstract DataSource getDataSource();

    protected void init(ParameterRuntimeModel runtimeModel) {
        this.runtimeModel = runtimeModel;

        paramMap = ParameterUtil.getUsedNotHiddenParametersMap(getNextReport());
        paramList = new ArrayList<QueryParameter>(paramMap.values());
        paramComponentsMap = new HashMap<QueryParameter, Component>();

        addComponents();
        setOutputMarkupId(true);
    }
    
    protected void init(ParameterRuntimeModel runtimeModel, boolean fromGlobalModel) {
        this.runtimeModel = runtimeModel;

        paramMap = ParameterUtil.getUsedNotHiddenParametersMap(getNextReport());
        
        // global settings : we may have less parameters (only common parameters)
        if (fromGlobalModel) {           	
        	List<String> keys = new ArrayList<String>();
        	for (Iterator it=runtimeModel.getParameters().keySet().iterator(); it.hasNext();) {        		        		
        		keys.add((String)it.next());
        	}
        	for (Iterator it = paramMap.keySet().iterator(); it.hasNext();) {
        		if (!keys.contains((String)it.next())) {
        			it.remove();
        		}
        	}
        } 
        paramList = new ArrayList<QueryParameter>(paramMap.values());
        paramComponentsMap = new HashMap<QueryParameter, Component>();

        addComponents();
        setOutputMarkupId(true);
    }

    private void addComponents() {
    	    	
    	// initialize model for some hidden hard-coded parameters
    	// there is possible that a report can contain only hidden hard-coded parameters!
    	for (QueryParameter parameter : ParameterUtil.getUsedHiddenParametersMap(getNextReport()).values()) {			
			if ((USER_PARAM.equals(parameter.getName()))) {
				runtimeModel.getParameters().put(parameter.getName(), createRuntimeModel(parameter));			    
			}
		}
    	
    	// initialize model for all not hidden parameters
		for (QueryParameter parameter : paramList) {			
			if (!runtimeModel.isEdit()) {
				runtimeModel.getParameters().put(parameter.getName(), createRuntimeModel(parameter));			    
			}
		}
        
        if (!runtimeModel.isEdit()) {
            // if some parameters initialized have default values, their dependent parameters
            // have to be initialized too
            for (QueryParameter qp : depParameters) {
                populateDependentParameters(qp, null, true);
            }
        }

        ListView<QueryParameter> listView = new ListView<QueryParameter>("params", new ArrayList<QueryParameter>(paramMap.values())) {

			private static final long serialVersionUID = 1L;

			@Override
            protected void populateItem(ListItem<QueryParameter> item) {
                createItem(item);
            }

        };
        listView.setReuseItems(true);
        add(listView);

        addWicketComponents();
    }

    @SuppressWarnings("unchecked")
    protected void createItem(ListItem<QueryParameter> item) {
        Component currentComponent = null;
        WebMarkupContainer paletteContainer = new WebMarkupContainer("paletteContainer");

        final QueryParameter parameter = item.getModelObject();

        final IModel generalModel = new PropertyModel(runtimeModel.getParameters(), parameter.getName() + ".rawValue");
        IModel listModel = new PropertyModel(runtimeModel.getParameters(), parameter.getName() + ".valueList");
        
        if (runtimeModel.isEdit()) {
            populateDependentParameters(parameter, null, true);
        }

        final TextField textField = new TextField("txtValue", generalModel);
        textField.setVisible(false);
        try {
            textField.setType(Class.forName(parameter.getValueClassName()));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOG.error(e.getMessage(), e);
            error(e.getMessage());
        }
       
        final DateTimeField txtTime = new DateTimeField("txtTime", generalModel) {

			private static final long serialVersionUID = 1L;

			@Override
            public IModel<String> getLabel() {
                return new Model<String>(getParameterName(parameter));
            }

            @Override
			protected boolean use12HourFormat() {
				return false;
			}
            
            @Override
			protected DateTextField newDateTextField(String id, PropertyModel dateFieldModel) {
				DateTextField f = super.newDateTextField(id, dateFieldModel);				
				// Important must create a new ajaxUpdate behavior (otherwise an error will rise)
				// DateTextField uses newDateTextField method in constructor (DateField uses it in onBeforeRenderer method)
				// that's why for DateField is ok to use the same ajax which is added when component is made visible
				// for DateTextField ajax behavior is added even if the component is not visible!
				f.add(createAjax(parameter, generalModel, f));				
				return f;
			}
            
            protected DatePicker newDatePicker() {
        		return new DatePicker() {
        			private static final long serialVersionUID = 1L;

        			@Override
        			protected void configure(final Map<String, Object> widgetProperties,
        				final IHeaderResponse response, final Map<String, Object> initVariables) {
        				super.configure(widgetProperties, response, initVariables);        				
        			}

					@Override
					protected boolean enableMonthYearSelection() {
						return true;
					}        			        			
        		};
        	}

        };  
        // add ajax update behavior on hours and minutes textfields 
        txtTime.get("hours").add(createAjax(parameter, generalModel, txtTime.get("hours"), "hours"));
        txtTime.get("minutes").add(createAjax(parameter, generalModel, txtTime.get("minutes"), "minutes"));
        txtTime.setVisible(false);
        
        final DateField txtDate = new DateField("txtDate", generalModel) {

			private static final long serialVersionUID = 1L;

			@Override
            public IModel<String> getLabel() {
                return new Model<String>(getParameterName(parameter));
            }    
			
			@Override
			protected DateTextField newDateTextField(java.lang.String id, PropertyModel dateFieldModel) {				
				DateTextField f = super.newDateTextField(id, dateFieldModel);												
				f.add(createAjax(parameter, generalModel, f));
				return f;
			}
			
			protected DatePicker newDatePicker() {
        		return new DatePicker() {
        			private static final long serialVersionUID = 1L;

        			@Override
        			protected void configure(final Map<String, Object> widgetProperties,
        				final IHeaderResponse response, final Map<String, Object> initVariables) {
        				super.configure(widgetProperties, response, initVariables);        				
        			}

					@Override
					protected boolean enableMonthYearSelection() {
						return true;
					}        			        			
        		};
        	}

        };        
        txtDate.setVisible(false);

        final CheckBox chkBox = new CheckBox("chkBox", generalModel);
        chkBox.setVisible(false);

        DropDownChoice downChoice = new DropDownChoice("cmbValue", generalModel, new ArrayList<String>());
        downChoice.setVisible(false);

        if (parameter.getSelection().equalsIgnoreCase(QueryParameter.SINGLE_SELECTION)) {
            if (parameter.getSource() != null && parameter.getSource().trim().length() > 0) {
                // combo
                downChoice = new DropDownChoice("cmbValue", generalModel, new LoadableDetachableModel() {

                    @Override
                    protected Object load() {
                        // for combo default value can be a simple value or selected for a source (is not an IdName so we make it)
                        // a default value from a manual source is an IdName
                        Object obj = generalModel.getObject();
                        if (! (obj instanceof IdName) ) {
                            IdName in  = new IdName();
                            in.setId((Serializable)obj);
                            in.setName((Serializable)obj);
                            generalModel.setObject(in);
                        }
                        return runtimeModel.getParameters().get(parameter.getName()).getValues();
                    }

                });
                if (!parameter.isHidden()) {
                    if (parameter.isMandatory()) {
                        downChoice.setRequired(true);
                    }
                    downChoice.setLabel(new Model<String>(getParameterName(parameter)));
                    downChoice.setVisible(true);
                }
                currentComponent = downChoice;
            } else {
                // not combo
                if (parameter.getValueClassName().contains("Date")) {
                	if (!parameter.isHidden()) {
                        if (generalModel.getObject() == null) {
                            generalModel.setObject(DateUtil.floor(new Date()));                                                        
                        }
                        if (parameter.isMandatory()) {
                        	txtDate.setRequired(true);
                        }
                        txtDate.setVisible(true);
                    }                	
                    currentComponent = txtDate;
                } else if(parameter.getValueClassName().contains("Timestamp") ||
                    parameter.getValueClassName().contains("Time")    ) {
                    if (!parameter.isHidden()) {
                        if (generalModel.getObject() == null) {
                            generalModel.setObject(DateUtil.floor(new Date()));                                                        
                        }
                        if (parameter.isMandatory()) {
                        	txtTime.setRequired(true);
                        }
                        txtTime.setVisible(true);
                    }
                    currentComponent = txtTime;
                } else if (parameter.getValueClassName().contains("Boolean")) {
                    if (!parameter.isHidden()) {
                        if (parameter.isMandatory()) {
                            chkBox.setRequired(true);
                        }
                        chkBox.setLabel(new Model<String>(getParameterName(parameter)));
                        chkBox.setVisible(true);
                    }
                    currentComponent = chkBox;
                } else {
                    if (!parameter.isHidden()) {
                        if (parameter.isMandatory()) {
                            textField.setRequired(true);
                        }
                        textField.setLabel(new Model<String>(getParameterName(parameter)));
                        textField.setVisible(true);
                    }
                    currentComponent = textField;
                }
            }

            paletteContainer.add(new EmptyPanel("palette"));
        } else {
            if (parameter.getSource() != null && parameter.getSource().trim().length() > 0) {
                if (!parameter.isHidden()) {
                    Palette palette = createPalette(parameter, listModel, createAjax(parameter));
                    paletteContainer.add(palette.setOutputMarkupId(true));
                    currentComponent = palette;                    
                } else {
                    paletteContainer.add(new EmptyPanel("palette"));
                }
            } else {
                ManualListPanel list = new ManualListPanel(parameter, listModel, 10, createAjax(parameter));
                paletteContainer.add(list.setOutputMarkupId(true));
                currentComponent = list;
            }

        }

        paramComponentsMap.put(parameter, currentComponent);

        // if this parameter has dependent parameters
        // we must update values for those using an ajax update
        // for Palette this is done in its class
        // for DateField and DateTimeField is done on the inner DateTextField        
        if (ParameterUtil.getChildDependentParameters(getNextReport(), parameter).size() > 0) {
        	boolean ajaxAlreadyAdded = (currentComponent instanceof Palette) ||
			   (currentComponent instanceof DateField) ||
			   (currentComponent instanceof DateTimeField);        	        	
        	if (!ajaxAlreadyAdded) {                  		
                currentComponent.add(createAjax(parameter));
            }
        }

        String name = getDisplayableParameterName(parameter);
        Label lbl = new Label("name", name);
        lbl.setEscapeModelStrings(false);
        lbl.setOutputMarkupId(true);
        item.add(lbl);
        txtTime.setOutputMarkupId(true);
        item.add(txtTime);
        txtDate.setOutputMarkupId(true);
        item.add(txtDate);
        downChoice.setOutputMarkupId(true);
        item.add(downChoice);
        paletteContainer.setOutputMarkupId(true);
        item.add(paletteContainer);
        textField.setOutputMarkupId(true);
        item.add(textField);
        chkBox.setOutputMarkupId(true);
        item.add(chkBox);
    }

    private NextRuntimeParameterModel createRuntimeModel(QueryParameter parameter) {
        boolean isMultipleSelection = parameter.getSelection().equalsIgnoreCase(QueryParameter.MULTIPLE_SELECTION);
        NextRuntimeParameterModel runtimeModel = new NextRuntimeParameterModel(parameter.getName(), getParameterName(parameter), isMultipleSelection);
        runtimeModel.setMandatory(parameter.isMandatory());

        List<IdName> values = new ArrayList<IdName>();
        // set in the model only the values for parameters which are not dependent
        if ((parameter.getSource() != null) && (parameter.getSource().trim().length() > 0)
                && !parameter.isDependent()) {
            try {
                values = dataSourceService.getParameterValues(getDataSource(), parameter);
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("Get parameter values for : " + parameter.getName() + "  > " + e.getMessage(), e);
                error(e.getMessage());                
            }
        }
        runtimeModel.setParameterValues(values);                

        initDefaultValues(runtimeModel, parameter, values);

        return runtimeModel;
    }

    private void initDefaultValues(NextRuntimeParameterModel runtimeModel, QueryParameter parameter, List<IdName> values) {
        List<Serializable> defaultValues = new ArrayList<Serializable>();
        if ((parameter.getDefaultValues() != null)) {
            defaultValues = parameter.getDefaultValues();
        }
        if ((parameter.getDefaultSource() != null) && (parameter.getDefaultSource().trim().length() > 0)) {
            try {
                defaultValues = dataSourceService.getDefaultSourceValues(getDataSource(), parameter);
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("Get default source values for parameter : " + parameter.getName() + "  > " + e.getMessage(), e);
                error(e.getMessage());
            }
        }

        depDefValues.put(parameter, defaultValues);
        if (defaultValues.size() == 0) {
            return;
        }

        // for source parameters, the values are not entire objects (id, name) but only the ids
        // so we have to look in the parameter values for them
        if ((parameter.getSource() != null) && !parameter.getSource().trim().equals("")) {
            defaultValues = getSelectedValues(values, defaultValues);
        }
        if (defaultValues.size() == 0) {
            return;
        }
        boolean populateDependent = false;
        if  (USER_PARAM.equals(parameter.getName())) {
            runtimeModel.setRawValue(ServerUtil.getUsernameWithoutRealm());
            populateDependent  = true;
		} else {
			if (QueryParameter.MULTIPLE_SELECTION.equals(parameter.getSelection())) {
				if (defaultValues.size() == 0) {
					runtimeModel.setValueList(new ArrayList<Object>());
				} else {
					ArrayList<Object> list = new ArrayList<Object>();
					list.addAll(defaultValues);
					runtimeModel.setValueList(list);
					populateDependent = true;
				}
			} else {
				if (defaultValues.size() > 0) {
					runtimeModel.setRawValue(defaultValues.get(0));
					populateDependent = true;
				}
			}
		}
        // mark the dependent parameters that must be populated after initilize the default values
        if (populateDependent) {
            this.runtimeModel.getParameters().put(parameter.getName(), runtimeModel);
            depParameters.add(parameter);
        }
    }

    // a default value must be simple java object , or an IdName but only with the id
    private List<Serializable> getSelectedValues(List<IdName> values, List<Serializable> defaultValues) {
        List<Serializable> selectedValues = new ArrayList<Serializable>();
        if (defaultValues == null) {
            return selectedValues;
        }
        for (Serializable s : defaultValues) {
            for  (IdName in : values) {
                if (s instanceof IdName) {
                    if ((in.getId() != null) && in.getId().equals( ((IdName)s).getId())) {
                        selectedValues.add(in);
                        break;
                    }
                } else if ((in.getId() != null) && in.getId().equals(s)) {
                    selectedValues.add(in);
                    break;
                }
            }
        }
        
        return selectedValues;
    }

     private List<Object> getSelectedValuesAsObject(List<Serializable> defaultValues) {
        List<Object> selectedValues = new ArrayList<Object>();
        for (Serializable s  : defaultValues) {
            selectedValues.add(s);
        }
        
         return selectedValues;
     }

    private AjaxFormComponentUpdatingBehavior createAjax(final QueryParameter parameter) {
        return new AjaxFormComponentUpdatingBehavior("onchange") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                populateDependentParameters(parameter, target, false);
            }

        };
    }
    
    private AjaxFormComponentUpdatingBehavior createAjax(final QueryParameter parameter, final IModel model, final DateTextField dateField) {
        return new AjaxFormComponentUpdatingBehavior("onchange") {

            @SuppressWarnings("unchecked")
			@Override
            protected void onUpdate(AjaxRequestTarget target) {
            	// @todo wicket 1.5 does not update model for DateField and DateTimeField
                // https://issues.apache.org/jira/browse/WICKET-4496	
                // use this as an workaround	
				model.setObject(dateField.getDefaultModelObject());
				
                populateDependentParameters(parameter, target, false);
            }

        };
    }
    
    // @todo wicket 1.5
    // used to update hours and minutes
    private AjaxFormComponentUpdatingBehavior createAjax(final QueryParameter parameter, final IModel model, final Component component, final String time) {
        return new AjaxFormComponentUpdatingBehavior("onchange") {

            @SuppressWarnings("unchecked")
			@Override
            protected void onUpdate(AjaxRequestTarget target) {
            	// @todo wicket 1.5 does not update model for DateField and DateTimeField
                // https://issues.apache.org/jira/browse/WICKET-4496	
                // use this as an workaround	
            	if ((model == null) || (model.getObject() == null)) {
            		return;
            	}
            	Date date = (Date)model.getObject();
            	if ("hours".equals(time)) {
            		date = DateUtil.setHours(date, (Integer)component.getDefaultModelObject());
            	} else if ("minutes".equals(time)) {
            		date = DateUtil.setMinutes(date, (Integer)component.getDefaultModelObject());
            	}            	
				model.setObject(date);
				
                populateDependentParameters(parameter, target, false);
            }

        };
    }

    private void populateDependentParameters(QueryParameter parameter, AjaxRequestTarget target, boolean recursive) {
        Report nextReport = getNextReport();
        if (nextReport == null) {
        	return;
        }
        Map<String, QueryParameter> childParams = ParameterUtil.getChildDependentParameters(nextReport, parameter);        

        // update model parameter values for every child parameter
        for (QueryParameter childParam : childParams.values()) {        	
            if (!paramList.contains(childParam)) {
                continue;
            }

            Component childComponent = paramComponentsMap.get(childParam);

            List<IdName> values = new ArrayList<IdName>();
            boolean allParentsHaveValues = true;

            Map<String, QueryParameter> allParentParams = ParameterUtil.getParentDependentParameters(nextReport, childParam);
            for (QueryParameter parentParam : allParentParams.values()) {            	
                if (runtimeModel.getParameters().get(parentParam.getName()).getProcessingValue() == null) {
                    allParentsHaveValues = false;
                    break;
                }
            }

            if ((childParam.getSource() != null) && (childParam.getSource().trim().length() > 0)
                    && allParentsHaveValues) {
                try {
                    Map<String, Serializable> allParameterValues = new HashMap<String, Serializable>();

                    for (String name : runtimeModel.getParameters().keySet()) {
                        ReportRuntimeParameterModel model = runtimeModel.getParameters().get(name);
                        allParameterValues.put(model.getName(), (Serializable) model.getProcessingValue());
                    }
                    
                    //System.out.println("!!!! map = " + ParameterUtil.getDebugParameters(allParameterValues));
                    
                    values = dataSourceService.getDependentParameterValues(
                            getDataSource(), childParam, paramMap, allParameterValues);
                } catch (Exception e) {
                    e.printStackTrace();
                    LOG.error("Get dependent parameter values for : " + childParam.getName() + "  > " + e.getMessage(), e);
                    error(e.getMessage());
                }
            }

            NextRuntimeParameterModel parameterModel = (NextRuntimeParameterModel) runtimeModel.getParameters().get(childParam.getName());

            // if nothing is selected for that parameter (see scheduler) we look for the default values
            if ((parameterModel.getValueList() == null) || (parameterModel.getValueList().size() == 0)) {
                List<Serializable> list = getSelectedValues(values, depDefValues.get(childParam));
                if (list.size() > 0) {
                    parameterModel.setValueList(getSelectedValuesAsObject(list));
                }
            }
            if (values != null && values.size() > 0) {
                parameterModel.setParameterValues(values);
            } else {
                parameterModel.setParameterValues(new ArrayList<IdName>());
            }

            if (target != null) {
                target.add(childComponent);
            }

            if (recursive) {
                populateDependentParameters(childParam, target, true);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Palette createPalette(QueryParameter parameter, IModel listModel, AjaxFormComponentUpdatingBehavior ajaxUpdate) {
        ParameterPalette parameterPalette = new ParameterPalette(parameter,  listModel, 10);
        parameterPalette.setUpdatingBehavior(ajaxUpdate);
        
        return parameterPalette;
    }

    private String getDisplayableParameterName(QueryParameter parameter) {
        if (parameter.isHidden()) {
            return "";
        }
        String name = getParameterName(parameter);
        if (parameter.isMandatory()) {
            name += "&nbsp;<em>*</em>";
        }

        return name;
    }

    private String getParameterName(QueryParameter parameter) {
        String name = parameter.getRuntimeName();
        if ((name == null) || name.trim().equals("")) {
            name = parameter.getName();
        } else {
        	name = StringUtil.getI18nString(name,getLocaleLanguage());  
        }        
        return name;
    }    
        
    class ParameterPalette extends ExtendedPalette<Object> {
    	
		private static final long serialVersionUID = 1L;
		
		private QueryParameter parameter;
		private AjaxFormComponentUpdatingBehavior updatingBehavior;

		public ParameterPalette(QueryParameter parameter, IModel<List<Object>> listModel, int rows) {
    		//super("palette", listModel,  new ParameterChoicesModel(parameter), new ParameterChoiceRenderer(), rows, false);
    		super("palette", listModel,  new ParameterChoicesModel(parameter), new ParameterChoiceRenderer(), rows, false, true);
    		this.parameter = parameter;
    	}
    	
		public void setUpdatingBehavior(AjaxFormComponentUpdatingBehavior updatingBehavior) {
			this.updatingBehavior = updatingBehavior;
		}
		        
		@Override
        protected Recorder<Object> newRecorderComponent() {
            Recorder<Object> recorder = super.newRecorderComponent();
            String paramaterName = getParameterName(parameter);
            recorder.setLabel(new Model<String>(paramaterName));
            
            if (!parameter.isHidden()) {
                if (parameter.isMandatory()) {
                    recorder.setRequired(true);
                }
            }

            if (ParameterUtil.getChildDependentParameters(getNextReport(), parameter).size() > 0) {
                recorder.add(updatingBehavior);
            }

            return recorder;
        }

    }
    
    class ParameterChoicesModel extends LoadableDetachableModel<List<IdName>> {
    	
		private static final long serialVersionUID = 1L;
		
		private QueryParameter parameter;
    	
    	public ParameterChoicesModel(QueryParameter parameter) {
    		this.parameter = parameter;
    	}
    	
        @Override
        protected List<IdName> load() {
        	String parameterName = parameter.getName();
        	List<IdName> list = runtimeModel.getParameters().get(parameterName).getValues();
        	
            return list.size() > 0 ? list : new ArrayList<IdName>();
        }

    }
    
    class ParameterChoiceRenderer implements IChoiceRenderer<Object> {
    	
		private static final long serialVersionUID = 1L;

		public Object getDisplayValue(Object object) {
            IdName value = (IdName) object;
            return (value.getName() == null) ? value.getId() : value.getName();
        }

        public String getIdValue(Object object, int index) {
            if (object == null) {
                return "";
            }

            if (!(object instanceof IdName)) {
                return object.toString();
            }

            IdName value = (IdName) object;
            if (value.getId() == null) {
                return Integer.toString(index);
            }            

            Object returnValue = value.getId();
            if (returnValue == null) {
                return "";
            }

            // IMPORTANT : if values start or end with space , on submit first rawValue will be ignored!
            // so we assure that the id never starts or ends with space!
            //
            // replace comma with other character (otherwise values with comma will be interpreted as two 
            // values and no selection will be done for them)
            return "@" + returnValue.toString().replace(",", "-") + "@";
        }

    }       
    
    public boolean hasPalette() {
    	for (QueryParameter parameter : paramList) {
    		if (parameter.getSelection().equalsIgnoreCase(QueryParameter.MULTIPLE_SELECTION)) {
    			if (parameter.getSource() != null && parameter.getSource().trim().length() > 0) {
                    if (!parameter.isHidden()) {
                    	return true;
                    }
    			}    
    		}
    	}	
    	return false;
    }
    
}
