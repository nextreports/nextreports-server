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
package ro.nextreports.server.util;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.model.StringResourceModel;

import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.domain.NextContent;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.ReportRuntime;
import ro.nextreports.server.domain.ReportRuntimeParameterModel;
import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.domain.SchedulerTime;
import ro.nextreports.server.report.AbstractReportRuntimeParameterModel;
import ro.nextreports.server.report.ExternalParameter;
import ro.nextreports.server.report.ReportConstants;
import ro.nextreports.server.report.jasper.JasperParameter;
import ro.nextreports.server.report.jasper.JasperParameterSource;
import ro.nextreports.server.report.jasper.JasperReportsUtil;
import ro.nextreports.server.report.jasper.JasperRuntimeParameterModel;
import ro.nextreports.server.report.next.NextRuntimeParameterModel;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.schedule.ScheduleConstants;
import ro.nextreports.server.service.DataSourceService;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.report.ReportRuntimeModel;

import ro.nextreports.engine.queryexec.IdName;
import ro.nextreports.engine.queryexec.QueryParameter;
import ro.nextreports.engine.util.ParameterUtil;

/**
 * @author Decebal Suiu
 */
public class SchedulerUtil {

//    public static final DateFormat DAY_HM_FORMAT= new SimpleDateFormat("dd/MM/yy HH:mm");
	public static final DateFormat DAY_HM_FORMAT= DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
//    public static final DateFormat DAY_FORMAT= new SimpleDateFormat("dd/MM/yy");
	public static final DateFormat DAY_FORMAT= DateFormat.getDateInstance(DateFormat.SHORT);

    public static String getTooltip(SchedulerTime st) {
    	    	
    	String s = new StringResourceModel("JobPanel.type." + st.getType(), null).getString();
    	        
        if (ScheduleConstants.ONCE_TYPE.equals(st.getType())) {
            return s + " " + new StringResourceModel("JobPanel.description.at", null).getString()  + " " + SchedulerUtil.DAY_HM_FORMAT.format(st.getRunDate());
        }
        
        if (st.getAdvanced()){
            return s + " (" +  new StringResourceModel("JobPanel.description.advanced", null).getString()  + ")";
        }

        String between = " " + new StringResourceModel("JobPanel.description.between", null).getString() + " " + 
        		SchedulerUtil.DAY_FORMAT.format(st.getStartActivationDate()) + " " + 
        		new StringResourceModel("JobPanel.description.and", null).getString() + " " +
                SchedulerUtil.DAY_FORMAT.format(st.getEndActivationDate());

        if (ScheduleConstants.MINUTELY_TYPE.equals(st.getType())) {
            return s + between;
        } else if (ScheduleConstants.HOURLY_TYPE.equals(st.getType())) {
            return s + between + " " + new StringResourceModel("JobPanel.description.atMinute", null).getString()  + " " + st.getMinute();
        } else if (ScheduleConstants.DAILY_TYPE.equals(st.getType())) {
            return s + between + " " + new StringResourceModel("JobPanel.description.at", null).getString()  + " " + st.getHours() + ":" + st.getMinute();
        } else if (ScheduleConstants.WEEKLY_TYPE.equals(st.getType())) {
            return s + between + " " + new StringResourceModel("JobPanel.description.in", null).getString() + " " + st.getDaysOfWeek();
        } else if (ScheduleConstants.MONTHLY_TYPE.equals(st.getType())) {
            return s + between +  " " + new StringResourceModel("JobPanel.description.inDay", null).getString()  + " " + st.getDays() + 
            		" " + new StringResourceModel("JobPanel.description.at", null).getString() + " " + st.getHours() + ":" + st.getMinute();
        } else {
            return s;
        }
    }

