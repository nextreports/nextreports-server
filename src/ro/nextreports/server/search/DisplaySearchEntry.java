package ro.nextreports.server.search;

import org.apache.wicket.model.StringResourceModel;

public class DisplaySearchEntry extends SearchEntry {

	private Tristate display;

	public Tristate getDisplay() {
		return display;
	}

	public void setDisplay(Tristate display) {
		this.display = display;
	}

	public String getMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append("* ").append(new StringResourceModel("ActionContributor.Search.entry.display", null).getString()).append(" = '");
		sb.append(display.getName());
		sb.append("'");
		return sb.toString();
	}
}
