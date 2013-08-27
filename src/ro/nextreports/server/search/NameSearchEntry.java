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
package ro.nextreports.server.search;

import org.apache.wicket.model.StringResourceModel;

/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: May 7, 2008
 * Time: 10:21:30 AM
 */
public class NameSearchEntry extends SearchEntry {

    private static final long serialVersionUID = -6169385974609139822L;

    private String name;
    private boolean ignoredCase;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isIgnoredCase() {
        return ignoredCase;
    }

    public void setIgnoredCase(boolean ignoredCase) {
        this.ignoredCase = ignoredCase;
    }

    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("* ").append(new StringResourceModel("ActionContributor.Search.entry.name", null).getString()).append(" ");
        if (ignoredCase) {
            sb.append("(").append(new StringResourceModel("ActionContributor.Search.entry.ignoreCase", null).getString()).append(") ");
        }
        sb.append(new StringResourceModel("ActionContributor.Search.entry.contains", null).getString());
        sb.append(" '");
        sb.append(name);
        sb.append("'");
        return sb.toString();
    }
}
