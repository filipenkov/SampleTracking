/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.plugins.importer.XmlUtil;
import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.atlassian.jira.plugins.importer.external.beans.NamedExternalObject;
import com.atlassian.jira.plugins.importer.imports.config.UserNameMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class MembershipsParserTest {
	public static Element getRootElement(String resourcePath) throws JDOMException, IOException {
		final SAXBuilder builder = XmlUtil.getSAXBuilder();
		return builder.build(MembershipsParserTest.class.getResourceAsStream(resourcePath)).getRootElement();
	}

	@Test
	public void testGetProject() throws JDOMException, IOException {
		List<ExternalUser> users = loadSampleMemberships();
		Assert.assertEquals(4, users.size());
		ImmutableMap<String, ExternalUser> userMap = Maps.uniqueIndex(users, NamedExternalObject.NAME_FUNCTION);

		// make sure we know what happens to name/fullname wrt character case so username mapping works as expected
		Assert.assertEquals("Test Member", userMap.get("test member").getFullname());
		Assert.assertEquals("Wojciech Seliga", userMap.get("wojciech seliga").getFullname());
		Assert.assertEquals("Pawel Niewiadomski", userMap.get("pawel niewiadomski").getFullname());
		Assert.assertEquals("wseliga", userMap.get("wseliga").getFullname());
	}

	public static List<ExternalUser> loadSampleMemberships() throws JDOMException, IOException {
		final Element membersElement = getRootElement("/pivotal/memberships.xml");

		ProjectMembershipParser parser = new ProjectMembershipParser(UserNameMapper.NO_MAPPING);
		return parser.parseUsers("1", membersElement);
	}
}
