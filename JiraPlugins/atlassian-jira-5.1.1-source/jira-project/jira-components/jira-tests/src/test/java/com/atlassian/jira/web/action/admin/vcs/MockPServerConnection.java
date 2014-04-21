package com.atlassian.jira.web.action.admin.vcs;

import org.netbeans.lib.cvsclient.connection.PServerConnection;

import java.io.IOException;

/**
 * This class was extracted from TestAddRepository. Originally both separate classes were defined in the same file.
 */
public class MockPServerConnection extends PServerConnection
{
    public void close() throws java.io.IOException
    {
        throw new IOException();
    }
}
