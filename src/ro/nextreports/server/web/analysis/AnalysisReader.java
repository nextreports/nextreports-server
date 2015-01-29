package ro.nextreports.server.web.analysis;

import java.util.Iterator;
import java.util.List;

import ro.nextreports.server.domain.Analysis;
import ro.nextreports.server.web.analysis.util.AnalysisException;

public interface AnalysisReader {

	public List<String> getHeader(Analysis analysis);		
	
	public Integer getRowCount(Analysis analysis) throws AnalysisException;
	
	public Iterator<AnalysisRow> iterator(Analysis analysis, long first, long count) throws AnalysisException;
	
	public void reset();
	
}
