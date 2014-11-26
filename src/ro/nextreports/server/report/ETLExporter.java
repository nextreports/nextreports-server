package ro.nextreports.server.report;

import java.sql.Connection;
import java.util.HashMap;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.engine.FluentReportRunner;
import ro.nextreports.engine.Report;
import ro.nextreports.engine.ReportRunnerException;
import ro.nextreports.engine.queryexec.QueryResult;
import ro.nextreports.server.domain.NextContent;
import ro.nextreports.server.exception.ReportEngineException;
import ro.nextreports.server.report.next.NextRunnerFactory;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.ConnectionUtil;

public class ETLExporter {
	
	private static final Logger LOG = LoggerFactory.getLogger(ETLExporter.class);
	
	private ExportContext exportContext;
	private StorageService storageService;

	public ETLExporter(ExportContext exportContext) {		
		this.exportContext = exportContext;
	}
	
	public void setStorageService(StorageService storageService) {
		this.storageService = storageService;
	}

	private Connection createConnection() throws ReportEngineException {
		Connection connection;
		try {
			connection = ConnectionUtil.createConnection(storageService, exportContext.getReportDataSource());
			return connection;
		} catch (RepositoryException e) {
			throw new ReportEngineException("Cannot connect to database", e);
		}
	}
	
	public void export() throws ReportEngineException, InterruptedException {		
		Report report = NextUtil.getNextReport(storageService.getSettings(), (NextContent) exportContext.getReportContent());
		LOG.info("Export report '" + report.getName() + "' format="+ReportConstants.ETL_FORMAT + 
       		 " queryTimeout="+storageService.getSettings().getQueryTimeout());
		FluentReportRunner runner = FluentReportRunner.report(report);
		NextRunnerFactory.addRunner(exportContext.getKey(), runner);
		Connection conn = createConnection();
		runner = runner.connectTo(conn).                		
        		withQueryTimeout(storageService.getSettings().getQueryTimeout()).
                withParameterValues(new HashMap<String, Object>(exportContext.getReportParameterValues()));
				                     
        try {        
        	QueryResult qr = runner.executeQuery();        	
        	process(qr);
        } catch (ReportRunnerException ex) {
        	throw new ReportEngineException(ex.getMessage(), ex);
        } finally {
            NextRunnerFactory.removeRunner(exportContext.getKey());
            ConnectionUtil.closeConnection(conn);
        }
		
	}
	
	private void process(QueryResult qr) {
		
	}

}
