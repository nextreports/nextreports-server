package ro.nextreports.server.domain;

import org.jcrom.annotations.JcrProperty;

import ro.nextreports.server.distribution.Destination;
import ro.nextreports.server.distribution.DestinationType;

public class CopyDestination extends Destination {
	
	private static final long serialVersionUID = 1L;

	@JcrProperty
	private String fileName;
	
	public CopyDestination() {
		super();
		setName(DestinationType.COPY.toString());
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getType() {
		return DestinationType.COPY.toString();
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("SmtpAlertDestination[");
		buffer.append("name = ").append(name);
		buffer.append(", path = ").append(path);
		buffer.append(", fileName = ").append(getFileName());
		buffer.append("]");
		return buffer.toString();
	}

}
