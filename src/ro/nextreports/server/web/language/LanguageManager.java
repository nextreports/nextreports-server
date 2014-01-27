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
package ro.nextreports.server.web.language;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LanguageManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(LanguageManager.class);
					
	public static final String LANGUAGE_ENGLISH = "en";
	public static final String COUNTRY_ENGLISH = "US";	
	public static final String PROPERTY_NAME_ENGLISH = LANGUAGE_ENGLISH + "_" + COUNTRY_ENGLISH;	

	public static final String LANGUAGE_FRENCH = "fr";
	public static final String COUNTRY_FRENCH = "FR";
	public static final String PROPERTY_NAME_FRENCH = LANGUAGE_FRENCH + "_" + COUNTRY_FRENCH;
	
	public static final String LANGUAGE_ROMANIAN = "ro";
	public static final String COUNTRY_ROMANIAN = "RO";
	public static final String PROPERTY_NAME_ROMANIAN = LANGUAGE_ROMANIAN + "_" + COUNTRY_ROMANIAN;
	
	public static final String LANGUAGE_POLISH = "pl";
	public static final String COUNTRY_POLISH = "PL";
	public static final String PROPERTY_NAME_POLISH = LANGUAGE_POLISH + "_" + COUNTRY_POLISH;
	
	public static final List<Language> languages = new ArrayList<Language>();			
	public static final List<String> LANGUAGES = new ArrayList<String>();		
	
	private static LanguageManager instance;
	
	@SuppressWarnings("unchecked")
	private LanguageManager() {		
		
		languages.add(new Language(LANGUAGE_ENGLISH, COUNTRY_ENGLISH, PROPERTY_NAME_ENGLISH));
		languages.add(new Language(LANGUAGE_FRENCH, COUNTRY_FRENCH, PROPERTY_NAME_FRENCH));
		languages.add(new Language(LANGUAGE_ROMANIAN, COUNTRY_ROMANIAN, PROPERTY_NAME_ROMANIAN));
		languages.add(new Language(LANGUAGE_POLISH, COUNTRY_POLISH, PROPERTY_NAME_POLISH));
		
		LANGUAGES.add(PROPERTY_NAME_ENGLISH);
		LANGUAGES.add(PROPERTY_NAME_FRENCH);
		LANGUAGES.add(PROPERTY_NAME_ROMANIAN);
		LANGUAGES.add(PROPERTY_NAME_POLISH);
		
		// try to see if other internatinalization filee where added by hand
		// must have name like NextServerApplication_<lang>_<country>.properties
		// you must add in all other i18n files the property:
		// Settings.personalize.language.<lang>_<country> to see it in seetings
		Collection<File> files = FileUtils.listFiles(new File("."), I18NFileFilter.INSTANCE, TrueFileFilter.INSTANCE);		
		Set<String> fileNames = new HashSet<String>();
		for (File file : files) {					
			String name = file.getName();
			String baseName = name.substring(0, name.indexOf(".properties"));
			String[] s = baseName.split("_");
			if (s.length == 3) {
				fileNames.add(name);
				languages.add(new Language(s[1], s[2], s[1]+"_" +s[2]));
				LANGUAGES.add(s[1]+"_" +s[2]);				
			}			
		}		
	}	
	
	public static synchronized LanguageManager getInstance() {
		if (instance == null) {
			instance = new LanguageManager();
		}
		return instance;
	}

	public Locale getLocale(String languageProperty) {			
		Locale locale = new Locale(getLanguage(languageProperty), getCountry(languageProperty));
		Locale.setDefault(locale);
		return locale;
	}
	
	private String getLanguage(String languageProperty) {
		for  (int i=0; i<languages.size(); i++) {
            if (languages.get(i).getProperty().equals(languageProperty)) {
            	return languages.get(i).getLanguage();
            }
         }
		return languages.get(0).getLanguage();
	}
	
	private String getCountry(String languageProperty) {
		for  (int i=0; i<languages.size(); i++) {
            if (languages.get(i).getProperty().equals(languageProperty)) {
            	return languages.get(i).getCountry();
            }
         }
		return languages.get(0).getCountry();
	}
	

}