    public static void updateSchedulerTime(SchedulerTime schedulerTime) {
        String type = schedulerTime.getType();
        if (ScheduleConstants.ONCE_TYPE.equals(type)) {

        } else if (ScheduleConstants.MINUTELY_TYPE.equals(type) ||
                ScheduleConstants.HOURLY_TYPE.equals(type)) {
            if (schedulerTime.getDays() == null) {
                schedulerTime.setDays("*");
            }
            if (schedulerTime.getHours() == null) {
                schedulerTime.setHours("*");
            }
            if (schedulerTime.getMonths() == null) {
                schedulerTime.setMonths("*");
            }
        } else if (ScheduleConstants.DAILY_TYPE.equals(type)) {
            if (schedulerTime.getDays() == null) {
                schedulerTime.setDays("*");
            }
            if (schedulerTime.getHours() == null) {
                schedulerTime.setHours("*");
            }
            schedulerTime.setDaysOfWeek("*");
        } else if (ScheduleConstants.WEEKLY_TYPE.equals(type)) {
            if (schedulerTime.getDaysOfWeek() == null) {
                schedulerTime.setDaysOfWeek("*");
            }
            if (schedulerTime.getHours() == null) {
                schedulerTime.setHours("*");
            }
            schedulerTime.setDays("*");
        } else if (ScheduleConstants.MONTHLY_TYPE.equals(type)) {
            if (schedulerTime.getHours() == null) {
                schedulerTime.setHours("*");
            }
            if (schedulerTime.getMonths() == null) {
                schedulerTime.setMonths("*");
            }

            int monthlytype = schedulerTime.getMonthlyType();
            if (ScheduleConstants.MONTHLY_GENERAL_TYPE == monthlytype) {
                if (schedulerTime.getDays() == null) {
                    schedulerTime.setDays("*");
                }
            } else {
                schedulerTime.setDays("?");
            }


            if (ScheduleConstants.MONTHLY_LAST_DAY_TYPE == monthlytype) {
                schedulerTime.setDaysOfWeek("?");
            } else if (ScheduleConstants.MONTHLY_GENERAL_TYPE == monthlytype) {
                if (schedulerTime.getDaysOfWeek() == null) {
                    schedulerTime.setDaysOfWeek("*");
                }
            }
        }
    }

    public static void restoreSchedulerTime(SchedulerTime schedulerTime) {
        if ("*".equals(schedulerTime.getHours())) {
            schedulerTime.setHours(null);
        }
        if ("*".equals(schedulerTime.getMonths())) {
            schedulerTime.setMonths(null);
        }
        if ("*".equals(schedulerTime.getDays()) || "?".equals(schedulerTime.getDays())) {
            schedulerTime.setDays(null);
        }
        if ("*".equals(schedulerTime.getDaysOfWeek()) || "?".equals(schedulerTime.getDaysOfWeek())) {
            schedulerTime.setDaysOfWeek(null);
        }
    }

    public static ReportRuntimeModel getStoredRuntimeModel(StorageService storageService, SchedulerJob schedulerJob, ReportService reportService,
    		DataSourceService dataSourceService) {    	
        return getStoredRuntimeModel(storageService, schedulerJob.getReport(), schedulerJob.getReportRuntime(), reportService, dataSourceService);
    }
    
