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

import net.sf.jasperreports.engine.fill.AsynchronousFillHandle;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReportsContext;

import java.sql.Connection;
import java.util.Map;
//
// Created by IntelliJ IDEA.
// User: mihai.panaitescu
// Date: 12-Oct-2009
// Time: 15:17:18

//
public class JasperAsynchronousFillHandle extends AsynchronousFillHandle {

    protected JasperAsynchronousFillHandle (JasperReportsContext context, JasperReport jasperReport,Map parameters,Connection conn)
            throws JRException {
 		super(context, jasperReport, parameters, conn);
 	}

    public boolean isCancelled() {
        return cancelled;
    }

    public Thread getFillThread() {
        return fillThread;
    }

    public JasperPrint getJasperPrint() {
        return filler.getJasperPrint();
    }
}
