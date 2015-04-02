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
package ro.nextreports.server.api;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;

import org.jcrom.JcrDataProviderImpl;
import org.jcrom.JcrFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.api.client.ChartMetaData;
import ro.nextreports.server.api.client.DataSourceMetaData;
import ro.nextreports.server.api.client.EntityConstants;
import ro.nextreports.server.api.client.EntityMetaData;
import ro.nextreports.server.api.client.ErrorCodes;
import ro.nextreports.server.api.client.FileMetaData;
import ro.nextreports.server.api.client.ReportMetaData;
import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.ChartContent;
import ro.nextreports.server.domain.DashboardState;
import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Folder;
import ro.nextreports.server.domain.Group;
import ro.nextreports.server.domain.NextContent;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.domain.User;
import ro.nextreports.server.domain.WidgetState;
import ro.nextreports.server.exception.DuplicationException;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.report.ReportConstants;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.ConnectionUtil;
import ro.nextreports.server.util.MimeTypeUtil;
import ro.nextreports.server.util.StorageUtil;
import ro.nextreports.engine.ReleaseInfoAdapter;
import ro.nextreports.engine.util.NextChartUtil;
import ro.nextreports.engine.util.ReportUtil;
import com.sun.jersey.api.core.InjectParam;

/**
 * @author Decebal Suiu
 */
@Path("storage")
public class StorageWebService {

	private static final Logger LOG = LoggerFactory.getLogger(StorageWebService.class);
	
	@InjectParam
	private StorageService storageService;
	
	@POST
	@Path("publishReport")
	public void publishReport(ReportMetaData reportMetaData) {
        // Path is relative to reports root ('/nextServer'). Ex: '/reports/test' <=> '/nextServer/reports/test' 
		String absolutePath = StorageConstants.NEXT_SERVER_ROOT + reportMetaData.getPath(); 
		if (LOG.isDebugEnabled()) {
			LOG.debug("absolutePath = " + absolutePath);
		}
		if (!absolutePath.startsWith(StorageConstants.REPORTS_ROOT)) {
			throw new WebApplicationException(new Exception("Invalid report path prefix (must be '/reports')"), 
					ErrorCodes.INVALID_REPORT_PATH);
		}

        byte status = ReportUtil.isValidReportVersion(reportMetaData.getMainFile().getFileContent());
        if (ReportUtil.REPORT_INVALID_OLDER == status) {
            throw new WebApplicationException(new Exception("Cannot publish an older version than 2.0."), 
            		ErrorCodes.OLD_REPORT_VERSION);
        } else if (ReportUtil.REPORT_INVALID_NEWER == status) {
            throw new WebApplicationException(new Exception("Cannot publish a newer version than " + ReleaseInfoAdapter.getVersionNumber()), 
            		ErrorCodes.NEW_REPORT_VERSION);            
        }

        boolean update = storageService.entityExists(absolutePath);
		if (LOG.isDebugEnabled()) {
			LOG.debug("update = " + update);
		}
		
		Report report;
		if (update) {
			try {
				report = (Report) storageService.getEntity(absolutePath);
			} catch (NotFoundException e) {
				throw new WebApplicationException(new Exception("Report path not found."), ErrorCodes.REPORT_PATH_NOT_FOUND);
			}
		} else {
            if (!storageService.entityExists(StorageUtil.getParentPath(absolutePath))) {
                throw new WebApplicationException(new Exception("Path not found."), ErrorCodes.PATH_NOT_FOUND);      
            }
            report = new Report();
		    report.setPath(absolutePath);
		    report.setName(StorageUtil.getName(reportMetaData.getPath()));
	        report.setType(ReportConstants.NEXT);
		}
        report.setSpecialType(reportMetaData.getSpecialType());
        report.setDescription(reportMetaData.getDescription());
	    if (reportMetaData.getDataSourcePath() != null) {
	    	String dataSourcePath = StorageConstants.NEXT_SERVER_ROOT + reportMetaData.getDataSourcePath();
			if (!dataSourcePath.startsWith(StorageConstants.DATASOURCES_ROOT)) {
				throw new WebApplicationException(new Exception("Invalid datasource path prefix (must be '/dataSources')"), 
						ErrorCodes.INVALID_DATASOURCE_PATH);
			}	    	
	    	DataSource dataSource;
			try {
				dataSource = (DataSource) storageService.getEntity(dataSourcePath);
			} catch (NotFoundException e) {
				throw new WebApplicationException(new Exception("Datasource path not found."), ErrorCodes.DATASOURCE_PATH_NOT_FOUND);
			}
	    	report.setDataSource(dataSource);
	    }
		
	    NextContent reportContent = createNextContent(reportMetaData, report.getPath());
	    report.setContent(reportContent);
        report = NextUtil.renameImagesAsUnique(report);
        if (update) {
			storageService.modifyEntity(report);	    	
	    } else {
		    try {
				storageService.addEntity(report);
		    } catch (DuplicationException e) {
				throw new WebApplicationException(e, ErrorCodes.DUPLICATION);
		    }
		}	    
	}

