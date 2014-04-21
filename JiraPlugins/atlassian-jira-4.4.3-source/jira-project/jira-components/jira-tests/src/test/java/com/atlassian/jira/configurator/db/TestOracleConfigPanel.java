package com.atlassian.jira.configurator.db;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.jira.exception.ParseException;

public class TestOracleConfigPanel extends ListeningTestCase
{
    @Test
    public void testInvalid()
    {
        OracleConfigPanel configPanel = new OracleConfigPanel();
        try
        {
            configPanel.parseUrl("jdbc:oracle:fat:");
            fail();
        }
        catch (ParseException e)
        {
            // cool
        }
    }

    @Test
    public void testMissingAt()
    {
        OracleConfigPanel configPanel = new OracleConfigPanel();
        try
        {
            configPanel.parseUrl("jdbc:oracle:thin:localhost:SID");
            fail();
        }
        catch (ParseException e)
        {
            // cool
        }
    }

    @Test
    public void testMinimal() throws Exception
    {
        OracleConfigPanel configPanel = new OracleConfigPanel();
        OracleConfigPanel.OracleConnectionProperties connectionProperties = configPanel.parseUrl("jdbc:oracle:thin:@:ORCL");
        assertEquals("", connectionProperties.host);
        assertEquals("", connectionProperties.port);
        assertEquals("ORCL", connectionProperties.sid);
    }

    @Test
    public void testFull() throws Exception
    {
        OracleConfigPanel configPanel = new OracleConfigPanel();
        OracleConfigPanel.OracleConnectionProperties connectionProperties = configPanel.parseUrl("jdbc:oracle:thin:@db.acme.com:1522:ORCL");
        assertEquals("db.acme.com", connectionProperties.host);
        assertEquals("1522", connectionProperties.port);
        assertEquals("ORCL", connectionProperties.sid);
    }

    @Test
    public void testDefaultHost() throws Exception
    {
        OracleConfigPanel configPanel = new OracleConfigPanel();
        OracleConfigPanel.OracleConnectionProperties connectionProperties = configPanel.parseUrl("jdbc:oracle:thin:@:1522:ORCL");
        assertEquals("", connectionProperties.host);
        assertEquals("1522", connectionProperties.port);
        assertEquals("ORCL", connectionProperties.sid);
    }

    @Test
    public void testDefaultPort() throws Exception
    {
        OracleConfigPanel configPanel = new OracleConfigPanel();
        OracleConfigPanel.OracleConnectionProperties connectionProperties = configPanel.parseUrl("jdbc:oracle:thin:@localhost:ORCL");
        assertEquals("localhost", connectionProperties.host);
        assertEquals("", connectionProperties.port);
        assertEquals("ORCL", connectionProperties.sid);
    }

    @Test
    public void testSlashMinimal() throws Exception
    {
        OracleConfigPanel configPanel = new OracleConfigPanel();
        OracleConfigPanel.OracleConnectionProperties connectionProperties = configPanel.parseUrl("jdbc:oracle:thin:@///ORCL");
        assertEquals("", connectionProperties.host);
        assertEquals("", connectionProperties.port);
        assertEquals("ORCL", connectionProperties.sid);
    }

    @Test
    public void testSlashFull() throws Exception
    {
        OracleConfigPanel configPanel = new OracleConfigPanel();
        OracleConfigPanel.OracleConnectionProperties connectionProperties = configPanel.parseUrl("jdbc:oracle:thin:@//db.acme.com:1522/ORCL");
        assertEquals("db.acme.com", connectionProperties.host);
        assertEquals("1522", connectionProperties.port);
        assertEquals("ORCL", connectionProperties.sid);
    }

    @Test
    public void testSlashDefaultHost() throws Exception
    {
        OracleConfigPanel configPanel = new OracleConfigPanel();
        OracleConfigPanel.OracleConnectionProperties connectionProperties = configPanel.parseUrl("jdbc:oracle:thin:@//:1522/ORCL");
        assertEquals("", connectionProperties.host);
        assertEquals("1522", connectionProperties.port);
        assertEquals("ORCL", connectionProperties.sid);
    }

    @Test
    public void testSlashDefaultPort() throws Exception
    {
        OracleConfigPanel configPanel = new OracleConfigPanel();
        OracleConfigPanel.OracleConnectionProperties connectionProperties = configPanel.parseUrl("jdbc:oracle:thin:@//localhost/ORCL");
        assertEquals("localhost", connectionProperties.host);
        assertEquals("", connectionProperties.port);
        assertEquals("ORCL", connectionProperties.sid);
    }
}
