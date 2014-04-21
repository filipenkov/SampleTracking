package com.atlassian.jira.upgrade.util;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizFactory;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.appconsistency.db.TableColumnCheckResult;
import com.atlassian.jira.local.testutils.UtilsForTestSetup;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.BuildUtilsInfoImpl;
import com.atlassian.jira.local.ListeningTestCase;
import org.ofbiz.core.entity.ConnectionFactory;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TestUpgradeUtils extends ListeningTestCase
{
    private UpgradeUtils upgradeUtils = null;
    private OfBizDelegator ofBizDelegator = null;
    private static final String QRTZJOB_DETAILS = "QRTZJobDetails";
    private static final String JOB_NAME_COL = "JOB_NAME";
    private static final String JOB_NAME = "jobName";
    private static final String QRTZ_JOB_DETAILS_TABLE = "qrtz_job_details";
    private static final String DESCRIPTION_COLUMN_NAME = "JIRA_DESC";

    private static final String PROPERTY_ENTRY_TABLE = "PROPERTYENTRY";
    private static final String PROPERTY_STRING_TABLE = "PROPERTYSTRING";
    private static final String PROPERTY_STRING_PROPERTY_VALUE = "PROPERTYVALUE";
    private static final String PROPERTY_ENTRY_PROPERTY_KEY = "PROPERTY_KEY";
    private BuildUtilsInfo buildUtilsInfo = new BuildUtilsInfoImpl();

    @Before
    public void setUp() throws Exception
    {
        UtilsForTestSetup.loadDatabaseDriver();
        UtilsForTestSetup.deleteAllEntities();

        ofBizDelegator = OfBizFactory.getOfBizDelegator();
        upgradeUtils = new UpgradeUtils(ofBizDelegator);

        // Create a column that is not in the entitymodel.xml
        alterColumnOnTable(true);

        // Setup table to play with
        insertIntoTable(QRTZ_JOB_DETAILS_TABLE, DESCRIPTION_COLUMN_NAME, 1, "value 1");
        insertIntoTable(QRTZ_JOB_DETAILS_TABLE, DESCRIPTION_COLUMN_NAME, 2, "value 2");
        insertIntoTable(QRTZ_JOB_DETAILS_TABLE, DESCRIPTION_COLUMN_NAME, 3, "value 3");

        // Set up some version information for JIRA
        insertIntoTable(PROPERTY_ENTRY_TABLE, PROPERTY_ENTRY_PROPERTY_KEY, 1, "jira.version.patched");
        insertIntoTable(PROPERTY_STRING_TABLE, PROPERTY_STRING_PROPERTY_VALUE, 1, buildUtilsInfo.getCurrentBuildNumber());

        final MockComponentWorker componentAccessorWorker = new MockComponentWorker();
        componentAccessorWorker.registerMock(OfBizDelegator.class, ofBizDelegator);
        ComponentAccessor.initialiseWorker(componentAccessorWorker);
    }

    @After
    public void tearDown() throws Exception
    {
        UtilsForTestSetup.deleteAllEntities();
        alterColumnOnTable(false);
    }

    @Test
    public void testClearColumns() throws GenericEntityException
    {
        EntityUtils.createValue(QRTZJOB_DETAILS, EasyMap.build("id", "4", JOB_NAME, "value 1"));

        upgradeUtils.clearColumn(QRTZJOB_DETAILS, JOB_NAME);

        List jobs = ofBizDelegator.findAll(QRTZJOB_DETAILS);
        for (Iterator iterator = jobs.iterator(); iterator.hasNext();)
        {
            GenericValue genericValue = (GenericValue) iterator.next();
            assertNull(genericValue.getString(JOB_NAME));
        }
    }

    @Test
    public void testClearColumnWithInvalidColumn()
    {
        try
        {
            upgradeUtils.clearColumn(QRTZJOB_DETAILS, "junkOne");
            fail();
        }
        catch(IllegalArgumentException e)
        {
            // An exception should of been thrown
        }

    }

    @Test
    public void testGetExactColumnName()
    {
        try
        {
            String tableName_issueType = "issuetype";
            String exactColumnName = UpgradeUtils.getExactColumnName(tableName_issueType, "ISSUETYPE");
            assertNull(exactColumnName);

            exactColumnName = UpgradeUtils.getExactColumnName(tableName_issueType, "PNAME");
            assertTrue("pname".equalsIgnoreCase(exactColumnName));

            tableName_issueType = "fieldconfigschemeissuetype";
            exactColumnName = UpgradeUtils.getExactColumnName(tableName_issueType, "ISSUETYPE");
            assertTrue("issuetype".equalsIgnoreCase(exactColumnName));

            exactColumnName = UpgradeUtils.getExactColumnName(tableName_issueType, "PNAME");
            assertNull(exactColumnName);
        }
        catch (Exception e)
        {

        }
    }

    @Test
    public void testDoColumnsOrTablesExist()
    {
        try
        {
            String tableName_issueType = "issuetype";
            List tableColumnCheckResults = new ArrayList();
            tableColumnCheckResults.add(new TableColumnCheckResult(tableName_issueType, "ISSUETYPE"));
            UpgradeUtils.doColumnsOrTablesExist(tableColumnCheckResults);
            assertFalse(((TableColumnCheckResult)tableColumnCheckResults.get(0)).isExists());

            tableColumnCheckResults = new ArrayList();
            tableColumnCheckResults.add(new TableColumnCheckResult(tableName_issueType, "PNAME"));
            UpgradeUtils.doColumnsOrTablesExist(tableColumnCheckResults);
            assertTrue(((TableColumnCheckResult)tableColumnCheckResults.get(0)).isExists());

            tableName_issueType = "fieldconfigschemeissuetype";
            tableColumnCheckResults = new ArrayList();
            tableColumnCheckResults.add(new TableColumnCheckResult(tableName_issueType, "ISSUETYPE"));
            UpgradeUtils.doColumnsOrTablesExist(tableColumnCheckResults);
            assertTrue(((TableColumnCheckResult)tableColumnCheckResults.get(0)).isExists());

            tableColumnCheckResults = new ArrayList();
            tableColumnCheckResults.add(new TableColumnCheckResult(tableName_issueType, "PNAME"));
            UpgradeUtils.doColumnsOrTablesExist(tableColumnCheckResults);
            assertFalse(((TableColumnCheckResult)tableColumnCheckResults.get(0)).isExists());
        }
        catch (Exception e)
        {

        }
    }

    @Test
    public void testGetBuildVersionNumber()
    {
        int buildNumber = UpgradeUtils.getJIRABuildVersionNumber();
        assertEquals(Integer.parseInt(buildUtilsInfo.getCurrentBuildNumber()), buildNumber);
    }

    private void alterColumnOnTable(boolean create)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        StringBuilder sql = new StringBuilder();
        try
        {
            connection = ConnectionFactory.getConnection("defaultDS");
            sql.append("ALTER TABLE ");
            sql.append(QRTZ_JOB_DETAILS_TABLE);
            if (create)
            {
                sql.append(" ADD COLUMN ");
                sql.append(DESCRIPTION_COLUMN_NAME);
                sql.append(" VARCHAR ");
            }
            else
            {
                sql.append(" DROP COLUMN ").append(DESCRIPTION_COLUMN_NAME);
            }
            preparedStatement = connection.prepareStatement(sql.toString());
            preparedStatement.execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
        finally
        {
            if (preparedStatement != null)
            {
                try
                {
                    preparedStatement.close();
                }
                catch (SQLException e)
                {
                    // Oh Well :(
                }
            }
            if (connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (SQLException e)
                {
                    // To late now!
                }
            }
        }

    }

    private void insertIntoTable(String tableName, String columnName, long id, Object value)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        StringBuilder sql = new StringBuilder();
        try
        {
            connection = ConnectionFactory.getConnection("defaultDS");
            sql.append("INSERT INTO ");
            sql.append(tableName);
            sql.append(" ( ");
            sql.append("id, ");
            sql.append(columnName);
            sql.append(" ) ");
            sql.append(" values ");
            sql.append(" ( ");
            sql.append(" ?, ");
            sql.append(" ?");
            sql.append(" ) ");
            preparedStatement = connection.prepareStatement(sql.toString());
            preparedStatement.setLong(1, id);
            preparedStatement.setObject(2, value);
            preparedStatement.execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
        finally
        {
            if (preparedStatement != null)
            {
                try
                {
                    preparedStatement.close();
                }
                catch (SQLException e)
                {
                    // Oh Well :(
                }
            }
            if (connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (SQLException e)
                {
                    // To late now!
                }
            }
        }
    }

    private String getDescColValueWhere(String jobName)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        StringBuilder sql = new StringBuilder();
        try
        {
            connection = ConnectionFactory.getConnection("defaultDS");
            sql.append("SELECT ");
            sql.append(DESCRIPTION_COLUMN_NAME);
            sql.append(" FROM ");
            sql.append(QRTZ_JOB_DETAILS_TABLE);
            sql.append(" WHERE ");
            sql.append(JOB_NAME_COL);
            sql.append(" = ?");
            preparedStatement = connection.prepareStatement(sql.toString());
            preparedStatement.setString(1, jobName);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
            {
                return resultSet.getString(DESCRIPTION_COLUMN_NAME);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
        finally
        {
            if (resultSet != null)
            {
                try
                {
                    resultSet.close();
                }
                catch (SQLException e)
                {
                    // oh well
                }
            }
            if (preparedStatement != null)
            {
                try
                {
                    preparedStatement.close();
                }
                catch (SQLException e)
                {
                    // Oh Well :(
                }
            }
            if (connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (SQLException e)
                {
                    // To late now!
                }
            }
        }
        return null;
    }

}
