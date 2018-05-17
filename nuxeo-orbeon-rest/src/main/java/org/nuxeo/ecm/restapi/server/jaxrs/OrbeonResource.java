package org.nuxeo.ecm.restapi.server.jaxrs;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.core.repository.RepositoryService;
import org.nuxeo.ecm.webengine.model.Template;
import org.nuxeo.ecm.webengine.model.WebContext;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.DefaultObject;
import org.nuxeo.ecm.webengine.scripting.ScriptFile;
import org.nuxeo.runtime.api.Framework;

@WebObject(type = "orbeon")
@Produces(MediaType.APPLICATION_XML)
public class OrbeonResource extends DefaultObject {

	protected static final Log log = LogFactory.getLog(OrbeonResource.class);

	@GET
	@Path("crud/{app}/{form}/data/{id}/data.xml")
	public Object getFormData(@PathParam("app") String app, @PathParam("form") String form,
			@PathParam("id") String id) {

		//XXX Hack to bypass security
		class UnrestrictedSessionFetcher extends UnrestrictedSessionRunner {
			
			protected String orbeonXML;
			
			public UnrestrictedSessionFetcher(String repository) {
				super(repository);
			}			
			@Override
			public void run() {
				DocumentModel formDoc = getOrCreateFormDoc(session, app, form, id, false);
				orbeonXML = mapNuxeoDocumentToOrbeonData(formDoc);
				session.save();
			}
		}
		UnrestrictedSessionFetcher fetcher = new UnrestrictedSessionFetcher(getRepositoryName());		
		fetcher.runUnrestricted();

		return Response.status(Status.OK).entity(fetcher.orbeonXML).build();

	}

	@PUT
	@Path("crud/{app}/{form}/data/{id}/data.xml")
	public Response createUpdateFormData(@PathParam("app") String app, @PathParam("form") String form,
			@PathParam("id") String id) throws Exception {

		//XXX Hack to bypass security
		new UnrestrictedSessionRunner(getRepositoryName()) {
			@Override
			public void run() {
				DocumentModel formDoc = getOrCreateFormDoc(session, app, form, id, true);

				String orbeonXML = readBodyAsXMLString();
				formDoc = mapOrbeonDataToNuxeoDocument(formDoc, orbeonXML);
				session.save();
			}
		}.runUnrestricted();
		return Response.status(Status.CREATED).build();
	}

	
	@POST
	@Path("search/{app}/{form}")
	public Object searchForm(@PathParam("app") String app, @PathParam("form") String form) throws IOException {

		String orbeonXML = readBodyAsXMLString();
		
		System.out.println(orbeonXML);
		
		// XXX parse the search fields 

		
		//XXX Hack to bypass security
		class UnrestrictedSessionSearch extends UnrestrictedSessionRunner {
			
			protected DocumentModelList forms; 
					
			public UnrestrictedSessionSearch(String repository) {
				super(repository);
			}			
			@Override
			public void run() {
				
				forms=session.query("select * from File where ob:app='" + app + "' and ob:formName='" + form + "'");
				for (DocumentModel doc:forms) {
					doc.detach(true);
				}
			}
		}
		UnrestrictedSessionSearch fetcher = new UnrestrictedSessionSearch(getRepositoryName());		
		fetcher.runUnrestricted();
		Template template = new Template(ctx,"obsearch.ftl");
		Map<String, Object> args = new HashMap<>();
		args.put("forms", fetcher.forms);
		args.put("app", app);
		
		template.args(args);

		return template;		
	}

	protected String readBodyAsXMLString() {
		String orbeonXML = null;
		try {
			InputStream xmlStream = ctx.getRequest().getInputStream();
			StringWriter writer = new StringWriter();
			IOUtils.copy(xmlStream, writer, "UTF-8");
			orbeonXML = writer.toString();

		} catch (Exception e) {
			log.error("Unable to read XML payload", e);
		}
		return orbeonXML;
	}
	
	protected String getRepositoryName() {
		// yurk!
		return Framework.getService(RepositoryService.class).getRepositoryNames().get(0);
	}

	protected DocumentModel mapOrbeonDataToNuxeoDocument(DocumentModel formDoc, String orbeonXML) {

		StringBlob blob = new StringBlob(orbeonXML);
		blob.setFilename("data.xml");
		blob.setMimeType("application/xml");

		formDoc.setPropertyValue("file:content", blob);
		return formDoc.getCoreSession().saveDocument(formDoc);
	}

	protected String mapNuxeoDocumentToOrbeonData(DocumentModel formDoc) {

		Blob blob = (Blob) formDoc.getPropertyValue("file:content");	
		try {
			return blob.getString();
		} catch (Exception e) {
			log.error("Unable to read Orbeon data from blob", e);
			return "";
		}
	}

	protected DocumentModel getOrCreateAppRoot(CoreSession session, String app, boolean create) {
		DocumentRef ref = new PathRef("/" + app);
		if (!session.exists(ref) && create) {
			DocumentModel doc = session.createDocumentModel("/", app, "Workspace");
			doc.setPropertyValue("dc:title", app);
			doc.addFacet("orbeon");
			doc.setPropertyValue("ob:app", app);
			session.createDocument(doc);
			session.save();
		}
		return session.getDocument(ref);
	}

	protected DocumentModel getOrCreateFormRoot(CoreSession session, String app, String form, boolean create) {

		DocumentModel appDoc = getOrCreateAppRoot(session, app, create);
		DocumentRef ref = null;

		try {
			ref = new PathRef(appDoc.getPathAsString() + "/" + form);
		} catch (Exception e) {
			log.error("Unable to create Form Root", e);
		}
		try {
			if (!session.exists(ref) && create) {
				DocumentModel doc = session.createDocumentModel(appDoc.getPathAsString(), form, "Folder");
				doc.setPropertyValue("dc:title", form);
				doc.addFacet("orbeon");
				doc.setPropertyValue("ob:app", app);
				doc.setPropertyValue("ob:formName", form);
				session.createDocument(doc);
				session.save();
			}
		} catch (Exception e) {
			log.error("Unable to create Form Root", e);
		}
		return session.getDocument(ref);
	}

	protected DocumentModel getOrCreateFormDoc(CoreSession session, String app, String form, String id,
			boolean create) {

		DocumentModel formFolder = getOrCreateFormRoot(session, app, form, create);

		DocumentRef ref = new PathRef(formFolder.getPathAsString() + "/" + id);
		if (!session.exists(ref) && create) {
			DocumentModel doc = session.createDocumentModel(formFolder.getPathAsString(), id, "File");
			doc.setPropertyValue("dc:title", "Orbeon Folder:" + id);
			doc.addFacet("orbeon");
			doc.setPropertyValue("ob:app", app);
			doc.setPropertyValue("ob:formName", form);
			doc.setPropertyValue("ob:formId", id);
			session.createDocument(doc);
			session.save();
		}
		return session.getDocument(ref);
	}

}
