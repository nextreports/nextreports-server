package ro.nextreports.server.web.dashboard.display;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import ro.nextreports.engine.exporter.util.DisplayData;
import ro.nextreports.server.web.NextServerApplication;
import ro.nextreports.server.web.dashboard.WidgetPopupMenuModel;
import ro.nextreports.server.web.dashboard.chart.ChartHTML5Panel;

public class DisplayImagePanel extends Panel {
	
	private static final long serialVersionUID = 1L;

	private final ResourceReference NEXT_JS = new JavaScriptResourceReference(ChartHTML5Panel.class, NextServerApplication.NEXT_CHARTS_JS);
	
	private DisplayDinamicImageResource imageResource;
	private String width; 
	private String height;
	
	public DisplayImagePanel(String id, String w, String h, final IModel<DisplayData> model) {
		super(id, model);
		this.width = w;
		this.height = h;

		NonCachingImage image = new NonCachingImage("displayImage", new PropertyModel(this, "imageResource")) {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected void onBeforeRender() {
				int w = 200;
				int h = 120;
				if ("100%".equals(width) || "100%".equals(height)) {
					// see WidgetPopupMenuModel
					w = WidgetPopupMenuModel.POPUP_WIDTH;
					h = WidgetPopupMenuModel.POPUP_HEIGHT;
				} else {
					w = Integer.parseInt(width);
					h = Integer.parseInt(height);
				}
				imageResource = new DisplayDinamicImageResource(w, h, model.getObject());
				super.onBeforeRender();
			}
		};
		
		add(image);
//		add(new ResizeBehavior());
	}			
			
	private class ResizeBehavior extends AbstractDefaultAjaxBehavior {		
		
		private static final long serialVersionUID = 1L;
		
		private static final String HEIGHT = "height";
		
		@Override
		public void renderHead(Component component, IHeaderResponse response) {			
			super.renderHead(component, response);
			
			//include js file
			response.render(JavaScriptHeaderItem.forReference(NEXT_JS));
			
			response.render(OnLoadHeaderItem.forScript(getResizeEndDefinition()));	
			response.render(OnLoadHeaderItem.forScript(getResizeJavaScript()));			
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
		// will be made only when resize event finished!
		private String getResizeJavaScript() {
			StringBuilder sb = new StringBuilder();
			sb.append("$(window).bind(\'resizeEnd\', function(){");
			sb.append("var ih = getIndicatorHeight();");
			sb.append("Wicket.Ajax.get({ u: '");
			sb.append(getCallbackUrl());
			sb.append("&");
			sb.append(HEIGHT);
			sb.append("=");
			sb.append("ih");
			sb.append("});");
						
//			System.out.println("--->   " + sb.toString());
			
			return sb.toString();
		}

		@Override
		protected void respond(AjaxRequestTarget target) {
			height = this.getComponent().getRequest().getRequestParameters().getParameterValue(HEIGHT).toString();
			width = height;
//			System.out.println("*** height="+height);
			target.add(this.getComponent());
		}
		
	}

}
