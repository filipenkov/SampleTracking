/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.rest;

import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginInformation;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

import static junit.framework.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

public class TestPluginResource {
	private BuildUtilsInfo mockBuildUtils;
	private PluginAccessor pa;

	@Before
	public void setup() {
		mockBuildUtils = mock(BuildUtilsInfo.class);
		when(mockBuildUtils.getCurrentBuildNumber()).thenReturn("587");

		pa = mock(PluginAccessor.class);
	}

	/**
	 * Test for https://studio.atlassian.com/browse/JIM-155
	 * If there's no newer version and getVersionFromPac returns null upgradeAvailable should not be set to true
	 */
	@Test
	public void testPacNullDoesntShowUpAsANewVersion() {
		PluginResource pr = new PluginResource(pa, mockBuildUtils) {
			@Override
			public String getVersionFromPac()
					throws IOException, SAXException, JDOMException {
				return null;
			}
		};

		Plugin plugin = mock(Plugin.class);
		PluginInformation pInfo = new PluginInformation();
		pInfo.setVersion("1.5");
		
		when(pa.getPlugin(PluginResource.PLUGIN_KEY)).thenReturn(plugin);
		when(plugin.getPluginInformation()).thenReturn(pInfo);

		javax.ws.rs.core.Response resp = pr.isUpgradeAvailable();
		assertFalse(Boolean.valueOf(resp.getEntity().toString()));

		verify(pa).getPlugin(PluginResource.PLUGIN_KEY);
	}
}
