package com.atlassian.jira.workflow;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.collect.MapBuilder;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test the default workflow scheme manager
 */
public class TestDefaultWorkflowSchemeManager extends ListeningTestCase
{
    @Test
    public void testUpdateSchemesForRenamedWorkflow()
    {
        _testUpdateSchemesForRenamedWorkflowWithNullOldName(null, "new", false);
        _testUpdateSchemesForRenamedWorkflowWithNullOldName("", "new", false);
        _testUpdateSchemesForRenamedWorkflowWithNullOldName("old", null, false);
        _testUpdateSchemesForRenamedWorkflowWithNullOldName("old", "", false);
        _testUpdateSchemesForRenamedWorkflowWithNullOldName("old", "new", true);
    }

    public void _testUpdateSchemesForRenamedWorkflowWithNullOldName(final String oldWorkflowName, final String newWorkflowName, final boolean isExecuted)
    {
        final AtomicBoolean isClearCacheCalled = new AtomicBoolean(false);

        final Mock mockOfBizDelegator = new Mock(OfBizDelegator.class);
        mockOfBizDelegator.setStrict(true);
        final WorkflowSchemeManager workflowSchemeManager = new DefaultWorkflowSchemeManager(null, null, null, null, null, null,
            (OfBizDelegator) mockOfBizDelegator.proxy(), null, null, null)
        {
            @Override
            public void clearWorkflowCache()
            {
                isClearCacheCalled.set(true);
                super.clearWorkflowCache();
            }
        };

        if (isExecuted)
        {
            mockOfBizDelegator.expectAndReturn("bulkUpdateByAnd", P.args(new IsEqual("WorkflowSchemeEntity"), new IsEqual(EasyMap.build("workflow",
                newWorkflowName)), new IsEqual(EasyMap.build("workflow", oldWorkflowName))), new Integer(1));
            workflowSchemeManager.updateSchemesForRenamedWorkflow(oldWorkflowName, newWorkflowName);
        }
        else
        {
            try
            {
                workflowSchemeManager.updateSchemesForRenamedWorkflow(oldWorkflowName, newWorkflowName);
                fail("Expected an exception to be thrown");
            }
            catch (final IllegalArgumentException e)
            {
                assertTrue(e.getMessage().contains("must not be null or empty string"));
            }
        }

        assertEquals(isExecuted, isClearCacheCalled.get());//cache should have been flushed only if executed

        mockOfBizDelegator.verify();
    }

    @Test
    public void testGetWorkflowMap() throws Exception
    {
        final Project project = createProject(100002L, "BJB");
        final GenericValue schemeGv = createSchemeGV(9000L);
        final AtomicLong callCount = new AtomicLong(0);

        final DefaultWorkflowSchemeManager testingClass = new DefaultWorkflowSchemeManager(null, null, null, null, null, null, null, null, null, null)
        {
            @Override
            public List<GenericValue> getEntities(final GenericValue scheme)
                    throws GenericEntityException
            {
                callCount.incrementAndGet();
                assertSame(schemeGv, scheme);
                return createSchemeEntries("one", "two", "three", "four");
            }

            @Override
            public List<GenericValue> getSchemes(final GenericValue projectGV) throws GenericEntityException
            {
                assertSame(project.getGenericValue(), projectGV);

                return Collections.singletonList(schemeGv);
            }
        };

        final Map<String, String> expectedMap = MapBuilder.<String, String>newBuilder().add(null, "one")
                .add("two", "two").add("three", "three").add("four", "four").toMutableMap();

        assertEquals(expectedMap, testingClass.getWorkflowMap(project));
        assertEquals(expectedMap, testingClass.getWorkflowMap(project));

        //Make sure that we use the cache for the second call.
        assertEquals(1, callCount.get());
    }