    @POST
	@Path("publishChart")
	public void publishChart(ChartMetaData chartMetaData) {
        // Path is relative to reports root ('/nextServer'). Ex: '/reports/test' <=> '/nextServer/reports/test'
		String absolutePath = StorageConstants.NEXT_SERVER_ROOT + chartMetaData.getPath();
		if (LOG.isDebugEnabled()) {
			LOG.debug("absolutePath = " + absolutePath);
		}
		if (!absolutePath.startsWith(StorageConstants.CHARTS_ROOT)) {
			throw new WebApplicationException(new Exception("Invalid chart path prefix (must be '/charts')"),
					ErrorCodes.INVALID_CHART_PATH);
		}

        byte status = ReportUtil.isValidReportVersion(chartMetaData.getMainFile().getFileContent());

        if (NextChartUtil.CHART_INVALID_NEWER == status) {
            throw new WebApplicationException(new Exception("Cannot publish a newer version than " + ReleaseInfoAdapter.getVersionNumber()),
            		ErrorCodes.NEW_CHART_VERSION);
        }

        boolean update = storageService.entityExists(absolutePath);
		if (LOG.isDebugEnabled()) {
			LOG.debug("update = " + update);
		}

		Chart chart;
		if (update) {
			try {
				chart = (Chart) storageService.getEntity(absolutePath);
			} catch (NotFoundException e) {
				throw new WebApplicationException(new Exception("Chart path not found."), ErrorCodes.CHART_PATH_NOT_FOUND);
			}
		} else {
            if (!storageService.entityExists(StorageUtil.getParentPath(absolutePath))) {
                throw new WebApplicationException(new Exception("Path not found."), ErrorCodes.PATH_NOT_FOUND);
            }
            chart = new Chart();
		    chart.setPath(absolutePath);
		    chart.setName(StorageUtil.getName(chartMetaData.getPath()));
		}

	    chart.setDescription(chartMetaData.getDescription());
	    if (chartMetaData.getDataSourcePath() != null) {
	    	String dataSourcePath = StorageConstants.NEXT_SERVER_ROOT + chartMetaData.getDataSourcePath();
			if (!dataSourcePath.startsWith(StorageConstants.DATASOURCES_ROOT)) {
				throw new WebApplicationException(new Exception("Invalid datasource path prefix (must be '/dataSources')"),
						ErrorCodes.INVALID_DATASOURCE_PATH);
			}
	    	DataSource dataSource;
			try {
				dataSource = (DataSource) storageService.getEntity(dataSourcePath);
			} catch (NotFoundException e) {
				throw new WebApplicationException(new Exception("Datasource path not found."), ErrorCodes.DATASOURCE_PATH_NOT_FOUND);
			}
	    	chart.setDataSource(dataSource);
	    }

	    ChartContent chartContent = createChartContent(chartMetaData, chart.getPath());
	    chart.setContent(chartContent);
	    if (update) {
			storageService.modifyEntity(chart);
	    } else {
		    try {
				storageService.addEntity(chart);
		    } catch (DuplicationException e) {
				throw new WebApplicationException(e, ErrorCodes.DUPLICATION);
		    }
		}
	}
    
