/* Created by JReleaseInfo AntTask from Open Source Competence Group */
/* Creation date Wed Aug 28 17:22:11 EEST 2013 */
package ro.nextreports.server;

import java.util.Date;

/**
 * This class provides information gathered from the build environment.
 * 
 * @author JReleaseInfo AntTask
 */
public class ReleaseInfo {


   /** buildDate (set during build process to 1377699731243L). */
   private static Date buildDate = new Date(1377699731243L);

   /**
    * Get buildDate (set during build process to Wed Aug 28 17:22:11 EEST 2013).
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


   /** version (set during build process to "6.2-SNAPSHOT"). */
   private static String version = "6.2-SNAPSHOT";

   /**
    * Get version (set during build process to "6.2-SNAPSHOT").
    * @return String version
    */
   public static final String getVersion() { return version; }

}
