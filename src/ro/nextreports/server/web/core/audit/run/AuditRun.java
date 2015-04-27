package ro.nextreports.server.web.core.audit.run;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import ro.nextreports.engine.util.DateUtil;
import ro.nextreports.server.domain.Entity;

public class AuditRun implements Serializable {
	
	public static final String STATUS_SUCCESS = "Success";
	public static final String STATUS_FAILED = "Failed";
	public static final String STATUS_ALL = "All";
	
	public static List<String> STATUS_LIST = Arrays.asList(STATUS_ALL, STATUS_SUCCESS, STATUS_FAILED);
		
	private String owner;
	private String path;
	private Date startDate;
	private Date endDate;
	private String status;
	
	// needed for inner form where we select report path
	private Set<Entity> tree;
	
	public AuditRun() {
		Date currentDate = new Date();
		startDate = DateUtil.floor(currentDate);
		endDate = DateUtil.ceil(currentDate);
		status = STATUS_ALL;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public Set<Entity> getTree() {
		return tree;
	}

	public void setTree(Set<Entity> tree) {
		this.tree = tree;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result
				+ ((startDate == null) ? 0 : startDate.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		AuditRun other = (AuditRun) obj;
		if (endDate == null) {
			if (other.endDate != null) return false;
		} else if (!endDate.equals(other.endDate))
			return false;
		if (owner == null) {
			if (other.owner != null) return false;
		} else if (!owner.equals(other.owner)) return false;
		if (path == null) {
			if (other.path != null) return false;
		} else if (!path.equals(other.path)) return false;
		if (startDate == null) {
			if (other.startDate != null) return false;
		} else if (!startDate.equals(other.startDate))
			return false;
		if (status == null) {
			if (other.status != null) return false;
		} else if (!status.equals(other.status)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "AuditRun [owner=" + owner + ", path=" + path + ", startDate=" + startDate
				+ ", endDate=" + endDate + ", status=" + status + "]";
	}
		
}
