/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.config;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.resolution.ResolutionImpl;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.StoreException;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class TestDefaultConstantsManager extends LegacyJiraMockTestCase
{
    private GenericValue subTaskIssueType1;
    private GenericValue subTaskIssueType2;
    private GenericValue issueType;
    private GenericValue issueType2;
    private GenericValue issueType3;
    private DefaultConstantsManager defaultConstantsManager;
    MockOfBizDelegator mockOfBizDelegator = new MockOfBizDelegator();

    public TestDefaultConstantsManager(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        defaultConstantsManager = new DefaultConstantsManager(null, null, mockOfBizDelegator, null);
    }

    public void testGetPriorities()
    {
        GenericValue priority = UtilsForTests.getTestEntity("Priority", EasyMap.build("id", "1", "name", "High"));
        GenericValue priority2 = UtilsForTests.getTestEntity("Priority", EasyMap.build("id", "2", "name", "Low"));

        assertEquals(2, ManagerFactory.getConstantsManager().getPriorities().size());
        assertTrue(ManagerFactory.getConstantsManager().getPriorities().contains(priority));
        assertTrue(ManagerFactory.getConstantsManager().getPriorities().contains(priority2));
    }

    public void testRefreshPriorities()
    {
        testGetPriorities();

        GenericValue priority3 = UtilsForTests.getTestEntity("Priority", EasyMap.build("id", "3", "name", "High"));

        assertEquals(2, ManagerFactory.getConstantsManager().getPriorities().size());

        ManagerFactory.getConstantsManager().refreshPriorities();

        assertEquals(3, ManagerFactory.getConstantsManager().getPriorities().size());
        assertTrue(ManagerFactory.getConstantsManager().getPriorities().contains(priority3));
    }

    public void testGetPriority()
    {
        GenericValue priority = UtilsForTests.getTestEntity("Priority", EasyMap.build("id", "1", "name", "High"));

        assertEquals(priority, ManagerFactory.getConstantsManager().getPriority("1"));
    }

    public void testGetResolutions()
    {
        GenericValue resolution = UtilsForTests.getTestEntity("Resolution", EasyMap.build("id", "1", "name", "High"));
        GenericValue resolution2 = UtilsForTests.getTestEntity("Resolution", EasyMap.build("id", "2", "name", "Low"));

        assertEquals(2, ManagerFactory.getConstantsManager().getResolutions().size());
        assertTrue(ManagerFactory.getConstantsManager().getResolutions().contains(resolution));
        assertTrue(ManagerFactory.getConstantsManager().getResolutions().contains(resolution2));
    }

    public void testRefreshResolutions()
    {
        testGetResolutions();

        GenericValue resolution3 = UtilsForTests.getTestEntity("Resolution", EasyMap.build("id", "3", "name", "High"));

        assertEquals(2, ManagerFactory.getConstantsManager().getResolutions().size());

        ManagerFactory.getConstantsManager().refreshResolutions();

        assertEquals(3, ManagerFactory.getConstantsManager().getResolutions().size());
        assertTrue(ManagerFactory.getConstantsManager().getResolutions().contains(resolution3));
    }

    public void testGetResolution()
    {
        GenericValue resolution = UtilsForTests.getTestEntity("Resolution", EasyMap.build("id", "1", "name", "High"));

        assertEquals(resolution, ManagerFactory.getConstantsManager().getResolution("1"));
    }

    public void testGetConstantsByNameIgnoresCase()
    {
        GenericValue gv = UtilsForTests.getTestEntity("Resolution", EasyMap.build("id", "1", "name", "High"));
        Resolution resolution = new ResolutionImpl(gv, null, null);

        assertEquals(resolution, ManagerFactory.getConstantsManager().getConstantByNameIgnoreCase("Resolution", "hiGH"));
    }
    
    public void testGetIssueTypes()
    {
        GenericValue issueType = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "1", "name", "High"));
        GenericValue issueType2 = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "2", "name", "Low"));

        assertEquals(2, ManagerFactory.getConstantsManager().getIssueTypes().size());
        assertTrue(ManagerFactory.getConstantsManager().getIssueTypes().contains(issueType));
        assertTrue(ManagerFactory.getConstantsManager().getIssueTypes().contains(issueType2));
    }

    public void testRefreshIssueTypes()
    {
        testGetIssueTypes();

        GenericValue issueType3 = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "3", "name", "High"));

        assertEquals(2, ManagerFactory.getConstantsManager().getIssueTypes().size());

        ManagerFactory.getConstantsManager().refreshIssueTypes();

        assertEquals(3, ManagerFactory.getConstantsManager().getIssueTypes().size());
        assertTrue(ManagerFactory.getConstantsManager().getIssueTypes().contains(issueType3));
    }

    public void testGetIssueType()
    {
        GenericValue issueType = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "1", "name", "High"));

        assertEquals(issueType, ManagerFactory.getConstantsManager().getIssueType("1"));
    }

    public void testCreateIssueType() throws CreateException, GenericEntityException
    {
        String name = "issue type name";
        Long sequence = new Long(1);
        String description = "issue type description";
        String iconurl = "issue type icon url";
        String style = "issue type style";

        final DefaultConstantsManager defaultConstantsManager = new DefaultConstantsManager(null, null, new MockOfBizDelegator(), null);
        defaultConstantsManager.createIssueType(name, sequence, style, description, iconurl);

        final List issueTypes = CoreFactory.getGenericDelegator().findByAnd("IssueType", EasyMap.build("name", name, "description", description, "iconurl", iconurl, "style", style));
        assertNotNull(issueTypes);
        assertEquals(1, issueTypes.size());
    }

    public void testStoreIssueTypes() throws StoreException
    {
        setupIssueTypes();

        final List list = EasyList.build(issueType, issueType2, issueType3);

        final String name = "Very High";
        final String name2 = "Very Medium";
        final String name3 = "Very Low";
        issueType.set("name", name);
        issueType2.set("name", name2);
        issueType3.set("name", name3);

        final DefaultConstantsManager defaultConstantsManager = new DefaultConstantsManager(null, null, new MockOfBizDelegator(), null);
        defaultConstantsManager.storeIssueTypes(list);

        final Collection subTaskIssueTypes = defaultConstantsManager.getIssueTypes();
        assertEquals(3, subTaskIssueTypes.size());
        final Iterator iterator = subTaskIssueTypes.iterator();
        GenericValue subTaskIssueType = (GenericValue) iterator.next();
        assertEquals(name, subTaskIssueType.getString("name"));
        subTaskIssueType = (GenericValue) iterator.next();
        assertEquals(name3, subTaskIssueType.getString("name"));
        subTaskIssueType = (GenericValue) iterator.next();
        assertEquals(name2, subTaskIssueType.getString("name"));
    }

    private void setupIssueTypes()
    {
        int i = 0;
        issueType = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "1", "name", "High", "sequence", new Long(i++)));
        mockOfBizDelegator.store(issueType);
        issueType2 = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "2", "name", "Medium", "sequence", new Long(i++)));
        mockOfBizDelegator.store(issueType2);
        issueType3 = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "3", "name", "Low", "sequence", new Long(i++)));
        mockOfBizDelegator.store(issueType3);
    }

    public void testUpdateIssueTypeNullId() throws StoreException, GenericEntityException
    {
        String id = null;
        final DefaultConstantsManager defaultConstantsManager = new DefaultConstantsManager(null, null, null, null);
        try
        {
            defaultConstantsManager.updateIssueType(id, null, null, null, null, null);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch(IllegalArgumentException e)
        {
            assertEquals("Id cannot be null.", e.getMessage());
        }
    }

    public void testUpdateIssueTypeIssueTypeDoesNotExist() throws StoreException, GenericEntityException
    {
        String id = "1";
        final DefaultConstantsManager defaultConstantsManager = new DefaultConstantsManager(null, null, new MockOfBizDelegator(), null);
        try
        {
            defaultConstantsManager.updateIssueType(id, null, null, null, null, null);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch(IllegalArgumentException e)
        {
            assertEquals("Issue Type with id '" + id + "' does not exist.", e.getMessage());
        }
    }

    public void testUpdateIssueType() throws StoreException, GenericEntityException
    {
        String id = "1";
        String name = "issue type name";
        Long sequence = new Long(1);
        String description = "issue type description";
        String iconurl = "issue type icon url";
        String style = "issue type style";

        // Create issue type in the ofBizDelegator with null values
        MockOfBizDelegator ofBizDelegator = new MockOfBizDelegator();
        ofBizDelegator.createValue("IssueType", EasyMap.build("id", id, "name", null, "sequence", null, "description", null, "iconurl", null, "style", null));
        final DefaultConstantsManager defaultConstantsManager = new DefaultConstantsManager(null, null, ofBizDelegator, null);
        // update the issue via the ConstantsManager
        defaultConstantsManager.updateIssueType(id, name, sequence, style, description, iconurl);

        final List issueTypes = ofBizDelegator.findByAnd("IssueType", EasyMap.build("id", id));

        assertNotNull(issueTypes);
        assertEquals(1, issueTypes.size());
        GenericValue issueTypeGV = (GenericValue) issueTypes.get(0);
        assertEquals(name, issueTypeGV.getString("name"));
        assertEquals(sequence, issueTypeGV.getLong("sequence"));
        assertEquals(description, issueTypeGV.getString("description"));
        assertEquals(iconurl, issueTypeGV.getString("iconurl"));
        assertEquals(style, issueTypeGV.getString("style"));
    }

    public void testGetSubTaskIssueTypes()
    {
        setupSubTaskIssueTypes();
        MockOfBizDelegator ofBizDelegator = new MockOfBizDelegator();
        ofBizDelegator.setGenericValues(CollectionBuilder.list(subTaskIssueType1, subTaskIssueType2));

        final DefaultConstantsManager defaultConstantsManager = new DefaultConstantsManager(null, null, ofBizDelegator, null);
        final Collection result = defaultConstantsManager.getSubTaskIssueTypes();
        assertNotNull(result);
        assertEquals(2, result.size());
        final Iterator iterator = result.iterator();
        assertEquals(subTaskIssueType1, iterator.next());
        assertEquals(subTaskIssueType2, iterator.next());
    }

    public void testExpandIssueTypeIds()
    {
        List expectedIds = EasyList.build("1", "2", "3");
        final DefaultConstantsManager defaultConstantsManager = new DefaultConstantsManager(null, null, new MockOfBizDelegator(), null);
        final List result = defaultConstantsManager.expandIssueTypeIds(expectedIds);
        assertEquals(expectedIds, result);
    }

    public void testExpandIssueTypesAllStandardIssueTypes()
    {
        setupIssueTypes();
        setupSubTaskIssueTypes();

        List expectedIssueTypes = EasyList.build(issueType.getString("id"), issueType3.getString("id"), issueType2.getString("id"));
        List result = defaultConstantsManager.expandIssueTypeIds(EasyList.build("1", ConstantsManager.ALL_STANDARD_ISSUE_TYPES));
        assertEquals(expectedIssueTypes, result);
        
        expectedIssueTypes = EasyList.build(subTaskIssueType1.getString("id"), subTaskIssueType2.getString("id"));
        result = defaultConstantsManager.expandIssueTypeIds(EasyList.build("1", ConstantsManager.ALL_SUB_TASK_ISSUE_TYPES));
        assertEquals(expectedIssueTypes, result);
    }

    private void setupSubTaskIssueTypes()
    {
        subTaskIssueType2 = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "20", "name", "b", "sequence", new Long(2), "description", null, "iconurl", "some url", "style", SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE));
        mockOfBizDelegator.store(subTaskIssueType2);
        subTaskIssueType1 = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "10", "name", "a", "sequence", new Long(1), "description", null, "iconurl", "some url", "style", SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE));
        mockOfBizDelegator.store(subTaskIssueType1);
    }
}
