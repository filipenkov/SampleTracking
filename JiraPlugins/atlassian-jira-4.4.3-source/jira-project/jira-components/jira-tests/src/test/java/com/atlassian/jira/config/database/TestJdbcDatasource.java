package com.atlassian.jira.config.database;

import com.atlassian.config.db.DatabaseDetails;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link JdbcDatasource}.
 *
 * @since v4.4
 */
public class TestJdbcDatasource
{
    @Test
    public void createDbDetails()
    {
        JdbcDatasource jdbcDatasource = new JdbcDatasource("url", "java.lang.String", "whoYoDaddy", "pssst", 31337, null, null, null);
        final DatabaseDetails dbDetails = jdbcDatasource.createDbDetails();
        Assert.assertEquals("url", dbDetails.getDatabaseUrl());
        Assert.assertEquals("java.lang.String", dbDetails.getDriverClassName());
        Assert.assertEquals("whoYoDaddy", dbDetails.getUserName());
        Assert.assertEquals("pssst", dbDetails.getPassword());
        Assert.assertEquals(31337, dbDetails.getPoolSize());
    }

    @Test
    public void constructorBlankPasswordOk()
    {
        // just checking we don't throw
        new JdbcDatasource("url", "java.lang.String", "whoYoDaddy", "", 31337, null, null, null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorNegativePoolSize()
    {
        new JdbcDatasource("url", "java.lang.String", "whoYoDaddy", "pssst", -31337, null, null, null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorNullUrl()
    {
        new JdbcDatasource(null, "java.lang.String", "whoYoDaddy", "pssst", 31337, null, null, null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorNullDriver()
    {
        new JdbcDatasource("url", null, "whoYoDaddy", "pssst", 31337, null, null, null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorBlankUsername()
    {
        new JdbcDatasource("url", "java.lang.String", "", "pssst", 31337, null, null, null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorNullPassword()
    {
        new JdbcDatasource("url", "java.lang.String", "whoYoDaddy", null, 31337, null, null, null);
    }
}
