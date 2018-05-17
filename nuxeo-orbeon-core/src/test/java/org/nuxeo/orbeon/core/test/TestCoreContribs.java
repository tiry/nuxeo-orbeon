package org.nuxeo.orbeon.core.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

@RunWith(FeaturesRunner.class)
@Features({ CoreFeature.class })
@Deploy({ "nuxeo.orbeon.core" })
@RepositoryConfig(cleanup = Granularity.METHOD, init = ServerInit.class)
public class TestCoreContribs {

	@Inject
	CoreSession session;

	@Test
	public void shouldCreateDocWithFacet() {

		final String formId = "f53a2d0041e53f4ebef826c7341d7c500d26e77b";
		DocumentModel doc = session.createDocumentModel("/", "aDoc", "File");
		doc.setPropertyValue("dc:title", "myFormDoc");
		doc.addFacet("orbeon");
		doc.setPropertyValue("ob:app", "nuxeo");
		doc.setPropertyValue("ob:formName", "SimpleForm");
		doc.setPropertyValue("ob:formId", formId);
		session.createDocument(doc);
		session.save();

		DocumentModelList docs = session.query("select * from Document where ob:formId='" + formId + "'");
		assertEquals(1, docs.size());
	}
}
