package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.workflow.WorkflowTransitionUtil;

/**
 * @since v4.0
 */
public class TestUpgradeTask_Build132 extends MockControllerTestCase
{
    @Test
    public void testCreateWorkFlowScreenKeysExist() throws Exception
    {
        final I18nHelper i18nHelper = (I18nHelper)mockController.getMock(I18nHelper.class);
        i18nHelper.getText("admin.field.screens.workflow.name");
        mockController.setReturnValue("Workflow Screen");
        i18nHelper.getText("admin.field.screens.workflow.description");
        mockController.setReturnValue("This screen is used in the workflow and enables you to assign issues.");

        final FieldScreen fieldScreen = (FieldScreen) mockController.getMock(FieldScreen.class);
        fieldScreen.getName();
        mockController.setReturnValue("Assign Issue Screen");
        fieldScreen.setName("Workflow Screen");        
        fieldScreen.setDescription("This screen is used in the workflow and enables you to assign issues.");
        fieldScreen.store();

        final FieldScreenManager fieldScreenManager = (FieldScreenManager) mockController.getMock(FieldScreenManager.class);
        fieldScreenManager.getFieldScreen(WorkflowTransitionUtil.VIEW_COMMENTASSIGN_ID);
        mockController.setReturnValue(fieldScreen);
        mockController.replay();

        com.atlassian.jira.upgrade.tasks.UpgradeTask_Build132 upgrade = new com.atlassian.jira.upgrade.tasks.UpgradeTask_Build132(fieldScreenManager)
        {
            I18nHelper getApplicationI18n()
            {
                return i18nHelper;
            }
        };

        upgrade.doUpgrade(false);

        mockController.verify();

    }

    @Test
    public void testCreateWorkFlowScreenKeysDontExist() throws Exception
    {
        final I18nHelper i18nHelper = (I18nHelper)mockController.getMock(I18nHelper.class);
        i18nHelper.getText("admin.field.screens.workflow.name");
        mockController.setReturnValue("admin.field.screens.workflow.name");
        i18nHelper.getText("admin.field.screens.workflow.description");
        mockController.setReturnValue("admin.field.screens.workflow.description");

        final FieldScreen fieldScreen = (FieldScreen) mockController.getMock(FieldScreen.class);
        fieldScreen.getName();
        mockController.setReturnValue("Assign Issue Screen");
        fieldScreen.setName("Workflow Screen");
        fieldScreen.setDescription("This screen is used in the workflow and enables you to assign issues");
        fieldScreen.store();

        final FieldScreenManager fieldScreenManager = (FieldScreenManager) mockController.getMock(FieldScreenManager.class);
        fieldScreenManager.getFieldScreen(WorkflowTransitionUtil.VIEW_COMMENTASSIGN_ID);
        mockController.setReturnValue(fieldScreen);
        mockController.replay();

        com.atlassian.jira.upgrade.tasks.UpgradeTask_Build132 upgrade = new com.atlassian.jira.upgrade.tasks.UpgradeTask_Build132(fieldScreenManager)
        {
            I18nHelper getApplicationI18n()
            {
                return i18nHelper;
            }
        };

        upgrade.doUpgrade(false);

        mockController.verify();

    }
}
