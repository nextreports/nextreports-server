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
package ro.nextreports.server.web.themes;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;



//import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.http.client.utils.URIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.web.NextServerApplication;


public class ThemesManager {

	private static final Logger LOG = LoggerFactory.getLogger(ThemesManager.class);

	public static final String DEFAULT_THEME = "default";
	public static final String GREEN_THEME = "green";
	public static final String RED_THEME = "red";
	public static final String BLUE_THEME = "blue";

	public static final String GREEN_THEME_FILE_NAME = "theme-green-apple";
	public static final String RED_THEME_FILE_NAME = "theme-red-rose";
	public static final String BLUE_THEME_FILE_NAME = "theme-blue-sea";

	public static List<String> THEMES = new ArrayList<String>();

	private String theme = DEFAULT_THEME;

	public static ThemesManager INSTANCE;

	@SuppressWarnings("unchecked")
	private ThemesManager() {
		THEMES.add(DEFAULT_THEME);
		THEMES.add(RED_THEME);		
		THEMES.add(BLUE_THEME);
		// try to see if other theme files where added by hand
	    // must have name like theme-<color>.properties
		// you must add in all other i18n files the property:
		// Settings.personalize.theme.theme-<color> to see it in seetings
		long start = System.currentTimeMillis();
		File themesPath = new File("./webapp/themes");
		if (!themesPath.exists()) {
			themesPath = new File(".");
		}
		LOG.info("Check directory '" + themesPath.getAbsolutePath() + " for themes ...");
		String[] directories = themesPath.list(new FilenameFilter() {
			  @Override
			  public boolean accept(File current, String name) {
			    return new File(current, name).isDirectory();
			  }
			});
		
		System.out.println(Arrays.asList(directories));
		System.out.println("----------------- " +  getThemeRelativePathCss());
		long end = System.currentTimeMillis();
		LOG.info("Found "+ THEMES.size() + " theme files in " + (end-start) + " ms.");
	}

	public static synchronized ThemesManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ThemesManager();
		}
		return INSTANCE;
	}

	public void setTheme(String theme) {
		this.theme = theme;
		//generateStyle();
	}
	
	public String getThemeRelativePathCss() {
		return "../themes/" + theme + "/style.css";
	}

	private void generateStyle() {
		generateStyle("style_template.css", "/css/style.css");
		generateStyle("table_template.css", "/css/table.css");
		generateStyle("dashboard_template.css", "/WEB-INF/classes/ro/nextreports/server/web/dashboard/dashboard.css");
		generateStyle("analysis_template.css", "/WEB-INF/classes/ro/nextreports/server/web/analysis/analysis.css");
        generateStyle("slidebar_template.css", "/css/slidebar.css");
     // @TODO - remove after develop
        generateStyle2("dashboard_template.css", "/target/classes/ro/nextreports/server/web/dashboard/dashboard.css");
	}

	private void generateStyle(String templateFile, String generatedFilePath) {
		InputStream styleTemplateStream = getClass().getResourceAsStream(templateFile);
		InputStream propertiesStream = getClass().getResourceAsStream(getPropertiesFile(theme));
		try {
			String styleTemplate = IOUtils.toString(styleTemplateStream);
			StrSubstitutor sub = new StrSubstitutor(createValues(propertiesStream));
			String resolvedString = sub.replace(styleTemplate);
			ServletContext context = NextServerApplication.get().getServletContext();
			String fileName = context.getRealPath(generatedFilePath);
//			URI outputURI = new URI(("file:///"+ URIUtil.encodePath(fileName)));
//			File styleFile = new File(outputURI);
			File styleFile = new File(fileName);
			FileUtils.writeStringToFile(styleFile, resolvedString);
			LOG.info("Generated style file " + templateFile + " in folder " + styleFile.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeInputStream(styleTemplateStream);
			closeInputStream(propertiesStream);
		}
	}
	// @TODO - remove after develop
	private void generateStyle2(String templateFile, String generatedFilePath) {
        InputStream styleTemplateStream = getClass().getResourceAsStream(templateFile);
        InputStream propertiesStream = getClass().getResourceAsStream(getPropertiesFile(theme));
        try {              
               String styleTemplate = IOUtils.toString(styleTemplateStream);
               StrSubstitutor sub = new StrSubstitutor(createValues(propertiesStream));
               String resolvedString = sub.replace(styleTemplate);        
               String fileName = new File(".").getAbsolutePath() + File.separator + generatedFilePath;
               File styleFile = new File(fileName);
               FileUtils.writeStringToFile(styleFile, resolvedString);
               LOG.info("Generated style file " + templateFile + " in folder " + styleFile.getAbsolutePath());
        } catch (Exception e) {
               e.printStackTrace();
        } finally {
               closeInputStream(styleTemplateStream);
               closeInputStream(propertiesStream);
        }
  }

	private Map<String, String> createValues(InputStream is) {
		Map<String, String> valuesMap = new HashMap<String, String>();
		Properties prop = new Properties();
		try {
			prop.load(is);
			for (Object o : prop.keySet()) {
				valuesMap.put((String)o, prop.getProperty((String)o));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return valuesMap;
	}

	private String getPropertiesFile(String theme) {
		String fileName;
		if (GREEN_THEME.equals(theme))  {
			fileName = "theme-green-apple.properties";
		} else if (RED_THEME.equals(theme)) {
			fileName = "theme-red-rose.properties";
		} else if (BLUE_THEME.equals(theme)) {
			fileName = "theme-blue-sea.properties";
		} else {
			fileName = theme + ".properties";
		}
		return fileName;
	}

	private void closeInputStream(InputStream is) {
		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static String getTickImage(String theme, NextServerApplication application ) {
		if (ThemesManager.RED_THEME.equals(theme)) {
			return "tick_red.png";
		} else if (ThemesManager.BLUE_THEME.equals(theme)) {
			return "tick_blue.png";
		} else if (ThemesManager.GREEN_THEME.equals(theme)) {
			return "tick_green.png";
		}

		String file = "tick_" + theme + ".png";
		ServletContext context = application.getServletContext();
    	File imgFile = new File(context.getRealPath("images/" + file));
    	if (imgFile.exists()) {
    		return file;
    	} else {
    		return "tick_green.png";
    	}
	}

}
