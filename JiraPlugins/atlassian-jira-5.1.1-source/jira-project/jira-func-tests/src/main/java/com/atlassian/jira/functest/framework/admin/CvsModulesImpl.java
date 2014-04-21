package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.assertions.Assertions;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;

import java.io.IOException;

/**
 * @since v4.1
 */
public class CvsModulesImpl extends AbstractFuncTestUtil implements CvsModules
{
    private final Navigation navigation;
    private final Assertions assertions;

    public CvsModulesImpl(WebTester tester, JIRAEnvironmentData environmentData, final Navigation navigation, final Assertions assertions)
    {
        super(tester, environmentData, 2);
        this.navigation = navigation;
        this.assertions = assertions;
    }

    public void addCvsModule(final String moduleName, final String logFile)
    {
        navigation.gotoAdminSection("cvs_modules");
        tester.clickLinkWithText("Add");
        tester.setFormElement("name", moduleName);
        tester.setFormElement("cvsRoot", ":pserver:anonymous@example.com:/");
        tester.setFormElement("moduleName", "dummy");
        tester.checkCheckbox("fetchLog", "false");
        try
        {
            tester.setFormElement("logFilePath", getEnvironmentData().getXMLDataLocation().getCanonicalPath() + "/" + logFile);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        tester.submit(" Add ");

        // Ensure the CVS module was created
        tester.assertTextInTable("cvs_modules_table", moduleName);
    }
}
