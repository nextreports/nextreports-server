package ro.nextreports.server.distribution;

import java.io.File;
import java.io.IOException;

import org.apache.wicket.util.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistributorUtil {
	
	private static final Logger LOG = LoggerFactory.getLogger(DistributorUtil.class);
	
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

}
