/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.hosted;

import com.atlassian.jira.plugins.importer.XmlUtil;
import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;

public class PeopleParserTest {
	private final PeopleParser PeopleParser = new PeopleParser();

	@Test
	public void testGetPeople() throws Exception {
		final Element rootElement = getRootElement("/fogbugz/people.xml");
		final Collection<ExternalUser> people = PeopleParser.getPeople(rootElement);
		Assert.assertEquals(2, people.size());
	}

	public static Element getRootElement(String resourcePath) throws JDOMException, IOException {
		final SAXBuilder builder = XmlUtil.getSAXBuilder();
		return builder.build(PeopleParserTest.class.getResourceAsStream(resourcePath)).getRootElement();
	}

	@Test
	public void testGetPerson() throws JDOMException, IOException {
		final Element projectElement = getRootElement("/fogbugz/people.xml")
				.getChild("people").getChild("person");

		final ExternalUser user = PeopleParser.getPerson(projectElement);
		Assert.assertNotNull(user);
		Assert.assertEquals("pniewiadomski@atlassian.com", user.getName());
		Assert.assertEquals("Pawel Niewiadomski", user.getFullname());
		Assert.assertEquals("pniewiadomski@atlassian.com", user.getEmail());
	}
}
