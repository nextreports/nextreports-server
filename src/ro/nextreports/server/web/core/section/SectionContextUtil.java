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

import ro.nextreports.server.web.NextServerSession;

/**
 * @author Decebal Suiu
 */
public class SectionContextUtil {

    public static String getCurrentPath(String sectionId) {
        SectionContext sectionContext = NextServerSession.get().getSectionContext(sectionId);
        return sectionContext.getData().getString(SectionContextConstants.CURRENT_PATH);    	
    }
    
    public static void setCurrentPath(String sectionId, String path) {
        SectionContext sectionContext = NextServerSession.get().getSectionContext(sectionId);
        sectionContext.getData().put(SectionContextConstants.CURRENT_PATH, path);    	    	
    }

    public static String getSelectedEntityPath(String sectionId) {
        SectionContext sectionContext = NextServerSession.get().getSectionContext(sectionId);
        return sectionContext.getData().getString(SectionContextConstants.SELECTED_ENTITY_PATH);    	
    }

    public static void setSelectedEntityPath(String sectionId, String path) {
        SectionContext sectionContext = NextServerSession.get().getSectionContext(sectionId);
        sectionContext.getData().put(SectionContextConstants.SELECTED_ENTITY_PATH, path);    	    	    	
    }
    
    public static String getLookFor(String sectionId) {
        SectionContext sectionContext = NextServerSession.get().getSectionContext(sectionId);
        return sectionContext.getData().getString(SectionContextConstants.LOOK_FOR);    	
    }

    public static void setLookFor(String sectionId, String lookFor) {
        SectionContext sectionContext = NextServerSession.get().getSectionContext(sectionId);
        sectionContext.getData().put(SectionContextConstants.LOOK_FOR, lookFor);    	    	
    }

    public static String getCurrentEntityChildren(String sectionId) {
        SectionContext sectionContext = NextServerSession.get().getSectionContext(sectionId);
        return sectionContext.getData().getString(SectionContextConstants.SELECTED_ENTITY_CHILDREN);
    }

    public static void setCurrentEntityChildren(String sectionId, String childrenCount) {
        SectionContext sectionContext = NextServerSession.get().getSectionContext(sectionId);
        sectionContext.getData().put(SectionContextConstants.SELECTED_ENTITY_CHILDREN, childrenCount);
    }

}
