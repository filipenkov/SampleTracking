package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.collect.MapBuilder;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * 'Unit' test to test the scenario where an issue has more than one changegroups with 'resolution' and 'Resolution'.
 * Had a bug, where this could have resulted in the wrong date being set for the resolution date.
 */
public class TestUpgradeTask_Build401_DB extends LegacyJiraMockTestCase
{
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        UtilsForTests.cleanOFBiz();
    }

    public void testCalculateResolutionDate() throws GenericEntityException, SQLException
    {
        final long currentTime = System.currentTimeMillis();

        final GenericValue issueGV = EntityUtils.createValue("Issue", EasyMap.build("key", "ABC-1", "type", 1L, "created", new Timestamp(
            currentTime - 50000), "resolution", "resolved"));

        //creating a changegroup that should really be last first in the DB, such that when queried it will show
        //up first in the list of results.  If the group-by isn't working right and we get back 2 results for the issue above
        //then the resolutiondate would be set to the wrong date (ie 20s ago, rather than currentTime)
        final GenericValue changeGroup3GV = EntityUtils.createValue("ChangeGroup", EasyMap.build("issue", issueGV.getLong("id"), "author", "admin",
            "created", new Timestamp(currentTime)));
        //a changegroup 20 seconds in the past
        final GenericValue changeGroup1GV = EntityUtils.createValue("ChangeGroup", EasyMap.build("issue", issueGV.getLong("id"), "author", "admin",
            "created", new Timestamp(currentTime - 20000)));
        //10 secs ago
        final GenericValue changeGroup2GV = EntityUtils.createValue("ChangeGroup", EasyMap.build("issue", issueGV.getLong("id"), "author", "admin",
            "created", new Timestamp(currentTime - 10000)));

        Map<String, Object> paramMap = MapBuilder.<String, Object> newBuilder().add("group", changeGroup3GV.getLong("id")).add("fieldtype", "jira").add(
            "field", "Resolution").add("oldvalue", null).add("oldstring", null).add("newvalue", "5").add("newstring", "resolved").toMap();
        EntityUtils.createValue("ChangeItem", paramMap);

        paramMap = MapBuilder.<String, Object> newBuilder().add("group", changeGroup2GV.getLong("id")).add("fieldtype", "jira").add("field",
            "resolution").add("oldvalue", "5").add("oldstring", "resolved").add("newvalue", null).add("newstring", null).toMap();
        EntityUtils.createValue("ChangeItem", paramMap);

        paramMap = MapBuilder.<String, Object> newBuilder().add("group", changeGroup1GV.getLong("id")).add("fieldtype", "jira").add("field",
            "resolution").add("oldvalue", null).add("oldstring", null).add("newvalue", "5").add("newstring", "resolved").toMap();
        EntityUtils.createValue("ChangeItem", paramMap);

        final OfBizDelegator ofBizDelegator = ComponentManager.getComponentInstanceOfType(OfBizDelegator.class);
        final List<GenericValue> originalGVs = ofBizDelegator.findByAnd("Issue", EasyMap.build("key", "ABC-1"));
        assertEquals(1, originalGVs.size());
        //first check we don't have a resolution date.
        assertNull(null, originalGVs.iterator().next().getTimestamp("resolutiondate"));

        final UpgradeTask_Build401 upgradeTask_build401 = new UpgradeTask_Build401(ofBizDelegator, null, null, null, null);
        upgradeTask_build401.calculateResolutionDateForAllIssues();

        final List<GenericValue> gvs = ofBizDelegator.findByAnd("Issue", EasyMap.build("key", "ABC-1"));
        assertEquals(1, gvs.size());
        //then check we have the resolution date and it's set to the correct value!!
        assertEquals(new Timestamp(currentTime), gvs.iterator().next().getTimestamp("resolutiondate"));
    }

}