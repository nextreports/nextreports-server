package ro.nextreports.server.web.analysis.feature.select;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;

public abstract class ColumnsOrderBehavior extends AbstractDefaultAjaxBehavior {
	
	private String OLD_INDEX = "oldIndex";
	private String NEW_INDEX = "newIndex";

	@Override
	protected void respond(AjaxRequestTarget target) {
		IRequestParameters request = RequestCycle.get().getRequest().getRequestParameters();
		int oldIndex = request.getParameterValue(OLD_INDEX).toInt();
		int newIndex = request.getParameterValue(NEW_INDEX).toInt();	
		onResponse(oldIndex, newIndex, target);
	}
	
	public String getJavascript() {
		return "Wicket.Ajax.get({ 'u': '" + getCallbackUrl() + "', 'ep' : {'" + OLD_INDEX + "': oldIndex, '" + NEW_INDEX + "': newIndex}}); ";
	}

	public abstract void onResponse(int oldIndex, int newIndex, AjaxRequestTarget target);		
	
}  

