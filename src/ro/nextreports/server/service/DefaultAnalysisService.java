package ro.nextreports.server.service;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.apache.wicket.model.StringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.Analysis;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Folder;
import ro.nextreports.server.domain.Link;
import ro.nextreports.server.domain.ReportResultEvent;
import ro.nextreports.server.etl.OrientDbUtils;
import ro.nextreports.server.exception.DuplicationException;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.util.AnalysisUtil;
import ro.nextreports.server.util.PermissionUtil;
import ro.nextreports.server.util.ServerUtil;
import ro.nextreports.server.web.security.SecurityUtil;

public class DefaultAnalysisService implements AnalysisService {
	
	private static final Logger LOG = LoggerFactory.getLogger(DefaultAnalysisService.class); 
	
	private StorageService storageService;
	private SecurityService securityService;
	private ReportService reportService;
	
	@Required
    public void setStorageService(StorageService storageService) {
        this.storageService = storageService;
    }
	
	@Required
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }
	
	@Required
    public void setReportService(ReportService reportService) {
        this.reportService = reportService;
    }
	
	private String getUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
	
	private String getMyAnalysisPath() {
        return getMyAnalysisPath(getUsername());
    }
	
	private String getMyAnalysisPath(String creator) {
        return StorageConstants.ANALYSIS_ROOT + "/" + creator;
    }
	
	@Transactional(readOnly=true)
	public String getAnalysisPath(Analysis analysis, String creator) {
        String id = analysis.getId();
        if (id == null) {
            return getMyAnalysisPath(creator) + "/" + analysis.getName();
        }

        try {
			return storageService.getEntityPath(id);
		} catch (NotFoundException e) {
			// never happening
			throw new RuntimeException(e);
		}
    }
	
	@Transactional
    public List<Analysis> getMyAnalysis() {        		
        Entity[] entities;
        try {
        	checkAnalysisPath();
            entities = storageService.getEntitiesByClassName(getMyAnalysisPath(), Analysis.class.getName());
            System.out.println("***** myAnalysis path = " +getMyAnalysisPath());
            return getAnalysis(entities);
        } catch (NotFoundException e) {
            // never happening
            throw new RuntimeException(e);
        }              
    }		
	
	@Transactional
	public List<Analysis> getAnalysisByTable(String tableName) {
		Entity[] entities;
        try {
        	checkAnalysisPath();
            entities = storageService.getEntitiesByClassName(StorageConstants.ANALYSIS_ROOT, Analysis.class.getName());            
            return getAnalysis(entities, tableName);
        } catch (NotFoundException e) {
            // never happening
            throw new RuntimeException(e);
        }       		
	}
	
	public List<Link> getAnalysisLinks() {
		return getAnalysisLinks(PermissionUtil.getRead(), ServerUtil.getUsername());
	}
	
	public List<Link> getAnalysisLinks(String user) {
		return getAnalysisLinks(PermissionUtil.getRead(), user);
	}
    
    public List<Link> getWritableAnalysisLinks() {
    	return getAnalysisLinks(PermissionUtil.getWrite(), ServerUtil.getUsername());
    }
    
    private List<Link> getAnalysisLinks(int permission, String user) {
        // TODO improve filtering
        Entity[] entities;
        try {
            entities = storageService.getEntitiesByClassName(StorageConstants.ANALYSIS_ROOT, Analysis.class.getName());
        } catch (NotFoundException e) {
            // never happening
            throw new RuntimeException(e);
        }

        List<Entity> tmp = new ArrayList<Entity>();
        String analysisPath = StorageConstants.ANALYSIS_ROOT + "/" + user;
        for (Entity entity : entities) {
            if (!entity.getPath().startsWith(analysisPath)) {
                tmp.add(entity);
            }
        }
        entities = new Entity[tmp.size()];
        entities = tmp.toArray(entities);

        List<Link> links = new ArrayList<Link>();
        for (Entity entity : entities) {
            try {
                boolean hasRead = securityService.hasPermissionsById(user, permission, entity.getId());
                if (hasRead) {
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
	public String addAnalysis(Analysis analysis) {
		try {			
			return storageService.addEntity(analysis);
		} catch (DuplicationException e) {
			// never happening
			throw new RuntimeException(e);
		}
	}
	
	@Transactional
	public boolean checkAnalysisPath() {
		String analysisPath = getMyAnalysisPath();
        String username = getUsername();
        if (!storageService.entityExists(analysisPath)) {
            Folder analysisFolder = new Folder(username, analysisPath);
            try {
                storageService.addEntity(analysisFolder);
            } catch (DuplicationException e) {
                // never happening
                throw new RuntimeException(e);
            }
            LOG.info("Created 'analysis' repository for user '" + username + "'");
            return false;
		}
        return true;
	}
	
	private List<Analysis> getAnalysis(Entity[] entities) {       
        return getAnalysis(entities, null);
    }
	
	private List<Analysis> getAnalysis(Entity[] entities, String tableName) {       
        List<Analysis> analysis = new ArrayList<Analysis>();
        for (Entity entity : entities) {         
        	Analysis a = (Analysis)entity;
        	if (tableName != null) {
        		if (a.getTableName().equals(tableName)) {
        			analysis.add(a);
        		}
        	} else {
        		analysis.add(a);
        	}
        }
        Collections.sort(analysis, new Comparator<Analysis>() {

            public int compare(Analysis o1, Analysis o2) {                
                return Collator.getInstance().compare(o1.getName(), o2.getName());                
            }
        });
        return analysis;
    }
	
	
	@Transactional
	public void removeAnalysis(String analysisId) throws NotFoundException {	
		Analysis a = (Analysis)storageService.getEntityById(analysisId);
		List<Analysis> list = getAnalysisByTable(a.getTableName());
		int no = list.size();
		
		storageService.removeEntityById(analysisId);		
		
		LOG.debug("**** For table " + a.getTableName() +"  there are " + no + " analysis");
		if (no == 1) {
			//last analysis deleted, delete also the table
			ODatabaseDocumentTx db = null;
			try {
				LOG.debug("**** Delete table : " + a.getTableName());
				String prefix = SecurityUtil.getLoggedUsername() + "-";
				db = new ODatabaseDocumentTx(getDatabasePath(), false).open("admin", "admin");
				OrientDbUtils.dropClass(db, a.getTableName());
			} catch (Throwable t) {
				LOG.error(t.getMessage(), t);
			} finally {
				if (db != null) {
					db.close();
				}
			}
		}		
	}
	
	@Transactional
	public void modifyAnalysis(Analysis analysis) {
		storageService.modifyEntity(analysis);
	}
	
	public String getDatabasePath() {		
		return "plocal:" + System.getProperty("nextserver.home") + "/analytics-data";
	}
	
	@Transactional
	public void freeze(final Analysis analysis) {
		final String message = new StringResourceModel("Analysis.freezed", null, new Object[] {analysis.getName()}).getString();		
		final ReportService service = reportService;
		Runnable r = new Runnable() {
			
			@Override
			public void run() {				
				ODatabaseDocumentTx db = null;
				ReportResultEvent event = null;
				String freezeClassName;
				try {
					db = new ODatabaseDocumentTx(getDatabasePath(), false).open("admin", "admin");
					freezeClassName = analysis.getTableName() + AnalysisUtil.FREEZE_MARKUP + UUID.randomUUID();
					OrientDbUtils.duplicateClass(db, analysis.getTableName(), freezeClassName);

					analysis.setFreezed(true);
					analysis.setTableName(freezeClassName);
					modifyAnalysis(analysis);
					event = new ReportResultEvent(analysis.getCreatedBy(), analysis.getName(), AnalysisUtil.FREEZE_ACTION,
							message);
				} catch (Throwable t) {
					// critical case
					t.printStackTrace();
					LOG.error(t.getMessage(), t);
					// freezed unsuccessfull
					analysis.setFreezed(false);
					modifyAnalysis(analysis);
					event = new ReportResultEvent(analysis.getCreatedBy(), analysis.getName(), AnalysisUtil.FREEZE_ACTION,
							AnalysisUtil.FREEZE_FAILED + t.getMessage());
				} finally {
					if (db != null) {
						db.close();
						db = null;
					}
				}
				service.notifyReportListener(event);														
			}
		};
		new Thread(r).start();
			   
	}
		
}