    /**
     * Test version of local entity versus server engine version
     * @param localVersion
     * @return
     */
    @GET
	@Path("version")
    public byte getVersionStatus(@QueryParam("localVersion") String localVersion) {
    	return ReportUtil.isValid(localVersion);
    }

    /**
	 * Path is relative to nextserver root ('/nextServer'). Ex: '/reports/test' <=> '/nextServer/reports/test'
	 * 
	 * @param path path
	 * @return list of entities from path
	 */
	@GET
	@Path("getEntities")
	public List<EntityMetaData> getEntities(@QueryParam("path") String path) {
		String absolutePath = StorageConstants.NEXT_SERVER_ROOT + path;
		List<EntityMetaData> result = new ArrayList<EntityMetaData>();
		Entity[] entities = new Entity[0];
		try {
			entities = storageService.getEntityChildren(absolutePath);
		} catch (NotFoundException e) {
			// TODO
			e.printStackTrace();
		}
		
		List<Entity> list = Arrays.asList(entities);
		Collections.sort(list, new Comparator<Entity>() {
			public int compare(Entity e1, Entity e2) {
				if (e1 instanceof Folder) {
					if (e2 instanceof Folder) {
						return Collator.getInstance().compare(e1.getName(), e2.getName());
					} else {
						return -1;
					}
				} else {
					if (e2 instanceof Folder) {
						return 1;
					} else {
						return Collator.getInstance().compare(e1.getName(), e2.getName());
					}
				}
			}
		});
		
		for (Entity entity : list) {
			result.add(createMetaData(entity));
		}
		
		return  result;
	}
	
	@POST
	@Path("publishDataSource")
	public void publishDataSource(DataSourceMetaData dataSourceMetaData) {
		String absolutePath = StorageConstants.NEXT_SERVER_ROOT + dataSourceMetaData.getPath(); 
		if (LOG.isDebugEnabled()) {
			LOG.debug("absolutePath = " + absolutePath);
		}
		if (!absolutePath.startsWith(StorageConstants.DATASOURCES_ROOT)) {
			throw new WebApplicationException(new Exception("Invalid dataSource path prefix (must be '/dataSources')"), 
					ErrorCodes.INVALID_DATASOURCE_PATH);
		}

		boolean update = storageService.entityExists(absolutePath);
		if (LOG.isDebugEnabled()) {
			LOG.debug("update = " + update);
		}
		
		DataSource dataSource;
		if (update) {
			try {
				dataSource = (DataSource) storageService.getEntity(absolutePath);
			} catch (NotFoundException e) {
				throw new WebApplicationException(new Exception("Dayasource path not found."), ErrorCodes.DATASOURCE_PATH_NOT_FOUND);
			}
		} else {		
			dataSource = new DataSource();
			dataSource.setPath(StorageConstants.NEXT_SERVER_ROOT + dataSourceMetaData.getPath());
			dataSource.setName(StorageUtil.getName(dataSourceMetaData.getPath())); 
		}
		dataSource.setVendor(dataSourceMetaData.getVendor());
		dataSource.setDriver(dataSourceMetaData.getDriver());
		dataSource.setUrl(dataSourceMetaData.getUrl());
		dataSource.setUsername(dataSourceMetaData.getUsername());
		dataSource.setPassword(dataSourceMetaData.getPassword());
		dataSource.setProperties(ConnectionUtil.convertPropertiesToList(dataSourceMetaData.getProperties(), dataSource.getPath()));
		
	    if (update) {
			storageService.modifyEntity(dataSource);	    	
	    } else {
		    try {
				storageService.addEntity(dataSource);
		    } catch (DuplicationException e) {
				throw new WebApplicationException(e, ErrorCodes.DUPLICATION);
		    }
		}	    
	}

