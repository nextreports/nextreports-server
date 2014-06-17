package ro.nextreports.server.web.dashboard.alarm;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import ro.nextreports.engine.exporter.util.AlarmData;
import ro.nextreports.server.web.dashboard.chart.ChartHTML5Panel;

public class AlarmImagePanel extends Panel {
	
	private final ResourceReference NEXT_JS = new JavaScriptResourceReference(ChartHTML5Panel.class, "nextcharts-1.2.min.js");
	
	private AlarmDynamicImageResource imageResource;
	private String width; 
	private String height;
	
	public AlarmImagePanel(String id, String width, String height, final IModel<AlarmData> model) {
		super(id, model);
		this.width = width;
		this.height = height;
		
		NonCachingImage image = new NonCachingImage("image", new PropertyModel(this, "imageResource")){
            private static final long serialVersionUID = 1L;
	           
            @Override
            protected void onBeforeRender() {            	
            	imageResource =  new AlarmDynamicImageResource(80, model.getObject().getColor());       
                super.onBeforeRender();
            }           
        }; 	                
		add(image);
		
		add(new Label("status", model));
		
	}		

}
