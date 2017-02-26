package ro.nextreports.server.web.core.settings;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import ro.nextreports.server.web.core.section.tab.ImageTabbedPanel;
import ro.nextreports.server.web.core.section.tab.SettingsTab;

public class FullTabSettingsPanel extends Panel {
	
	private static final long serialVersionUID = -7954890890962023838L;

	public FullTabSettingsPanel(String id) {
		super(id);
				
    	final List<ITab> tabs = new ArrayList<ITab>();
        tabs.add(new SettingsTab(SettingsTab.GENERAL_SETTINGS, "Settings.general.title", "images/settings_general.png")); 
        tabs.add(new SettingsTab(SettingsTab.LOOK_SETTINGS, "Settings.personalize.title", "images/settings_look.png"));
        tabs.add(new SettingsTab(SettingsTab.DISTRIBUTION_SETTINGS, "Settings.distribution.title", "images/settings_distribution.png"));   
        tabs.add(new SettingsTab(SettingsTab.JASPER_SETTINGS, "Settings.jasper.title", "images/settings_jasper.png"));        
        tabs.add(new SettingsTab(SettingsTab.SYNCHRONIZER_SETTINGS, "Settings.synchronizer.title", "images/settings_synchronizer.png"));
        tabs.add(new SettingsTab(SettingsTab.THREAD_POOL_SETTINGS, "Settings.thread.title", "images/settings_thread_pool.png"));
        tabs.add(new SettingsTab(SettingsTab.IFRAME_SETTINGS, "Settings.iframe.title", "images/settings_iframe.png"));
        tabs.add(new SettingsTab(SettingsTab.HISTORY_SETTINGS, "Settings.cleanHistory.title", "images/settings_history.png"));
        tabs.add(new SettingsTab(SettingsTab.INTEGRATION_SETTINGS, "Settings.integration.title", "images/settings_integration.png"));                
    	
        ImageTabbedPanel panel = new ImageTabbedPanel("tabs", tabs) {
        	
        	protected void afterInit() {
            	setSelectedTab(0);
            }
        	
        	protected void onAjaxUpdate(AjaxRequestTarget target) {
        		PanelVisitor visitor = new PanelVisitor();
                visitChildren(visitor);                
                visitor.getPanel().add(AttributeModifier.replace("class", "tab-panel-settings"));                     		
        	}        	        
        };
        panel.setSelectedTab(0);
        PanelVisitor visitor = new PanelVisitor();
        panel.visitChildren(visitor);
        panel.get("tabs-container").add(AttributeModifier.replace("class", "tab-row-settings"));
        visitor.getPanel().add(AttributeModifier.replace("class", "tab-panel-settings"));        
    	add(panel);
	}
	
	private class PanelVisitor implements IVisitor<Component, Void>, Serializable {

		private static final long serialVersionUID = 1L;

		private Component visited;
		
		public Component getPanel() {
			return visited;
		}

		@Override
		public void component(Component object, IVisit<Void> visit) {
			if (object.getId().startsWith("panel")) {				
				visited = object;				
			}
		}

	}

}
