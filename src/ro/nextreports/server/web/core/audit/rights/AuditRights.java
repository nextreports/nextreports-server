package ro.nextreports.server.web.core.audit.rights;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ro.nextreports.server.util.PermissionUtil;

public class AuditRights implements Serializable {
	
	public static final String USER_TYPE = "User";
	public static final String GROUP_TYPE = "Group";
	
	public static List<String> TYPES = Arrays.asList(USER_TYPE, GROUP_TYPE);
	
	public static final String ENTITY_DATA_SOURCES = "DataSources";
	public static final String ENTITY_REPORTS = "Reports";
	public static final String ENTITY_CHARTS = "Charts";
	public static final String ENTITY_SCHEDULERS = "Schedulers";
	public static final String ENTITY_DASHBOARDS = "Dashboards";
	public static final String ENTITY_ANALYSIS = "Analysis";
	
	public static List<String> ENTITIES = Arrays.asList(ENTITY_DATA_SOURCES, ENTITY_REPORTS, ENTITY_CHARTS, ENTITY_SCHEDULERS,
			ENTITY_DASHBOARDS, ENTITY_ANALYSIS);
	
	public static List<String> RIGHTS = Arrays.asList(
			String.valueOf(PermissionUtil.READ_SYMBOL), 
			String.valueOf(PermissionUtil.EXECUTE_SYMBOL),
			String.valueOf(PermissionUtil.WRITE_SYMBOL),			
			String.valueOf(PermissionUtil.DELETE_SYMBOL), 
			String.valueOf(PermissionUtil.SECURITY_SYMBOL));
	
	private String type;
	private String name;
	private List<String> entities;
	private List<String> rights;
	
	public AuditRights() {	
		type = USER_TYPE;
		name = "admin";
		entities = new ArrayList<String>();
		rights = new ArrayList<String>();
		rights.add(String.valueOf(PermissionUtil.READ_SYMBOL));
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getEntities() {
		return entities;
	}

	public void setEntities(List<String> entities) {
		this.entities = entities;
	}

	public List<String> getRights() {
		return rights;
	}

	public void setRights(List<String> rights) {
		this.rights = rights;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result	+ ((entities == null) ? 0 : entities.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((rights == null) ? 0 : rights.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		AuditRights other = (AuditRights) obj;
		if (entities == null) {
			if (other.entities != null) return false;
		} else if (!entities.equals(other.entities))
			return false;
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name))
			return false;
		if (rights == null) {
			if (other.rights != null) return false;
		} else if (!rights.equals(other.rights))
			return false;
		if (type == null) {
			if (other.type != null) return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AuditRights [type=" + type + ", name=" + name + ", entities="
				+ entities + ", rights=" + rights + "]";
	}
	
	

}
