package com.atlassian.jira.webtests.ztests;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.LicenseKeys;

@WebTest ({ Category.FUNC_TEST, Category.IMPORT_EXPORT })
public class TestXmlRestore extends FuncTestCase
{
    public TestXmlRestore(String name)
    {
        this.setName(name);
    }

    // JRA-14662: when restoring data which does not include the currently logged in user, do not throw an exception
    public void testRestoreDataWhereCurrentUserDoesntExist()
    {
        administration.restoreBlankInstance();
        administration.usersAndGroups().addUser("idontexist");
        administration.usersAndGroups().addUserToGroup("idontexist", "jira-administrators");
        navigation.logout();
        navigation.login("idontexist");

        // restore data again
        getTester().gotoPage("secure/admin/XmlRestore!default.jspa");
        getTester().setWorkingForm("jiraform");

        String filePath = getEnvironmentData().getXMLDataLocation().getAbsolutePath() + "/blankprojects.xml";
        getTester().setFormElement("filename", filePath);
        getTester().setFormElement("license", LicenseKeys.V2_COMMERCIAL.getLicenseString());
        getTester().submit();
        administration.waitForRestore();
        getTester().assertTextPresent("Your project has been successfully imported");
        getTester().assertTextNotPresent("NullPointerException");

        navigation.disableWebSudo();
        navigation.login(oldway_consider_porting.ADMIN_USERNAME, oldway_consider_porting.ADMIN_PASSWORD);

        administration.generalConfiguration().setBaseUrl(getBaseUrl());
    }

    public void testRestoreWithCustomPathsErrors()
    {
        try
        {
            administration.restoreData("TestXMLRestore.xml");
        }
        catch (Throwable e)
        {
            assertTrue("custom path should not be created",e.getMessage().startsWith("Failed to restore JIRA data. Cause: specified in the backup file is not valid"));
        }
        tester.assertTextPresent("specified in the backup file is not valid");
    }

    private String getBaseUrl() {return getEnvironmentData().getBaseUrl().toExternalForm();}
}