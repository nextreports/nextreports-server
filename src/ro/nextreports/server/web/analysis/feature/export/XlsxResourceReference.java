package ro.nextreports.server.web.analysis.feature.export;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

public class XlsxResourceReference  extends ResourceReference {
	
	private XlsxResource resource;
	
	public XlsxResourceReference(XlsxResource resource, String name) {	   
	    super(XlsxResourceReference.class, name);
	    this.resource = resource;
	}
	 
	@Override
	public IResource getResource() {
	    return resource;
	}

}