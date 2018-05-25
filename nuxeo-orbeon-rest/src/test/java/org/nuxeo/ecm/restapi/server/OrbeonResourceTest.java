package org.nuxeo.ecm.restapi.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.restapi.test.BaseTest;
import org.nuxeo.ecm.restapi.test.RestServerFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.Jetty;

import com.google.inject.Inject;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

@RunWith(FeaturesRunner.class)
@Features({ RestServerFeature.class, PlatformFeature.class })
@Deploy({ "nuxeo.orbeon.core", "nuxeo.orbeon.rest" })
@Jetty(port = 18090)
@RepositoryConfig(cleanup = Granularity.METHOD, init = RestServerInit.class)
public class OrbeonResourceTest extends BaseTest {

    @Inject
    CoreSession session;

    private WebResource resource;

    @Test
    public void shouldCreateAndReadForm() throws Exception {

        resource = getServiceFor(REST_API_URL, "Administrator", "Administrator");

        final String obXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><form xmlns:fr=\"http://orbeon.org/oxf/xml/form-runner\" fr:data-format-version=\"4.0.0\">\n"
                + "                    <dublincore-section>\n" + "                        <title>test</title>\n"
                + "                        <description>tets</description>\n"
                + "                        <picture>/fr/service/persistence/crud/Nuxeo/SimpleForm/data/"
                + "f53a2d0041e53f4ebef826c7341d7c500d26e77b/1b2e9188de34af6e52105acc568235831ff60265.bin</picture>\n"
                + "                    </dublincore-section>\n" + "                    <Note-section>\n"
                + "                        <note>etetet</note>\n" + "                    </Note-section>\n" + "</form>";

        ClientResponse response = resource.path(
                "orbeon/crud/Nuxeo/SimpleForm/data/f53a2d0041e53f4ebef826c7341d7c500d26e77b/data.xml")
                                          .entity(obXML)
                                          .put(ClientResponse.class);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        session.save();

        ClientResponse attachment = resource.path(
                "orbeon/crud/Nuxeo/SimpleForm/data/f53a2d0041e53f4ebef826c7341d7c500d26e77b/1b2e9188de34af6e52105acc568235831ff60265.bin")
                                            .entity(getClass().getResourceAsStream(
                                                    "/1b2e9188de34af6e52105acc568235831ff60265.bin"))
                                            .put(ClientResponse.class);

        assertEquals(Response.Status.CREATED.getStatusCode(), attachment.getStatus());
        session.save();

        // Duplicate file upload
        attachment = resource.path(
                "orbeon/crud/Nuxeo/SimpleForm/data/f53a2d0041e53f4ebef826c7341d7c500d26e77b/1b2e9188de34af6e52105acc568235831ff60265.bin")
                             .entity(getClass().getResourceAsStream("/1b2e9188de34af6e52105acc568235831ff60265.bin"))
                             .put(ClientResponse.class);

        assertEquals(Response.Status.CREATED.getStatusCode(), attachment.getStatus());
        session.save();

        DocumentModelList docs = session.query("select * from Document where ecm:mixinType = 'orbeon'");
        assertTrue(docs.size() == 3);
        for (DocumentModel doc : docs) {
            System.out.println(doc.getPathAsString() + "(" + doc.getType() + ")");
        }

        response = resource.path("orbeon/crud/Nuxeo/SimpleForm/data/f53a2d0041e53f4ebef826c7341d7c500d26e77b/data.xml")
                           .get(ClientResponse.class);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        String xml = response.getEntity(String.class);
        assertEquals(xml, obXML);

        attachment = resource.path(
                "orbeon/crud/Nuxeo/SimpleForm/data/f53a2d0041e53f4ebef826c7341d7c500d26e77b/1b2e9188de34af6e52105acc568235831ff60265.bin")
                             .get(ClientResponse.class);
        assertEquals(Response.Status.OK.getStatusCode(), attachment.getStatus());
        assertTrue(attachment.hasEntity());
        InputStream image = attachment.getEntityInputStream();
        boolean same = IOUtils.contentEquals(
                getClass().getResourceAsStream("/1b2e9188de34af6e52105acc568235831ff60265.bin"), image);
        assertTrue("image data is not the same", same);

        response = resource.path(
                "orbeon/crud/Nuxeo/SimpleForm/data/f53a2d0041e53f4ebef826c7341d7c500d26e77b/1b2e9188de34af6e52105acc568235831ff60265.bin")
                           .delete(ClientResponse.class);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        session.save();

        response = resource.path("orbeon/crud/Nuxeo/SimpleForm/data/f53a2d0041e53f4ebef826c7341d7c500d26e77b/1b2e9188de34af6e52105acc568235831ff60265.bin")
                           .get(ClientResponse.class);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

        response = resource.path("orbeon/crud/Nuxeo/SimpleForm/data/f53a2d0041e53f4ebef826c7341d7c500d26e77b/data.xml")
                           .delete(ClientResponse.class);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        session.save();

        docs = session.query("select * from Document where ecm:mixinType = 'orbeon'");
        assertTrue(docs.size() == 2);
        for (DocumentModel doc : docs) {
            System.out.println(doc.getPathAsString() + "(" + doc.getType() + ")");
        }
    }

    @Test
    public void shouldCreateAndSearchForm() throws Exception {

        resource = getServiceFor(REST_API_URL, "Administrator", "Administrator");

        final String obXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><form xmlns:fr=\"http://orbeon.org/oxf/xml/form-runner\" fr:data-format-version=\"4.0.0\">\n"
                + "                    <dublincore-section>\n" + "                        <title>test</title>\n"
                + "                        <description>tets</description>\n"
                + "                    </dublincore-section>\n" + "                    <Note-section>\n"
                + "                        <note>etetet</note>\n" + "                    </Note-section>\n" + "</form>";

        ClientResponse response = resource.path("orbeon/crud/Nuxeo/SimpleForm2/data/new-id/data.xml").entity(obXML).put(
                ClientResponse.class);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        session.save();

        response = resource.path("orbeon/search/Nuxeo/SimpleForm2").entity("").post(ClientResponse.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        String xml = response.getEntity(String.class);

        System.out.print(xml);

    }

}
