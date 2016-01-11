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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

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

	private ThemesManager() {
		// for new themes you must add in all other i18n files the property:
		// Settings.personalize.theme.<theme_folder> to see it in seetings
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
		for (String dir : directories) {
			THEMES.add(dir);
		}
				
		long end = System.currentTimeMillis();
		LOG.info("Current theme is " + getThemeRelativePathCss());
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
	}
	
	public String getThemeRelativePathCss() {
		return "../themes/" + theme + "/style.css";
	}
	
	//@todo
	public String getOutsideThemeRelativePathCss() {
		return "../../themes/" + theme + "/style.css";
	}
	
	public String get3rdThemeRelativePathCss() {
		return "../../../themes/" + theme + "/style.css";
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
