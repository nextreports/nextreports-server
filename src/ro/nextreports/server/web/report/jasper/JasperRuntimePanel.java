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
package ro.nextreports.server.web.report.jasper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.extensions.markup.html.form.palette.component.Recorder;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.ReportRuntimeParameterModel;
import ro.nextreports.server.report.ExternalParameter;
import ro.nextreports.server.report.ReportConstants;
import ro.nextreports.server.report.jasper.JasperParameter;
import ro.nextreports.server.report.jasper.JasperParameterSource;
import ro.nextreports.server.report.jasper.JasperReportsUtil;
import ro.nextreports.server.report.jasper.JasperRuntimeParameterModel;
import ro.nextreports.server.service.DataSourceService;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.ServerUtil;
import ro.nextreports.server.web.common.misc.ExtendedPalette;
import ro.nextreports.server.web.core.MessageErrorPage;
import ro.nextreports.server.web.report.ManualListPanel;
import ro.nextreports.server.web.report.ReportRuntimeModel;

import ro.nextreports.engine.queryexec.IdName;
import ro.nextreports.engine.queryexec.QueryParameter;

public class JasperRuntimePanel extends Panel {

    public static final String USER_PARAM = "__USER__";

    private Report report;

    @SpringBean
    private ReportService reportService;
    
    @SpringBean
    private StorageService storageService;
    
    @SpringBean
    private DataSourceService dataSourceService;

    private static List<String> typeList;

    private Map<String, Serializable> paramMap;
    private Map<String, QueryParameter> convertMap;
    private List<QueryParameter> convertList;
    private Map<QueryParameter, Component> paramComponentsMap;

    private ReportRuntimeModel runtimeModel;

    // all dependent parameters that must be initialized after completing default values
    private transient List<QueryParameter> depParameters = new ArrayList<QueryParameter>();

    public JasperRuntimePanel(String id, final Report report, ReportRuntimeModel runtimeModel) {
        super(id);        
        typeList = reportService.getSupportedOutputs(report);

        this.report = report;
        this.runtimeModel = runtimeModel;

        if (runtimeModel.getExportType() == null) {
            runtimeModel.setExportType(ReportConstants.HTML_FORMAT);
        }

        try {
            paramMap = reportService.getReportUserParameters(report, new ArrayList<ExternalParameter>());
            convertMap = convert(report.getDataSource(), paramMap);
            convertList = new LinkedList<QueryParameter>(convertMap.values());
            paramComponentsMap = new HashMap<QueryParameter, Component>();
        } catch (Exception e) {
            //@todo alert
            e.printStackTrace();
            getSession().error(e.getMessage());
            throw new RestartResponseException(new MessageErrorPage(e.getMessage() + " : Verify the parameters sources."));

        }
        
        addComponents();
    }

    @SuppressWarnings("unchecked")
    private Map<String, QueryParameter> convert(DataSource ds, Map<String, Serializable> params) throws Exception {
        Map<String, QueryParameter> result = new LinkedHashMap<String, QueryParameter>();
        Set s = params.entrySet();
        for (Object value : s) {
            Map.Entry<String, Serializable> me = (Map.Entry<String, Serializable>) value;
            JasperParameter jp = (JasperParameter) me.getValue();
            String className = JasperReportsUtil.getValueClassName(storageService, ds, jp);
            QueryParameter qp = new QueryParameter(jp.getName(), jp.getDescription(), className);
            //System.out.println(">>> Source = " + jp.getSelect());            
            qp.setSource(jp.getSelect());
            qp.setMandatory(jp.isMandatory());
            qp.setRuntimeName(jp.getShortName());
            if (JasperParameterSource.LIST.equals(jp.getType())) {
                qp.setSelection(QueryParameter.MULTIPLE_SELECTION);
                qp.setManualSource(true);
            } else if (JasperParameterSource.COMBO.equals(jp.getType())) {
                qp.setSelection(QueryParameter.SINGLE_SELECTION);
                qp.setManualSource(true);
            } else {
                // SINGLE
                qp.setSelection(QueryParameter.SINGLE_SELECTION);
                qp.setManualSource(false);
            }
            //System.out.println("Convert qp=" + qp.getName() + "  " + qp.getSelection() + "  " + qp.getValueClass());
            result.put(me.getKey(), qp);
        }
        
        return result;
    }

