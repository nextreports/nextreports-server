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
public class ReleaseInfo {


   /** buildDate (set during build process to 1375191439461L). */
   private static Date buildDate = new Date(1375191439461L);

   /**
    * Get buildDate (set during build process to Tue Jul 30 16:37:19 EEST 2013).
    * @return Date buildDate
    */
   public static final Date getBuildDate() { return buildDate; }


   /** home (set during build process to "http://www.next-reports.com"). */
   private static String home = "http://www.next-reports.com";

   /**
    * Get home (set during build process to "http://www.next-reports.com").
    * @return String home
    */
   public static final String getHome() { return home; }


   /**
    * Get buildNumber (set during build process to 0).
    * @return int buildNumber
    */
   public static final int getBuildNumber() { return 0; }


   /** company (set during build process to "NextReports"). */
   private static String company = "NextReports";

   /**
    * Get company (set during build process to "NextReports").
    * @return String company
    */
   public static final String getCompany() { return company; }


   /** project (set during build process to "NextReports Server"). */
   private static String project = "NextReports Server";

   /**
    * Get project (set during build process to "NextReports Server").
    * @return String project
    */
   public static final String getProject() { return project; }


   /** copyright (set during build process to "2013"). */
   private static String copyright = "2013";

   /**
    * Get copyright (set during build process to "2013").
    * @return String copyright
    */
   public static final String getCopyright() { return copyright; }


   /** version (set during build process to "6.2"). */
   private static String version = "6.2";

   /**
    * Get version (set during build process to "6.2").
    * @return String version
    */
   public static final String getVersion() { return version; }

}
