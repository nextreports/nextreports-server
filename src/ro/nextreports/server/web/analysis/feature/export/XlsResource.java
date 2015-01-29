package ro.nextreports.server.web.analysis.feature.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import ro.nextreports.server.web.analysis.AnalysisDataProvider;

public class XlsResource extends ExportResource {

	private static final long serialVersionUID = 1L;

	private AnalysisDataProvider provider;
	
	private transient HSSFWorkbook wb;
    private transient HSSFSheet sheet;
    private transient HSSFRow detailRow;
	
	public XlsResource(AnalysisDataProvider provider) {
		super(provider, EXCEL_CONTENT_TYPE);					
	}
	
	@Override
	protected void printHeader(List<String> header, ByteArrayOutputStream out) {
		wb = new HSSFWorkbook();
        sheet = wb.createSheet("NextReports");

        HSSFRow headerRow = sheet.createRow(0);
        int col = 0;        
		if (header != null) {
			for (String s : header) {
				HSSFCell cell = headerRow.createCell(col);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);
				if (s == null) {
					s = "";
				}
				cell.setCellValue(new HSSFRichTextString(s));
				col++;
			}
		}		
	}

	@Override
	protected void createDetailRow(int row) {
		detailRow = sheet.createRow(row);		
	}

	@Override
	protected void createDetailCell(int column, Object element) {
		HSSFCell cell = detailRow.createCell(column);
		cell.setCellType(HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new HSSFRichTextString(element.toString()));
	}
	
	@Override
	protected void endDetailRow() {			
	}

	@Override
	protected void write(ByteArrayOutputStream out) {
		try {
			wb.write(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	

}
