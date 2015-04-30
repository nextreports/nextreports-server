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
package ro.nextreports.server.service;

import java.awt.Color;
import java.sql.Connection;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.jcr.RepositoryException;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.aop.Profile;
import ro.nextreports.server.cache.Cache;
import ro.nextreports.server.cache.CacheFactory;
import ro.nextreports.server.cache.QueryCacheKey;
import ro.nextreports.server.cache.ehcache.WidgetCacheEventListener;
import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.DashboardState;
import ro.nextreports.server.domain.DrillEntityContext;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Folder;
import ro.nextreports.server.domain.Link;
import ro.nextreports.server.domain.NextContent;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.UserWidgetParameters;
import ro.nextreports.server.domain.WidgetState;
import ro.nextreports.server.exception.DuplicationException;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.exception.ReferenceException;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.report.util.ReportUtil;
import ro.nextreports.server.util.ChartUtil;
import ro.nextreports.server.util.ConnectionUtil;
import ro.nextreports.server.util.PermissionUtil;
import ro.nextreports.server.util.ServerUtil;
import ro.nextreports.server.util.StorageUtil;
import ro.nextreports.server.util.WidgetUtil;
import ro.nextreports.server.web.dashboard.Dashboard;
import ro.nextreports.server.web.dashboard.DashboardColumn;
import ro.nextreports.server.web.dashboard.DashboardUtil;
import ro.nextreports.server.web.dashboard.DefaultDashboard;
import ro.nextreports.server.web.dashboard.EntityWidget;
import ro.nextreports.server.web.dashboard.Widget;
import ro.nextreports.server.web.dashboard.WidgetDescriptor;
import ro.nextreports.server.web.dashboard.WidgetFactory;
import ro.nextreports.server.web.dashboard.WidgetLocation;
import ro.nextreports.server.web.dashboard.WidgetRegistry;
import ro.nextreports.server.web.dashboard.alarm.AlarmWidget;
import ro.nextreports.server.web.dashboard.chart.ChartWidget;
import ro.nextreports.server.web.dashboard.display.DisplayWidget;
import ro.nextreports.server.web.dashboard.drilldown.DrillDownWidget;
import ro.nextreports.server.web.dashboard.indicator.IndicatorWidget;
import ro.nextreports.server.web.dashboard.table.TableWidget;

import ro.nextreports.engine.ReportRunner;
import ro.nextreports.engine.ReportRunnerException;
import ro.nextreports.engine.chart.ChartRunner;
import ro.nextreports.engine.exporter.exception.NoDataFoundException;
import ro.nextreports.engine.exporter.util.AlarmData;
import ro.nextreports.engine.exporter.util.DisplayData;
import ro.nextreports.engine.exporter.util.IndicatorData;
import ro.nextreports.engine.exporter.util.TableData;
import ro.nextreports.engine.i18n.I18nLanguage;
import ro.nextreports.engine.i18n.I18nUtil;
import ro.nextreports.engine.querybuilder.sql.dialect.CSVDialect;
import ro.nextreports.engine.util.ColorUtil;

/**
 * @author Decebal Suiu
 */
