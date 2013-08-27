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
package ro.nextreports.server;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * Launch Jetty embedded.
 * 
 * @author Decebal Suiu
 */
public class JettyLauncher {
	
	private static final int PORT = 8081;
	
    public static void main(String[] args) throws Exception {
        Server server = new Server();

        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(PORT);
        server.addConnector(connector);
        server.setStopAtShutdown(true);

        // the orders of handlers is very important!
        ContextHandler contextHandler = new ContextHandler();
        contextHandler.setContextPath("/reports");
        contextHandler.setResourceBase("./reports/");
        contextHandler.addHandler(new ResourceHandler());
        server.addHandler(contextHandler);

        server.addHandler(new WebAppContext("webapp", "/nextserver"));

        long t = System.currentTimeMillis();
        server.start();
        t = System.currentTimeMillis() - t;
        String version = server.getClass().getPackage().getImplementationVersion();
        System.out.println("Started Jetty Server " + version + " on port " + PORT + " in " + t / 1000 + "s");
        
        server.join();
    }
    
}
