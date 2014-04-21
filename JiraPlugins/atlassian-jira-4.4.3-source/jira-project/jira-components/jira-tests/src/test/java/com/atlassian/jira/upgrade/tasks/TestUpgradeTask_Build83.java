package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.workflow.WorkflowTransitionUtil;

/**
 * @since v4.0
 */
public class TestUpgradeTask_Build83 extends MockControllerTestCase
{
    @Test
    public void testSetupIssueTypeScreenSchemeKeysExist() throws Exception
    {
        final I18nHelper i18nHelper = (I18nHelper)mockController.getMock(I18nHelper.class);

        i18nHelper.getText("admin.field.screens.default.issue.type.screen.scheme.name");
        mockController.setReturnValue("Default Issue Type Screen Scheme");
        i18nHelper.getText("admin.field.screens.default.issue.type.screen.scheme.description");
        mockController.setReturnValue("The default issue type screen scheme");

        // Create Issue Type Screen Scheme
        final IssueTypeScreenScheme issueTypeScreenScheme = (IssueTypeScreenScheme) mockController.getMock(IssueTypeScreenScheme.class);
        issueTypeScreenScheme.setId(IssueTypeScreenScheme.DEFAULT_SCHEME_ID);
        issueTypeScreenScheme.setName("Default Issue Type Screen Scheme");
        issueTypeScreenScheme.setDescription("The default issue type screen scheme");
        issueTypeScreenScheme.store();

        mockController.replay();
        new MockUpgradeTask_Build83(i18nHelper).setupIssueTypeScreenScheme(issueTypeScreenScheme);
        mockController.verify();
    }

    @Test
    public void testSetupResolveIssueScreenKeyExists() throws Exception
    {
        final I18nHelper i18nHelper = (I18nHelper)mockController.getMock(I18nHelper.class);

        i18nHelper.getText("admin.field.screens.resolve.issue.name");
        mockController.setReturnValue( "Resolve Issue Screen");
        i18nHelper.getText("admin.field.screens.resolve.issue.description");
        mockController.setReturnValue("Allows to set resolution, change fix versions and assign an issue.");

        final FieldScreen resolveFieldScreen = (FieldScreen) mockController.getMock(FieldScreen.class);
        resolveFieldScreen.setId(WorkflowTransitionUtil.VIEW_RESOLVE_ID);
        resolveFieldScreen.setName("Resolve Issue Screen");
        resolveFieldScreen.setDescription("Allows to set resolution, change fix versions and assign an issue.");
        resolveFieldScreen.store();

        mockController.replay();
        new MockUpgradeTask_Build83(i18nHelper).setupResolveIssueScreen(resolveFieldScreen);
        mockController.verify();
    }

    @Test
    public void testSetupDefaultScreenKeyExists() throws Exception
    {
        final I18nHelper i18nHelper = (I18nHelper)mockController.getMock(I18nHelper.class);

        i18nHelper.getText("admin.field.screens.default.name");
        mockController.setReturnValue("Default Screen");
        i18nHelper.getText("admin.field.screens.default.description");
        mockController.setReturnValue("Allows to update all system fields.");



        final FieldScreen defaultFieldScreen = (FieldScreen) mockController.getMock(FieldScreen.class);
        defaultFieldScreen.setId(FieldScreenSchemeManager.DEFAULT_FIELD_SCREEN_SCHEME_ID);
        defaultFieldScreen.setName("Default Screen");
        defaultFieldScreen.setDescription("Allows to update all system fields.");
        defaultFieldScreen.store();

        mockController.replay();
        new MockUpgradeTask_Build83(i18nHelper).setupDefaultFieldScreen(defaultFieldScreen);
        mockController.verify();
    }

    @Test
    public void testSetupDefaultScreenSchemeKeysExist() throws Exception
    {
        final I18nHelper i18nHelper = (I18nHelper)mockController.getMock(I18nHelper.class);

        i18nHelper.getText("admin.field.screens.default.screen.scheme.name");
        mockController.setReturnValue("Default Screen Scheme");
        i18nHelper.getText("admin.field.screens.default.screen.scheme.description");
        mockController.setReturnValue("Default Screen Scheme");

        final FieldScreenScheme fieldScreenScheme = (FieldScreenScheme) mockController.getMock(FieldScreenScheme.class);
        fieldScreenScheme.setId(FieldScreenSchemeManager.DEFAULT_FIELD_SCREEN_SCHEME_ID);
        fieldScreenScheme.setName("Default Screen Scheme");
        fieldScreenScheme.setDescription("Default Screen Scheme");
        fieldScreenScheme.store();

        mockController.replay();
        new MockUpgradeTask_Build83(i18nHelper).setupDefaultFieldScreenScheme(fieldScreenScheme);
        mockController.verify();
    }

