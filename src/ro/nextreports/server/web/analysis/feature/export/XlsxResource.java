package ro.nextreports.server.web.analysis.feature.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import ro.nextreports.server.web.analysis.AnalysisDataProvider;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class XlsxResource extends ExportResource {

	private static final long serialVersionUID = 1L;

	private AnalysisDataProvider provider;
	
	private transient XSSFWorkbook wb;
    private transient XSSFSheet sheet;
    private transient XSSFRow detailRow;
	
	public XlsxResource(AnalysisDataProvider provider) {
		super(provider, EXCEL_CONTENT_TYPE);					
	}
	
	@Override
	protected void printHeader(List<String> header, ByteArrayOutputStream out) {
		wb = new XSSFWorkbook();
        sheet = wb.createSheet("NextReports");

        XSSFRow headerRow = sheet.createRow(0);
        int col = 0;        
		if (header != null) {
			for (String s : header) {
				XSSFCell cell = headerRow.createCell(col);
				cell.setCellType(XSSFCell.CELL_TYPE_STRING);
				if (s == null) {
					s = "";
				}
				cell.setCellValue(wb.getCreationHelper().createRichTextString(s));
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
		XSSFCell cell = detailRow.createCell(column);
		cell.setCellType(XSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(wb.getCreationHelper().createRichTextString(element.toString()));
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
