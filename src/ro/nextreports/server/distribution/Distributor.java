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
package ro.nextreports.server.distribution;


import java.io.File;

import ro.nextreports.server.domain.RunReportHistory;

/**
 * @author Decebal Suiu
 */
public interface Distributor {
	
	/**
	 * Distribute a (report) file to a destination. 
	 * 
	 * @param file file
	 * @param destination destination
     * @param context distribution context
	 * @throws DistributionException if file cannot be distributed
	 */
	public void distribute(File file, Destination destination, DistributionContext context) throws DistributionException;


    /** 
     * After distribution method.
     *
     * @param history report history
     * @param context distribution context
     */
    public void afterDistribute(RunReportHistory history, DistributionContext context);
    
    public boolean isTestable();
    
    /**
     * Test (connection) method.
     */
    public void test(Destination destination) throws DistributionException;
    
}
