package ro.nextreports.server.web.analysis.feature.export;

import java.io.ByteArrayOutputStream;
import java.util.List;

import ro.nextreports.server.web.analysis.AnalysisDataProvider;

public class CsvResource extends ExportResource {

	private static final long serialVersionUID = 1L;
	
	private AnalysisDataProvider provider;
	
	private transient CsvWriter writer;
	
	public CsvResource(AnalysisDataProvider provider) {
		super(provider, CSV_CONTENT_TYPE);				
	}	

	@Override
	protected void printHeader(List<String> header, ByteArrayOutputStream out) {
		writer = new CsvWriter(out);
		for (String head : header) {
			writer.write(head);
		}
		writer.endLine();		
	}	

	@Override
	protected void createDetailRow(int row) {				
	}

	@Override
	protected void createDetailCell(int column, Object element) {
		writer.write(element);		
	}
	
	@Override
	protected void endDetailRow() {
		writer.endLine();		
	}

	@Override
	protected void write(ByteArrayOutputStream out) {
		writer.close();				
	}

}
