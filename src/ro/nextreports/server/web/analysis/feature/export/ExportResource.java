package ro.nextreports.server.web.analysis.feature.export;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.request.resource.ByteArrayResource;

import ro.nextreports.server.web.analysis.AnalysisDataProvider;
import ro.nextreports.server.web.analysis.AnalysisRow;

public abstract class ExportResource extends ByteArrayResource {
	
	private static final long serialVersionUID = 1L;
	
	public static final String EXCEL_CONTENT_TYPE = "excel/ms-excel";
	public static final String CSV_CONTENT_TYPE = "text/csv";	

	private AnalysisDataProvider provider;
	private String fileName;
	private String extension;
	private String DEFAULT_FILE_NAME="export";
	
	public ExportResource(AnalysisDataProvider provider, String contentType) {
		super(contentType);		
		this.provider = provider;	
		if (EXCEL_CONTENT_TYPE.equals(contentType)) {
			extension = "xls";
		} else if (CSV_CONTENT_TYPE.equals(contentType)) {
			extension = "csv";
		} else {
			extension = "txt";
		}
	}
			
	public void setProvider(AnalysisDataProvider provider) {
		this.provider = provider;
	}

	public String getFileName() {
		if (fileName == null) {
			return DEFAULT_FILE_NAME;
		}
		return fileName;
	}
			
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String getExtension() {
		return extension;
	}
	
	@Override
	protected byte[] getData(Attributes attributes) {	
	
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
        printHeader(provider.getHeader(), out);
        
        
        int row = 1;
//        Pager pager = new Pager(provider.getRowsPerPage(), (int)provider.size());
        Pager pager = new Pager(Integer.MAX_VALUE, (int)provider.size());
		for (int i = 0; i < pager.pages(); i++) {			
//			Iterator<AnalysisRow> it = provider.iterator(pager.offset(i), provider.getRowsPerPage());			
			Iterator<AnalysisRow> it = provider.iterator(pager.offset(i), Integer.MAX_VALUE);
			while (it.hasNext()) {
				createDetailRow(row);
				int col = 0;
				AnalysisRow analysisRow = it.next();				
				for (Object element : analysisRow.getCellValues()) {													
	                if (element == null) {
	                	element = "";
	                }
	                createDetailCell(col, element);
	                col++;
				}
				endDetailRow();
				row++;
			}			
		}
        
		                 
        write(out);
								
		provider.detach();
		return out.toByteArray();
	}

	@Override
	protected void setResponseHeaders(ResourceResponse data, Attributes attributes) {
		data.disableCaching();
		data.setFileName(getFileName()+"."+getExtension());
		super.setResponseHeaders(data, attributes);		
	}
	
	protected abstract void printHeader(List<String> header, ByteArrayOutputStream out);
	
	protected abstract void createDetailRow(int row);
	
	protected abstract void createDetailCell(int column, Object element);
	
	protected abstract void endDetailRow();
	
	protected abstract void write(ByteArrayOutputStream out);

}