	@GET
	public boolean authenticationChecker() {
		return true;
	}
	
	/**
	 * Path is relative to nextserver root ('/nextServer'). Ex: '/reports/test' <=> '/nextServer/reports/test'
	 * 
	 * @param path path
	 * @return report meta data from that path
	 */
	@GET
	@Path("getReport")
	public ReportMetaData getReport(@QueryParam("path") String path) {
		String absolutePath = StorageConstants.NEXT_SERVER_ROOT + path;
		if (!storageService.entityExists(absolutePath)) {
			throw new WebApplicationException(new Exception("No report at '"+  path + "'"), 
					ErrorCodes.NOT_FOUND);			
		}
		Report report;
		try {
			report = (Report) storageService.getEntity(absolutePath);
		} catch (NotFoundException e) {
			throw new WebApplicationException(new Exception("No report at '"+  path + "'"), 
					ErrorCodes.NOT_FOUND);
		}
		if (!ReportConstants.NEXT.equals(report.getType())) {
			throw new WebApplicationException(new Exception("No report at '"+  path + "'"), 
					ErrorCodes.NOT_FOUND);
		}
		
		return createReportMetaData(report);
	}

    @GET
    @Path("getChart")
    public ReportMetaData getChart(@QueryParam("path") String path) {
        String absolutePath = StorageConstants.NEXT_SERVER_ROOT + path;
        if (!storageService.entityExists(absolutePath)) {
            throw new WebApplicationException(new Exception("No chart at '" + path + "'"),
                    ErrorCodes.NOT_FOUND);
        }
        Chart chart = null;
		try {
			chart = (Chart) storageService.getEntity(absolutePath);
		} catch (NotFoundException e) {
			// never happening
		}
		
        return createChartMetaData(chart);
    }
    
    @GET
    @Path("getDataSource")
    public DataSourceMetaData getDataSource(@QueryParam("path") String path) {
        String absolutePath = StorageConstants.NEXT_SERVER_ROOT + path;
        if (!storageService.entityExists(absolutePath)) {
            throw new WebApplicationException(new Exception("No data source at '" + path + "'"), ErrorCodes.NOT_FOUND);
        }
        DataSource ds = null;
		try {
			ds = (DataSource) storageService.getEntity(absolutePath);
		} catch (NotFoundException e) {
			// never happening
		}
		
        return createDataSourceMetaData(ds);
    }

    @GET
	@Path("entityExists")	
	public int entityExists(@QueryParam("path") String path) {
		boolean exists = storageService.entityExists(StorageConstants.NEXT_SERVER_ROOT + path);
        if  (!exists)  {
             return EntityConstants.ENTITY_NOT_FOUND;
        } else {
            Entity entity;
			try {
				entity = storageService.getEntity(StorageConstants.NEXT_SERVER_ROOT + path);
			} catch (NotFoundException e) {
	            throw new WebApplicationException(new Exception("Not found '" + path + "'"),
	                    ErrorCodes.NOT_FOUND);
			}
            if (entity instanceof Folder) {
                return EntityConstants.FOLDER_FOUND;
            } else if (entity instanceof Report) {
                return EntityConstants.REPORT_FOUND;
            } else if (entity instanceof Chart) {
                return EntityConstants.CHART_FOUND;
            } else if (entity instanceof DataSource) {
                return EntityConstants.DATA_SOURCE_FOUND;
            } else if (entity instanceof SchedulerJob) {
                return EntityConstants.SCHEDULER_JOB_FOUND;
            } else if (entity instanceof User) {
                return EntityConstants.USER_FOUND;
            } else if (entity instanceof Group) {
                return EntityConstants.GROUP_FOUND;
            } else {
                return EntityConstants.UNKNOWN_FOUND;
            }
        }
    }

