/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.bugzilla.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalLink;
import com.atlassian.jira.plugins.importer.imports.bugzilla.BugzillaConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

public class TestDuplicateLinksTransformer {

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-140
	 */
	@Test
	public void testLinkName() throws SQLException {
		BugzillaConfigBean configMock = mock(BugzillaConfigBean.class);

		ResultSetTransformer transformer = new DuplicateLinksTransformer(configMock);

		ResultSet rs = mock(ResultSet.class);
		transformer.transform(rs);

		verify(configMock).getLinkMapping(BugzillaConfigBean.DUPLICATES_LINK_NAME);


	}

	@Test
	public void testLinkTransformer() throws SQLException {
		BugzillaConfigBean configMock = mock(BugzillaConfigBean.class);

		DuplicateLinksTransformer transformer = new DuplicateLinksTransformer(configMock);

		ResultSet rs = mock(ResultSet.class);
		when(rs.getString("dupe_of")).thenReturn("12");
		when(rs.getString("dupe")).thenReturn("17");

		when(configMock.getLinkMapping(BugzillaConfigBean.DUPLICATES_LINK_NAME)).thenReturn("Duplicate");

		ExternalLink link = transformer.transform(rs);
		assertEquals("17", link.getSourceId());
		assertEquals("12", link.getDestinationId());
		assertEquals("Duplicate", link.getName());

		verify(rs, times(2)).getString(anyString());
	}
}
