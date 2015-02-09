package ro.nextreports.server.distribution;

import java.io.File;

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
			DistributorUtil.getFileCopy(file, copyDestination.getFileName());
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
