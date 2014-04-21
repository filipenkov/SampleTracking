/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.bugzilla;

import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.plugins.importer.web.SiteConfiguration;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class TestBugzillaDataBean {

    @Test
    public void testRewriteLinkRegex() throws SQLException {
        JdbcConnection jdbcConnection = mock(JdbcConnection.class);

        Connection connectionMock = mock(Connection.class);
        DatabaseMetaData metaMock = mock(DatabaseMetaData.class);
		DatabaseMetaData metaMock1 = mock(DatabaseMetaData.class);
        ResultSet rsMock = mock(ResultSet.class);
        when(jdbcConnection.getConnection()).thenReturn(connectionMock);
        when(connectionMock.getMetaData()).thenReturn(metaMock, metaMock1);
        when(metaMock.getColumns((String) isNull(), (String) isNull(), anyString(), (String) isNull()))
                .thenReturn(rsMock);

        BugzillaConfigBean configMock = mock(BugzillaConfigBean.class);
        SiteConfiguration siteConfiguration = mock(SiteConfiguration.class);
		DateTimeFormatterFactory dateTimeFormatterFactory = mock(DateTimeFormatterFactory.class);

        BugzillaDataBean bean = new BugzillaDataBean(jdbcConnection, configMock, siteConfiguration,
                dateTimeFormatterFactory);

        Assert.assertEquals(bean.getIssueKeyRegex(), BugzillaDataBean.ISSUE_KEY_REGEX);

        verify(rsMock).close();
        verifyZeroInteractions(configMock);
        verifyZeroInteractions(siteConfiguration);
        verifyZeroInteractions(dateTimeFormatterFactory);
    }

}
