/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.upgrade.tasks;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.condition.SubTaskBlockingCondition;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.workflow.loader.*;
import com.atlassian.jira.mock.workflow.MockJiraWorkflow;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

public class TestUpgradeTask_Build155 extends ListeningTestCase
{
    private static final int INDENT = 3;

    @Test
    public void testUpgradeConditionDescriptor()
    {
        UpgradeTask_Build155 ut = new UpgradeTask_Build155(null);
        assertFalse(ut.upgradeConditionDescriptor(null, false));

        ConditionDescriptor conditionDescriptor = DescriptorFactory.getFactory().createConditionDescriptor();
        assertFalse(ut.upgradeConditionDescriptor(conditionDescriptor, false));

        conditionDescriptor = DescriptorFactory.getFactory().createConditionDescriptor();
        conditionDescriptor.setType("sometype");
        assertFalse(ut.upgradeConditionDescriptor(conditionDescriptor, false));

        conditionDescriptor = DescriptorFactory.getFactory().createConditionDescriptor();
        conditionDescriptor.setType("class");
        assertFalse(ut.upgradeConditionDescriptor(conditionDescriptor, false));

        _testUpgradeConditionDescriptor(false, EasyMap.build("somearg", "withvalue"), EasyMap.build("somearg", "withvalue"));

        _testUpgradeConditionDescriptor(false, EasyMap.build("class.name", SubTaskBlockingCondition.class.getName()), EasyMap.build("class.name", SubTaskBlockingCondition.class.getName()));

        _testUpgradeConditionDescriptor(false, EasyMap.build("class.name", SubTaskBlockingCondition.class.getName(), "statuses", ""), EasyMap.build("class.name", SubTaskBlockingCondition.class.getName(), "statuses", ""));

        _testUpgradeConditionDescriptor(false, EasyMap.build("class.name", SubTaskBlockingCondition.class.getName(), "anotherarg", "somevalue"), EasyMap.build("class.name", SubTaskBlockingCondition.class.getName(), "anotherarg", "somevalue"));

        _testUpgradeConditionDescriptor(false, EasyMap.build("class.name", SubTaskBlockingCondition.class.getName(), "statuses", "5"), EasyMap.build("class.name", SubTaskBlockingCondition.class.getName(), "statuses", "5"));

        _testUpgradeConditionDescriptor(false, EasyMap.build("class.name", SubTaskBlockingCondition.class.getName(), "statuses", "5,6"), EasyMap.build("class.name", SubTaskBlockingCondition.class.getName(), "statuses", "5,6"));

        _testUpgradeConditionDescriptor(true, EasyMap.build("class.name", SubTaskBlockingCondition.class.getName(), "statuses", "5,6,totalcrap"), EasyMap.build("class.name", SubTaskBlockingCondition.class.getName(), "statuses", "5,6"));

        _testUpgradeConditionDescriptor(true, EasyMap.build("class.name", SubTaskBlockingCondition.class.getName(), "statuses", "5,a,4"), EasyMap.build("class.name", SubTaskBlockingCondition.class.getName(), "statuses", "5,4"));

        _testUpgradeConditionDescriptor(true, EasyMap.build("class.name", SubTaskBlockingCondition.class.getName(), "statuses", "b,5,a,12,10"), EasyMap.build("class.name", SubTaskBlockingCondition.class.getName(), "statuses", "5,12,10"));

        _testUpgradeConditionDescriptor(true, EasyMap.build("class.name", SubTaskBlockingCondition.class.getName(), "statuses", "3,nested,count,1"), EasyMap.build("class.name", SubTaskBlockingCondition.class.getName(), "statuses", "3,1"));
    }

    private void _testUpgradeConditionDescriptor(boolean retVal, Map originalArgs, Map expectedArgs)
    {
        UpgradeTask_Build155 ut = new UpgradeTask_Build155(null);
        ConditionDescriptor conditionDescriptor = DescriptorFactory.getFactory().createConditionDescriptor();
        conditionDescriptor.setType("class");
        conditionDescriptor.getArgs().putAll(originalArgs);
        assertEquals(retVal, ut.upgradeConditionDescriptor(conditionDescriptor, false));
        assertEquals(expectedArgs, conditionDescriptor.getArgs());
    }

