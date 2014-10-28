package ro.nextreports.server.distribution;

import java.io.File;
import java.io.IOException;

import org.apache.wicket.util.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.domain.CopyDestination;
import ro.nextreports.server.domain.RunReportHistory;

public class CopyDistributor implements Distributor {
	
	private static final Logger LOG = LoggerFactory.getLogger(CopyDistributor.class);

	@Override
	public void distribute(File file, Destination destination, DistributionContext context) throws DistributionException {
		CopyDestination copyDestination = (CopyDestination) destination;
		if (file != null) {
			String parentPath = file.getParentFile().getAbsolutePath();
			int index = file.getName().lastIndexOf(".");
			String extension = file.getName().substring(index+1);
			String newFileName = copyDestination.getFileName();
			if (!newFileName.endsWith(extension)) {
				newFileName = newFileName + "." + extension;
			}
			File newFile = new File(parentPath + File.separator + newFileName);
			try {
				Files.copy(file, newFile);
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
	            throw new DistributionException(e.getMessage());
			}
		}		
	}

	@Override
	public void afterDistribute(RunReportHistory history, DistributionContext context) {				
	}

	@Override
	public boolean isTestable() {		
		return false;
	}

	@Override
	public void test(Destination destination) throws DistributionException {				
	}

}
