package ro.nextreports.server.distribution;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.wicket.util.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.report.util.ReportUtil;

public class DistributorUtil {
	
	private static final Logger LOG = LoggerFactory.getLogger(DistributorUtil.class);
	
	public static String DATE_TEMPLATE = "${date}";
	private static String DATE_TEMPLATE_ESC = "\\$\\{date\\}";
	
	public static String TIME_TEMPLATE = "${time}";
	private static String TIME_TEMPLATE_ESC = "\\$\\{time\\}";
	
	public static String REPORT_NAME_TEMPLATE = "${name}";
	private static String REPORT_NAME_TEMPLATE_ESC = "\\$\\{name\\}";
	
	public static String PARAMETER_START_TEMPLATE = "$P{";
	public static String PARAMETER_END_TEMPLATE = "}";
	public static String PARAMETER_START_TEMPLATE_ESC = "\\$P\\{";
	public static String PARAMETER_END_TEMPLATE_ESC = "\\}";
	
	public static File getFileCopy(File originalFile, String copyName) throws DistributionException {
		if ((originalFile != null) && (copyName != null)) {
			String parentPath = originalFile.getParentFile().getAbsolutePath();
			int index = originalFile.getName().lastIndexOf(".");
			String extension = originalFile.getName().substring(index+1);
			String newFileName = copyName;
			if (!newFileName.endsWith(extension)) {
				newFileName = newFileName + "." + extension;
			}
			File newFile = new File(parentPath + File.separator + newFileName);
			try {
				Files.copy(originalFile, newFile);
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
	            throw new DistributionException(e.getMessage());
			}
			return newFile;
		} else {
			return null;
		}
	}
	
	public static void deleteFileCopy(String changedFileName, File file) {
		if (changedFileName != null) {	
			try {
				java.nio.file.Files.delete(file.toPath());
			} catch (IOException e) {				
				e.printStackTrace();
			}
		}
	}
	
	public static String replaceTemplates(String s, DistributionContext context) {
		Date local = new Date();
		if (s.contains(DATE_TEMPLATE)) {			
			DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
	    	String formattedDate = df.format(local);	    		    	
	    	s = s.replaceAll(DATE_TEMPLATE_ESC, formattedDate);
		}
		if (s.contains(TIME_TEMPLATE)) {
			DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
	    	String formattedTime = df.format(local);
	    	s = s.replaceAll(TIME_TEMPLATE_ESC, formattedTime);
		}
		if (s.contains(REPORT_NAME_TEMPLATE)) {
			s = s.replaceAll(REPORT_NAME_TEMPLATE_ESC, context.getReportName());
		}
		
		if (s.contains(PARAMETER_START_TEMPLATE)) {
			if (context.getParameterValues() != null) {
				for (String param : context.getParameterValues().keySet()) {
					Object value = context.getParameterValues().get(param);
					if ((value != null) && (s.contains(PARAMETER_START_TEMPLATE + param + PARAMETER_END_TEMPLATE))) {
						s = s.replaceAll(PARAMETER_START_TEMPLATE_ESC + param + PARAMETER_END_TEMPLATE_ESC, ReportUtil.getDisplayParameterValueAsString(value));
					}
				}
			}
		}
    	return s;
	}

}
