package ro.nextreports.server.web.core.section.tab;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import ro.nextreports.server.web.common.tab.ImageTab;
import ro.nextreports.server.web.core.settings.ChangeLogoPanel;
import ro.nextreports.server.web.core.settings.CleanHistorySettingsPanel;
import ro.nextreports.server.web.core.settings.GeneralSettingsPanel;
import ro.nextreports.server.web.core.settings.IFrameSettingsPanel;
import ro.nextreports.server.web.core.settings.IntegrationSettingsPanel;
import ro.nextreports.server.web.core.settings.JasperSettingsPanel;
import ro.nextreports.server.web.core.settings.SchedulerSettingsPanel;
import ro.nextreports.server.web.core.settings.SynchronizerSettingsPanel;

public class SettingsTab implements ImageTab {
	
	public static final byte GENERAL_SETTINGS = 1;
	public static final byte JASPER_SETTINGS = 2;
	public static final byte LOOK_SETTINGS = 3;
	public static final byte SYNCHRONIZER_SETTINGS = 4;
	public static final byte THREAD_POOL_SETTINGS = 5;
	public static final byte IFRAME_SETTINGS = 6;
	public static final byte HISTORY_SETTINGS = 7;
	public static final byte INTEGRATION_SETTINGS = 8;

	private static final long serialVersionUID = 1L;
	
	private byte type;
	private String titleKey;
	private String image;
		
	public SettingsTab(byte type, String titleKey, String image) {
		this.type = type;
		this.titleKey = titleKey;
		this.image = image;
	}

	public IModel<String> getTitle() {		 
		return new StringResourceModel(titleKey, null);
	}

	public String getImage() {
		return image;
	}
	
	@Override
	public boolean isVisible() {		
		return true;
	}	
	
	public Panel getPanel(String panelId) {
		if (type == GENERAL_SETTINGS) {
			return new GeneralSettingsPanel("panel");
		} else if (type == JASPER_SETTINGS) {
			return new JasperSettingsPanel("panel");
		} else if (type == LOOK_SETTINGS) {
			return new ChangeLogoPanel("panel");
		} else if (type == SYNCHRONIZER_SETTINGS) {
			return new SynchronizerSettingsPanel("panel");
		} else if (type == THREAD_POOL_SETTINGS) {
			return new SchedulerSettingsPanel("panel");
		} else if (type == IFRAME_SETTINGS) {
			return new IFrameSettingsPanel("panel");
		} else if (type == INTEGRATION_SETTINGS) {
			return new IntegrationSettingsPanel("panel");
		} else if (type == HISTORY_SETTINGS) {			
			return new CleanHistorySettingsPanel("panel");
		} else {			
			return new GeneralSettingsPanel("panel");
		}
	}		
	
}
