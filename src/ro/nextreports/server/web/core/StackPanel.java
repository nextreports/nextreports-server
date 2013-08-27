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
package ro.nextreports.server.web.core;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.ajax.AjaxRequestTarget;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.web.common.panel.GenericPanel;

import java.util.Stack;


/**
 * User: mihai.panaitescu
 * Date: 05-Mar-2010
 * Time: 15:53:33
 */
public class StackPanel extends GenericPanel<Entity> {

    protected static final int STACK_MAX_SIZE = 10;
    
    private WebMarkupContainer workContainer;
    private Stack<Panel> stack;
    
    public StackPanel(String id) {
        super(id);

        stack = new Stack<Panel>();
        workContainer = new WebMarkupContainer("workContainer");
        workContainer.setOutputMarkupId(true);

        add(workContainer);
    }

    protected void initWorkspace(Panel panel) {
       stack.push(panel);
       workContainer.add(panel);         
    }

    // used for components without form (like Delete)
    public boolean isForward(AjaxRequestTarget target) {
        return (stack.size() > 1);
    }

    // use for components with form
    public boolean isFormForward(AjaxRequestTarget target) {
        return (stack.size() > 2);
    }

    public void restoreWorkspace(AjaxRequestTarget target) {
        if (stack.size() > 1) {
            stack.pop();
            while (stack.size() > 1) {
                stack.pop();
            }
            workContainer.replace(stack.peek());
        }

        if (target != null) {
            target.add(workContainer);
        }
    }

    public void backwardWorkspace(AjaxRequestTarget target) {
        if (stack.size() > 1) {
            stack.pop();
            workContainer.replace(stack.peek());
        } else {
            throw new RuntimeException("Cannot back with only one panel.");
        }

        if (target != null) {
            target.add(workContainer);
        }
    }

    public void forwardWorkspace(Panel panel, AjaxRequestTarget target) {
        if (stack.size() > STACK_MAX_SIZE) {
            // clear all
            stack.pop();
            while (stack.size() > 1) {
                stack.pop();
            }
        }

        panel.setOutputMarkupId(true);
        workContainer.replace(panel);
        stack.push(panel);

        if (target != null) {
            target.add(workContainer);
        }
    }
}
