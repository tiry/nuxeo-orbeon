package org.nuxeo.ecm.restapi.server;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.test.CoreFeature;
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
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.multipart.impl.MultiPartWriter;

@RunWith(FeaturesRunner.class)
@Features({ RestServerFeature.class, PlatformFeature.class })
@Deploy({ "nuxeo.orbeon.rest" })
@Jetty(port = 18090)
@RepositoryConfig(cleanup = Granularity.METHOD, init = RestServerInit.class)
public class OrbeonResourceTest extends BaseTest{

	@Inject
	protected CoreFeature coreFeature;
	 
    @Inject
    CoreSession session;

    private WebResource resource;
    
    @Test
    public void should_return_OK() throws Exception {

    	resource = getServiceFor(REST_API_URL, "Administrator", "Administrator");
    	
    	ClientResponse response = resource.path("orbeon/crud/nuxeoapp/form1/1")
                                          .get(ClientResponse.class);

        // Then i get a document
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    }


}