public class DefaultDashboardService implements DashboardService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultDashboardService.class);  

    private StorageService storageService;
    private SecurityService securityService;
    private WidgetFactory widgetFactory;
    private WidgetRegistry widgetRegistry;

	private CacheFactory cacheFactory;
	private CacheManager cacheManager;
	
	@Required
	public void setCacheFactory(CacheFactory cacheFactory) {
		this.cacheFactory = cacheFactory;
	}

    @Required
    public void setStorageService(StorageService storageService) {
        this.storageService = storageService;
    }

    @Required
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }
    
    @Required
    public void setWidgetFactory(WidgetFactory widgetFactory) {
        this.widgetFactory = widgetFactory;
    }

    @Required
    public void setWidgetRegistry(WidgetRegistry widgetRegistry) {
        this.widgetRegistry = widgetRegistry;
    }
        
    @Required
    public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;		
		// TODO in spring 3.x you can add CacheEventListener in EhCacheFactoryBean !!!
		Ehcache cache =  cacheManager.getEhcache("entitiesCache");	
		WidgetCacheEventListener listener = new WidgetCacheEventListener();
		listener.setDashboardService(DefaultDashboardService.this);
		cache.getCacheEventNotificationService().registerListener(listener);
	}

	@Transactional
    public List<Dashboard> getMyDashboards() {
        String dashboardsPath = getMyDashboardsPath();
        String username = getUsername();
        if (!storageService.entityExists(dashboardsPath)) {
            Folder dashboardsFolder = new Folder(username, dashboardsPath);
            try {
                storageService.addEntity(dashboardsFolder);
            } catch (DuplicationException e) {
                // never happening
                throw new RuntimeException(e);
            }
            LOG.info("Created 'dashboards' repository for user '" + username + "'");
            String id = addDashboard(new DefaultDashboard(MY_DASHBOARD_NAME, 2));
            LOG.info("Created '" + MY_DASHBOARD_NAME + "' dashboard for user '" + username + "'");

            // cannot use getEntitiesByClassName (see below) because transaction is not committed yet
            try {
                Dashboard dashboard = getDashboard(id);
                List<Dashboard> tmp = new ArrayList<Dashboard>();
                tmp.add(dashboard);

                return tmp;
            } catch (NotFoundException e) {
                // never happening
                throw new RuntimeException(e);
            }
        }

        Entity[] entities;
        try {
            entities = storageService.getEntitiesByClassName(dashboardsPath, DashboardState.class.getName());
        } catch (NotFoundException e) {
            // never happening
            throw new RuntimeException(e);
        }       

        return getDashboards(entities);
    }
	
	@Transactional(readOnly = true)
    public List<DashboardState> getDashboards(String user) {
		List<DashboardState> result = new ArrayList<DashboardState>();
        String dashboardsPath = StorageConstants.DASHBOARDS_ROOT + "/" + user;              
        if (!storageService.entityExists(dashboardsPath)) {
           return result;
        }
        Entity[] entities;
        try {
            entities = storageService.getEntitiesByClassName(dashboardsPath, DashboardState.class.getName());            
        } catch (NotFoundException e) {
        	LOG.error(e.getMessage(), e);
            // never happening
            throw new RuntimeException(e);
        }  
        for (Entity entity : entities) {
        	result.add((DashboardState)entity);
        }
        Collections.sort(result, new Comparator<DashboardState>() {

            public int compare(DashboardState o1, DashboardState o2) {
                if (MY_DASHBOARD_NAME.equals(o1.getName())) {
                    return -1;
                } else if (MY_DASHBOARD_NAME.equals(o2.getName())) {
                    return 1;
                } else {
                    return Collator.getInstance().compare(o1.getName(), o2.getName());
                }
            }
        });
        return result;
    }
	
	@Transactional(readOnly = true)
    public List<WidgetState>  getWidgets(String dashboardPath) {
		List<WidgetState> result = new ArrayList<WidgetState>();						
		try {
			Dashboard dashboard = getDashboardByPath(StorageConstants.NEXT_SERVER_ROOT + dashboardPath);			
			List<Widget> widgets = dashboard.getWidgets();
			for (Widget w : widgets) {
				if (!w.isCollapsed()) {
					result.add(createWidgetState(dashboardPath, w));
				}
			}
			
			Collections.sort(result, new Comparator<WidgetState>() {
	            public int compare(WidgetState o1, WidgetState o2) {	            	
	                return Collator.getInstance().compare(o1.getName(), o2.getName());	                
	            }
	        });
		} catch (NotFoundException e) {
			LOG.error(e.getMessage(), e);			
		}
		return result;		
	}	
	
	@Transactional(readOnly = true)
    public List<Link> getDashboardLinks(String user) {
        return getDashboardLinks(PermissionUtil.getRead(), user);
    }

    @Transactional(readOnly = true)
    public List<Link> getDashboardLinks() {
        return getDashboardLinks(PermissionUtil.getRead(), ServerUtil.getUsername());
    }
    
    @Transactional(readOnly = true)
    public List<Link> getWritableDashboardLinks() {
    	return getDashboardLinks(PermissionUtil.getWrite(), ServerUtil.getUsername());
    }
    
    private List<Link> getDashboardLinks(int permission, String user) {
        // TODO improve filtering
        Entity[] entities;
        try {
            entities = storageService.getEntitiesByClassName(StorageConstants.DASHBOARDS_ROOT, DashboardState.class.getName());
        } catch (NotFoundException e) {
            // never happening
            throw new RuntimeException(e);
        }

        List<Entity> tmp = new ArrayList<Entity>();
        String dashboardsPath = StorageConstants.DASHBOARDS_ROOT + "/" + user;
        for (Entity entity : entities) {
            if (!entity.getPath().startsWith(dashboardsPath)) {
                tmp.add(entity);
            }
        }
        entities = new Entity[tmp.size()];
        entities = tmp.toArray(entities);

        List<Link> links = new ArrayList<Link>();
        for (Entity entity : entities) {
            try {
                boolean hasRead = securityService.hasPermissionsById(user, permission, entity.getId());
                if (hasRead && !MY_DASHBOARD_NAME.equals(entity.getName())) {
                	Link link = new Link(entity.getName(), entity.getPath());                	
                	link.setReference(entity.getId());
                    links.add(link);
                }
            } catch (NotFoundException e) {
            	// never happening
            	throw new RuntimeException(e);
            }
        }
        Collections.sort(links, new Comparator<Link>() {
			@Override
			public int compare(Link o1, Link o2) {
				return Collator.getInstance().compare(o1.getName(), o2.getName());
			}        	
        });
        
        return links;
    }

    @Transactional
    public String addDashboard(Dashboard dashboard) {
        try {
			return storageService.addEntity(createState(dashboard));
		} catch (DuplicationException e) {
			// never happening
        	throw new RuntimeException(e);
		}
    }
    
    @Transactional
    public void modifyDashboard(Dashboard dashboard) {
        try {
        	DashboardState state = (DashboardState)storageService.getEntityById(dashboard.getId());
        	state.setName(dashboard.getTitle());
        	state.setColumnCount(dashboard.getColumnCount());
        	state.setPath(getDashboardPath(dashboard));
        	// exclude widgetStates children (otherwise their id is modified and existing iframes cannot work)
			storageService.modifyEntity(state, "widgetStates");
		} catch (NotFoundException e) {
        	throw new RuntimeException(e);
		}
    }

    @Transactional
    public void removeDashboard(String id) throws NotFoundException {        
        // must delete user widget data
        for (Widget widget : getDashboard(id).getWidgets()) {
        	storageService.clearUserWidgetData(widget.getId());
        }
        storageService.removeEntityById(id);
    }

    @Transactional(readOnly = true)
    public Dashboard getDashboard(String id) throws NotFoundException {
        DashboardState state = (DashboardState) storageService.getEntityById(id);
        return load(state);
    }
        
    private Dashboard getDashboardByPath(String path) throws NotFoundException {
        DashboardState state = (DashboardState) storageService.getEntity(path);
        return load(state);
    }

    @Transactional(readOnly = true)
    public String getDashboardOwner(String dashboardId) throws NotFoundException {
        String path = storageService.getEntityPath(dashboardId);
        String parentPath = StorageUtil.getParentPath(path);
        String username = StorageUtil.getName(parentPath);

        return username;
    }

    @Transactional(readOnly = true)
    public Widget getWidgetById(String id) throws NotFoundException {    	
        WidgetState state = (WidgetState) storageService.getEntityById(id);
        return loadWidget(state);
    }

    @Transactional(readOnly = true)
    public DashboardColumn getDashboardColumn(String dashboardId, int column) throws NotFoundException {
        Dashboard dashboard = getDashboard(dashboardId);
        return new DashboardColumn(column, dashboard.getWidgets(column));
    }

    @Transactional
    public String addWidget(String dashboardId, Widget widget) throws NotFoundException {
        return addWidget(dashboardId, widget, null);
    }

    @Transactional
    public String addWidget(String dashboardId, Widget widget, WidgetLocation location) throws NotFoundException {
    	Dashboard dashboard = getDashboardById(dashboardId);
        try {
            return addWidget(dashboard, widget, location);
        } catch (DuplicationException e) {
            // TODO
            e.printStackTrace();
            return null;
        }
    }       


    @Profile
    @Transactional
    public void modifyWidget(String dashboardId, Widget widget) throws NotFoundException {
    	String dashboardPath = storageService.getEntityPath(dashboardId);
    	WidgetState state = createWidgetState(dashboardPath, widget);
        storageService.modifyEntity(state);       
    }

    @Transactional
    public void updateWidgetLocations(String dashboardId, Map<String, WidgetLocation> widgetLocations) throws NotFoundException {
    	Dashboard dashboard = getDashboard(dashboardId);
        List<Widget> widgets = dashboard.getWidgets();
        for (Widget widget : widgets) {
            String id = widget.getId();
            WidgetLocation location = widgetLocations.get(id);
            if ((widget.getRow() != location.getRow()) || (widget.getColumn() != location.getColumn())) {
                widget.setColumn(location.getColumn());
                widget.setRow(location.getRow());
                modifyWidget(dashboardId, widget);
            }                         
            resetDrillDownableCache(widget.getInternalSettings().get(ChartWidget.ENTITY_ID));
        }
    }

    @Transactional
    public void removeWidget(String dashboardId, String id) throws NotFoundException {
        removeWidgetById(dashboardId, id);        
    }
    
    private void removeWidgetById(String dashboardId, String id) throws NotFoundException {
        Dashboard dashboard = getDashboardById(dashboardId);
        try {
            removeWidget(dashboard, id);
        } catch (ReferenceException e) {
            // TODO
            e.printStackTrace();
        }
    }
    
    @Transactional
    public void copyWidget(String fromDashboardId, String toDashboardId, String widgetId) throws NotFoundException {    	
    	Widget widget = getWidgetById(widgetId);
    	Dashboard dashboard = getDashboardById(toDashboardId);     	
    	widget.setTitle(getUniqueWidgetTitle(dashboard, widget.getTitle(), 0));    	       
        try {
			addWidget(dashboard, widget, null);			    
		} catch (DuplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.error(e.getMessage(),e);
		}            		
    }
    
    @Transactional
    public void moveWidget(String fromDashboardId, String toDashboardId, String widgetId) throws NotFoundException {
    	// try to add it first: if successful we can remove it
    	Widget widget = getWidgetById(widgetId);
    	Dashboard dashboard = getDashboardById(toDashboardId);     	
    	widget.setTitle(getUniqueWidgetTitle(dashboard, widget.getTitle(), 0));    	       
        try {
			addWidget(dashboard, widget, null);
			removeWidgetById(fromDashboardId, widgetId);    
		} catch (DuplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.error(e.getMessage(),e);
		}            		
    }
    
    private String getUniqueWidgetTitle(Dashboard dashboard, String title, int count) {
		String uniqueTitle = title;
		if (count > 0) {
			uniqueTitle = title + " " + count;
		}
		
		int columnCount = dashboard.getColumnCount();
		for (int i = 0; i < columnCount; i++) {
			DashboardColumn dashboardColumn = new DashboardColumn(i);
			dashboardColumn.setWidgets(dashboard.getWidgets(i));
			if (dashboardColumn.widgetExists(uniqueTitle)) {
				uniqueTitle = getUniqueWidgetTitle(dashboard, title, count + 1);
			}
		}				 
		return uniqueTitle;
	}
    
    public TableData getTableData(String widgetId,  Map<String, Object> urlQueryParameters) throws ReportRunnerException, NoDataFoundException, TimeoutException {
    	return getTableData(widgetId, null, urlQueryParameters);
    		
    }

    public TableData getTableData(String widgetId, DrillEntityContext drillContext,  Map<String, Object> urlQueryParameters) throws ReportRunnerException, NoDataFoundException, TimeoutException {
        Entity entity = null;
        Widget widget = null;
        try {
            widget = getWidgetById(widgetId);
            if (widget instanceof TableWidget) {                
                entity  = ((TableWidget) widget).getEntity();
            } else if (widget instanceof ChartWidget) {
                entity = ((ChartWidget) widget).getEntity();
            } else if (widget instanceof DrillDownWidget) {
            	entity = ((DrillDownWidget) widget).getEntity();
            }
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }

        if (entity == null) {
            throw new ReportRunnerException("Widget class unknown.");
        }
        
        Map<String, Object> parameterValues = new HashMap<String, Object>();        
		ChartUtil.initParameterSettings(parameterValues, widget.getQueryRuntime(), getUserWidgetParameters(widgetId));
		
		// drill parameters
        if (drillContext != null) {
            // put first settings values of the parent root report (some of then may be overridden by drill parameters)
            // there is a name parameter convention between current drilldown report and parent root report (the default
            // values of the current drill down report are overidden by the settings values of the parent root report)
            if (drillContext.getDrillParameterValues().size() > 0) {
                Map<String, Object> settingsParams = drillContext.getSettingsValues();
                for (String key : settingsParams.keySet()) {
                	parameterValues.put(key, settingsParams.get(key));
                }
            }

            Map<String , Object> drillParams = drillContext.getDrillParameterValues();
            for  (String key : drillParams.keySet()) {
//                if (!parameterValues.containsKey(key)) {
//                    System.err.println("Parameter " + key + " not found!");
//                } else {
//                    System.out.println("Parameter " + key + " updated with value " + drillParams.get(key));
//                }
                parameterValues.put(key, drillParams.get(key));
            }
            
            // if a drill down is present and a table follows we have to load that entity
			try {				
				entity = storageService.getEntityById(drillContext.getDrillLink());
			} catch (NotFoundException ex) {
				LOG.error(ex.getMessage(), ex);
			}
            
        }
        
        // parameters from embedded code
        try {
			ReportUtil.addUrlQueryParameters(storageService.getSettings(), entity, parameterValues, urlQueryParameters);
		} catch (Exception e1) {			
			e1.printStackTrace();
			LOG.error(e1.getMessage(), e1);
		}
		        
        // used by 'save as excel' action
        if (entity instanceof Chart) {
            Chart chart = (Chart) entity;
                        
            Cache cache = null;
            QueryCacheKey cacheKey = null;
            boolean cacheable = chart.getExpirationTime() > 0;
            if (cacheable) {
    	        cache = cacheFactory.getCache(chart.getId(), chart.getExpirationTime());
    	        cacheKey = new QueryCacheKey(parameterValues);
    	        boolean hitCache = cache.hasElement(cacheKey);
    	        if (hitCache) {
    	        	TableData tableData = (TableData) cache.get(cacheKey);
    	        	if (LOG.isDebugEnabled()) {
    	        		LOG.debug("Get tableData for '" + StorageUtil.getPathWithoutRoot(chart.getPath()) + "' from cache");
    	        	}
    	        	
    	        	return tableData;
    	        }
            }
             
            final ChartRunner chartRunner = new ChartRunner();
            chartRunner.setParameterValues(parameterValues);
            ro.nextreports.engine.chart.Chart nextChart = NextUtil.getChart(chart.getContent());
            chartRunner.setChart(nextChart); // nextChart
            chartRunner.setFormat(ChartRunner.TABLE_FORMAT);
            I18nLanguage language = I18nUtil.getLocaleLanguage(nextChart);
    		if (language != null) {
    			chartRunner.setLanguage(language.getName());
    		}
    		Connection connection;
            try {
                connection = ConnectionUtil.createConnection(storageService, chart.getDataSource());
            } catch (RepositoryException e) {
                throw new ReportRunnerException("Cannot connect to database", e);
            }
            boolean csv = chart.getDataSource().getDriver().equals(CSVDialect.DRIVER_CLASS);
            chartRunner.setConnection(connection, csv);
            
            int timeout = WidgetUtil.getTimeout(this, widget);
            chartRunner.setQueryTimeout(timeout);
            
            FutureTask<TableData> runTask = null;
	        try {
	        	runTask = new FutureTask<TableData>(new Callable<TableData>() {	        		
	        		public TableData call() throws Exception {        
	        			chartRunner.run();	        			
	        			return chartRunner.getTableData();	        						        			
	        		}	        		
	        	});
	        	new Thread(runTask).start();
	        	TableData tableData = runTask.get(timeout, TimeUnit.SECONDS);
	        	if (cacheable) {
    	        	if (LOG.isDebugEnabled()) {
    	        		LOG.debug("Put tableData for '" + StorageUtil.getPathWithoutRoot(chart.getPath()) + "' in cache");
    	        	}    	        	      		
    	        	cache.put(cacheKey, tableData);    	        		
                }
	        	return tableData;
			} catch (Exception e) {		
				// ehcache uses BlockingCache which waits someone to put a value in cache if gets a null value
				// so this put must be done to unblock the cache if a timeout or ane exception occurs)!!
				cache.put(cacheKey, null);	
				if (e instanceof TimeoutException) {					
					throw new TimeoutException("Timeout of " + timeout + " seconds ellapsed.");
				} else {
					throw new ReportRunnerException(e);
				}
			} finally {
    			ConnectionUtil.closeConnection(connection);
    		}

        } else {
            Report report = (Report) entity;                                 
            Cache cache = null;
            QueryCacheKey cacheKey = null;
            boolean cacheable = report.getExpirationTime() > 0;
            if (cacheable) {
    	        cache = cacheFactory.getCache(report.getId(), report.getExpirationTime());
    	        cacheKey = new QueryCacheKey(parameterValues);
    	        boolean hitCache = cache.hasElement(cacheKey);
    	        if (hitCache) {
    	        	TableData tableData = (TableData) cache.get(cacheKey);
    	        	if (LOG.isDebugEnabled()) {
    	        		LOG.debug("Get tableData for '" + StorageUtil.getPathWithoutRoot(report.getPath()) + "' from cache");
    	        	}
    	        	
    	        	return tableData;
    	        }
            }
           
            ro.nextreports.engine.Report nextReport = NextUtil.getNextReport(storageService.getSettings(), (NextContent) report.getContent());
            final ReportRunner reportRunner = new ReportRunner();
            reportRunner.setParameterValues(parameterValues);
            reportRunner.setReport(nextReport); 
            reportRunner.setFormat(ReportRunner.TABLE_FORMAT);
            if (TableWidget.ALLOW_COLUMNS_SORTING) {
            	reportRunner.setTableRawData(true); // formatted data is shown inside TableRendererPanel in any PropertyColumn!
            }
            I18nLanguage language = I18nUtil.getLocaleLanguage(nextReport.getLayout());
    		if (language != null) {
    			reportRunner.setLanguage(language.getName());
    		}
    		Connection connection;
            try {
                connection = ConnectionUtil.createConnection(storageService, report.getDataSource());
            } catch (RepositoryException e) {
                throw new ReportRunnerException("Cannot connect to database", e);
            }
            boolean csv = report.getDataSource().getDriver().equals(CSVDialect.DRIVER_CLASS);
            reportRunner.setConnection(connection, csv);

            int timeout = WidgetUtil.getTimeout(this, widget);
            reportRunner.setQueryTimeout(timeout);
                        
            FutureTask<TableData> runTask = null;
	        try {
	        	runTask = new FutureTask<TableData>(new Callable<TableData>() {	        		
	        		public TableData call() throws Exception {
	        				reportRunner.run();
	        			return reportRunner.getTableData();	        				        			        			
	        		}	        		
	        	});
	        	new Thread(runTask).start();
	        	TableData tableData = runTask.get(timeout, TimeUnit.SECONDS);
	        	if (cacheable) {
    	        	if (LOG.isDebugEnabled()) {
    	        		LOG.debug("Put tableData for '" + StorageUtil.getPathWithoutRoot(report.getPath()) + "' in cache");
    	        	}    	        	        		
    	        	cache.put(cacheKey, tableData);    	        		
                }
	        	return tableData;
			} catch (Exception e) {		
				// ehcache uses BlockingCache which waits someone to put a value in cache if gets a null value
				// so this put must be done to unblock the cache if a timeout or an exception occurs)!!
				if (cacheable) {
					cache.put(cacheKey, null);
				}
				if (e instanceof TimeoutException) {					
					throw new TimeoutException("Timeout of " + timeout + " seconds ellapsed.");
				} else if (e.getMessage().contains("NoDataFoundException")) {
					throw new NoDataFoundException();
				} else {
					throw new ReportRunnerException(e);
				}
			} finally {
    			ConnectionUtil.closeConnection(connection);
    		}		
            
        }
    }
    
	public AlarmData getAlarmData(String widgetId, Map<String, Object> urlQueryParameters) throws ReportRunnerException, NoDataFoundException, TimeoutException {
		Entity entity = null;
		Widget widget = null;
		try {
			widget = getWidgetById(widgetId);
			if (widget instanceof AlarmWidget) {
				entity = ((AlarmWidget) widget).getEntity();
			}
		} catch (NotFoundException e) {
			throw new RuntimeException(e);
		}

		if (entity == null) {
			throw new ReportRunnerException("Widget class unknown for alarm.");
		}

		Report report = (Report) entity;
		
        Map<String, Object> parameterValues = new HashMap<String, Object>();        
		ChartUtil.initParameterSettings(parameterValues, widget.getQueryRuntime(), getUserWidgetParameters(widgetId));
		
        // parameters from embedded code
		try {
			ReportUtil.addUrlQueryParameters(storageService.getSettings(), entity, parameterValues, urlQueryParameters);
		} catch (Exception e1) {
			e1.printStackTrace();
			LOG.error(e1.getMessage(), e1);
		}
		
        Cache cache = null;
        QueryCacheKey cacheKey = null;
        boolean cacheable = report.getExpirationTime() > 0;
        if (cacheable) {
	        cache = cacheFactory.getCache(report.getId(), report.getExpirationTime());
	        cacheKey = new QueryCacheKey(parameterValues);
	        boolean hitCache = cache.hasElement(cacheKey);
	        if (hitCache) {
	        	AlarmData alarmData = (AlarmData) cache.get(cacheKey);
	        	if (LOG.isDebugEnabled()) {
	        		LOG.debug("Get alarmData for '" + StorageUtil.getPathWithoutRoot(report.getPath()) + "' from cache");
	        	}
	        	
	        	return alarmData;
	        }
        }
              
		ro.nextreports.engine.Report nextReport = NextUtil.getNextReport(storageService.getSettings(), (NextContent) report.getContent());
		final ReportRunner reportRunner = new ReportRunner();
		reportRunner.setParameterValues(parameterValues);
		reportRunner.setReport(nextReport);
		reportRunner.setFormat(ReportRunner.ALARM_FORMAT);
		I18nLanguage language = I18nUtil.getLocaleLanguage(nextReport.getLayout());
		if (language != null) {
			reportRunner.setLanguage(language.getName());
		}
		Connection connection;
		try {
			connection = ConnectionUtil.createConnection(storageService, report.getDataSource());
		} catch (RepositoryException e) {
			throw new ReportRunnerException("Cannot connect to database", e);
		}
		boolean csv = report.getDataSource().getDriver().equals(CSVDialect.DRIVER_CLASS);
		reportRunner.setConnection(connection, csv);

		int timeout = WidgetUtil.getTimeout(this, widget);
		reportRunner.setQueryTimeout(timeout);
		
		FutureTask<AlarmData> runTask = null;
        try {
        	runTask = new FutureTask<AlarmData>(new Callable<AlarmData>() {        		
        		public AlarmData call() throws Exception {     
        			try {
        				reportRunner.run();
        			} catch (NoDataFoundException ex) {
        				return new AlarmData(ColorUtil.getHexColor(Color.WHITE), "No Data");        				
        			}	
        			return reportRunner.getAlarmData();        			    			        			
        		}        		
        	});
        	new Thread(runTask).start();
        	AlarmData alarmData = runTask.get(timeout, TimeUnit.SECONDS);
        	if (cacheable) {
	        	if (LOG.isDebugEnabled()) {
	        		LOG.debug("Put alarmData for '" + StorageUtil.getPathWithoutRoot(report.getPath()) + "' in cache");
	        	}	        		        		
	        	cache.put(cacheKey, alarmData);	        	
            }
        	return alarmData;
		} catch (Exception e) {		
			// ehcache uses BlockingCache which waits someone to put a value in cache if gets a null value
			// so this put must be done to unblock the cache if a timeout or an exception occurs)!!
			if (cacheable) {
				cache.put(cacheKey, null);
			}
			if (e instanceof TimeoutException) {						        	
				throw new TimeoutException("Timeout of " + timeout + " seconds ellapsed.");
			} else {
				throw new ReportRunnerException(e);
			}
		} finally {
			ConnectionUtil.closeConnection(connection);
		}		

	}
	
	public IndicatorData getIndicatorData(String widgetId, Map<String, Object> urlQueryParameters) throws ReportRunnerException, NoDataFoundException, TimeoutException {
		Entity entity = null;
		Widget widget = null;
		try {
			widget = getWidgetById(widgetId);
			if (widget instanceof IndicatorWidget) {
				entity = ((IndicatorWidget) widget).getEntity();
			}
		} catch (NotFoundException e) {
			throw new RuntimeException(e);
		}

		if (entity == null) {
			throw new ReportRunnerException("Widget class unknown for indicator.");
		}

		Report report = (Report) entity;
		
        Map<String, Object> parameterValues = new HashMap<String, Object>();        
		ChartUtil.initParameterSettings(parameterValues, widget.getQueryRuntime(), getUserWidgetParameters(widgetId));
		
        // parameters from embedded code
		try {
			ReportUtil.addUrlQueryParameters(storageService.getSettings(), entity, parameterValues, urlQueryParameters);
		} catch (Exception e1) {
			e1.printStackTrace();
			LOG.error(e1.getMessage(), e1);
		}
		
        Cache cache = null;
        QueryCacheKey cacheKey = null;
        boolean cacheable = report.getExpirationTime() > 0;
        if (cacheable) {
	        cache = cacheFactory.getCache(report.getId(), report.getExpirationTime());
	        cacheKey = new QueryCacheKey(parameterValues);
	        boolean hitCache = cache.hasElement(cacheKey);
	        if (hitCache) {
	        	IndicatorData indicatorData = (IndicatorData) cache.get(cacheKey);
	        	if (LOG.isDebugEnabled()) {
	        		LOG.debug("Get indicatorData for '" + StorageUtil.getPathWithoutRoot(report.getPath()) + "' from cache");
	        	}	        	
	        	return indicatorData;
	        }
        }
              
		ro.nextreports.engine.Report nextReport = NextUtil.getNextReport(storageService.getSettings(), (NextContent) report.getContent());
		final ReportRunner reportRunner = new ReportRunner();
		reportRunner.setParameterValues(parameterValues);
		reportRunner.setReport(nextReport);
		reportRunner.setFormat(ReportRunner.INDICATOR_FORMAT);
		I18nLanguage language = I18nUtil.getLocaleLanguage(nextReport.getLayout());
		if (language != null) {
			reportRunner.setLanguage(language.getName());
		}
		Connection connection;
		try {
			connection = ConnectionUtil.createConnection(storageService, report.getDataSource());
		} catch (RepositoryException e) {
			throw new ReportRunnerException("Cannot connect to database", e);
		}
		boolean csv = report.getDataSource().getDriver().equals(CSVDialect.DRIVER_CLASS);
		reportRunner.setConnection(connection, csv);

		int timeout = WidgetUtil.getTimeout(this, widget);
		reportRunner.setQueryTimeout(timeout);
		
		FutureTask<IndicatorData> runTask = null;
        try {
        	runTask = new FutureTask<IndicatorData>(new Callable<IndicatorData>() {        		
        		public IndicatorData call() throws Exception {        
        			try {
        				reportRunner.run();
        			} catch (NoDataFoundException ex) {
        				IndicatorData data = new IndicatorData();
        				data.setTitle("No Data");
        				return data;
        			}
        			return reportRunner.getIndicatorData();       			    			        			
        		}        		
        	});
        	new Thread(runTask).start();
        	IndicatorData indicatorData = runTask.get(timeout, TimeUnit.SECONDS);
        	if (cacheable) {
	        	if (LOG.isDebugEnabled()) {
	        		LOG.debug("Put indicatorData for '" + StorageUtil.getPathWithoutRoot(report.getPath()) + "' in cache");
	        	}	        		        		
	        	cache.put(cacheKey, indicatorData);	        	
            }
        	return indicatorData;
		} catch (Exception e) {		
			e.printStackTrace();
			// ehcache uses BlockingCache which waits someone to put a value in cache if gets a null value
			// so this put must be done to unblock the cache if a timeout or an exception occurs)!!
			if (cacheable) {
				cache.put(cacheKey, null);
			}
			if (e instanceof TimeoutException) {						        	
				throw new TimeoutException("Timeout of " + timeout + " seconds ellapsed.");
			} else {
				throw new ReportRunnerException(e);
			}
		} finally {
			ConnectionUtil.closeConnection(connection);
		}		

	}
	
	public DisplayData getDisplayData(String widgetId, Map<String, Object> urlQueryParameters) throws ReportRunnerException, NoDataFoundException, TimeoutException {
		Entity entity = null;
		Widget widget = null;
		try {
			widget = getWidgetById(widgetId);
			if (widget instanceof DisplayWidget) {
				entity = ((DisplayWidget) widget).getEntity();
			}
		} catch (NotFoundException e) {
			throw new RuntimeException(e);
		}

		if (entity == null) {
			throw new ReportRunnerException("Widget class unknown for display.");
		}

		Report report = (Report) entity;
		
        Map<String, Object> parameterValues = new HashMap<String, Object>();        
		ChartUtil.initParameterSettings(parameterValues, widget.getQueryRuntime(), getUserWidgetParameters(widgetId));
		
        // parameters from embedded code
		try {
			ReportUtil.addUrlQueryParameters(storageService.getSettings(), entity, parameterValues, urlQueryParameters);
		} catch (Exception e1) {
			e1.printStackTrace();
			LOG.error(e1.getMessage(), e1);
		}
		
        Cache cache = null;
        QueryCacheKey cacheKey = null;
        boolean cacheable = report.getExpirationTime() > 0;
        if (cacheable) {
	        cache = cacheFactory.getCache(report.getId(), report.getExpirationTime());
	        cacheKey = new QueryCacheKey(parameterValues);
	        boolean hitCache = cache.hasElement(cacheKey);
	        if (hitCache) {
	        	DisplayData displayData = (DisplayData) cache.get(cacheKey);
	        	if (LOG.isDebugEnabled()) {
	        		LOG.debug("Get displayData for '" + StorageUtil.getPathWithoutRoot(report.getPath()) + "' from cache");
	        	}	        	
	        	return displayData;
	        }
        }
              
		ro.nextreports.engine.Report nextReport = NextUtil.getNextReport(storageService.getSettings(), (NextContent) report.getContent());
		final ReportRunner reportRunner = new ReportRunner();
		reportRunner.setParameterValues(parameterValues);
		reportRunner.setReport(nextReport);
		reportRunner.setFormat(ReportRunner.DISPLAY_FORMAT);
		I18nLanguage language = I18nUtil.getLocaleLanguage(nextReport.getLayout());
		if (language != null) {
			reportRunner.setLanguage(language.getName());
		}
		Connection connection;
		try {
			connection = ConnectionUtil.createConnection(storageService, report.getDataSource());
		} catch (RepositoryException e) {
			throw new ReportRunnerException("Cannot connect to database", e);
		}
		boolean csv = report.getDataSource().getDriver().equals(CSVDialect.DRIVER_CLASS);
		reportRunner.setConnection(connection, csv);

		int timeout = WidgetUtil.getTimeout(this, widget);
		reportRunner.setQueryTimeout(timeout);
		
		FutureTask<DisplayData> runTask = null;
        try {
        	runTask = new FutureTask<DisplayData>(new Callable<DisplayData>() {        		
        		public DisplayData call() throws Exception {   
        			try {
        				reportRunner.run();
        			} catch (NoDataFoundException ex) {
        				DisplayData data = new DisplayData();
        				data.setTitle("No Data");
        				return data;
        			}
        			return reportRunner.getDisplayData();       			    			        			
        		}        		
        	});
        	new Thread(runTask).start();
        	DisplayData displayData = runTask.get(timeout, TimeUnit.SECONDS);
        	if (cacheable) {
	        	if (LOG.isDebugEnabled()) {
	        		LOG.debug("Put displayData for '" + StorageUtil.getPathWithoutRoot(report.getPath()) + "' in cache");
	        	}	        		        		
	        	cache.put(cacheKey, displayData);	        	
            }
        	return displayData;
		} catch (Exception e) {		
			e.printStackTrace();
			// ehcache uses BlockingCache which waits someone to put a value in cache if gets a null value
			// so this put must be done to unblock the cache if a timeout or an exception occurs)!!
			if (cacheable) {
				cache.put(cacheKey, null);
			}
			if (e instanceof TimeoutException) {						        	
				throw new TimeoutException("Timeout of " + timeout + " seconds ellapsed.");
			} else if (e instanceof NoDataFoundException) {		
				throw (NoDataFoundException)e;
			} else {
				throw new ReportRunnerException(e);
			}
		} finally {
			ConnectionUtil.closeConnection(connection);
		}		

	}

    public void resetCache(String entityId) {
    	cacheFactory.resetCache(entityId);
    }
    
    public void resetCache(List<String> entityIds) {
    	for (String entityId : entityIds) {
    		cacheFactory.resetCache(entityId);
    	}
    }

    public DashboardState getDashboardState(WidgetState widgetState) {
   	 Entity[] entities;
        try {
        	// for embedded widgets in external html's we do not have any authorization object in session
            entities = storageService.getEntitiesByClassNameWithoutSecurity(StorageConstants.DASHBOARDS_ROOT, DashboardState.class.getName());
            for (Entity entity : entities) {            	 
           	 DashboardState ds = (DashboardState)entity;           	 
           	 if (widgetState.getPath().contains(ds.getPath())) {
           		 // we may have dashboards with same name + suffix (ex: Test, Test1)
           		 // we have to test that following character in widgetState path is /
           		 String followingPath = widgetState.getPath().substring(ds.getPath().length());           		 
           		 if (followingPath.startsWith("/")) {
           			 return ds;
           		 }
           	 }
            }
            return null;
        } catch (NotFoundException e) {
            // never happening
            throw new RuntimeException(e);
        }
   }		

	public int getWidgetColumn(String widgetId) {
        try {
			WidgetState state = (WidgetState) storageService.getEntityById(widgetId);
			return state.getColumn();
		} catch (NotFoundException e) {
			return -1;
		}
	}
	
	// just a single widget in a dashboard with a single column
	public boolean isSingleWidget(String widgetId) {
		try {
			WidgetState state = (WidgetState) storageService.getEntityById(widgetId);
			DashboardState dState = getDashboardState(state);
			if (dState.getColumnCount() > 1) {
				return false;
			}
			if (dState.getWidgetStates().size() > 1) {
				return false;
			}
			return true;
		} catch (NotFoundException e) {
			return false;
		}
	}
	
	public void removeUserDashboards(String userName) {
		String dashboardsPath = StorageConstants.DASHBOARDS_ROOT + "/" + userName;
		try {
			storageService.removeEntity(dashboardsPath);
		} catch (ReferenceException e) {
			LOG.error(e.getMessage(), e);
		} 
	}
	
	private String getUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private String getMyDashboardsPath() {
        return StorageConstants.DASHBOARDS_ROOT + "/" + getUsername();
    }

    private List<Dashboard> getDashboards(Entity[] entities) {
        int n = entities.length;
        DashboardState[] states = new DashboardState[n];
        System.arraycopy(entities, 0, states, 0, n);

        List<Dashboard> dashboards = new ArrayList<Dashboard>();
        for (DashboardState state : states) {
            Dashboard dashboard = load(state);
            dashboards.add(dashboard);
        }
        Collections.sort(dashboards, new Comparator<Dashboard>() {

            public int compare(Dashboard o1, Dashboard o2) {
                if (MY_DASHBOARD_NAME.equals(o1.getTitle())) {
                    return -1;
                } else if (MY_DASHBOARD_NAME.equals(o2.getTitle())) {
                    return 1;
                } else {
                    return Collator.getInstance().compare(o1.getTitle(), o2.getTitle());
                }
            }
        });

        return dashboards;
    }

    private Dashboard load(DashboardState state) {
        Dashboard dashboard = new DefaultDashboard(state.getId(), state.getName(), state.getColumnCount());
        List<Widget> widgets = loadWidgets(state.getWidgetStates());
        for (Widget widget : widgets) {
            dashboard.addWidget(widget);
        }

        return dashboard;
    }

    private List<Widget> loadWidgets(List<WidgetState> states) {
        List<Widget> widgets = new ArrayList<Widget>();
        for (WidgetState state : states) {
            Widget widget = loadWidget(state);
            widgets.add(widget);
        }

        return widgets;
    }

    private Widget loadWidget(WidgetState state) {    	
        WidgetDescriptor widgetDescriptor = widgetRegistry.getWidgetDescriptor(state.getWidgetClassName());
        Widget widget = widgetFactory.createWidget(widgetDescriptor);
        widget.setId(state.getId());
        widget.setTitle(state.getName());
        widget.setSettings(state.getSettings());
        widget.setInternalSettings(state.getInternalSettings());
        widget.setQueryRuntime(state.getQueryRuntime());
        widget.setColumn(state.getColumn());
        widget.setRow(state.getRow());
        widget.init(); // collapsed is a property in internalSettings and I must call init after setInternalSettings        
        
        if (widget instanceof EntityWidget) {	
            String entityId = widget.getInternalSettings().get(EntityWidget.ENTITY_ID);
            try {
                ((EntityWidget) widget).setEntity(storageService.getEntityById(entityId));
            } catch (NotFoundException e) {                
                e.printStackTrace();
            }        	
        }

        return widget;
    }

    private DashboardState createState(Dashboard dashboard) {
        String name = dashboard.getTitle();
        String path = getDashboardPath(dashboard);
        DashboardState state = new DashboardState(name, path);
        state.setId(dashboard.getId());
        state.setColumnCount(dashboard.getColumnCount());
        List<WidgetState> widgetStates = new ArrayList<WidgetState>();
        List<Widget> widgets = dashboard.getWidgets();
        for (Widget widget : widgets) {
            WidgetState columnState = createWidgetState(path, widget);
            widgetStates.add(columnState);
        }
        state.setWidgetStates(widgetStates);

        return state;
    }

    private WidgetState createWidgetState(String dashboardPath, Widget widget) {
        String name = widget.getTitle();
        String path = getWidgetPath(dashboardPath, name);
        WidgetState widgetState = new WidgetState(name, path);

        widgetState.setId(widget.getId());
        widgetState.setWidgetClassName(widget.getClass().getName());
        widgetState.setSettings(widget.getSettings());
        widgetState.setInternalSettings(widget.getInternalSettings());
        widgetState.setQueryRuntime(widget.getQueryRuntime());
        widgetState.setColumn(widget.getColumn());
        widgetState.setRow(widget.getRow());

        return widgetState;
    }

    private Dashboard getDashboardById(String id) throws NotFoundException {
        DashboardState state = (DashboardState) storageService.getEntityById(id);
        return load(state);
    }

    private String addWidget(Dashboard dashboard, Widget widget, WidgetLocation location) throws DuplicationException {
        String dashboardPath = getDashboardPath(dashboard);
        int row = (location == null) ? 0 : location.getRow();
        int column = (location == null) ? 0 : location.getColumn();

        // update row indexes
        List<Widget> widgets = dashboard.getWidgets(column);
        for (Widget w : widgets) {
            if (w.getRow() >= row) {
                w.setRow(w.getRow() + 1);
                // update widget state in storage
                WidgetState state = createWidgetState(dashboardPath, w);
                storageService.modifyEntity(state);                       
                resetDrillDownableCache(w.getInternalSettings().get(ChartWidget.ENTITY_ID));
            }
        }

        // add widget to storage
        widget.setRow(row);
        widget.setColumn(column);
        WidgetState state = createWidgetState(dashboardPath, widget);
        return storageService.addEntity(state);
    }

    private void removeWidget(Dashboard dashboard, String id) throws NotFoundException, ReferenceException {
        String dashboardPath = getDashboardPath(dashboard);
        Widget widget = getWidgetById(id);
        WidgetLocation location = DashboardUtil.getWidgetLocation(dashboard, widget);
        int row = location.getRow();
        int column = location.getColumn();

        // update row indexes
        List<Widget> widgets = dashboard.getWidgets(column);
        for (Widget w : widgets) {
            if (w.getRow() > row) {
                w.setRow(w.getRow() - 1);
                // update widget state in storage
                WidgetState state = createWidgetState(dashboardPath, w);
                storageService.modifyEntity(state);                                  
                resetDrillDownableCache(w.getInternalSettings().get(ChartWidget.ENTITY_ID));
            }
        }

        // remove widget from storage
        storageService.removeEntityById(id);      
        
        // if user widget data is saved for this widget, we have to delete it
        storageService.clearUserWidgetData(id);
    }       

    private String getDashboardPath(Dashboard dashboard) {
        String id = dashboard.getId();
        if (id == null) {
            return getMyDashboardsPath() + "/" + dashboard.getTitle();
        }

        try {
			return storageService.getEntityPath(id);
		} catch (NotFoundException e) {
			// never happening
			throw new RuntimeException(e);
		}
    }

    private String getWidgetPath(String dashboardPath, String title) {
        return dashboardPath + "/widgetStates/" + title;
    }
        
    // drill downable chart widgets contains also the on-click ajax script with position
    // we must reset cache otherwise drill down will not work    
    // for drill down reports there is no need to reset cache when we move the widget
    private void resetDrillDownableCache(String entityId) {
    	try {
    		// has no chart id in settings
    		if (entityId == null) {
    			return;
    		}
    		Entity entity = storageService.getEntityById(entityId);
    		if (entity instanceof Chart) {
    			if (((Chart)entity).isDrillDownable()) {
    				resetCache(entityId);
    			}
    		}
    	} catch  (NotFoundException ex) {
    		LOG.error("Reset cache - not found", ex);
    	}
    }
    
    @Transactional
    public void setDefaultDashboard(String dashboardId) {
    	String path = getMyDashboardsPath();
    	storageService.setDefaultProperty(path, dashboardId);
    }
    
    @Transactional(readOnly = true)
    public String getDefaultDashboardId() throws NotFoundException {
    	String path = getMyDashboardsPath();
    	return storageService.getDefaultProperty(path);
    }      
    
    @Transactional(readOnly = true)
    public UserWidgetParameters getUserWidgetParameters(String widgetId) {  
    	if (widgetId == null) {
    		return null;
    	}    	
    	UserWidgetParameters wp = null;
    	try {
    		String dashboardId = storageService.getDashboardId(widgetId);    		
    		String user = ServerUtil.getUsername();    	
    		if (ServerUtil.UNKNOWN_USER.equals(user)) {
    			// if we do not have user  inside session (like for an iframe without authentication)    			
    			return wp;
    		}
    		String owner = getDashboardOwner(dashboardId);			
			boolean isDashboardLink = !owner.equals(user);
    		String parentPath = WidgetUtil.getUserWidgetParametersPath(user); 
			String path = parentPath + "/" + widgetId;
			boolean hasWrite =  securityService.hasPermissionsById(user, PermissionUtil.getWrite(), dashboardId);			
			if (isDashboardLink && !hasWrite) {
				if (storageService.entityExists(path)) {								
					wp = (UserWidgetParameters)storageService.getEntity(path);					
				}
			}			
			return wp;
    	} catch (NotFoundException ex) {
    		throw new RuntimeException(ex);
    	}
    }       
    
}
