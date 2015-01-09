package ro.nextreports.server.web.analysis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Required;

import ro.nextreports.server.domain.Analysis;
import ro.nextreports.server.service.AnalysisService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.analysis.util.AnalysisException;
import ro.nextreports.server.web.analysis.util.DatabaseUtil;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.orientechnologies.orient.jdbc.OrientJdbcDriver;

public class DatabaseAnalysisReader implements AnalysisReader {
		
	//private ComboPooledDataSource dataSource;
	private StorageService storageService;
	private AnalysisService analysisService;
	
	private Connection con = null;
	private int rowCount = -1;	
	private String reportId;
	
	public DatabaseAnalysisReader() {				
	}		

	@Override
	public List<String> getHeader(Analysis analysis) {	
		if (analysis == null) {
			return new ArrayList<String>();
		}
		reportId = analysis.getReportId();
		initConnection();
		List<String> columnNames = analysis.getColumns();		
		if ((columnNames == null) || columnNames.isEmpty()) {
			long start = System.currentTimeMillis();
			System.out.println("---------- getHeader");
			Columns columns = getColumns(analysis);
			columnNames = columns.getColumnNames();
			analysis.setColumns(columnNames);		
			analysis.setColumnTypes(columns.getColumnTypes());
			// select all by default
			List<Boolean> selected = new ArrayList<Boolean>();
			for (int i=0, size=columnNames.size(); i<size; i++) {
				selected.add(true);
			}
			analysis.setSelected(selected);
			long end = System.currentTimeMillis();
			System.out.println("*** getHeader  in " + (end-start) + " ms");
		} 
		if (analysis.getSortProperty() == null) {
			List<String> sortProperty = new ArrayList<String>();
			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$ sort = " +columnNames.get(0));
			sortProperty.add(columnNames.get(0));
			analysis.setSortProperty(sortProperty);
			List<Boolean> ascending = new ArrayList<Boolean>();
			ascending.add(true);
			analysis.setAscending(ascending);
		}
		System.out.println("----------> HEADER : " +  analysis.getSelectedColumns());
		return getHeaderColumnNames(analysis.getSelectedColumns());			
	}

	@Override
	public Integer getRowCount(Analysis analysis) throws AnalysisException {
		if (analysis == null) {
			return 0;
		}
		if (rowCount == -1) {
			long start = System.currentTimeMillis();
			System.out.println("---------- getRowCount");
			String sql = analysis.toSql(false);
			System.out.println("       sql="+sql);
			ResultSet rs = null;
			Statement stmt = null;
			int count = 0;
			try {
				stmt = con.createStatement();
				rs = stmt.executeQuery(sql);
				count = rs.last() ? rs.getRow()+1 : 0;
				rs.beforeFirst();
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new AnalysisException(ex.getMessage(), ex);
			} finally {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}			
			long end = System.currentTimeMillis();
			System.out.println("*** count = " + count + "  in " + (end-start) + " ms");
			rowCount = count;
		}
		return rowCount;
	}

	@Override
	public Iterator<AnalysisRow> iterator(Analysis analysis, long first, long count) throws AnalysisException {		
		long start = System.currentTimeMillis();
		System.out.println("---------- iterator  first="+first + " count="+count);
		List<AnalysisRow> list = new ArrayList<AnalysisRow>();
		if (analysis == null) {
			return list.iterator();
		}
		String sql = analysis.toSql(false);
		System.out.println("*** SQL = " + sql);		
		ResultSet rs = null;
		Statement stmt = null;
		
		try {
			int cols = getHeader(analysis).size();
			stmt = con.createStatement();
			if (analysis.getRowsPerPage() > 0) {
				stmt.setFetchSize((int)count);
			}
			rs = stmt.executeQuery(sql);
			if (first > 0) {
				rs.absolute((int)first-1);
			}
			int no = 0;
			while (rs.next()) {
				if (no > count) {
					break;
				} else {
					List<Object> cellValues = new ArrayList<Object>();				
					for (int i=1; i<=cols; i++) {
						cellValues.add(rs.getObject(i));
					}
					AnalysisRow row = new AnalysisRow(cellValues);
					list.add(row);
				}
				no++;
			}			
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new AnalysisException(ex.getMessage(), ex);
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {			
				e.printStackTrace();
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("*** iterator  in " + (end-start) + " ms");
		return list.iterator();
		
	}
	
	//@todo analysis
//	@Required
//    public void setDataSource(ComboPooledDataSource dataSource) {
//        this.dataSource = dataSource;
//    }
	
	@Required
	public void setStorageService(StorageService storageService) {
		this.storageService = storageService;
	}		
	
	@Required
	public void setAnalysisService(AnalysisService analysisService) {
		this.analysisService = analysisService;
	}

	
	private void initConnection() {
		if (con == null) {
//			try {
//				//con = dataSource.getConnection();
//				Report report = (Report)storageService.getEntityById(reportId);
//				con = ConnectionUtil.createConnection(storageService, report.getDataSource());
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			
		        Properties info = new Properties();
		        info.put("user", "admin");
		        info.put("password", "admin");
//		        info.put("db.usePool", "true"); // USE THE POOL
//		        info.put("db.pool.min", "3");   // MINIMUM POOL SIZE
//		        info.put("db.pool.max", "30");  // MAXIMUM POOL SIZE

		        try {
		        	DriverManager.registerDriver(new OrientJdbcDriver());
		        	con = DriverManager.getConnection("jdbc:orient:" + analysisService.getDatabasePath(), info);
		        } catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		        		    
			
		}		
	}
	
	

	private Columns getColumns(Analysis analysis) {
		String sql = analysis.toSql(true);
		System.out.println("*** SQL = " + sql);		
		ResultSet rs = null;
		Statement stmt = null;
		List<String> columnNames = new LinkedList<String>();
		Map<String, String> columnTypes = new HashMap<String, String>();
		Columns columns = new Columns();
		columns.setColumnNames(columnNames);
		columns.setColumnTypes(columnTypes);
		try {
			
			stmt = con.createStatement();
			stmt.setMaxRows(1);
			rs = stmt.executeQuery(sql);
			
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			
			for (int i = 0; i < columnCount; i++) {
				String name = rsmd.getColumnLabel(i + 1);
				columnNames.add(name);
				columnTypes.put(name, DatabaseUtil.getJavaType(rsmd.getColumnType(i + 1)));
			}
			System.out.println("--->  columnTypes="+columnTypes);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {			
				e.printStackTrace();
			}
		}
		return columns;
	}		
	
	public void reset() {
		rowCount = -1;
	}		
	
	private class Columns {
		private List<String> columnNames; 
		private Map<String, String> columnTypes;
		
		public Columns() {			
		}

		public List<String> getColumnNames() {
			return columnNames;
		}

		public void setColumnNames(List<String> columnNames) {
			this.columnNames = columnNames;
		}

		public Map<String, String> getColumnTypes() {
			return columnTypes;
		}

		public void setColumnTypes(Map<String, String> columnTypes) {
			this.columnTypes = columnTypes;
		}				
		
	}
	
	private List<String> getHeaderColumnNames(List<String> names) {
		List<String> result = new ArrayList<String>();
		for (String name : names) {
			result.add(DatabaseUtil.getColumnAlias(name));
		}
		return result;
	}

}