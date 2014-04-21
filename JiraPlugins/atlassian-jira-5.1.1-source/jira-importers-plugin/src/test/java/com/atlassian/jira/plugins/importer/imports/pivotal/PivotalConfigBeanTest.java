/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingHelper;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.pivotal.config.LoginNameValueMapper;
import com.atlassian.jira.plugins.importer.web.SiteConfiguration;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Map;
import java.util.Set;

public class PivotalConfigBeanTest {
	@Mock(answer = Answers.RETURNS_MOCKS)
	private ExternalUtils externalUtils;
	@Mock
	private PivotalImporterController pivotalImporterController;
	@Mock
	private PivotalDataBean pivotalDataBean;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		Mockito.when(pivotalImporterController.createDataBean(false)).thenReturn(pivotalDataBean);
		Set<ExternalUser> remoteUsers = ImmutableSet.copyOf(MembershipsParserTest.loadSampleMemberships());
		Mockito.when(pivotalDataBean.getAllUsers(Mockito.<ImportLogger>any())).thenReturn(remoteUsers);
	}

	@Test
	public void testUnmappedGetUsernameForLoginName() throws Exception {
		PivotalConfigBean configBean = new PivotalConfigBean(new SiteConfiguration(""), externalUtils, pivotalImporterController);
		configBean.setShowUserMappingPage(false);
		configBean.initializeValueMappingHelper();

		Assert.assertFalse(configBean.getValueMappingHelper().isMapValueForField(LoginNameValueMapper.FIELD));
		
		Assert.assertEquals("Unknown", configBean.getUsernameForLoginName("Unknown"));
		Assert.assertEquals("wseliga", configBean.getUsernameForLoginName("wseliga"));
		Assert.assertEquals("Wojciech Seliga", configBean.getUsernameForLoginName("Wojciech Seliga"));
		Assert.assertEquals("Pawel Niewiadomski", configBean.getUsernameForLoginName("Pawel Niewiadomski"));
		Assert.assertEquals("Test Member", configBean.getUsernameForLoginName("Test Member"));
	}

	@Test
	public void testGetUsernameForLoginName() throws Exception {
		PivotalConfigBean configBean = new PivotalConfigBean(new SiteConfiguration(""), externalUtils, pivotalImporterController);
		configBean.setShowUserMappingPage(true);
		configBean.initializeValueMappingHelper();

		final ValueMappingHelper valueMappingHelper = configBean.getValueMappingHelper();
		valueMappingHelper.initDistinctValuesCache();
		Assert.assertTrue(valueMappingHelper.isMapValueForField(LoginNameValueMapper.FIELD));
		final Set<String> values = valueMappingHelper.getValueMappingDefinition(LoginNameValueMapper.FIELD) .getDistinctValues();

		Assert.assertEquals(ImmutableSet.of("wseliga", "Wojciech Seliga", "Pawel Niewiadomski", "Test Member"), values);

		Map<String, String> theMapping = ImmutableMap.of("Wojciech Seliga", "MappedWS", "Test Member", "MappedTM");
		Map<String, String> mockParams = Maps.newHashMap();
		for (Map.Entry<String, String> m : theMapping.entrySet()) {
			final String key = valueMappingHelper.getValueMappingFieldName(LoginNameValueMapper.FIELD, m.getKey());
			mockParams.put(key, m.getValue());
		}
		valueMappingHelper.populateValueMappings(mockParams);

		Assert.assertEquals("Unknown", configBean.getUsernameForLoginName("Unknown"));
		Assert.assertEquals("wseliga", configBean.getUsernameForLoginName("wseliga"));
		Assert.assertEquals("MappedWS", configBean.getUsernameForLoginName("Wojciech Seliga"));
		Assert.assertEquals("Pawel Niewiadomski", configBean.getUsernameForLoginName("Pawel Niewiadomski"));
		Assert.assertEquals("MappedTM", configBean.getUsernameForLoginName("Test Member"));


	}
}
