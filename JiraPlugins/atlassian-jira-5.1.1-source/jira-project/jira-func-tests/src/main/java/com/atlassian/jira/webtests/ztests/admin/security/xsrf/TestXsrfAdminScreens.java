package com.atlassian.jira.webtests.ztests.admin.security.xsrf;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.ConfigureScreen;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfTestSuite;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Responsible for holding tests which verify that the User Administration actions are not susceptible to XSRF attacks.
 * @since v4.1
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.SECURITY })
public class TestXsrfAdminScreens extends FuncTestCase
{
    private static final String SCREEN_NAME = "super-screen";

    protected void setUpTest()
    {
        administration.restoreData("TestEditCustomFieldDescription.xml");
    }    

    public void testScreenOperations() throws Exception
    {
        new XsrfTestSuite(
                new XsrfCheck(
                        "Add Screen",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.gotoAdminSection("field_screens");
                                tester.clickLink("add-field-screen");
                                tester.setFormElement("fieldScreenName", SCREEN_NAME);
                            }
                        },
                        new XsrfCheck.FormSubmission("Add")),
                new XsrfCheck(
                        "Edit Screen",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.gotoAdminSection("field_screens");
                                tester.clickLink("edit_fieldscreen_" + SCREEN_NAME);
                            }
                        },
                        new XsrfCheck.FormSubmission("Update")),
                new XsrfCheck(
                        "Copy Screen",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.gotoAdminSection("field_screens");
                                tester.clickLink("copy_fieldscreen_" + SCREEN_NAME);
                            }
                        },
                        new XsrfCheck.FormSubmission("Copy")),
                new XsrfCheck(
                        "Delete Screen",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.gotoAdminSection("field_screens");
                                tester.clickLink("delete_fieldscreen_" + SCREEN_NAME);
                            }
                        },
                        new XsrfCheck.FormSubmission("Delete"))

        ).run(funcTestHelperFactory);
    }

    public void testScreenConfigOperations() throws Exception
    {
        addScreen();

        addField("Attachment");


        new XsrfTestSuite(
                new XsrfCheck(
                        "Configure Screen Add Field",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                gotoConfigure();
                                tester.selectOption("fieldId", "Assignee");
                            }
                        },
                        new XsrfCheck.FormSubmissionWithId("add_field_submit")),
                new XsrfCheck(
                        "Configure Screen Move Field",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                gotoConfigure();
                                tester.setFormElement("newFieldPosition_assignee", "0");
                            }
                        },
                        new XsrfCheck.FormSubmission("moveFieldsToPosition")),
                new XsrfCheck(
                        "Configure Screen Remove Field",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                gotoConfigure();
                                tester.checkCheckbox("removeField_0");
                            }
                        },
                        new XsrfCheck.FormSubmission("deleteFieldsFromTab")),
                new XsrfCheck(
                        "Configure Screen Add Tab",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                gotoConfigure();
                                tester.setFormElement("newTabName", "Tab Cola");
                            }
                        },
                        new XsrfCheck.FormSubmissionWithId("add_tab_submit")),
                new XsrfCheck(
                        "Configure Screen Rename Tab",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                gotoConfigure();
                                tester.setFormElement("tabName", "Pepsi Cola");
                            }
                        },
                        new XsrfCheck.FormSubmissionWithId("rename_tab_submit")),
                new XsrfCheck(
                        "Configure Screen Delete Tab",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                gotoConfigure();
                                tester.clickLink("delete_fieldscreentab");
                            }
                        },
                        new XsrfCheck.FormSubmission("Delete"))

        ).run(funcTestHelperFactory);
    }

    private void addScreen()
    {
        navigation.gotoAdminSection("field_screens");
        tester.clickLink("add-field-screen");
        tester.setFormElement("fieldScreenName", SCREEN_NAME);
        tester.clickButton("field-screen-add-submit");
    }

    private void addField(String field)
    {
        gotoConfigure();
        tester.selectOption("fieldId", field);
        tester.clickButton("add_field_submit");
    }

    private void gotoConfigure()
    {
        navigation.gotoAdminSection("field_screens");
        tester.clickLink("configure_fieldscreen_" + SCREEN_NAME);
    }
}
