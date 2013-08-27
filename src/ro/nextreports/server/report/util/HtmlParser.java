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

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.File;


/**
 * User: mihai.panaitescu
 * Date: 10-Dec-2009
 * Time: 11:47:20
 */
public class HtmlParser {

    private List<String> images = new ArrayList<String>();  

    // write regex so the image file name from src is a group : between '(' and ')'
    // and get with matcher.group(1)
    // see HTMLExporter in engine : always is of form "<img src"  and ends with </img> (never ends with /> )   
    private String imgRegex = "<img\\s+src=\"([^\"]+)\"";

    public HtmlParser(String text) {
        Pattern pattern = Pattern.compile(imgRegex);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String s = matcher.group(1);
            // use this line to get rid of ./ 
            s = new File(s).getName();            
            if (!images.contains(s)) {
                images.add(s);
            }    
        }

    }

    public List<String> getImages() {
        return images;
    }
     
}
