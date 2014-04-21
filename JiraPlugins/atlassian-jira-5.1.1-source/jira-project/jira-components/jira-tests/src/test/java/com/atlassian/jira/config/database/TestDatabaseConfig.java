package com.atlassian.jira.config.database;

import org.junit.Test;
import org.ofbiz.core.entity.config.DatasourceInfo;
import org.ofbiz.core.entity.config.JdbcDatasourceInfo;
import org.ofbiz.core.entity.config.JndiDatasourceInfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit test for {@link DatabaseConfig}.
 */
public class TestDatabaseConfig
{
    @Test
    public void testGetDatasourceInfoJdbc()
    {
        JdbcDatasource datasource = new JdbcDatasource("url", "org.hsqldb.jdbcDriver", "username", "password", 10, null, null, null);
        DatabaseConfig config = new DatabaseConfig("datasource", "delegator", "hsqldb", "PUBLIC", datasource);
        DatasourceInfo dsi = config.getDatasourceInfo();
        assertEquals("datasource", dsi.getName());
        assertEquals("hsqldb", dsi.getFieldTypeName());
        assertEquals("PUBLIC", dsi.getSchemaName());
        assertNotNull(dsi.getJdbcDatasource());
        JdbcDatasourceInfo jdbc = dsi.getJdbcDatasource();
        assertEquals("url", jdbc.getUri());
        assertEquals("org.hsqldb.jdbcDriver", jdbc.getDriverClassName());
        assertEquals("username", jdbc.getUsername());
        assertEquals("password", jdbc.getPassword());
        assertEquals((Object) 10, jdbc.getConnectionPoolInfo().getMaxSize());
        assertEquals(null, jdbc.getConnectionPoolInfo().getValidationQuery());
        assertEquals(null, jdbc.getConnectionPoolInfo().getMinEvictableTimeMillis());
        assertEquals(null, jdbc.getConnectionPoolInfo().getTimeBetweenEvictionRunsMillis());
    }

    @Test
    public void testGetDatasourceInfoJdbcWithPoolSettings()
    {
        JdbcDatasource datasource = new JdbcDatasource("url", "org.hsqldb.jdbcDriver", "username", "password", 10, "SELECT 1", 123L, 456L);
        DatabaseConfig config = new DatabaseConfig("datasource", "delegator", "hsqldb", "PUBLIC", datasource);
        DatasourceInfo dsi = config.getDatasourceInfo();
        assertEquals("datasource", dsi.getName());
        assertEquals("hsqldb", dsi.getFieldTypeName());
        assertEquals("PUBLIC", dsi.getSchemaName());
        assertNotNull(dsi.getJdbcDatasource());
        JdbcDatasourceInfo jdbc = dsi.getJdbcDatasource();
        assertEquals("url", jdbc.getUri());
        assertEquals("org.hsqldb.jdbcDriver", jdbc.getDriverClassName());
        assertEquals("username", jdbc.getUsername());
        assertEquals("password", jdbc.getPassword());
        assertEquals((Object) 10, jdbc.getConnectionPoolInfo().getMaxSize());
        assertEquals("SELECT 1", jdbc.getConnectionPoolInfo().getValidationQuery());
        assertEquals(new Long(123), jdbc.getConnectionPoolInfo().getMinEvictableTimeMillis());
        assertEquals(new Long(456), jdbc.getConnectionPoolInfo().getTimeBetweenEvictionRunsMillis());
    }

    @Test
    public void testGetDatasourceInfoJndi()
    {
        JndiDatasource datasource = new JndiDatasource("java:comp/env/jndisucks");
        DatabaseConfig config = new DatabaseConfig("datasource", "delegator", "hsqldb", "PUBLIC", datasource);
        DatasourceInfo dsi = config.getDatasourceInfo();
        assertEquals("datasource", dsi.getName());
        assertEquals("hsqldb", dsi.getFieldTypeName());
        assertEquals("PUBLIC", dsi.getSchemaName());
        assertNotNull(dsi.getJndiDatasource());
        JndiDatasourceInfo jndi = dsi.getJndiDatasource();
        assertEquals("default", jndi.getJndiServerName());
        assertEquals("java:comp/env/jndisucks", jndi.getJndiName());
    }
}