	@POST
	@Path("createFolder")	
	public void createFolder(String path) {
		Folder folder = new Folder();
		folder.setName(StorageUtil.getName(path));
		folder.setPath(StorageConstants.NEXT_SERVER_ROOT + path);
		try {
			storageService.addEntity(folder);
		} catch (DuplicationException e) {
			throw new WebApplicationException(new Exception(e.getMessage()), 
					ErrorCodes.DUPLICATION);
		}
	}

	private NextContent createNextContent(ReportMetaData reportMetaData, String reportPath) {
		NextContent reportContent = new NextContent();
    	reportContent.setName("content");
    	reportContent.setPath(StorageUtil.createPath(reportPath, "content"));
    	JcrFile xmlFile = new JcrFile();
    	xmlFile.setName(reportMetaData.getMainFile().getFileName());
    	xmlFile.setLastModified(Calendar.getInstance());
    	xmlFile.setPath(StorageUtil.createPath(reportContent.getPath(), xmlFile.getName()));
    	xmlFile.setMimeType("text/xml");
    	xmlFile.setDataProvider(new JcrDataProviderImpl(reportMetaData.getMainFile().getFileContent()));
    	reportContent.setNextFile(xmlFile);

        List<JcrFile> imageFiles = new ArrayList<JcrFile>();
        if (reportMetaData.getImages() != null) {
            for (FileMetaData fmd : reportMetaData.getImages()) {
                JcrFile imageFile = new JcrFile();
                imageFile.setName(fmd.getFileName());
                imageFile.setPath(StorageUtil.createPath(reportContent.getPath(), imageFile.getName()));
                String mimeType = MimeTypeUtil.getMimeType(fmd.getFileContent());
                imageFile.setMimeType(mimeType);
                imageFile.setLastModified(Calendar.getInstance());
                imageFile.setDataProvider(new JcrDataProviderImpl(fmd.getFileContent()));
                imageFiles.add(imageFile);
            }
        }
        reportContent.setImageFiles(imageFiles);
        
        if (reportMetaData.getTemplate() != null) {
        	FileMetaData fmd = reportMetaData.getTemplate();
        	JcrFile templateFile = new JcrFile();        	
            templateFile.setName(fmd.getFileName());
            templateFile.setPath(StorageUtil.createPath(reportContent.getPath(), templateFile.getName()));
            String mimeType = MimeTypeUtil.getMimeType(fmd.getFileContent());
            templateFile.setMimeType(mimeType);
            templateFile.setLastModified(Calendar.getInstance());
            templateFile.setDataProvider(new JcrDataProviderImpl(fmd.getFileContent()));
            reportContent.setTemplateFile(templateFile);
        }

        return reportContent;
	}

    private ChartContent createChartContent(ChartMetaData chartMetaData, String chartPath) {
		ChartContent chartContent = new ChartContent();
    	chartContent.setName("content");
    	chartContent.setPath(StorageUtil.createPath(chartPath, "content"));
    	JcrFile xmlFile = new JcrFile();
    	xmlFile.setName(chartMetaData.getMainFile().getFileName());
    	xmlFile.setLastModified(Calendar.getInstance());
    	xmlFile.setPath(StorageUtil.createPath(chartContent.getPath(), xmlFile.getName()));
    	xmlFile.setMimeType("text/xml");
    	xmlFile.setDataProvider(new JcrDataProviderImpl(chartMetaData.getMainFile().getFileContent()));
    	chartContent.setChartFile(xmlFile);
    	
		return chartContent;
	}

