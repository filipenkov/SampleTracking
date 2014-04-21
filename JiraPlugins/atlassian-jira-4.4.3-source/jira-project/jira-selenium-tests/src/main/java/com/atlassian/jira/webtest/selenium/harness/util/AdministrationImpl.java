package com.atlassian.jira.webtest.selenium.harness.util;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.selenium.framework.model.Mouse;
import com.atlassian.selenium.SeleniumAssertions;
import junit.framework.Assert;

public class AdministrationImpl extends AbstractSeleniumUtil implements Administration
{

    // TODO make implement page object
    private final Navigator navigator;
    private SeleniumAssertions assertThat;

    public AdministrationImpl(SeleniumContext ctx)
    {
        super(ctx.client(), ctx.environmentData());
        this.assertThat = ctx.assertions();
        this.navigator = new NavigatorImpl(ctx);
    }

    public void addPermissionForUser(String username, String permissionScheme, String permission)
    {
        navigator.gotoAdmin();
        selenium.click("permission_schemes", true);
        selenium.click("link=" + permissionScheme, true);
        selenium.click("add_perm_" + permission, true);
        selenium.click("id=type_user", false);
        selenium.type("user", username);
        selenium.click("document.jiraform.elements[' Add ']", true);
    }

    public void addUserToGroup(String username, String group)
    {
        navigator.gotoAdmin();
        selenium.click("id=user_browser", true);
        selenium.click("id=editgroups_" + username, true);
        selenium.addSelection("groupsToJoin", "label=" + group);
        selenium.click("join", true);
    }

    public void setProfiling(boolean turnOn)
    {
        navigator.gotoAdmin();
        selenium.click("logging_profiling", true);
        if (selenium.isElementPresent("xpath=//a[@id='disable_profiling']"))
        {
            // its currently on
            if (!turnOn)
            {
                selenium.click("disable_profiling", true);
            }
        }
        else
        {
            // its currently off
            if (turnOn)
            {
                selenium.click("enable_profiling", true);
            }
        }
    }

    public void enableAttachments()
    {
        setAttachmentsEnabled(true, null);
    }

    public void enableAttachments(final String maxSize)
    {
        setAttachmentsEnabled(true, maxSize);
    }

    public void disableAttachments()
    {
        setAttachmentsEnabled(false, null);
    }

    public void activateTimeTracking()
    {
        navigator.gotoAdmin();
        selenium.click("timetracking", true);
        selenium.click("activate_submit", true);
    }

    public String getCurrentAttachmentPath()
    {
        navigator.gotoAdmin();
        selenium.click("attachments", true);
        // Check that  'Attachment Path' is in the third row where we expect it:
        if ("Attachment Path".equals(selenium.getTable("id=table-AttachmentSettings.1.0")))
        {
            // Get the table 'attachmentSettings'.
            String attachmentPath = selenium.getTable("id=table-AttachmentSettings.1.1");
            if (attachmentPath.startsWith("Default Directory ["))
            {
                // Strip "Default Directory [" from the front, and the "]" from the end
                attachmentPath = attachmentPath.substring("Default Directory [".length(), attachmentPath.length() - 1).trim();
            }
            return attachmentPath;
        }
        else
        {
            throw new RuntimeException("Error occured when trying to screen-scrape the attachment path. 'Attachment Path' not found where expected in the table.");
        }
    }

    public void setMimeSniffingPolicy(final String policy)
    {
        navigator.gotoAdmin();
        selenium.click("general_configuration", true);
        selenium.clickLinkWithText("Edit Configuration", true);

        selenium.click("ieMimeSniffer_" + policy);
        selenium.click("id=edit_property", true);
    }

    public void enableGzipCompression()
    {
        navigator.gotoAdmin();
        selenium.click("general_configuration", true);
        selenium.clickLinkWithText("Edit Configuration", true);

        selenium.click("id=useGzipOn");
        selenium.click("id=edit_property", true);
    }

