package com.atlassian.jira.webtests.ztests.admin.security.xsrf;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfTestSuite;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.io.IOException;

/**
 * @since v4.1
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.SECURITY })
public class TestXsrfCvsModules extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testCvsModuleAdministration() throws Exception
    {
        new XsrfTestSuite(
            new XsrfCheck("AddCvsModule", new CvsModuleSetup()
            {
                public void setup()
                {
                    super.setup();
                    tester.clickLink("add_cvs_module");
                    tester.setFormElement("name", "MyModule");
                    tester.setFormElement("cvsRoot", ":pserver:anonymous@example.com:/");
                    tester.setFormElement("moduleName", "dummy");
                    tester.checkCheckbox("fetchLog", "false");
                    try
                    {
                        tester.setFormElement("logFilePath", getEnvironmentData().getXMLDataLocation().getCanonicalPath() + "/TestCvsIntegration.log");
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }, new XsrfCheck.FormSubmission(" Add ")),
            new XsrfCheck("EditCvsModule", new CvsModuleSetup()
            {
                public void setup()
                {
                    super.setup();
                    tester.clickLink("edit_10000");
                    tester.setFormElement("description", "This is MyModule");
                }
            }, new XsrfCheck.FormSubmission(" Update ")),
            new XsrfCheck("TestCvsModule", new CvsModuleSetup(), new XsrfCheck.LinkWithIdSubmission("test_10000")),
            new XsrfCheck("DeleteCvsModule", new CvsModuleSetup()
            {
                public void setup()
                {
                    super.setup();
                    tester.clickLink("delete_10000");
                }
            }, new XsrfCheck.FormSubmission("Delete"))
        ).run(funcTestHelperFactory);
    }

    private class CvsModuleSetup implements XsrfCheck.Setup
    {
        public void setup()
        {
            navigation.gotoAdminSection("cvs_modules");
        }
    }
}