    @Test
    public void testSetupIssueTypeScreenSchemeKeysDontExist() throws Exception
    {
        final I18nHelper i18nHelper = (I18nHelper)mockController.getMock(I18nHelper.class);

        i18nHelper.getText("admin.field.screens.default.issue.type.screen.scheme.name");
        mockController.setReturnValue("admin.field.screens.default.issue.type.screen.scheme.name");
        i18nHelper.getText("admin.field.screens.default.issue.type.screen.scheme.description");
        mockController.setReturnValue("admin.field.screens.default.issue.type.screen.scheme.description");

        // Create Issue Type Screen Scheme
        final IssueTypeScreenScheme issueTypeScreenScheme = (IssueTypeScreenScheme) mockController.getMock(IssueTypeScreenScheme.class);
        issueTypeScreenScheme.setId(IssueTypeScreenScheme.DEFAULT_SCHEME_ID);
        issueTypeScreenScheme.setName("Default Issue Type Screen Scheme");
        issueTypeScreenScheme.setDescription("The default issue type screen scheme");
        issueTypeScreenScheme.store();

        mockController.replay();
        new MockUpgradeTask_Build83(i18nHelper).setupIssueTypeScreenScheme(issueTypeScreenScheme);
        mockController.verify();
    }

    @Test
    public void testSetupResolveIssueScreenKeyDontExists() throws Exception
    {
        final I18nHelper i18nHelper = (I18nHelper)mockController.getMock(I18nHelper.class);

        i18nHelper.getText("admin.field.screens.resolve.issue.name");
        mockController.setReturnValue( "admin.field.screens.resolve.issue.name");
        i18nHelper.getText("admin.field.screens.resolve.issue.description");
        mockController.setReturnValue("admin.field.screens.resolve.issue.description");

        final FieldScreen resolveFieldScreen = (FieldScreen) mockController.getMock(FieldScreen.class);
        resolveFieldScreen.setId(WorkflowTransitionUtil.VIEW_RESOLVE_ID);
        resolveFieldScreen.setName("Resolve Issue Screen");
        resolveFieldScreen.setDescription("Allows to set resolution, change fix versions and assign an issue.");
        resolveFieldScreen.store();

        mockController.replay();
        new MockUpgradeTask_Build83(i18nHelper).setupResolveIssueScreen(resolveFieldScreen);
        mockController.verify();
    }

    @Test
    public void testSetupDefaultScreenKeyDontExists() throws Exception
    {
        final I18nHelper i18nHelper = (I18nHelper)mockController.getMock(I18nHelper.class);

        i18nHelper.getText("admin.field.screens.default.name");
        mockController.setReturnValue("admin.field.screens.default.name");
        i18nHelper.getText("admin.field.screens.default.description");
        mockController.setReturnValue("admin.field.screens.default.description");

        final FieldScreen defaultFieldScreen = (FieldScreen) mockController.getMock(FieldScreen.class);
        defaultFieldScreen.setId(FieldScreenSchemeManager.DEFAULT_FIELD_SCREEN_SCHEME_ID);
        defaultFieldScreen.setName("Default Screen");
        defaultFieldScreen.setDescription("Allows to update all system fields.");
        defaultFieldScreen.store();

        mockController.replay();
        new MockUpgradeTask_Build83(i18nHelper).setupDefaultFieldScreen(defaultFieldScreen);
        mockController.verify();
    }

    @Test
    public void testSetupDefaultScreenSchemeKeysDontExist() throws Exception
    {
        final I18nHelper i18nHelper = (I18nHelper)mockController.getMock(I18nHelper.class);

        i18nHelper.getText("admin.field.screens.default.screen.scheme.name");
        mockController.setReturnValue("admin.field.screens.default.screen.scheme.name");
        i18nHelper.getText("admin.field.screens.default.screen.scheme.description");
        mockController.setReturnValue("admin.field.screens.default.screen.scheme.description");

        final FieldScreenScheme fieldScreenScheme = (FieldScreenScheme) mockController.getMock(FieldScreenScheme.class);
        fieldScreenScheme.setId(FieldScreenSchemeManager.DEFAULT_FIELD_SCREEN_SCHEME_ID);
        fieldScreenScheme.setName("Default Screen Scheme");
        fieldScreenScheme.setDescription("Default Screen Scheme");
        fieldScreenScheme.store();

        mockController.replay();
        new MockUpgradeTask_Build83(i18nHelper).setupDefaultFieldScreenScheme(fieldScreenScheme);
        mockController.verify();
    }

    private static class MockUpgradeTask_Build83 extends UpgradeTask_Build83
    {
        private I18nHelper i18nHelper;

        public MockUpgradeTask_Build83(I18nHelper i18nHelper)
        {
            super(null, null, null, null, null, null, null);
            this.i18nHelper = i18nHelper;
        }

        I18nHelper getApplicationI18n()
        {
            return i18nHelper;
        }
    }
}