    private EntityMetaData createMetaData(Entity entity) {
		EntityMetaData entityMetaData = new EntityMetaData();
		entityMetaData.setEntityId(entity.getId());
		entityMetaData.setPath(entity.getPath().substring(StorageConstants.NEXT_SERVER_ROOT.length()));
		int type = EntityMetaData.OTHER;
		if (entity instanceof Folder) {
			type = EntityMetaData.FOLDER;
		} else if (entity instanceof Report) {
			Report report = (Report) entity;
			String reportType = report.getType();
			if (ReportConstants.NEXT.equals(reportType)) {
				type = EntityMetaData.NEXT_REPORT;
			} else if (ReportConstants.JASPER.equals(reportType)) {
				type = EntityMetaData.JASPER_REPORT;
			}
		} else if (entity instanceof DataSource) {
			type = EntityMetaData.DATA_SOURCE;
		} else if (entity instanceof Chart) {
            type  = EntityMetaData.CHART;
        } else if (entity instanceof DashboardState) {
        	type = EntityMetaData.DASHBOARD;
        } else if (entity instanceof WidgetState) {
        	type = EntityMetaData.WIDGET;
        }
		entityMetaData.setType(type);

		return entityMetaData;
	}
		
	private ReportMetaData createReportMetaData(Report report) {
        report = NextUtil.restoreImagesName(report);
        ReportMetaData reportMetaData = new ReportMetaData();
        reportMetaData.setSpecialType(report.getSpecialType());
        reportMetaData.setPath(report.getPath().substring(StorageConstants.NEXT_SERVER_ROOT.length()));
		reportMetaData.setDescription(report.getDescription());
		if (report.getDataSource() != null) {
			reportMetaData.setDataSourcePath(report.getDataSource().getPath().substring(StorageConstants.NEXT_SERVER_ROOT.length()));
		}
		NextContent nextContent = (NextContent) report.getContent();
        FileMetaData fileContent = new FileMetaData();
        fileContent.setFileName(nextContent.getFileName());
        fileContent.setFileContent(nextContent.getNextFile().getDataProvider().getBytes());
        reportMetaData.setMainFile(fileContent);

        List<JcrFile> images = nextContent.getImageFiles();
        if (images != null) {
            List<FileMetaData> imagesData = new ArrayList<FileMetaData>();
            for (JcrFile image : images) {
                FileMetaData fmd = new FileMetaData();
                fmd.setFileName(image.getName());
                fmd.setFileContent(image.getDataProvider().getBytes());
                imagesData.add(fmd);
            }
            reportMetaData.setImages(imagesData);
        }
        
        JcrFile template = nextContent.getTemplateFile();
        if (template != null) {
        	FileMetaData fmd = new FileMetaData();
            fmd.setFileName(template.getName());
            fmd.setFileContent(template.getDataProvider().getBytes());
            reportMetaData.setTemplate(fmd);
        }
		
        return reportMetaData;
	}

    private ChartMetaData createChartMetaData(Chart chart) {
		ChartMetaData chartMetaData = new ChartMetaData();
		chartMetaData.setPath(chart.getPath().substring(StorageConstants.NEXT_SERVER_ROOT.length()));
		chartMetaData.setDescription(chart.getDescription());
		if (chart.getDataSource() != null) {
			chartMetaData.setDataSourcePath(chart.getDataSource().getPath().substring(StorageConstants.NEXT_SERVER_ROOT.length()));
		}
		ChartContent chartContent = chart.getContent();
        FileMetaData fileContent = new FileMetaData();
        fileContent.setFileName(chartContent.getFileName());
		fileContent.setFileContent(chartContent.getChartFile().getDataProvider().getBytes());
        chartMetaData.setMainFile(fileContent);

        return chartMetaData;
	}
    
    private DataSourceMetaData createDataSourceMetaData(DataSource ds) {
    	DataSourceMetaData dsMetaData = new DataSourceMetaData();
		dsMetaData.setPath(ds.getPath().substring(StorageConstants.NEXT_SERVER_ROOT.length()));
		dsMetaData.setDriver(ds.getDriver());
		dsMetaData.setVendor(ds.getVendor());
		dsMetaData.setDriver(ds.getDriver());
		dsMetaData.setUrl(ds.getUrl());
		dsMetaData.setUsername(ds.getUsername());
		dsMetaData.setPassword(ds.getPassword());
		dsMetaData.setProperties(ConnectionUtil.convertListToProperties(ds.getProperties()));
        return dsMetaData;
	}
        
	
}
