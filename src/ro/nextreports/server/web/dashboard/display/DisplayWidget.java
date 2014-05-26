package ro.nextreports.server.web.dashboard.display;

import java.util.Map;

import ro.nextreports.server.web.dashboard.EntityWidget;
import ro.nextreports.server.web.dashboard.WidgetView;
import ro.nextreports.server.web.dashboard.model.WidgetModel;

public class DisplayWidget extends EntityWidget {

	private static final long serialVersionUID = 1L;
	
	public static final String DEFAULT_TITLE = "Display";
	
    public DisplayWidget() {
		setTitle(DEFAULT_TITLE);
	}	

    public WidgetView createView(String viewId, boolean zoom) {
        if (entity == null) {
            return new WidgetView(viewId, new WidgetModel(getId()), zoom); // dynamic
        }

		return new DisplayWidgetView(viewId, new WidgetModel(getId()), zoom); // dynamic;
	}
    
    public WidgetView createView(String viewId, String width, String height) {
        return createView(viewId, true);
	}
    
    public WidgetView createView(String viewId, boolean zoom, Map<String,Object> urlQueryParameters) {
    	 if (entity == null) {
             return new WidgetView(viewId, new WidgetModel(getId()), zoom); // dynamic
         }

 		return new DisplayWidgetView(viewId, new WidgetModel(getId()), zoom, urlQueryParameters); // dynamic;
    }
	
	public WidgetView createView(String viewId, String width, String height, Map<String,Object> urlQueryParameters) {
		return createView(viewId, true, urlQueryParameters);
	}
	
	
    public boolean saveToExcel() {
        return false;  
    }
   

}
