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
package ro.nextreports.server.report.util;

import org.jcrom.JcrFile;

import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.ParameterValue;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.Settings;
import ro.nextreports.server.report.ReportConstants;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.util.FileUtil;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;

import ro.nextreports.engine.queryexec.IdName;
import ro.nextreports.engine.queryexec.QueryParameter;
import ro.nextreports.engine.util.ParameterUtil;

/**
 * User: mihai.panaitescu
 * Date: 08-Dec-2009
 * Time: 12:00:57
 */
public class ReportUtil {

    public static final String FILE_SEPARATOR = "/";
    public static final String EXTENSION_SEPARATOR = ".";
    public static final String IMAGE_DELIM = "___";
    
    public static void copyTemplate(String directoryName, JcrFile template) throws Exception {
    	if (template != null) {
    		List<JcrFile> files = new ArrayList<JcrFile>();
    		files.add(template);
    		copyImages(directoryName, files);
    	}
    }

    public static void copyImages(String directoryName, List<JcrFile> images) throws Exception {
        if ((images == null) || (images.size() == 0)) {
            return;
        }
        File folder = new File(directoryName);
        folder.mkdir();
        for (JcrFile image : images) {
            String name = image.getName();

            byte[] xml = image.getDataProvider().getBytes();
            String fileName = directoryName + File.separator + name;
            File f = new File(fileName);
            if (f.exists()) {
                continue;
            }
            f.createNewFile();
            ByteArrayInputStream bais = new ByteArrayInputStream(xml);
            FileOutputStream fos = new FileOutputStream(f);
            try {
                int n = 0;
                byte[] b = new byte[1024];
                while ((n = bais.read(b)) >= 0) {
                    fos.write(b, 0, n);
                }
            } finally {
                fos.close();
                bais.close();
            }
        }
    }

    public static List<String> getHtmlImages(File htmlFile) throws Exception {
        HtmlParser parser = new HtmlParser(new String(FileUtil.getBytes(htmlFile)));        
        return parser.getImages();
    }
    
    public static String getDebugParameters(Map<String, Object> parametersValues) {
    	return getDebugParameters(parametersValues, null);
    }

    public static String getDebugParameters(Map<String, Object> parametersValues, Map<String, String> displayNames) {
    	SimpleDateFormat timeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    	SimpleDateFormat dayFormat = new SimpleDateFormat("dd/MM/yyyy");
    	if (parametersValues == null) {
    		return "";
    	}
        StringBuilder sb = new StringBuilder();
        for (String key : parametersValues.keySet()) {
            Object value = parametersValues.get(key);
            String left = (displayNames == null) ? key : displayNames.get(key);
            sb.append(" ").append(left).append(" = ");
            if (value == null) {
                sb.append("null\r\n");
                continue;
            }
            if (value instanceof Object[]) {
                Object[] values = (Object[]) value;
                sb.append("[");
                for (int i = 0, size = values.length; i < size; i++) {
                    Object obj = values[i];
                    if (obj instanceof IdName) {
                        sb.append(((IdName) obj).getId());
                    } else if (obj instanceof Date) {
                      	sb.append(dayFormat.format((Date)obj));
                    } else if (obj instanceof Timestamp) {
                        Date date = new Date(((Timestamp)obj).getTime());
                        sb.append(timeFormat.format(date));      
                    } else {
                        sb.append(obj);
                    }
                    if (i < size - 1) {
                        sb.append(";");
                    }
                }
                sb.append("]");
            } else if (value instanceof IdName) {
                sb.append(((IdName) value).getId());
            } else if (value instanceof Date) {
            	sb.append(dayFormat.format((Date)value));
            } else if (value instanceof Timestamp) {
            	Date date = new Date(((Timestamp)value).getTime());
            	sb.append(timeFormat.format(date));  
            } else {
                sb.append(value);
            }
            sb.append("\r\n");
        }
        return sb.toString();
    }

    public static String getParameterValueAsString(ParameterValue pv) {
        return getParameterValueAsString(pv.getValue());
    }

