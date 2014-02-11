package ro.nextreports.server.web.debug;

import java.io.Serializable;

public class Info implements Serializable {
	
	private static final long serialVersionUID = -8183165882706025392L;

	private String displayName;
	private String value;

	public Info(String displayName, String value) {
		this.displayName = displayName;
		this.value = value;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getValue() {
		return value;
	}		

}
