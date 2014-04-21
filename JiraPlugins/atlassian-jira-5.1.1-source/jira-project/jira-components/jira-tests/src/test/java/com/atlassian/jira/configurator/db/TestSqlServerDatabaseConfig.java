package com.atlassian.jira.configurator.db;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.exception.ParseException;

public class TestSqlServerDatabaseConfig extends ListeningTestCase
{
    @Test
    public void testGetUrl() throws Exception
    {
        SqlServerDatabaseConfig sqlServerDatabaseConfig = new SqlServerDatabaseConfig();
        
        assertEquals("jdbc:jtds:sqlserver://localhost:8543/JIRA", sqlServerDatabaseConfig.getUrl("    localhost    ", " 8543  ", "  JIRA    "));
        assertEquals("jdbc:jtds:sqlserver://192.168.3.201/JIRA", sqlServerDatabaseConfig.getUrl("192.168.3.201", "   ", "JIRA"));
        assertEquals("jdbc:jtds:sqlserver://localhost:4433", sqlServerDatabaseConfig.getUrl("localhost", " 4433  ", ""));
        assertEquals("jdbc:jtds:sqlserver://db.acme.com:4433", sqlServerDatabaseConfig.getUrl("db.acme.com", " 4433  ", " "));
    }

    @Test
    public void testParseError()
    {
        SqlServerDatabaseConfig sqlServerDatabaseConfig = new SqlServerDatabaseConfig();
        try
        {
            sqlServerDatabaseConfig.parseUrl("jdbc:notjtds:sqlserver://localhost:4444/JIRA");
            fail();
        }
        catch (ParseException e)
        {
            // goody
        }
    }

    @Test
    public void testParseJtds() throws Exception
    {
        SqlServerDatabaseConfig sqlServerDatabaseConfig = new SqlServerDatabaseConfig();
        DatabaseInstance dbInstance;

        dbInstance = sqlServerDatabaseConfig.parseUrl("jdbc:jtds:sqlserver://localhost:4444/JIRA");
        assertEquals("localhost", dbInstance.getHostname());
        assertEquals("4444", dbInstance.getPort());
        assertEquals("JIRA", dbInstance.getInstance());

        // jdbc:jtds:sqlserver://<server>[:<port>][/<database>][;<property>=<value>[;...]]
        dbInstance = sqlServerDatabaseConfig.parseUrl("jdbc:jtds:sqlserver://localhost:4444/JIRA;dude=true;lady=false");
        assertEquals("localhost", dbInstance.getHostname());
        assertEquals("4444", dbInstance.getPort());
        assertEquals("JIRA", dbInstance.getInstance());

        dbInstance = sqlServerDatabaseConfig.parseUrl("jdbc:jtds:sqlserver://db.some.com/myinstance;dude=true;lady=false");
        assertEquals("db.some.com", dbInstance.getHostname());
        assertEquals("", dbInstance.getPort());
        assertEquals("myinstance", dbInstance.getInstance());

        dbInstance = sqlServerDatabaseConfig.parseUrl("jdbc:jtds:sqlserver://12.12.12.12:333;dude=true;lady=false");
        assertEquals("12.12.12.12", dbInstance.getHostname());
        assertEquals("333", dbInstance.getPort());
        assertEquals("", dbInstance.getInstance());
    }
    @Test
    public void testParseMsDriverWrongPrefix()
    {
        SqlServerDatabaseConfig sqlServerDatabaseConfig = new SqlServerDatabaseConfig();
        try
        {
            sqlServerDatabaseConfig.parseUrl("jdbc:sqlserver:\\");
            fail();
        }
        catch (ParseException e)
        {
            // cool
        }
    }

    @Test
    public void testParseMsDriverMin() throws ParseException
    {
        SqlServerDatabaseConfig sqlServerDatabaseConfig = new SqlServerDatabaseConfig();
        DatabaseInstance connectionProperties = sqlServerDatabaseConfig.parseUrl("jdbc:sqlserver://");
        assertEquals("", connectionProperties.getHostname());
        assertEquals("", connectionProperties.getPort());
        assertEquals("", connectionProperties.getInstance());
    }

    @Test
    public void testParseMsDriverFull() throws ParseException
    {
        //  jdbc:postgresql://host:port/database
        SqlServerDatabaseConfig sqlServerDatabaseConfig = new SqlServerDatabaseConfig();
        DatabaseInstance connectionProperties = sqlServerDatabaseConfig.parseUrl("jdbc:sqlserver://dbserver\\jira:123");
        assertEquals("dbserver", connectionProperties.getHostname());
        assertEquals("123", connectionProperties.getPort());
        assertEquals("jira", connectionProperties.getInstance());
    }

    @Test
    public void testParseMsDriverDefaultPort() throws ParseException
    {
        //  jdbc:postgresql://host:port/database
        SqlServerDatabaseConfig sqlServerDatabaseConfig = new SqlServerDatabaseConfig();
        DatabaseInstance connectionProperties = sqlServerDatabaseConfig.parseUrl("jdbc:sqlserver://dbserver\\jira");
        assertEquals("dbserver", connectionProperties.getHostname());
        assertEquals("", connectionProperties.getPort());
        assertEquals("jira", connectionProperties.getInstance());
    }

    @Test
    public void testParseMsDriverDefaultInstance() throws ParseException
    {
        //  jdbc:postgresql://host:port/database
        SqlServerDatabaseConfig sqlServerDatabaseConfig = new SqlServerDatabaseConfig();
        DatabaseInstance connectionProperties = sqlServerDatabaseConfig.parseUrl("jdbc:sqlserver://dbserver:32");
        assertEquals("dbserver", connectionProperties.getHostname());
        assertEquals("32", connectionProperties.getPort());
        assertEquals("", connectionProperties.getInstance());
    }    
}
