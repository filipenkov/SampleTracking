package com.atlassian.jira.plugins.importer;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class XmlUtilTest {

	private Element getRootElement(String resourcePath) throws JDOMException, IOException {
		final SAXBuilder builder = XmlUtil.getSAXBuilder();
		return builder.build(XmlUtilTest.class.getResourceAsStream(resourcePath)).getRootElement();
	}

	@Test
	public void testBillionLolsVulnerability() throws JDOMException, IOException {
		try {
			getRootElement("/xml/vulnerabilities/onebillionlols.xml");
			Assert.fail("Exception expected about too many entity expansions detected in the document");
		} catch (JDOMException e) {
			Assert.assertTrue(e.getMessage().contains("entity expansions in this document"));
		}
	}

	@Test
	public void testFileEntityVulnerability() throws JDOMException, IOException {
		final Element element = getRootElement("/xml/vulnerabilities/systemEntityVulnerability.xml");
		Assert.assertEquals("<>\"A", element.getText().trim());
	}


}
