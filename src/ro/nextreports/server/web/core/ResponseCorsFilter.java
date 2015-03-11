package ro.nextreports.server.web.core;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
 
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
 
// This class is intended for Jersey web service client tests only when NextReports Server uses localhost as base url 
// and we have a test javascript client in a specific folder, not inside server application
// 
// see web.xml jersey.springServlet configuration where ResponseCorsFilter is specified (by default is is commented)
//
// Cross-origin resource sharing (CORS) is a mechanism that allows many resources (e.g. fonts, JavaScript, etc.) on a web page 
// to be requested from another domain outside the domain from which the resource originated
public class ResponseCorsFilter implements ContainerResponseFilter {
 
    @Override
    public ContainerResponse filter(ContainerRequest req, ContainerResponse contResp) {
 
        ResponseBuilder resp = Response.fromResponse(contResp.getResponse());
        resp.header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
 
        String reqHead = req.getHeaderValue("Access-Control-Request-Headers");
 
        if(null != reqHead && !reqHead.equals("")){
            resp.header("Access-Control-Allow-Headers", reqHead);
        }
 
        contResp.setResponse(resp.build());
        return contResp;
    }
 
}
