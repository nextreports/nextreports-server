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
package ro.nextreports.server.web.common.slidebar;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.template.PackageTextTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Decebal Suiu
 */
public class SlidebarBehavior extends Behavior {

    private static final long serialVersionUID = 1L;

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        super.renderHead(component, response);

        response.render(CssHeaderItem.forReference(new PackageResourceReference(SlidebarBehavior.class, "slidebars.css")));
        response.render(CssHeaderItem.forUrl("css/slidebar.css"));
        response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(SlidebarBehavior.class, "slidebars.js")));

        Map<String, String> vars = new HashMap<String, String>();
//        vars.put("siteClose", "true");

        PackageTextTemplate template = new PackageTextTemplate(SlidebarBehavior.class, "slidebar-behavior.template.js");
//        template.interpolate(vars);

        response.render(OnDomReadyHeaderItem.forScript(template.getString()));
    }

}
