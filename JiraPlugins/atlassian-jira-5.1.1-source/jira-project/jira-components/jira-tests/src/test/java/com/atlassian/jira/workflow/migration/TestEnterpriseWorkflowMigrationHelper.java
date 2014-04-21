package com.atlassian.jira.workflow.migration;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.SchemeManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;

/**
 * @since v5.1
 */
@RunWith(MockitoJUnitRunner.class)
public class TestEnterpriseWorkflowMigrationHelper
{
    @Mock
    private SchemeManager expectedSchemeManager;

    @Mock
    private OfBizDelegator delegator;

    @Mock
    private ConstantsManager constantsManager;

    @Mock
    private OfBizListIterator listIterator;

    private final Project expectedProject = new MockProject(123456789L, "KEY", "Name");
    private final GenericValue expectedScheme = new MockGenericValue("WorkflowScheme");

    @Before
    public void setup()
    {
        Mockito.stub(constantsManager.getAllIssueTypes()).toReturn(Collections.<GenericValue>emptyList());
    }

    @Test
    public void testCannotQuickMigrate() throws GenericEntityException
    {
        WorkflowMigrationHelper migrationHelper = new WorkflowMigrationHelper(expectedProject.getGenericValue(), expectedScheme, null, delegator, null, null, null, constantsManager, null, null)
        {
            @Override
            public boolean isHaveIssuesToMigrate()
            {
                return true;
            }

            @Override
            public void associateProjectAndWorkflowScheme(SchemeManager schemeManager, GenericValue project,
                    GenericValue scheme)
            {
                throw new RuntimeException("this shouldn't be called!");
            }
        };
        Assert.assertFalse(migrationHelper.doQuickMigrate());
    }

    @Test
    public void testCanQuickMigrate() throws GenericEntityException
    {
        WorkflowMigrationHelper migrationHelper = new WorkflowMigrationHelper(expectedProject.getGenericValue(), expectedScheme, null, delegator, expectedSchemeManager, null, null, constantsManager, null, null)
        {
            @Override
            public boolean isHaveIssuesToMigrate()
            {
                return false;
            }

            @Override
            public void associateProjectAndWorkflowScheme(SchemeManager schemeManager, GenericValue project,
                    GenericValue scheme)
            {
                Assert.assertEquals(expectedProject.getGenericValue(), project);
                Assert.assertEquals(expectedScheme, scheme);
                Assert.assertEquals(expectedSchemeManager, schemeManager);
            }
        };
        Assert.assertTrue(migrationHelper.doQuickMigrate());
    }

    @Test
    public void testHasIssuesToMigrate() throws GenericEntityException
    {
        ArgumentCaptor<EntityFieldMap> captor = ArgumentCaptor.forClass(EntityFieldMap.class);
        Mockito.stub(listIterator.next()).toReturn(new MockGenericValue(""));
        Mockito.stub(delegator.findListIteratorByCondition(Mockito.eq("Issue"), captor.capture())).toReturn(listIterator);
        WorkflowMigrationHelper migrationHelper = new WorkflowMigrationHelper(expectedProject.getGenericValue(), expectedScheme, null, delegator, null, null, null, constantsManager, null, null);
        Assert.assertTrue(migrationHelper.isHaveIssuesToMigrate());
        EntityFieldMap entityFieldMap = captor.getValue();
        Assert.assertEquals(expectedProject.getId(), entityFieldMap.getField("project"));
        Mockito.verify(listIterator, Mockito.times(1)).close();
    }

    @Test
    public void testHasNoIssuesToMigrate() throws GenericEntityException
    {
        ArgumentCaptor<EntityFieldMap> captor = ArgumentCaptor.forClass(EntityFieldMap.class);
        Mockito.stub(listIterator.next()).toReturn(null);
        Mockito.stub(delegator.findListIteratorByCondition(Mockito.eq("Issue"), captor.capture())).toReturn(listIterator);
        WorkflowMigrationHelper migrationHelper = new WorkflowMigrationHelper(expectedProject.getGenericValue(), expectedScheme, null, delegator, null, null, null, constantsManager, null, null);
        Assert.assertFalse(migrationHelper.isHaveIssuesToMigrate());
        EntityFieldMap entityFieldMap = captor.getValue();
        Assert.assertEquals(expectedProject.getId(), entityFieldMap.getField("project"));
        Mockito.verify(listIterator, Mockito.times(1)).close();
    }
}