    public void toogleCommentGroupVisibility(final boolean enable)
    {
        navigator.gotoAdmin();
        selenium.click("general_configuration", true);
        selenium.clickLinkWithText("Edit Configuration", true);

        if(enable)
        {
            selenium.click("id=groupVisibilityOn");
        }
        else
        {
            selenium.click("id=groupVisibilityOff");
        }
        selenium.click("id=edit_property", true);
    }

    public void removeRolePermission(final int permissionId, final int roleId)
    {
        navigator.gotoAdmin();
        selenium.click("permission_schemes", true);
        selenium.clickLinkWithText("Default Permission Scheme", true);
        if (selenium.isElementPresent("id=del_perm_" + permissionId + "_" + roleId))
        {
            selenium.click("id=del_perm_" + permissionId + "_" + roleId, true);
            selenium.click("Delete", true);
        }
        Assert.assertFalse(selenium.isElementPresent("id=del_perm_" + permissionId + "_" + roleId));
    }

    public void setRendererForField(final String fieldId, final String renderer)
    {
        //goto to the default field configuration configuration page
        navigator.gotoPage("/secure/admin/ViewIssueFields.jspa", true);
        selenium.click("renderer_" + fieldId, true);
        selenium.selectOption("selectedRendererType", renderer);
        selenium.click("update_submit", true);
        assertThat.textPresent("Edit Field Renderer Confirmation");
        selenium.click("update_submit", true);
        assertThat.elementContainsText("renderer_value_" + fieldId, renderer);
    }

    @Override
    public void disablePluginModule(String pluginId, String moduleName)
    {
        navigateToPluginModule(pluginId, moduleName);
        assertThat.elementPresentByTimeout("jquery=.upm-module:contains(" + moduleName + ") .upm-module-disable", 5000);
        selenium.click("jquery=.upm-module:contains(" + moduleName + ") .upm-module-disable");
        selenium.waitForAjaxWithJquery(5000);
    }

    @Override
    public void enablePluginModule(String pluginId, String moduleName)
    {
        navigateToPluginModule(pluginId, moduleName);
        assertThat.elementPresentByTimeout("jquery=.upm-module:contains(" + moduleName + ") .upm-module-enable", 5000);
        selenium.click("jquery=.upm-module:contains(" + moduleName + ") .upm-module-enable");
        selenium.waitForAjaxWithJquery(5000);
    }

    @Override
    public void createProject(String name, String key, String lead)
    {
        navigator.gotoPage("/secure/admin/AddProject!default.jspa", true);
        selenium.typeInElementWithName("name", name);
        selenium.typeInElementWithName("key", key);
        selenium.typeInElementWithName("lead", lead);
        selenium.click("Add", true);
    }

    private void navigateToPluginModule(String pluginId, String moduleName)
    {
        navigator.gotoAdmin();
        selenium.click("id=upm-admin-link", true);
        selenium.click("id=upm-manage-show-system");
        assertThat.elementPresentByTimeout("jquery=#upm-plugin-" + pluginId + " .upm-plugin-row", 5000);
        selenium.click("jquery=#upm-plugin-" + pluginId + " .upm-plugin-row p");
        assertThat.visibleByTimeout("jquery=#upm-plugin-" + pluginId + " .upm-module-toggle", 20000);
        selenium.click("jquery=#upm-plugin-" + pluginId + " .upm-module-toggle");
        Mouse.mouseover(selenium, "jquery=.upm-module:contains(" + moduleName + ")");
    }

    private void setAttachmentsEnabled(boolean isEnable, String size)
    {
        navigator.gotoAdmin();
        selenium.click("attachments", true);
        selenium.click("id=edit-attachments", true);

        if (isEnable)
        {
            selenium.click("id=attachmentPathOption_DEFAULT");
            if(size != null)
            {
                selenium.type("attachmentSize", size);
            }
        }
        else
        {
            selenium.click("id=attachmentPathOption_DISABLED");
        }

        selenium.click("Update", true);
    }
}
