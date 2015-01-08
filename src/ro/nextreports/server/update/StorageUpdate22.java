package ro.nextreports.server.update;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.Folder;

public class StorageUpdate22 extends StorageUpdate {

	@Override
	protected void executeUpdate() throws Exception {
		createAnalysisNode();
	}
        
	private void createAnalysisNode() throws RepositoryException {
		LOG.info("Creating analysis node");
		
        Node rootNode = getTemplate().getRootNode();
        Node nextServerNode = rootNode.getNode(StorageConstants.NEXT_SERVER_FOLDER_NAME);
        
        Node analysisNode = nextServerNode.addNode(StorageConstants.ANALYSIS_FOLDER_NAME);
        analysisNode.addMixin("mix:referenceable");
        analysisNode.setProperty("className", Folder.class.getName());
        
        getTemplate().save();
	}
	
}
