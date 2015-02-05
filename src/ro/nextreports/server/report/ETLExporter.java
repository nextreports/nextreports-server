package ro.nextreports.server.report;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.engine.FluentReportRunner;
import ro.nextreports.engine.Report;
import ro.nextreports.engine.ReportRunnerException;
import ro.nextreports.engine.queryexec.QueryResult;
import ro.nextreports.server.domain.NextContent;
import ro.nextreports.server.etl.DefaultProcessor;
import ro.nextreports.server.etl.DocumentTransformer;
import ro.nextreports.server.etl.Extractor;
import ro.nextreports.server.etl.OrientDbLoader;
import ro.nextreports.server.etl.Processor;
import ro.nextreports.server.etl.ResultSetExtractor;
import ro.nextreports.server.etl.Transformer;
import ro.nextreports.server.exception.ReportEngineException;
import ro.nextreports.server.report.next.NextRunnerFactory;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.service.AnalysisService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.ConnectionUtil;

public class ETLExporter {

	private static final Logger LOG = LoggerFactory.getLogger(ETLExporter.class);

	private ExportContext exportContext;
	private StorageService storageService;
	private AnalysisService analysisService;

	public ETLExporter(ExportContext exportContext) {
		this.exportContext = exportContext;
	}

	public void setStorageService(StorageService storageService) {
		this.storageService = storageService;
	}

	public void setAnalysisService(AnalysisService analysisService) {
		this.analysisService = analysisService;
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
			String tableName = getEtlTableName(exportContext.getCreator(), report);
        	createEtl(runner.executeQuery(), tableName);
        } catch (ReportRunnerException ex) {
        	throw new ReportEngineException(ex.getMessage(), ex);
        } finally {
            NextRunnerFactory.removeRunner(exportContext.getKey());
            ConnectionUtil.closeConnection(conn);
        }

	}

	private String getEtlTableName(String userName, Report report) {
		return userName + "-" + report.getBaseName();
	}

	private void createEtl(QueryResult queryResult, String tableName) {
		// create extractor
		Extractor extractor = new ResultSetExtractor(queryResult.getResultSet());

		// create loader
		OrientDbLoader loader = new OrientDbLoader();
		loader.setDbUrl(analysisService.getDatabasePath());
		loader.setClassName(tableName);
		loader.setAutoDropClass(true);
        loader.setDbAutoCreateProperties(true);

		// create transformers
		List<Transformer> transformers = new ArrayList<Transformer>();
		transformers.add(new DocumentTransformer());

		// create processor and go
		Processor processor = new DefaultProcessor(extractor, transformers, loader);
		long time = System.currentTimeMillis();
		processor.init();
		processor.process();
		processor.destroy();
		time = System.currentTimeMillis() - time;
		LOG.info("Executed in {} ms", time);
	}

}
