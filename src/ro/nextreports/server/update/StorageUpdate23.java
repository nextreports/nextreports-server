package ro.nextreports.server.update;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.web.themes.ThemesManager;

// NextReports Server 9.0 changed all UI css
// Themes are now red, blue, default (instead of green)
// If found theme is green we change it to default
// If user created and selected another theme we change it to default (old custom themes must be rewritten)
public class StorageUpdate23 extends StorageUpdate {
	
	@Override
	protected void executeUpdate() throws Exception {
		modifyThemeSettings();
	}
	
	private void modifyThemeSettings()  throws RepositoryException, IOException {
						
		LOG.info("Modify Theme Settings");		
        Node rootNode = getTemplate().getRootNode();
        Node settingsNode = rootNode.getNode(StorageConstants.NEXT_SERVER_FOLDER_NAME + StorageConstants.PATH_SEPARATOR + StorageConstants.SETTINGS_FOLDER_NAME);               
                   
        String theme = settingsNode.getProperty(StorageConstants.THEME).getString();
        LOG.info("   Current theme = " + theme);
        if (!ThemesManager.BLUE_THEME.equals(theme) && !ThemesManager.RED_THEME.equals(theme)) {
        	settingsNode.setProperty(StorageConstants.THEME, ThemesManager.DEFAULT_THEME);
        	LOG.info("   Set theme to default green.");
        }        
        getTemplate().save();
	}     

}