    @Test
    public void testUpgradeConditionsDescriptor()
    {
        UpgradeTask_Build155 ut = new UpgradeTask_Build155(null);
        assertFalse(ut.upgradeConditionsDescriptor(null, false));

        ConditionsDescriptor conditionsDescriptor = DescriptorFactory.getFactory().createConditionsDescriptor();
        assertFalse(ut.upgradeConditionsDescriptor(conditionsDescriptor, false));

        _testUpgradeConditionsDescriptor(EasyMap.build("class.name", SubTaskBlockingCondition.class.getName(), "statuses", ""));
        _testUpgradeConditionsDescriptor(EasyMap.build("class.name", SubTaskBlockingCondition.class.getName(), "statuses", "5,6"));

        // Create first level ConditionsDescriptor
        conditionsDescriptor = DescriptorFactory.getFactory().createConditionsDescriptor();

        // Create Nested ConditionsDescriptor
        ConditionsDescriptor conditionsDescriptor2 = DescriptorFactory.getFactory().createConditionsDescriptor();

        ConditionDescriptor cd1 = DescriptorFactory.getFactory().createConditionDescriptor();
        cd1.setType("class");
        cd1.getArgs().put("class.name", "com.my.SomeCondition");
        cd1.getArgs().put("someArg", "some argument");

        conditionsDescriptor2.getConditions().add(cd1);

        ConditionDescriptor cd2 = DescriptorFactory.getFactory().createConditionDescriptor();
        cd2.setType("class");
        cd2.getArgs().put("class.name", "com.my.SomeOtherCondition");
        cd2.getArgs().put("argname", "some argument value");

        conditionsDescriptor.getConditions().add(conditionsDescriptor2);
        conditionsDescriptor.getConditions().add(cd2);

        assertFalse(ut.upgradeConditionsDescriptor(conditionsDescriptor, false));

        // Create first level ConditionsDescriptor
        conditionsDescriptor = DescriptorFactory.getFactory().createConditionsDescriptor();

        // Create Nested ConditionsDescriptor
        conditionsDescriptor2 = DescriptorFactory.getFactory().createConditionsDescriptor();

        cd1 = DescriptorFactory.getFactory().createConditionDescriptor();
        cd1.setType("class");
        cd1.getArgs().put("class.name", "com.my.SomeCondition");
        cd1.getArgs().put("someArg", "some argument");

        conditionsDescriptor2.getConditions().add(cd1);

        cd2 = DescriptorFactory.getFactory().createConditionDescriptor();
        cd2.setType("class");
        cd2.getArgs().put("class.name", "com.my.SomeOtherCondition");
        cd2.getArgs().put("argname", "some argument value");

        ConditionDescriptor cd3 = DescriptorFactory.getFactory().createConditionDescriptor();
        cd3.setType("class");
        cd3.getArgs().put("class.name", SubTaskBlockingCondition.class.getName());
        cd3.getArgs().put("statuses", "6,7,nested,count");

        conditionsDescriptor.getConditions().add(conditionsDescriptor2);
        conditionsDescriptor.getConditions().add(cd2);
        conditionsDescriptor.getConditions().add(cd3);

        assertTrue(ut.upgradeConditionsDescriptor(conditionsDescriptor, false));
    }

    private void _testUpgradeConditionsDescriptor(Map originalArgs)
    {
        UpgradeTask_Build155 ut = new UpgradeTask_Build155(null);
        ConditionsDescriptor conditionsDescriptor = DescriptorFactory.getFactory().createConditionsDescriptor();
        ConditionDescriptor cd1 = DescriptorFactory.getFactory().createConditionDescriptor();
        cd1.setType("class");
        cd1.getArgs().putAll(originalArgs);
        conditionsDescriptor.setConditions(EasyList.build(cd1));
        assertFalse(ut.upgradeConditionsDescriptor(conditionsDescriptor, false));
    }


    @Test
    public void testSaveWorkflow()
    {
        Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        mockWorkflowManager.expectVoid("saveWorkflowWithoutAudit", P.args(new IsAnything()));

        UpgradeTask_Build155 ut = new UpgradeTask_Build155((WorkflowManager) mockWorkflowManager.proxy());
        Mock mockWokflow = new Mock(JiraWorkflow.class);
        mockWokflow.setStrict(true);

        mockWokflow.expectAndReturn("isSystemWorkflow", Boolean.FALSE);

        ut.saveWorkflow((JiraWorkflow) mockWokflow.proxy());

        mockWokflow.verify();
    }

