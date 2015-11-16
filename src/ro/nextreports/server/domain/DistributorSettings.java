package ro.nextreports.server.domain;

import org.jcrom.annotations.JcrProperty;

public class DistributorSettings extends EntityFragment {
	
	private static final long serialVersionUID = 1L;

	@JcrProperty
	private String datePattern;
	
	@JcrProperty
	private String timePattern;

	public DistributorSettings() {
		super();        
    }

	public DistributorSettings(String name, String path) {
		super(name, path);
	}

	public String getDatePattern() {
		return datePattern;
	}

	public void setDatePattern(String datePattern) {
		this.datePattern = datePattern;
	}

	public String getTimePattern() {
		return timePattern;
	}

	public void setTimePattern(String timePattern) {
		this.timePattern = timePattern;
	}

	@Override
	public String toString() {
		return "DistributorSettings [datePattern=" + datePattern
				+ ", timePattern=" + timePattern + "]";
	}



}
