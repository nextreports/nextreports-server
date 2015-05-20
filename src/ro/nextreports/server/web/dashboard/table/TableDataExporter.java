/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.nextreports.server.web.dashboard.table;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.ServerUtil;

import ro.nextreports.engine.exporter.util.TableData;

/**
 * User: mihai.panaitescu
 * Date: 12-Apr-2010
 * Time: 13:23:27
 */
public class TableDataExporter {

    @SpringBean
    private StorageService storageService;

    public TableDataExporter() {
        Injector.get().inject(this);
    }

    public String toExcel(TableData data) {

        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("NextReports");

        HSSFRow headerRow = sheet.createRow(0);
        int col = 0;
		if (data.getHeader() != null) {
			for (int i = 0, size = data.getHeader().size(); i < size; i++) {
				if ( (data.getExcludedColumns() == null) ||
					 ((data.getExcludedColumns() != null) && !data.getExcludedColumns().contains(i)) ) {
					String s = data.getHeader().get(i);
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

		int row = 1;
		for (int i = 0, size = data.getData().size(); i < size; i++) {
			List<Object> rowData = data.getData().get(i);
			HSSFRow detailRow = sheet.createRow(row);
			col = 0;
			for (int j=0, cols=rowData.size(); j<cols; j++) {
				Object obj = rowData.get(j);
				if ( (data.getExcludedColumns() == null) ||
					 ((data.getExcludedColumns() != null) && !data.getExcludedColumns().contains(j))) {							
					HSSFCell cell = detailRow.createCell(col);
					cell.setCellType(HSSFCell.CELL_TYPE_STRING);
					if (obj == null) {
						obj = "";
					}
					cell.setCellValue(new HSSFRichTextString(obj.toString()));
					col++;
				}				
			}
			row++;
		}

        String file = getFile();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            wb.write(fos);
        } catch (IOException e) {
            e.printStackTrace();  
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace(); 
                }
            }
        }

        return file;

    }

    public String getFile() {
        StringBuilder sb = new StringBuilder(storageService.getSettings().getReportsHome());
        sb.append("/table/");
        File f = new File(sb.toString());
        f.mkdirs();
        sb.append(ServerUtil.getUsername());
        sb.append("_");
        sb.append(System.currentTimeMillis());
        sb.append(".xls");
        return sb.toString();        
    }
}