    @Test
    public void testGetWorkflowNameFromScheme() throws Exception
    {
        final GenericValue schemeGv = createSchemeGV(9000L);
        final AtomicLong callCount = new AtomicLong(0);

        final DefaultWorkflowSchemeManager testingClass = new DefaultWorkflowSchemeManager(null, null, null, null, null, null, null, null, null, null)
        {
            @Override
            public List<GenericValue> getEntities(final GenericValue scheme)
                    throws GenericEntityException
            {
                callCount.incrementAndGet();
                assertSame(schemeGv, scheme);
                return createSchemeEntries("one", "two", "three", "four");
            }
        };

        assertEquals("one", testingClass.getWorkflowName(schemeGv, "0"));
        assertEquals("two", testingClass.getWorkflowName(schemeGv, "two"));
        assertEquals("three", testingClass.getWorkflowName(schemeGv, "three"));
        assertEquals("four", testingClass.getWorkflowName(schemeGv, "four"));
        assertEquals("four", testingClass.getWorkflowName(schemeGv, "four"));

        //Make sure that we use the cache for the second call.
        assertEquals(1, callCount.get());
    }

    @Test
    public void testGetWorkflowNameFromProject() throws Exception
    {
        final Project project = createProject(100002L, "BJB");
        final GenericValue schemeGv = createSchemeGV(9000L);

        final DefaultWorkflowSchemeManager testingClass = new DefaultWorkflowSchemeManager(null, null, null, null, null, null, null, null, null, null)
        {
            @Override
            public String getWorkflowName(final GenericValue scheme, final String issueType)
            {
                assertSame(schemeGv, scheme);
                return issueType;
            }

            @Override
            public List<GenericValue> getSchemes(final GenericValue projectGV) throws GenericEntityException
            {
                assertSame(project.getGenericValue(), projectGV);

                return Collections.singletonList(schemeGv);
            }
        };

        assertEquals("0", testingClass.getWorkflowName(project, "0"));
        assertEquals("two", testingClass.getWorkflowName(schemeGv, "two"));
        assertEquals("three", testingClass.getWorkflowName(schemeGv, "three"));
        assertEquals("four", testingClass.getWorkflowName(schemeGv, "four"));
        assertEquals("four", testingClass.getWorkflowName(schemeGv, "four"));
    }

    public void testIsUsingDefaultWorkflowUsingDefault()
    {
        final Project project = createProject(100002L, "BJB");
        final GenericValue schemeGv = createSchemeGV(9000L);

        final DefaultWorkflowSchemeManager testingClass = new DefaultWorkflowSchemeManager(null, null, null, null, null, null, null, null, null, null)
        {
            @Override
            public List<GenericValue> getSchemes(final GenericValue projectGV)
            {
                assertSame(project.getGenericValue(), projectGV);

                return Collections.singletonList(schemeGv);
            }
        };

        assertFalse(testingClass.isUsingDefaultScheme(project));
    }

    public void testIsUsingDefaultWorkflowNotUsingDefault()
    {
        final Project project = createProject(100002L, "BJB");

        final DefaultWorkflowSchemeManager testingClass = new DefaultWorkflowSchemeManager(null, null, null, null, null, null, null, null, null, null)
        {
            @Override
            public List<GenericValue> getSchemes(final GenericValue projectGV)
            {
                assertSame(project.getGenericValue(), projectGV);

                return Collections.emptyList();
            }
        };

        assertTrue(testingClass.isUsingDefaultScheme(project));
    }
    
    private static GenericValue createSchemeGV(final long id)
    {
        final GenericValue schemeGv = new MockGenericValue("WorkflowScheme");
        schemeGv.set("id", id);
        return schemeGv;
    }

    private static List<GenericValue> createSchemeEntries(final String defaultWorkflow, String ... args)
    {
        List<GenericValue> entries = new ArrayList<GenericValue>(args.length / 2 + 1);
        if (defaultWorkflow != null)
        {
            MockGenericValue defaultValue = new MockGenericValue("sjsjs");
            defaultValue.set("workflow", defaultWorkflow);
            defaultValue.set("issuetype", "0");
            entries.add(defaultValue);
        }

        for (String arg : args)
        {
            MockGenericValue value = new MockGenericValue("sjsjs");
            value.set("workflow", arg);
            value.set("issuetype", arg);

            entries.add(value);
        }
        return entries;
    }

    private static Project createProject(long id, String name)
    {
        final GenericValue projectGv = new MockGenericValue("Project");
        projectGv.set("id", id);
        return new MockProject(id, name, name, projectGv);
    }
}
