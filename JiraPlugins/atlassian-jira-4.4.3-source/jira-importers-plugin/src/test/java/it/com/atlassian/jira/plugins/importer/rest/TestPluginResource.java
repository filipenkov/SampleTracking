/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package it.com.atlassian.jira.plugins.importer.rest;

import com.atlassian.jira.plugins.importer.rest.PluginResource;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.plugin.PluginAccessor;
import org.jaxen.JaxenException;
import org.jdom.JDOMException;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.regex.Pattern;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.powermock.api.mockito.PowerMockito.when;

public class TestPluginResource {

	@Test
	public void testGetVersionFromPac()
			throws JDOMException, IOException, SAXException, XPathExpressionException, ParserConfigurationException,
			JaxenException {
		BuildUtilsInfo mockBuildUtils = mock(BuildUtilsInfo.class);
		when(mockBuildUtils.getCurrentBuildNumber()).thenReturn("587");

		PluginAccessor pa = mock(PluginAccessor.class);

		PluginResource pr = new PluginResource(pa, mockBuildUtils);
		String version = pr.getVersionFromPac();
		Assert.assertTrue(Pattern.matches("[1-9]+\\.[0-9]+.*", version));

		verifyZeroInteractions(pa);
	}
}
