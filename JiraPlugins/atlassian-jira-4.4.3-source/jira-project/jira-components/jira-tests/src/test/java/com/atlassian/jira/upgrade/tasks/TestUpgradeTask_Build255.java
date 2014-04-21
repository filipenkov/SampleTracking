package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.issue.ActionConstants;
import com.atlassian.jira.local.LegacyJiraMockTestCase;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.model.ModelEntity;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestUpgradeTask_Build255 extends LegacyJiraMockTestCase
{
    UpgradeTask_Build255 upgradeTask_build255;
    private static final Long ID_123 = new Long(123);
    private static final Long ID_555 = new Long(555);
    private static final Long ID_444 = new Long(444);

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        upgradeTask_build255 = new UpgradeTask_Build255(CoreFactory.getGenericDelegator());
    }

    public void testBuildSequencerInsertSQL()
    {
        final String sequenceInsertSQL = upgradeTask_build255.buildSequencerInsertSQL("TEST_TABLE", "TEST_NAME_COL", "TEST_ID_COL");
        assertEquals("insert into TEST_TABLE (TEST_ID_COL, TEST_NAME_COL) values (?, ?)", sequenceInsertSQL);
    }

    public void testBuildSequencerUpdateSQL()
    {
        final String sequenceInsertSQL = upgradeTask_build255.buildSequencerUpdateSQL("TEST_TABLE", "TEST_NAME_COL", "TEST_ID_COL");
        assertEquals("update TEST_TABLE set TEST_ID_COL = ? where TEST_NAME_COL = ?", sequenceInsertSQL);
    }

    public void testGetModelEntityForName()
    {
        final ModelEntity modelEntity = upgradeTask_build255.getModelEntityForName(UpgradeTask_Build255.WORKLOG_ENTITY_NAME);
        assertNotNull(modelEntity);
        assertEquals(UpgradeTask_Build255.WORKLOG_ENTITY_NAME, modelEntity.getEntityName());
        assertEquals("PUBLIC.worklog", modelEntity.getTableName("defaultDS"));
    }

    public void testGetDbColumnName()
    {
        final ModelEntity modelEntity = upgradeTask_build255.getModelEntityForName(UpgradeTask_Build255.WORKLOG_ENTITY_NAME);
        assertNotNull(modelEntity);
        assertEquals("issueid", upgradeTask_build255.getDbColumnName(modelEntity, "issue"));
    }

    public void testBuildSequencerSQLInsert()
    {
        assertEquals("insert into PUBLIC.SEQUENCE_VALUE_ITEM (SEQ_ID, SEQ_NAME) values (?, ?)", upgradeTask_build255.buildSequencerSQL());
    }

    public void testBuildSequencerSQLUpdate() throws GenericEntityException
    {
        final GenericValue value = CoreFactory.getGenericDelegator().makeValue(UpgradeTask_Build255.SEQUENCE_VALUE_ITEM_ENTITY_NAME,
            EasyMap.build(UpgradeTask_Build255.SEQ_NAME, UpgradeTask_Build255.WORKLOG_ENTITY_NAME, UpgradeTask_Build255.SEQ_ID, new Long(12345)));
        value.create();
        assertEquals("update PUBLIC.SEQUENCE_VALUE_ITEM set SEQ_ID = ? where SEQ_NAME = ?", upgradeTask_build255.buildSequencerSQL());
    }

    public void testBuildConversionSQL()
    {
        assertEquals(
            "insert into PUBLIC.worklog (ID, issueid, AUTHOR, grouplevel, rolelevel, worklogbody, CREATED, UPDATEAUTHOR, UPDATED, STARTDATE, timeworked) select ID, issueid, AUTHOR, actionlevel, rolelevel, actionbody, CREATED, AUTHOR, CREATED, CREATED, actionnum from PUBLIC.jiraaction where actiontype = ?",
            upgradeTask_build255.buildConversionSQL());
    }

    public void testGetWorklogIdMax() throws GenericEntityException
    {
        final GenericValue value = CoreFactory.getGenericDelegator().makeValue(UpgradeTask_Build255.WORKLOG_ENTITY_NAME,
            EasyMap.build("id", new Long(10000)));
        value.create();
        final GenericValue value2 = CoreFactory.getGenericDelegator().makeValue(UpgradeTask_Build255.WORKLOG_ENTITY_NAME,
            EasyMap.build("id", new Long(10999)));
        value2.create();
        assertEquals(new Long(10999), upgradeTask_build255.getWorklogIdMax());
    }

    public void testGetWorklogIdMaxNoWorklogs() throws GenericEntityException
    {
        assertNull(upgradeTask_build255.getWorklogIdMax());
    }

    public void testRemoveWorklogsFromActionTable() throws GenericEntityException
    {
        EntityUtils.createValue(UpgradeTask_Build255.ACTION_ENTITY_NAME, EasyMap.build("type", ActionConstants.TYPE_WORKLOG, "issue", new Long(1)));
        EntityUtils.createValue(UpgradeTask_Build255.ACTION_ENTITY_NAME, EasyMap.build("type", ActionConstants.TYPE_WORKLOG, "issue", new Long(2)));
        EntityUtils.createValue(UpgradeTask_Build255.ACTION_ENTITY_NAME, EasyMap.build("type", ActionConstants.TYPE_WORKLOG, "issue", new Long(3)));
        EntityUtils.createValue(UpgradeTask_Build255.ACTION_ENTITY_NAME, EasyMap.build("type", ActionConstants.TYPE_COMMENT, "issue", new Long(4)));
        assertEquals(4, CoreFactory.getGenericDelegator().findAll(UpgradeTask_Build255.ACTION_ENTITY_NAME).size());
        upgradeTask_build255.removeWorklogsFromActionTable();
        assertEquals(1, CoreFactory.getGenericDelegator().findAll(UpgradeTask_Build255.ACTION_ENTITY_NAME).size());
    }

    public void testUpdateWorklogSequence() throws SQLException, GenericEntityException
    {
        upgradeTask_build255 = new UpgradeTask_Build255(CoreFactory.getGenericDelegator())
        {
            @Override
            Long getWorklogIdMax() throws GenericEntityException
            {
                return new Long(12345);
            }
        };

        upgradeTask_build255.updateWorklogSequence();

        final List worklogSequenceEntity = CoreFactory.getGenericDelegator().findByAnd(UpgradeTask_Build255.SEQUENCE_VALUE_ITEM_ENTITY_NAME,
            EasyMap.build(UpgradeTask_Build255.SEQ_NAME, UpgradeTask_Build255.WORKLOG_ENTITY_NAME));
        assertNotNull(worklogSequenceEntity);
        assertEquals(1, worklogSequenceEntity.size());
        final GenericValue seqEntity = (GenericValue) worklogSequenceEntity.get(0);
        assertEquals(new Long(12345 + UpgradeTask_Build255.SEQ_INCREMENT_VALUE), seqEntity.getLong(UpgradeTask_Build255.SEQ_ID));
    }

    public void testUpdateWorklogSequenceNullMaxId() throws SQLException, GenericEntityException
    {
        upgradeTask_build255 = new UpgradeTask_Build255(CoreFactory.getGenericDelegator())
        {
            @Override
            Long getWorklogIdMax() throws GenericEntityException
            {
                return null;
            }
        };

        upgradeTask_build255.updateWorklogSequence();

        final List worklogSequenceEntity = CoreFactory.getGenericDelegator().findByAnd(UpgradeTask_Build255.SEQUENCE_VALUE_ITEM_ENTITY_NAME,
            EasyMap.build(UpgradeTask_Build255.SEQ_NAME, UpgradeTask_Build255.WORKLOG_ENTITY_NAME));
        assertNotNull(worklogSequenceEntity);
        assertEquals(0, worklogSequenceEntity.size());
    }

    public void testCopyFromActionToWorklog() throws GenericEntityException
    {
        final Map genericAttributes = new HashMap();
        genericAttributes.put("issue", new Long(1));
        genericAttributes.put("author", "testauthor");
        genericAttributes.put("type", ActionConstants.TYPE_WORKLOG);
        genericAttributes.put("level", "testlevel");
        genericAttributes.put("rolelevel", ID_123);
        genericAttributes.put("body", "testbody");
        genericAttributes.put("created", new Timestamp(System.currentTimeMillis()));
        genericAttributes.put("numvalue", new Long(12345));

        final GenericValue value = EntityUtils.createValue(UpgradeTask_Build255.ACTION_ENTITY_NAME, genericAttributes);
        EntityUtils.createValue(UpgradeTask_Build255.ACTION_ENTITY_NAME, EasyMap.build("type", ActionConstants.TYPE_WORKLOG, "issue", new Long(2)));
        EntityUtils.createValue(UpgradeTask_Build255.ACTION_ENTITY_NAME, EasyMap.build("type", ActionConstants.TYPE_WORKLOG, "issue", new Long(3)));
        EntityUtils.createValue(UpgradeTask_Build255.ACTION_ENTITY_NAME, EasyMap.build("type", ActionConstants.TYPE_COMMENT, "issue", new Long(4)));
        assertEquals(4, CoreFactory.getGenericDelegator().findAll(UpgradeTask_Build255.ACTION_ENTITY_NAME).size());
        upgradeTask_build255.copyFromActionToWorklog();

        final List all = CoreFactory.getGenericDelegator().findAll(UpgradeTask_Build255.WORKLOG_ENTITY_NAME);
        assertEquals(3, all.size());
        boolean matchingGVFound = false;
        for (final Iterator iterator = all.iterator(); iterator.hasNext();)
        {
            final GenericValue gv = (GenericValue) iterator.next();
            if (value.getLong("id").equals(gv.getLong("id")))
            {
                assertEquals(genericAttributes.get("issue"), gv.getLong("issue"));
                assertEquals(genericAttributes.get("author"), gv.getString("author"));
                assertEquals(genericAttributes.get("level"), gv.getString("grouplevel"));
                assertEquals(genericAttributes.get("rolelevel"), gv.getLong("rolelevel"));
                assertEquals(genericAttributes.get("body"), gv.getString("body"));
                assertEquals(genericAttributes.get("created"), gv.getTimestamp("created"));
                assertEquals(genericAttributes.get("author"), gv.getString("updateauthor"));
                assertEquals(genericAttributes.get("created"), gv.getTimestamp("updated"));
                assertEquals(genericAttributes.get("created"), gv.getTimestamp("startdate"));
                assertEquals(genericAttributes.get("numvalue"), gv.getLong("timeworked"));
                matchingGVFound = true;
            }
        }
        assertTrue("Didn't find test generic value in Worklog", matchingGVFound);
    }

    public void testCleanWorklogTableIfNeededWorklogHasAndActionHas() throws GenericEntityException
    {
        EntityUtils.createValue(UpgradeTask_Build255.ACTION_ENTITY_NAME, EasyMap.build("type", ActionConstants.TYPE_WORKLOG, "issue", new Long(1),
            "id", ID_123));
        EntityUtils.createValue(UpgradeTask_Build255.WORKLOG_ENTITY_NAME, EasyMap.build("issue", new Long(1), "id", ID_123));

        final AtomicBoolean deleteWasCalled = new AtomicBoolean(false);
        upgradeTask_build255 = new UpgradeTask_Build255(CoreFactory.getGenericDelegator())
        {
            @Override
            void deleteFromWorklog(final Collection collection) throws GenericEntityException
            {
                deleteWasCalled.set(true);
                assertTrue(collection.contains(ID_123));
            }
        };

        upgradeTask_build255.cleanWorklogTableIfNeeded();
        assertTrue(deleteWasCalled.get());
    }

    public void testCleanWorklogTableIfNeededWorklogHasAndActionHasAndIdsIntersect() throws GenericEntityException
    {
        EntityUtils.createValue(UpgradeTask_Build255.ACTION_ENTITY_NAME, EasyMap.build("type", ActionConstants.TYPE_WORKLOG, "issue", new Long(1),
            "id", ID_123));
        EntityUtils.createValue(UpgradeTask_Build255.WORKLOG_ENTITY_NAME, EasyMap.build("issue", new Long(1), "id", ID_123));
        EntityUtils.createValue(UpgradeTask_Build255.ACTION_ENTITY_NAME, EasyMap.build("type", ActionConstants.TYPE_WORKLOG, "issue", new Long(1),
            "id", ID_555));
        EntityUtils.createValue(UpgradeTask_Build255.ACTION_ENTITY_NAME, EasyMap.build("type", ActionConstants.TYPE_COMMENT, "issue", new Long(1),
            "id", ID_444));
        EntityUtils.createValue(UpgradeTask_Build255.WORKLOG_ENTITY_NAME, EasyMap.build("issue", new Long(1), "id", ID_444));

        final AtomicBoolean deleteWasCalled = new AtomicBoolean(false);
        upgradeTask_build255 = new UpgradeTask_Build255(CoreFactory.getGenericDelegator())
        {
            @Override
            void deleteFromWorklog(final Collection collection) throws GenericEntityException
            {
                deleteWasCalled.set(true);
                assertTrue(collection.contains(ID_123));
                assertFalse(collection.contains(ID_555));
                assertFalse(collection.contains(ID_444));
                assertEquals(1, collection.size());
            }
        };

        upgradeTask_build255.cleanWorklogTableIfNeeded();
        assertTrue(deleteWasCalled.get());
    }

    public void testCleanWorklogTableIfNeededWorklogHasNotAndActionHas() throws GenericEntityException
    {
        EntityUtils.createValue(UpgradeTask_Build255.ACTION_ENTITY_NAME, EasyMap.build("type", ActionConstants.TYPE_WORKLOG, "issue", new Long(1)));

        final AtomicBoolean deleteWasCalled = new AtomicBoolean(false);
        upgradeTask_build255 = new UpgradeTask_Build255(CoreFactory.getGenericDelegator())
        {
            @Override
            void deleteFromWorklog(final Collection collection) throws GenericEntityException
            {
                deleteWasCalled.set(true);
            }
        };

        upgradeTask_build255.cleanWorklogTableIfNeeded();
        assertFalse(deleteWasCalled.get());
    }

    public void testCleanWorklogTableIfNeededWorklogHasNotAndActionHasNot() throws GenericEntityException
    {
        EntityUtils.createValue(UpgradeTask_Build255.ACTION_ENTITY_NAME, EasyMap.build("type", ActionConstants.TYPE_COMMENT, "issue", new Long(1)));

        final AtomicBoolean deleteWasCalled = new AtomicBoolean(false);
        upgradeTask_build255 = new UpgradeTask_Build255(CoreFactory.getGenericDelegator())
        {
            @Override
            void deleteFromWorklog(final Collection collection) throws GenericEntityException
            {
                deleteWasCalled.set(true);
            }
        };

        upgradeTask_build255.cleanWorklogTableIfNeeded();
        assertFalse(deleteWasCalled.get());
    }

    public void testDoUpgradeWithDataInBothTables() throws Exception
    {
        final GenericValue oldValue = EntityUtils.createValue(UpgradeTask_Build255.ACTION_ENTITY_NAME, EasyMap.build("type",
            ActionConstants.TYPE_WORKLOG, "issue", new Long(1)));
        EntityUtils.createValue(UpgradeTask_Build255.WORKLOG_ENTITY_NAME, EasyMap.build("issue", new Long(2)));

        upgradeTask_build255.doUpgrade(false);

        // Make sure the Action table was cleaned
        final List allActionEntities = CoreFactory.getGenericDelegator().findAll(UpgradeTask_Build255.ACTION_ENTITY_NAME);
        assertNotNull(allActionEntities);
        assertEquals(0, allActionEntities.size());

        // Get the stuff in the Worklog table and make sure it is right
        final List allWorklogEntities = CoreFactory.getGenericDelegator().findAll(UpgradeTask_Build255.WORKLOG_ENTITY_NAME);
        assertNotNull(allWorklogEntities);
        assertEquals(1, allWorklogEntities.size());
        assertEquals(oldValue.getLong("issue"), ((GenericValue) allWorklogEntities.get(0)).getLong("issue"));
    }

}
