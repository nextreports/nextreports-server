package ro.nextreports.server.service;

import java.util.List;

import ro.nextreports.server.domain.Analysis;
import ro.nextreports.server.domain.Link;
import ro.nextreports.server.exception.NotFoundException;

public interface AnalysisService {
	
	public boolean checkAnalysisPath();
	
	public String getAnalysisPath(Analysis analysis, String creator); 
	
	public List<Analysis> getMyAnalysis();
	
	public List<Link> getAnalysisLinks();
	
	public List<Link> getAnalysisLinks(String user);	
	
	public List<Analysis> getAnalysisByTable(String tableName);
    
    public List<Link> getWritableAnalysisLinks();
	
	public String addAnalysis(Analysis analysis);
	
	public void removeAnalysis(String analysisId) throws NotFoundException;
	
	public void modifyAnalysis(Analysis analysis);
	
	public String getDatabasePath();

}
