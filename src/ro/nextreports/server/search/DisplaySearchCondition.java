package ro.nextreports.server.search;

import ro.nextreports.server.dao.StorageDao;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.service.StorageService;

public class DisplaySearchCondition extends SearchCondition {

    private DisplaySearchEntry searchEntry;

    public DisplaySearchCondition(StorageDao storageDao, DisplaySearchEntry searchEntry) {
        set(storageDao);
        this.searchEntry = searchEntry;
    }

    @Override
    public int getStatus(StorageService storageService, Entity entity) {                
        Tristate display = searchEntry.getDisplay();
        if (display.getValue() == -1) { // all
        	return TRUE;
        }
        
        if (entity instanceof Report) {
        	Report report = (Report)entity;
        	if (display.getValue() == 0) { // false
        		return report.isDisplayType() ? FALSE : TRUE;
        	} else { // true
        		return report.isDisplayType() ? TRUE : FALSE;
        	}
        } else {
        	return FALSE;
        }
        
    }
}
