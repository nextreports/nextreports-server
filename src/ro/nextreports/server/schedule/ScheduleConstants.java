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
package ro.nextreports.server.schedule;

/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: Jun 9, 2008
 * Time: 3:16:49 PM
 */
public class ScheduleConstants {

    // scheduler constants
    public static final String ONCE_TYPE = "Once";
    public static final String MINUTELY_TYPE = "Minutely";
    public static final String HOURLY_TYPE = "Hourly";
    public static final String DAILY_TYPE = "Daily";
    public static final String WEEKLY_TYPE = "Weekly";
    public static final String MONTHLY_TYPE = "Monthly";

    public static final int MONTHLY_GENERAL_TYPE = 1;
    public static final int MONTHLY_DAY_OF_WEEK_TYPE = 2;
    public static final int MONTHLY_LAST_DAY_TYPE = 3;
    
    public static final int FIRST_DAY = 1;
    public static final int SECOND_DAY = 2;
    public static final int THIRD_DAY = 3;
    public static final int FOURTH_DAY = 4;
    public static final int LAST_DAY = 5;
    
}
