/* Created by JReleaseInfo AntTask from Open Source Competence Group */
/* Creation date Tue Jan 14 15:24:12 EET 2014 */
package ro.nextreports.server;

import java.util.Date;

/**
 * This class provides information gathered from the build environment.
 * 
 * @author JReleaseInfo AntTask
 */
public class ReleaseInfo {


   /** buildDate (set during build process to 1389705852145L). */
   private static Date buildDate = new Date(1389705852145L);

   /**
    * Get buildDate (set during build process to Tue Jan 14 15:24:12 EET 2014).
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


   /** version (set during build process to "7.0-SNAPSHOT"). */
   private static String version = "7.0-SNAPSHOT";

   /**
    * Get version (set during build process to "7.0-SNAPSHOT").
    * @return String version
    */
   public static final String getVersion() { return version; }

}
