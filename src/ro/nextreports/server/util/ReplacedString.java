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
package ro.nextreports.server.util;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: Feb 14, 2008
 * Time: 11:38:20 AM
 */
public class ReplacedString implements Serializable {

    private static final long serialVersionUID = 1110183978834799724L;

    private String s;         // new string with replacements
    private boolean replaced; // true if something was replaced

    public ReplacedString(String s, boolean replaced) {
        this.s = s;
        this.replaced = replaced;
    }


    public String getS() {
        return s;
    }

    public boolean isReplaced() {
        return replaced;
    }
}
