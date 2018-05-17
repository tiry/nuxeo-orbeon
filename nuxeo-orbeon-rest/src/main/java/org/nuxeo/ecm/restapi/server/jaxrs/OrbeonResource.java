package org.nuxeo.ecm.restapi.server.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.DefaultObject;

@WebObject(type = "orbeon")
@Produces(MediaType.APPLICATION_XML)
public class OrbeonResource extends DefaultObject {
	
	@GET
    @Path("crud/{app}/{form}/{id}")
    public Object getFormData(@PathParam("app") String app, @PathParam("form") String form,@PathParam("id") String id) {
    	
    	return "OK";
    }
    
	@POST
    @Path("crud/{app}/{form}/{id}")
    public Object createFormData(@PathParam("app") String app, @PathParam("form") String form,@PathParam("id") String id) {
    	
    	return "OK";
    }
    
	@PUT
    @Path("crud/{app}/{form}/{id}")
    public Object updateFormData(@PathParam("app") String app, @PathParam("form") String form,@PathParam("id") String id) {
    	
    	return "OK";
    }

	
	protected DocumentModel createDocument(String app, String form,String id) {
	
		CoreSession session = ctx.getCoreSession();
		
		return null;
	}
		
	
	protected DocumentModel resolveDocumentByFormId(String id) {
		CoreSession session = ctx.getCoreSession();
		
		
		return null;
	}

	
}