    public static ReportRuntimeModel getStoredRuntimeModel(StorageService storageService, Report report, ReportRuntime reportRuntime, ReportService reportService,
    		DataSourceService dataSourceService) {    	
        ReportRuntimeModel runtimeModel = new ReportRuntimeModel();
        runtimeModel.setExportLayout(reportRuntime.getLayoutType());
        runtimeModel.setExportType(reportRuntime.getOutputType());
        runtimeModel.setEdit(true);
        runtimeModel.setParameters(new HashMap<String, ReportRuntimeParameterModel>());
               
        Map<String, Object> parameterValues = reportRuntime.getParametersValues();
//        for (String key : parameterValues.keySet()) {
//        	System.out.println(" param= " + key);
//        	System.out.print(" val="  );
//        	Object val = parameterValues.get(key);
//        	if (val instanceof Object[]) {
//        	  Object[] array = (Object[])val;
//        	  for (Object obj : array) {
//        		  System.out.println("   --> " + obj +  "  class="+obj.getClass());
//        	  }
//        	} else {
//        		System.out.println(val +  "  class="+val.getClass());
//        	}
//        }
//        System.out.println("parameterValues = " + parameterValues);
        
        Map<String, QueryParameter> paramMap;        
        if (ReportConstants.NEXT.equals(report.getType())) {
            paramMap = ParameterUtil.getUsedParametersMap(NextUtil.getNextReport(storageService.getSettings(), (NextContent) report.getContent()));
            for (String key : paramMap.keySet()) {
                QueryParameter qp = paramMap.get(key);
                boolean isDynamic = reportRuntime.isDynamic(key);                
                ReportRuntimeParameterModel parameterModel = createRuntimeModel(report, parameterValues, qp, dataSourceService, isDynamic);
                runtimeModel.getParameters().put(key, parameterModel);
            }
        } else {
            Map<String, Serializable> map = null;
            try {
                map = reportService.getReportUserParameters(report, new ArrayList<ExternalParameter>());
                paramMap = convertJasper(storageService, report.getDataSource(), map);
                for (String key : paramMap.keySet()) {
                    QueryParameter qp = paramMap.get(key);
                    ReportRuntimeParameterModel parameterModel = createRuntimeModel(report, parameterValues, qp, dataSourceService);
                    runtimeModel.getParameters().put(key, parameterModel);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }            
        }
        
        return runtimeModel;
    }

    private static ReportRuntimeParameterModel createRuntimeModel(Report report, Map<String, Object> parameterValues,
    		QueryParameter parameter, DataSourceService dataSourceService) {
        return createRuntimeModel(report, parameterValues, parameter, dataSourceService, false);
    }

    private static ReportRuntimeParameterModel createRuntimeModel(Report report, Map<String, Object> parameterValues,
    		QueryParameter parameter, DataSourceService dataSourceService, boolean isDynamic) {
        AbstractReportRuntimeParameterModel runtimeModel;
        
        String parameterName = parameter.getName();
        ArrayList<IdName> values = new ArrayList<IdName>();
        boolean isMultipleSelection = parameter.getSelection().equalsIgnoreCase(QueryParameter.MULTIPLE_SELECTION);
        if (ReportConstants.NEXT.equals(report.getType())) {
            runtimeModel = new NextRuntimeParameterModel(parameterName, getParameterName(parameter), isMultipleSelection);
            runtimeModel.setDynamic(isDynamic);
            if ((parameter.getSource() != null) && (parameter.getSource().trim().length() > 0)
                    && !parameter.isDependent()) {
                try {
                    values.addAll(dataSourceService.getParameterValues(report.getDataSource(), parameter));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            runtimeModel = new JasperRuntimeParameterModel(parameterName, getParameterName(parameter), isMultipleSelection);
            if ((parameter.getSource() != null) && (parameter.getSource().trim().length() > 0)
                    && !parameter.isDependent()) {
                if (QueryParameter.SINGLE_SELECTION.equals(parameter.getSelection())) {
                    ((JasperRuntimeParameterModel) runtimeModel).setCombo(true);
                }
                try {
                    values.addAll(dataSourceService.getParameterValues(report.getDataSource(), parameter));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        runtimeModel.setParameterValues(values); 
        
        Object parameterValue = parameterValues.get(parameterName);
        if (QueryParameter.MULTIPLE_SELECTION.equals(parameter.getSelection())) {
            if (parameterValue == null) {
            	// if parameter is dynamic when we edit a scheduler object we must have 
            	// a non-null list in Palette, otherwise an exception is raised
                runtimeModel.setValueList(new ArrayList<Object>());
            } else {
                ArrayList<Object> list  = new ArrayList<Object>();
                list.addAll(Arrays.asList((Object[])parameterValue));
                runtimeModel.setValueList(list);
            }
        } else {
            runtimeModel.setRawValue(parameterValue);
        }

        runtimeModel.setMandatory(parameter.isMandatory());
        
        return runtimeModel;
    }

    private static String getParameterName(QueryParameter parameter) {
        String name = parameter.getRuntimeName();
        if ((name == null) || name.trim().equals("")) {
            name = parameter.getName();
        }
        
        return name;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, QueryParameter> convertJasper(StorageService storageService, DataSource ds, Map<String, Serializable> params) throws Exception {
        Map<String, QueryParameter> result = new LinkedHashMap<String, QueryParameter>();
        Set s = params.entrySet();
        for (Object value : s) {
            Map.Entry<String, Serializable> me = (Map.Entry<String, Serializable>) value;
            JasperParameter jp = (JasperParameter) me.getValue();
            String className = JasperReportsUtil.getValueClassName(storageService, ds, jp);
            QueryParameter qp = new QueryParameter(jp.getName(), jp.getDescription(), className);
            //System.out.println(">>> Source = " + jp.getSelect());
            qp.setSource(jp.getSelect());
            qp.setMandatory(true);
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

}
