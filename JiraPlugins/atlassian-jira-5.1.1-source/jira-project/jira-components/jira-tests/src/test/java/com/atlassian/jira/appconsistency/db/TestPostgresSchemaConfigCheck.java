package com.atlassian.jira.appconsistency.db;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.google.common.base.Suppliers;
import org.junit.Test;
import org.ofbiz.core.entity.config.DatasourceInfo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestPostgresSchemaConfigCheck extends MockControllerTestCase
{

    @Test
    public void testLogMessage() throws Exception
    {
        final ExternalLinkUtil mockExternalLinkUtil = mockController.getMock(ExternalLinkUtil.class);
        mockExternalLinkUtil.getProperty("external.link.jira.doc.postgres.db.config");
        mockController.setReturnValue("blah");

        final DatasourceInfo mockDatasourceInfo = mockController.getMock(DatasourceInfo.class);

        mockDatasourceInfo.getFieldTypeName();
        mockController.setReturnValue("postgres");

        mockDatasourceInfo.getSchemaName();
        mockController.setReturnValue("Test");

        final PostgresSchemaConfigCheck configCheck = new PostgresSchemaConfigCheck(null, mockExternalLinkUtil)
        {
            DatasourceInfo getDatasourceInfo()
            {
                return mockDatasourceInfo;
            }
        };

        mockController.replay();

        assertTrue(configCheck.isOk());
        assertTrue(configCheck.isLoggedError());

        mockController.verify();
    }

    @Test
    public void testLogMessagePostgres72() throws Exception
    {
        final ExternalLinkUtil mockExternalLinkUtil = mockController.getMock(ExternalLinkUtil.class);
        mockExternalLinkUtil.getProperty("external.link.jira.doc.postgres.db.config");
        mockController.setReturnValue("blah");

        final DatasourceInfo mockDatasourceInfo = mockController.getMock(DatasourceInfo.class);
        mockDatasourceInfo.getFieldTypeName();
        mockController.setReturnValue("postgres72");
        mockDatasourceInfo.getSchemaName();
        mockController.setReturnValue("Test");


        final PostgresSchemaConfigCheck configCheck = new PostgresSchemaConfigCheck(null, mockExternalLinkUtil)
        {
            DatasourceInfo getDatasourceInfo()
            {
                return mockDatasourceInfo;
            }
        };

        mockController.replay();

        assertTrue(configCheck.isOk());
        assertTrue(configCheck.isLoggedError());

        mockController.verify();
    }

    @Test
    public void testHappyPath() throws Exception
    {
        final ExternalLinkUtil mockExternalLinkUtil = mockController.getMock(ExternalLinkUtil.class);

        final DatasourceInfo mockDatasourceInfo = mockController.getMock(DatasourceInfo.class);
        mockDatasourceInfo.getFieldTypeName();
        mockController.setReturnValue("postgres");
        mockDatasourceInfo.getSchemaName();
        mockController.setReturnValue("test");

        final PostgresSchemaConfigCheck configCheck = new PostgresSchemaConfigCheck(Suppliers.ofInstance(mockDatasourceInfo), mockExternalLinkUtil);

        mockController.replay();

        assertTrue(configCheck.isOk());
        assertFalse(configCheck.isLoggedError());

        mockController.verify();
    }

    @Test
    public void testLogMessageNoDatasource() throws Exception
    {
        final ExternalLinkUtil mockExternalLinkUtil = mockController.getMock(ExternalLinkUtil.class);

        final PostgresSchemaConfigCheck configCheck = new PostgresSchemaConfigCheck(null, mockExternalLinkUtil)
        {
            DatasourceInfo getDatasourceInfo()
            {
                return null;
            }
        };

        mockController.replay();

        assertTrue(configCheck.isOk());
        assertTrue(configCheck.isLoggedError());

        mockController.verify();

    }
}

