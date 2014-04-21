package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;

/**
 * Implements the {@link com.atlassian.jira.functest.framework.admin.PermissionSchemes} and
 * {@link com.atlassian.jira.functest.framework.admin.PermissionSchemes.PermissionScheme} interfaces.
 *
 * @since v4.0
 */
public class PermissionSchemesImpl extends AbstractFuncTestUtil implements PermissionSchemes,
        PermissionSchemes.PermissionScheme
{
    public PermissionSchemesImpl(WebTester tester, JIRAEnvironmentData environmentData)
    {
        super(tester, environmentData, 2);
    }

    public PermissionScheme defaultScheme()
    {
        getNavigation().gotoAdminSection("permission_schemes");
        tester.clickLinkWithText("Default Permission Scheme");
        return this;
    }

    public PermissionScheme scheme(String schemeName)
    {
        getNavigation().gotoAdminSection("permission_schemes");
        tester.clickLinkWithText(schemeName);
        return this;
    }

    public void grantPermissionToGroup(final int permission, final String groupName)
    {
        tester.clickLink("add_perm_" + permission);
        tester.checkCheckbox("type", "group");
        tester.setFormElement("group", groupName);
        tester.submit(" Add ");
    }

    public void removePermission(final int permission, final String groupName)
    {   if (tester.getDialog().isLinkPresent("del_perm_" + permission + "_" + groupName))
        {
            tester.clickLink("del_perm_" + permission + "_" + groupName);
            tester.submit("Delete");
        }
    }

    protected Navigation getNavigation()
    {
        return getFuncTestHelperFactory().getNavigation();
    }
}
