package ro.nextreports.server.web.core.audit.list;

import java.io.Serializable;

import ro.nextreports.server.web.core.audit.rights.AuditRights;

public class AuditList implements Serializable {
	
	private String entityType;
	
	public AuditList() {
		this.entityType = AuditRights.ENTITY_DATA_SOURCES;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result	+ ((entityType == null) ? 0 : entityType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		AuditList other = (AuditList) obj;
		if (entityType == null) {
			if (other.entityType != null) return false;
		} else if (!entityType.equals(other.entityType)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "AuditList [entityType=" + entityType + "]";
	}

}