    // populate dependent parameters after all items (createItem) have been created!
    protected void onBeforeRender() {
        super.onBeforeRender();
        if (runtimeModel.isEdit()) {
            for (QueryParameter parameter : convertMap.values()) {
                populateDependentParameters(parameter, null);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addComponents() {

        // initialize model
        for (QueryParameter parameter : convertList) {
            if (!runtimeModel.isEdit() || (USER_PARAM.equals(parameter.getName()))) {
                JasperRuntimeParameterModel jasperRuntimeModel = createRuntimeModel(parameter);
                runtimeModel.getParameters().put(parameter.getName(), jasperRuntimeModel);
            }
        }
        if (!runtimeModel.isEdit()) {
            // if some parameters initialized have default values, their dependent parameters
            // have to be initialized too
            for (QueryParameter qp : depParameters) {
                populateDependentParameters(qp, null);
            }
        }

        ListView<QueryParameter> listView = new ListView<QueryParameter>("params", new ArrayList<QueryParameter>(convertMap.values())) {

            @Override
            protected void populateItem(ListItem<QueryParameter> item) {
                createItem(item);
            }

        };
        listView.setReuseItems(true);
        add(listView);

        add(new DropDownChoice("exportType", new PropertyModel(runtimeModel, "exportType"), typeList).setRequired(true));                
    }

    @SuppressWarnings("unchecked")
    private void createItem(ListItem<QueryParameter> item) {

        WebMarkupContainer paletteContainer = new WebMarkupContainer("paletteContainer");
        
        final QueryParameter parameter = item.getModelObject();

        IModel generalModel = new PropertyModel(runtimeModel.getParameters(), parameter.getName() + ".rawValue");
        IModel listModel = new PropertyModel(runtimeModel.getParameters(), parameter.getName() + ".valueList");

        AjaxFormComponentUpdatingBehavior ajaxUpdate = createAjax(parameter);


        final TextField textField = new TextField("txtValue", generalModel);
        textField.setVisible(false);
        try {
            textField.setType(Class.forName(parameter.getValueClassName()));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            error(e.getMessage());
        }

        final DateTextField txtDate = new DateTextField("txtDate", generalModel) {
        	
        	@Override
            public IModel<String> getLabel() {
                return new Model<String>(getParameterName(parameter));
            }
            
        };
        txtDate.add(new DatePicker() {
        	
        	@Override
            protected boolean enableMonthYearSelection() {
                return true;
            }
            
        });
        txtDate.setVisible(false);

        final CheckBox chkBox = new CheckBox("chkBox", generalModel);
        chkBox.setVisible(false);

        DropDownChoice downChoice = new DropDownChoice("cmbValue", generalModel, new ArrayList<String>());
        downChoice.setVisible(false);

        Component currentComponent;
        if (parameter.getSelection().equalsIgnoreCase(QueryParameter.SINGLE_SELECTION)) {
            if (parameter.getSource() != null && parameter.getSource().trim().length() > 0) {
                // combo
                downChoice = new DropDownChoice("cmbValue", generalModel, new LoadableDetachableModel() {
                    @Override
                    protected Object load() {
                        return runtimeModel.getParameters().get(parameter.getName()).getValues();
                    }
                });
                if (parameter.isMandatory()) {
                    downChoice.setRequired(true);
                }
                downChoice.setLabel(new Model<String>(getParameterName(parameter)));
                downChoice.setVisible(true);
                currentComponent = downChoice;
            } else {
                // not combo
                if (parameter.getValueClassName().contains("Date")) {
                    if (parameter.isMandatory()) {
                        txtDate.setRequired(true);
                    }
                    txtDate.setVisible(true);
                    currentComponent = txtDate;
                } else if (parameter.getValueClassName().contains("Boolean")) {
                    if (parameter.isMandatory()) {
                        chkBox.setRequired(true);
                    }
                    chkBox.setLabel(new Model<String>(getParameterName(parameter)));
                    chkBox.setVisible(true);
                    currentComponent = chkBox;
                } else {
                    if (parameter.isMandatory()) {
                        textField.setRequired(true);
                    }
                    textField.setLabel(new Model<String>(getParameterName(parameter)));
                    textField.setVisible(true);
                    currentComponent = textField;
                }
            }

            paletteContainer.add(new EmptyPanel("palette"));
        } else {
            if (parameter.getSource() != null && parameter.getSource().trim().length() > 0) {
                Palette palette = createPalette(parameter, listModel, ajaxUpdate);
                paletteContainer.add(palette.setOutputMarkupId(true));
                currentComponent = palette;
            } else {
                ManualListPanel list = new ManualListPanel(parameter, listModel, 10, ajaxUpdate);
                paletteContainer.add(list.setOutputMarkupId(true));
                currentComponent = list;
            }
        }

        if (USER_PARAM.equals(parameter.getName())) {
            currentComponent.setEnabled(false);
        }

        paramComponentsMap.put(parameter, currentComponent);

        // if this parameter has dependent parameters
        // we must update values for those using an ajax update
        // for Palette this is done in its class
        if (getChildDependentParameters(parameter).size() > 0) {
            if (!(currentComponent instanceof Palette)) {
                currentComponent.add(ajaxUpdate);
            }
        }

        String name = getDisplayableParameterName(parameter);
        Label lbl = new Label("name", name);
        lbl.setEscapeModelStrings(false);
        item.add(lbl);
        item.add(txtDate.setOutputMarkupId(true));
        item.add(downChoice.setOutputMarkupId(true));
        item.add(paletteContainer.setOutputMarkupId(true));
        item.add(textField.setOutputMarkupId(true));
        item.add(chkBox.setOutputMarkupId(true));

    }

    private JasperRuntimeParameterModel createRuntimeModel(QueryParameter parameter) {
        boolean isMultipleSelection = parameter.getSelection().equalsIgnoreCase(QueryParameter.MULTIPLE_SELECTION);
        JasperRuntimeParameterModel runtimeModel = new JasperRuntimeParameterModel(parameter.getName(), getParameterName(parameter), isMultipleSelection);
        runtimeModel.setMandatory(parameter.isMandatory());

        List<IdName> values = new ArrayList<IdName>();
        // set in the model only the values for parameters which are not dependent
        //System.out.println("*** param="+parameter.getName() + "  source="+parameter.getSource()+ "  dep="+parameter.isDependent() + "  selection="+parameter.getSelection());
        if ((parameter.getSource() != null) && (parameter.getSource().trim().length() > 0)
                && !parameter.isDependent()) {
            if (QueryParameter.SINGLE_SELECTION.equals(parameter.getSelection())) {
                runtimeModel.setCombo(true);
            }
            try {
                values = dataSourceService.getParameterValues(report.getDataSource(), parameter);
                //System.out.println("values="+values);
            } catch (Exception e) {
                //System.out.println(e.getMessage());
                info(e.getMessage());
            }
        }
        runtimeModel.setParameterValues(values);

        boolean populateDependent = false;
        if  (USER_PARAM.equals(parameter.getName())) {
            runtimeModel.setRawValue(ServerUtil.getUsernameWithoutRealm());
            populateDependent = true;
        }
                
        // mark the dependent parameters that must be populated after initilize the default values
        if (populateDependent) {
            this.runtimeModel.getParameters().put(parameter.getName(), runtimeModel);
            depParameters.add(parameter);
        }
        
        return runtimeModel;
    }

    private AjaxFormComponentUpdatingBehavior createAjax(final QueryParameter parameter) {
        return new AjaxFormComponentUpdatingBehavior("onchange") {
        	
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                populateDependentParameters(parameter,target);
            }
            
        };
    }

    private void populateDependentParameters(QueryParameter parameter, AjaxRequestTarget target) {
        Map<String, QueryParameter> childParams = getChildDependentParameters(parameter);
        //System.out.println("??? param=" + parameter + "  childSize="+ childParams.size());

        // update model parameter values for every child parameter
        for (QueryParameter childParam : childParams.values()) {

            if (!convertList.contains(childParam)) {
                continue;
            }

            Component childComponent = paramComponentsMap.get(childParam);

            List<IdName> values = new ArrayList<IdName>();
            boolean allParentsHaveValues = true;

            Map<String, QueryParameter> allParentParams = getParentDependentParameters(childParam);

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

                    values = dataSourceService.getDependentParameterValues(
                            report.getDataSource(), childParam, convertMap, allParameterValues);
                } catch (Exception e) {
                    //System.out.println(e.getMessage());
                    info(e.getMessage());
                }
            }

            JasperRuntimeParameterModel parameterModel = (JasperRuntimeParameterModel) runtimeModel.getParameters().get(childParam.getName());
            if (values != null && values.size() > 0) {
                parameterModel.setParameterValues(values);
            } else {
                parameterModel.setParameterValues(new ArrayList<IdName>());
            }
            if (target != null) {
                target.add(childComponent);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Palette createPalette(final QueryParameter parameter, IModel listModel, final AjaxFormComponentUpdatingBehavior ajaxUpdate) {
        return new ExtendedPalette("palette", listModel,

                new LoadableDetachableModel() {

                    @Override
                    protected Object load() {
                        if (runtimeModel.getParameters().get(parameter.getName()).getValues().size() > 0) {
                            return runtimeModel.getParameters().get(parameter.getName()).getValues();
                        } else {
                            return new ArrayList<IdName>();
                        }

                    }
                },
                new IChoiceRenderer() {

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
                        return "@" + returnValue.toString().replace(",", "-") +"@";
                    }

                }, 10, false, true) {

            @Override
            protected Recorder newRecorderComponent() {
                Recorder recorder = super.newRecorderComponent();
                if (parameter.isMandatory()) {
                    recorder.setLabel(new Model<String>(getParameterName(parameter)));
                    recorder.setRequired(true);
                }
                if (getChildDependentParameters(parameter).size() > 0) {
                    recorder.add(ajaxUpdate);
                }
                
                return recorder;
            }

        };
    }

    private String getDisplayableParameterName(QueryParameter parameter) {
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
        }
        
        return name;
    }


    private QueryParameter getParameter(String paramName) {
        return convertMap.get(paramName);
    }

    private Map<String, QueryParameter> getChildDependentParameters(QueryParameter p) {
        Map<String, QueryParameter> result = new LinkedHashMap<String, QueryParameter>();
        List<QueryParameter> params = getParameters();
        for (QueryParameter param : params) {
            if (!param.equals(p) && (param.getSource() != null)) {
                //System.out.println("---> param " + param.getName() + " : source=" + param.getSource());
                if (param.isDependent()) {
                    List<String> names = param.getDependentParameterNames();
                    if (names.contains(p.getName())) {
                        result.put(param.getName(), param);
                    }
                }
            }
        }
        
        return result;
    }

    private Map<String, QueryParameter> getParentDependentParameters(QueryParameter p) {
        List<String> names = p.getDependentParameterNames();
        Map<String, QueryParameter> result = new LinkedHashMap<String, QueryParameter>();
        for (String name : names) {
            result.put(name, getParameter(name));
        }
        
        return result;
    }

    private List<QueryParameter> getParameters() {
        return new LinkedList<QueryParameter>(convertMap.values());
    }

}
