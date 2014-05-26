package ro.nextreports.server.web.dashboard.display;

import ro.nextreports.server.web.dashboard.WidgetDescriptor;

public class DisplayWidgetDescriptor implements WidgetDescriptor {

	public String getDescription() {
		return "A display widget.";
	}

	public String getName() {
		return "Display";
	}

	public String getProvider() {
		return "Mihai Dinca";
	}

	public String getWidgetClassName() {
		return DisplayWidget.class.getName();
	}

}
