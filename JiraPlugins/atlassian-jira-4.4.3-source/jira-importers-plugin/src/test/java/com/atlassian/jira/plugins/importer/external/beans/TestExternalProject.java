/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.external.beans;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestExternalProject {

	@Test
	public void testCopyConstructor() {
		ExternalProject p1 = new ExternalProject().setJiraId(1l).setDescription("description").setName("name");
		ExternalProject p2 = p1.getClone();

		assertEquals(Long.valueOf(1l), p2.getJiraId());
		assertEquals("description", p2.getDescription());
		assertEquals("name", p2.getName());
		assertNull(p2.getId());
		assertNull(p2.getKey());
		assertNull(p2.getUrl());
		assertNull(p2.getLead());
		assertNull(p2.getProjectCategoryName());
		assertNull(p2.getAssigneeType());

		p1 = new ExternalProject().setId("a").setExternalName("external").setKey("KEY").setUrl("http://localhost")
			.setLead("lead").setProjectCategoryName("category");
		p2 = p1.getClone();

		assertNull(p2.getJiraId());
		assertEquals("a", p2.getId());
		assertEquals("external", p2.getExternalName());
		assertNull(p2.getName());
		assertEquals("KEY", p2.getKey());
		assertEquals("http://localhost", p2.getUrl());
		assertEquals("lead", p2.getLead());
		assertEquals("category", p2.getProjectCategoryName());
		assertNull(p2.getAssigneeType());
	}

	@Test
	public void testSetField() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
		ExternalProject p = new ExternalProject();
		p.setField("description", "test");
		assertEquals("test", p.getDescription());
		p.setField("key", "KEY");
		assertEquals("KEY", p.getKey());
		p.setField("lead", "L");
		assertEquals("L", p.getLead());
		p.setField("name", "Name");
		assertEquals("Name", p.getName());
		p.setField("url", "http://localhost");
		assertEquals("http://localhost", p.getUrl());
		p.setField("projectCategoryName", "CAT");
		assertEquals("CAT", p.getProjectCategoryName());
	}

}
