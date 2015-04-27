package ro.nextreports.server.web.core.audit;

import java.util.ArrayList;
import java.util.List;

public enum InnerReport {

	RIGHTS("Rights", "Rights"), 
	RUN("Run", "Run"),
	LIST("List", "List");

	private final String name;
	private final String description;

	private InnerReport(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public String toString() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public static List<String> getNames() {
		List<String> result = new ArrayList<String>();
		for (InnerReport ir : values()) {
			result.add(ir.toString());
		}
		return result;
	}

}
