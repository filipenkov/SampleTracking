package com.atlassian.config.db;

import junit.framework.TestCase;

public class DatabaseDetailsTest extends TestCase {
    private DatabaseDetails details;

    public void setUp() {
        details = new DatabaseDetails();
    }

    public void testSetDatabaseUrlTrim() {
        String databaseUrl = "jdbc:mysql://localhost:3306/confluence?autoReconnect=true&useUnicode=true&useServerPrepStmts=false&characterEncoding=UTF-8 ";
        details.setDatabaseUrl(databaseUrl);
        assertEquals(databaseUrl.trim(), details.getDatabaseUrl());
    }

    public void testSetDatabaseUrlTrimNull() {
        details.setDatabaseUrl(null);
        assertNull(details.getDatabaseUrl());
    }

    public void testSetDialectTrim() {
        String dialect = "dialect   ";
        details.setDialect(dialect);
        assertEquals(dialect.trim(), details.getDialect());
    }

    public void testSetDialectTrimNull() {
        String dialect = null;
        details.setDialect(dialect);
        assertNull(details.getDialect());
    }

    public void testSetDriverClassNameTrim() {
        String className = "com.some.class.name ";
        details.setDriverClassName(className);
        assertEquals(className.trim(), details.getDriverClassName());
    }

    public void testSetDriverClassNameTrimNull() {
        String className = null;
        details.setDriverClassName(className);
        assertNull(details.getDriverClassName());
    }
}
