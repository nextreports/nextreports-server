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
package ro.nextreports.server.report.jasper;

import java.util.HashMap;
//
// Created by IntelliJ IDEA.
// User: mihai.panaitescu
// Date: 12-Oct-2009
// Time: 15:04:09

//
public class JasperRunnerFactory {

    private JasperRunnerFactory() {
    }

    private static HashMap<String, JasperAsynchronousFillHandle> runners = new HashMap<String, JasperAsynchronousFillHandle>();

    public static void addRunner(String key, JasperAsynchronousFillHandle runner) {
        runners.put(key, runner);
    }

    public static void removeRunner(String key) {
        runners.remove(key);
    }

    public static JasperAsynchronousFillHandle getRunner(String key) {
        return runners.get(key);
    }

    public static boolean containsRunner(String key) {
        return runners.containsKey(key);
    }
}