    public static String getParameterValueAsString(Object value) {
        StringBuilder buffer = new StringBuilder();
        if (value instanceof Object[]) {
            buffer.append("[");
            for (Object obj : (Object[]) value) {
                if (obj instanceof IdName) {
                    buffer.append(((IdName) obj).getId());
                } else {
                    buffer.append(obj);
                }
                buffer.append(";");
            }
            if (buffer.toString().endsWith(";")) {
                buffer.deleteCharAt(buffer.length() - 1);
            }
            buffer.append("]");
        } else if (value instanceof IdName) {
            buffer.append(((IdName) value).getId());
        } else {
            buffer.append(value);
        }
        return buffer.toString();
    }
    
    public static String getDisplayParameterValueAsString(Object value) {
        StringBuilder buffer = new StringBuilder();
        if (value instanceof Object[]) {
            buffer.append("[");
            for (Object obj : (Object[]) value) {
                if (obj instanceof IdName) {
                    buffer.append(((IdName) obj).getName());
                } else {
                    buffer.append(obj);
                }
                buffer.append(";");
            }
            if (buffer.toString().endsWith(";")) {
                buffer.deleteCharAt(buffer.length() - 1);
            }
            buffer.append("]");
        } else if (value instanceof IdName) {
            buffer.append(((IdName) value).getName());
        } else {
            buffer.append(value);
        }
        return buffer.toString();
    }
    
    public static void addUrlQueryParameters(Settings settings, Entity entity, Map<String, Object> allParameters, Map<String, Object> urlQueryParameters) throws Exception {
    	
    	if ((urlQueryParameters == null) || (urlQueryParameters.size() == 0)) {
    		return;
    	}
    	
		ro.nextreports.engine.Report nextReport;
		if (entity instanceof Chart) {
			Chart chart = (Chart) entity;
			nextReport = NextUtil.getNextReport(settings, chart);
		} else {
			Report report = (Report) entity;
			nextReport = NextUtil.getNextReport(settings, report);
		}
		Map<String, QueryParameter> parameters = ParameterUtil.getUsedParametersMap(nextReport);

		for (String key : urlQueryParameters.keySet()) {
			Object value = urlQueryParameters.get(key);
			QueryParameter parameter = parameters.get(key);
			if (parameter == null) {
				// mispelled inside embeddd code
				continue;
			}
			String className = parameter.getValueClassName();
			Object convertedValue;
						
    		// on Server Locale can be en_US, fr_FR or ro_RO (from internationalization)
			// we must use a hardcoded one and this must be used when we pass Date parameters inside url
    		SimpleDateFormat sdf = new SimpleDateFormat("MM.dd.yyyy hh:mm", Locale.US);      
			
			if (value instanceof String) {
				convertedValue = ParameterUtil.getParameterValueFromString(className, (String) value, sdf);
			} else {
				// multiple values
				String[] values = (String[]) value;
				Object[] convertedValues = new Object[values.length];
				for (int i = 0, size = values.length; i < size; i++) {
					convertedValues[i] = ParameterUtil.getParameterValueFromString(className, values[i], sdf);
				}
				convertedValue = convertedValues;
			}
			allParameters.put(key, convertedValue);
		}

	}
    
    /**
     * Test if a report has two parameters with names start_date and end_date, of type date and with single selection
     * 
     * @param settings settings
     * @param report report
     * @return true if a report has two parameters with names start_date and end_date, of type date and with single selection
     */
    public static boolean hasIntervalParameters(Settings settings, Report report) {
    	if (!ReportConstants.NEXT.equals(report.getType())) {
    		return false;
    	}
    	ro.nextreports.engine.Report nextReport = NextUtil.getNextReport(settings, report);
    	Map<String, QueryParameter> parameters = ParameterUtil.getUsedParametersMap(nextReport);  
    	boolean hasStart = false;
    	boolean hasEnd = false;
    	for (QueryParameter qp : parameters.values()) {
    		if (QueryParameter.INTERVAL_START_DATE_NAME.equals(qp.getName())) {
    			if (!ParameterUtil.isDateTime(qp)) {
    				return false;
    			}
    			if (!qp.getSelection().equals(QueryParameter.SINGLE_SELECTION)) {
    				return false;
    			}
    			hasStart = true;
    		} else if (QueryParameter.INTERVAL_END_DATE_NAME.equals(qp.getName())) {
    			if (!ParameterUtil.isDateTime(qp)) {
    				return false;
    			}
    			if (!qp.getSelection().equals(QueryParameter.SINGLE_SELECTION)) {
    				return false;
    			}
    			hasEnd = true;
    		}
    	}
    	return (hasStart && hasEnd);    	
    }

}
