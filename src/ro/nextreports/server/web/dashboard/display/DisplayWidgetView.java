package ro.nextreports.server.web.dashboard.display;

import java.awt.Color;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.engine.ReportRunnerException;
import ro.nextreports.engine.exporter.exception.NoDataFoundException;
import ro.nextreports.engine.exporter.util.DisplayData;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.web.NextServerApplication;
import ro.nextreports.server.web.dashboard.WidgetView;
import ro.nextreports.server.web.dashboard.chart.ChartHTML5Panel;
import ro.nextreports.server.web.dashboard.model.WidgetModel;

public class DisplayWidgetView extends WidgetView  {
	
	private static final long serialVersionUID = 1L;

	private final ResourceReference NEXT_JS = new JavaScriptResourceReference(ChartHTML5Panel.class, NextServerApplication.NEXT_CHARTS_JS);
	
	private String PARAM = "Param";
	private String isHTML5 = "";
	private WebMarkupContainer container;
	
	@SpringBean
	private DashboardService dashboardService;			

	public DisplayWidgetView(String id, WidgetModel model, boolean zoom) {
		this(id, model, zoom, null);		
	}
	
	@SuppressWarnings("unchecked")
	public DisplayWidgetView(String id, WidgetModel model, boolean zoom, Map<String, Object> urlQueryParameters) {
		super(id, model, zoom);

		final String widgetId = model.getWidgetId();		

		final DisplayModel displayModel = new DisplayModel(widgetId, urlQueryParameters);
		
		setModel(new CompoundPropertyModel(displayModel));
					
		add(new Label("error", new Model<String>()) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				if (displayModel.getObject() == null) {
					if (displayModel.getError() instanceof NoDataFoundException) {
						setDefaultModelObject("No Data Found");
					} else {
						setDefaultModelObject(ExceptionUtils.getRootCauseMessage(displayModel.getError()));
					}
				}
			}

			@Override
			public boolean isVisible() {
				return displayModel.hasError();
			}
			
		});
		
		container = new WebMarkupContainer("display") {
			
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return !displayModel.hasError();
			}
			
		};
		container.setOutputMarkupId(true);
		// display background color is set to the entire container
		Color background = Color.decode(displayModel.getObject().getBackground());
		String s = "background-color: rgb(" + 
				background.getRed() + "," + 
				background.getGreen() + ","  +  
				background.getBlue() +  ");";
		container.add(new AttributeAppender("style", s));
		add(container);				
					           				
		add(new HTML5Behavior(zoom, displayModel));
		
		container.add(new EmptyPanel("image"));					
	}
	
	private DisplayData getDisplayData(String widgetId, Map<String, Object> urlQueryParameters) {
		try {					
			return dashboardService.getDisplayData(widgetId, urlQueryParameters);			
		} catch (ReportRunnerException e) {
			throw new RuntimeException(e);
		} catch (NoDataFoundException e) {
			DisplayData data = new DisplayData();
			data.setTitle("No Data");
			return data;
		} catch (TimeoutException e) {
			DisplayData data = new DisplayData();
			data.setTitle("Timeout Elapsed");
			return data;
		}
	}
	
	private class DisplayModel extends LoadableDetachableModel<DisplayData> {
		
		private static final long serialVersionUID = 1L;
		
		private Exception error;	
		private String widgetId;	
		private Map<String, Object> urlQueryParameters;

		public DisplayModel(String widgetId, Map<String, Object> urlQueryParameters) {
			super();
			this.widgetId = widgetId;
			this.urlQueryParameters = urlQueryParameters;
		}

		@Override
		protected DisplayData load() {	
			error = null;
			try {
				return getDisplayData(widgetId, urlQueryParameters);
			} catch (Exception e) {    
				e.printStackTrace();
				error = e;
				return null;
			}				
		}
		
		public Exception getError() {
			return error;
		}		
		
		public boolean hasError() {
			return error != null;
		}
	}
	
	// if canvas is supported for HTML5 we will show DisplayHTML5Panel
	// else we will show DisplayImagePanel
    private class HTML5Behavior extends AbstractDefaultAjaxBehavior {
    	
    	private static final long serialVersionUID = 1L;
    	
		private String width;
    	private String height;
    	private DisplayModel displayModel;
    	    			
		public HTML5Behavior(boolean zoom, DisplayModel displayModel) {
			super();
			
			this.displayModel = displayModel;
			
			// height used to have two displays (one under the other) in dashbord to occupy same height as a single chart
			width = "200";
			height = "122";
			if (zoom) {
				width = "100%";
				height = "100%";
			}			
		}

		@Override
		protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
			super.updateAjaxAttributes(attributes);
			
			StringBuilder javaScript = new StringBuilder();
			javaScript.append("var data = isCanvasEnabled();");
			javaScript.append("console.log(data);");
			javaScript.append("return { '" + PARAM + "': data }"); 
			
			attributes.getDynamicExtraParameters().add(javaScript);
		}

		@Override
		public void renderHead(Component component, IHeaderResponse response) {			
			super.renderHead(component, response);					
			
			//include js file
	        response.render(JavaScriptHeaderItem.forReference(NEXT_JS));
	        
	        response.render(OnLoadHeaderItem.forScript(getCallbackFunctionBody()));	
		}

		@Override
		protected void respond(AjaxRequestTarget target) {
			String param = getRequest().getRequestParameters().getParameterValue(PARAM).toString();					
			//System.out.println("--->  "+param);	
			// behavior is called on any refresh, we have to call it only once 
			// (otherwise the panel will be replaced in the same time the old one is refreshed)
			if (isHTML5.isEmpty()) {
				isHTML5 = param;
				if (Boolean.parseBoolean(param)) {
					container.replace(new DisplayHTML5Panel("image", width, height, displayModel).setOutputMarkupId(true));
				} else {
					// for image height must be a little less than html5 panel
					// to have two displays (one under the other) in dashboard to occupy same height as a single chart
					if ("122".equals(height)) {
						height = "120";
					}					
					container.replace(new DisplayImagePanel("image", width, height, displayModel).setOutputMarkupId(true));
				}				
				target.add(container);				
			}			
		}

    }	

}