    @Test
    public void testSaveWorkflowSystemWorkflow()
    {
        UpgradeTask_Build155 ut = new UpgradeTask_Build155(null);
        Mock mockWokflow = new Mock(JiraWorkflow.class);
        mockWokflow.setStrict(true);

        mockWokflow.expectAndReturn("isSystemWorkflow", Boolean.TRUE);
        mockWokflow.expectAndReturn("getName", "Same Workflow");

        ut.saveWorkflow((JiraWorkflow) mockWokflow.proxy());

        mockWokflow.verify();
    }

    @Test
    public void testDoUpgrade() throws Exception
    {
        _testDoUpgrade("com/atlassian/jira/upgrade/tasks/upgradetask_build155/simpleworkflow-broken.xml", "com/atlassian/jira/upgrade/tasks/upgradetask_build155/simpleworkflow-fixed.xml", true);
        _testDoUpgrade("com/atlassian/jira/upgrade/tasks/upgradetask_build155/workflow-broken.xml", "com/atlassian/jira/upgrade/tasks/upgradetask_build155/workflow-fixed.xml", true);
    }

    /**
     * Upgrade tasks should be idempotent.
     */
    @Test
    public void testDoUpgradeWorksWhenCalledMultipleTimes() throws Exception
    {
        _testDoUpgrade("com/atlassian/jira/upgrade/tasks/upgradetask_build155/simpleworkflow-fixed.xml", "com/atlassian/jira/upgrade/tasks/upgradetask_build155/simpleworkflow-fixed.xml", false);
        _testDoUpgrade("com/atlassian/jira/upgrade/tasks/upgradetask_build155/workflow-fixed.xml", "com/atlassian/jira/upgrade/tasks/upgradetask_build155/workflow-fixed.xml", false);
        _testDoUpgrade("com/atlassian/jira/upgrade/tasks/upgradetask_build155/notbroken-worklow.xml", "com/atlassian/jira/upgrade/tasks/upgradetask_build155/notbroken-worklow.xml", false);
    }

    private void _testDoUpgrade(String brokenFile, String fixedFile, boolean needsFix) throws Exception
    {
        Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        mockWorkflowManager.setStrict(true);

        WorkflowManager workflowManager = (WorkflowManager) mockWorkflowManager.proxy();
        JiraWorkflow workflow = new MockJiraWorkflow(workflowManager, brokenFile);
        WorkflowDescriptor brokenWorkflow = workflow.getDescriptor();

        mockWorkflowManager.expectAndReturn("getWorkflows", EasyList.build(workflow));
        mockWorkflowManager.expectAndReturn("isSystemWorkflow", P.args(new IsAnything()), Boolean.FALSE);

        if (needsFix)
        {
            mockWorkflowManager.expectVoid("saveWorkflowWithoutAudit", P.args(new IsAnything()));
        }

        UpgradeTask_Build155 ut = new UpgradeTask_Build155(workflowManager);

        // Run the upgarde task that should fix the workflow
        ut.doUpgrade(false);

        InputStream isExpectedWorkflow = ClassLoaderUtils.getResourceAsStream(fixedFile, getClass());
        WorkflowDescriptor expectedWorkflow;

        try
        {
            expectedWorkflow = WorkflowLoader.load(isExpectedWorkflow, true);
        }
        finally
        {
            if (isExpectedWorkflow != null)
                isExpectedWorkflow.close();
        }

        StringWriter fixedStringWriter = new StringWriter();
        PrintWriter fixedWriter = new PrintWriter(fixedStringWriter);
        brokenWorkflow.writeXML(fixedWriter, INDENT);
        fixedWriter.flush();

        StringWriter expectedStringWriter = new StringWriter();
        PrintWriter expectedWriter = new PrintWriter(expectedStringWriter);
        expectedWorkflow.writeXML(expectedWriter, INDENT);
        expectedWriter.flush();

        assertEquals(expectedStringWriter.toString(), fixedStringWriter.toString());

        mockWorkflowManager.verify();
    }
}
