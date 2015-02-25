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
package ro.nextreports.server.web.core.section;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import ro.nextreports.server.licence.ModuleLicence;
import ro.nextreports.server.licence.NextServerModuleLicence;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.analysis.AnalysisSection;


/**
 * @author Decebal Suiu
 */
public class DefaultSectionManager implements SectionManager, ApplicationContextAware, InitializingBean {
		
	private ModuleLicence moduleLicence;

	private ApplicationContext context;
	private Map<String, Section> sections;
	private List<Section> sectionsCache;
	private List<String> idsCache;
	
	@Required
	public void setModuleLicence(ModuleLicence moduleLicence) {
		this.moduleLicence = moduleLicence;
	}
	
	public int getSectionCount() {
		return sections.size();
	}

	public List<Section> getSections() {
		return sectionsCache;
	}
	
	public List<String> getIds() {
		return idsCache;
	}

	public Section getSection(String id) {
		return sections.get(id);
	}

	public String getSelectedSectionId() {
		return NextServerSession.get().getSelectedSectionId();
	}
	
	public Section getSelectedSection() {
		return getSection(getSelectedSectionId());
	}

	public void setSelectedSectionId(String sectionId) {
		NextServerSession.get().setSelectedSectionId(sectionId);
	}

	public void setSelectedSectionIndex(int index) {
		setSelectedSectionId(idsCache.get(index));
	}
	
	public int getSelectedSectionIndex() {
		return idsCache.indexOf(getSelectedSectionId());
	}
	
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}

	@SuppressWarnings("unchecked")
	public void afterPropertiesSet() throws Exception {
		sections = new LinkedHashMap<String, Section>();
		
		Map<String, Section> matches = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, Section.class);
		for (Section section: matches.values()) {
			if (AnalysisSection.ID.equals(section.getId())) {
				if(moduleLicence.isValid(NextServerModuleLicence.ANALYSIS_MODULE)) {				
					sections.put(section.getId(), section);
				}
			} else {
				sections.put(section.getId(), section);
			}			
		}
		
		sections = Collections.unmodifiableMap(sections);
				
		sectionsCache = new ArrayList<Section>(sections.values());
		sectionsCache = Collections.unmodifiableList(sectionsCache);
		
		idsCache = new ArrayList<String>(sections.keySet());
		idsCache = Collections.unmodifiableList(idsCache);		
	}

}
