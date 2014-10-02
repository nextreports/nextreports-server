package ro.nextreports.server.web.dashboard.display;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import ro.nextreports.engine.exporter.util.DisplayData;
import ro.nextreports.server.web.NextServerApplication;
import ro.nextreports.server.web.dashboard.chart.ChartHTML5Panel;

public class DisplayHTML5Panel extends GenericPanel<DisplayData> {
	
	private static final long serialVersionUID = 1L;
	
	private final ResourceReference NEXT_JS = new JavaScriptResourceReference(ChartHTML5Panel.class, NextServerApplication.NEXT_CHARTS_JS);
	private boolean zoom = false;

	public DisplayHTML5Panel(String id, String width, String height, IModel<DisplayData> model) {
		super(id, model);
		
		WebMarkupContainer container = new WebMarkupContainer("displayCanvas");
		container.setOutputMarkupId(true);
		container.add(new AttributeAppender("width", width));
		container.add(new AttributeAppender("height", height));
		zoom = "100%".equals(width) || "100%".equals(height);
		add(container);
	}
		
	@Override
    public void renderHead(IHeaderResponse response) {
		response.render(OnLoadHeaderItem.forScript(getResizeEndDefinition()));
		response.render(OnLoadHeaderItem.forScript(getResizeJavaScript()));
	
		// must call display onLoad instead of onDomReady to appear it in iframe
		// $(document).ready in the iframe seems to be fired too soon and the iframe content isn't even loaded yet
		response.render(OnLoadHeaderItem.forScript(getDisplayCall()));
		
		//include js file
        response.render(JavaScriptHeaderItem.forReference(NEXT_JS));
        
        //<script> tag
        //response.renderJavaScript(getJavaScript(), null); 
    }
	
	private String getDisplayCall() {		
		boolean useParentWidth = zoom ? false : true;
		DisplayData data = getModel().getObject();
		StringBuilder sb = new StringBuilder();		
		sb.append("nextWidget(\"display\",").		   
		   append(data.toJson()).append(",\"").
		   append(get("displayCanvas").getMarkupId()).		   
		   append("\",").append(zoom).
		   append(",").append(useParentWidth).
		   append(");");				
		return sb.toString();
	}
	
	// http://stackoverflow.com/questions/2996431/detect-when-a-window-is-resized-using-javascript
	public String getResizeEndDefinition() {
		StringBuilder sb = new StringBuilder();
		sb.append("$(window).resize(function() {").
		   append("if(this.resizeTO) clearTimeout(this.resizeTO);").
		   append("this.resizeTO = setTimeout(function() {").
		   append("$(this).trigger('resizeEnd');").
		   append("}, 500);").
		   append("});");
		return sb.toString();
	}
	
	// we want a redraw after browser resize
	// display call will be made only when resize event finished!	
	private String getResizeJavaScript() {				
		StringBuilder sb = new StringBuilder();
		sb.append("$(window).bind(\'resizeEnd\',function(){");
		sb.append(getDisplayCall());
		sb.append("});");
		return sb.toString();
	}		

}
