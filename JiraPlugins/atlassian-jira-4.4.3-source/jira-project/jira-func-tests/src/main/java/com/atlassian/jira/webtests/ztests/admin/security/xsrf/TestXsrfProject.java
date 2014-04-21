package com.atlassian.jira.webtests.ztests.admin.security.xsrf;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfTestSuite;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v4.1
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.SECURITY })
public class TestXsrfProject extends FuncTestCase
{
    private static final String PROJECT_MONKEY = "monkey";
    private static final String SUBMIT_ASSOCIATE = "Associate";
    private static final String SUBMIT_SELECT = "Select";

    protected void setUpTest()
    {
        administration.restoreData("TestXsrfProject.xml");
    }

    public void testProjectAdministration() throws Exception
    {
        new XsrfTestSuite(
            new XsrfCheck("DeleteProject", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    tester.gotoPage("/secure/admin/DeleteProject!default.jspa?pid=10000");
                }
            }, new XsrfCheck.FormSubmission("Delete")),
            new XsrfCheck("AddProject", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    navigation.gotoAdminSection("view_projects");
                    tester.clickLink("add_project");
                    tester.setFormElement("name", "Test Project");
                    tester.setFormElement("key", "TST");
                    tester.setFormElement("lead", ADMIN_USERNAME);
                }
            }, new XsrfCheck.FormSubmission("Add")),
            new XsrfCheck("EditProject", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    tester.gotoPage("/secure/project/EditProject!default.jspa?pid=10001");
                    tester.setFormElement("description", "PROJECT FOR MONKEYS");
                }
            }, new XsrfCheck.FormSubmission("Update")),

            new XsrfCheck("EditProjectEmail", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    tester.gotoPage("/secure/project/ProjectEmail!default.jspa?projectId=10001");
                    tester.setFormElement("fromAddress", "mailserver@mailserver");
                }
            }, new XsrfCheck.FormSubmission("update")),

            new IgniteProjectAssociationXsrfCheck("SelectIssueTypeScheme", "issuetypes",
                    "project-config-issuetype-scheme-change",
                    "schemeId", "Copy of Default Issue Type Scheme",
                    null, " OK "),
            new IgniteProjectAssociationXsrfCheck("SelectNotificationScheme", "notifications",
                    "project-config-notification-scheme-change",
                    "schemeIds", "Default Notification Scheme",
                    null, SUBMIT_ASSOCIATE),
            new IgniteProjectAssociationXsrfCheck("SelectPermissionScheme", "permissions",
                    "project-config-permissions-scheme-change",
                    "schemeIds", "Copy of Default Permission Scheme",
                    null, SUBMIT_ASSOCIATE),
            new IgniteProjectAssociationXsrfCheck("SelectIssueSecurityScheme", "issuesecurity",
                    "project-config-issuesecurity-scheme-change",
                    "newSchemeId", "Test Scheme",
                    "Next >>", SUBMIT_ASSOCIATE),
            new IgniteProjectAssociationXsrfCheck("SelectFieldConfigurationScheme", "fields",
                    "project-config-fields-scheme-change",
                    "schemeId", "New Field Config Scheme",
                    null, SUBMIT_ASSOCIATE),
            new IgniteProjectAssociationXsrfCheck("SelectIssueTypeScreenScheme", "screens",
                    "project-config-screens-scheme-change",
                    "schemeId", "Copy of Default Issue Type Screen Scheme",
                    null, SUBMIT_ASSOCIATE),
            new IgniteProjectAssociationXsrfCheck("SelectWorkflowScheme", "workflows",
                    "project-config-workflows-scheme-change",
                    "schemeId", "New Workflow Scheme",
                    SUBMIT_ASSOCIATE, SUBMIT_ASSOCIATE),
            new IgniteProjectAssociationXsrfCheck("SelectCvsModules", "summary",
                    "project-config-cvs-change",
                    "multipleRepositoryIds", "dummy",
                    null, SUBMIT_SELECT),
            new IgniteProjectAssociationXsrfCheck("SelectProjectCategory", "summary",
                    "project-config-details-project-category",
                    "pcid", "Category One",
                    null, SUBMIT_SELECT)
        ).run(funcTestHelperFactory);
    }


    /**
     * A Project Association Xsrf Check involves setting up the client to get to the association mutative action with a new
     * value selected and then submitting the form.
     */
    class IgniteProjectAssociationXsrfCheck extends XsrfCheck
    {
        public IgniteProjectAssociationXsrfCheck(String description, String tab, String linkId, String inputName,
                String inputValue, String optionalStepSubmit, String formSubmit)
        {
            super(description,
                    new IgniteProjectAssociationSetup(tab, linkId, inputName, inputValue, optionalStepSubmit),
                    new FormSubmission(formSubmit));
        }
    }

    /**
     * All the Project Association Setups involve:
     * - Viewing Project Monkey,
     * - Clicking on the association link,
     * - Setting a new value
     * - Optionally submitting the form to the final step before action is done
     */
    class IgniteProjectAssociationSetup implements XsrfCheck.Setup
    {
        private final String tab;
        private final String linkId;
        private final String inputName;
        private final String inputValue;
        private final String optionalStepSubmit;

        IgniteProjectAssociationSetup(final String tab, final String linkId, final String inputName, final String inputValue, final String optionalStepSubmit)
        {
            this.tab = tab;
            this.linkId = linkId;
            this.inputName = inputName;
            this.inputValue = inputValue;
            this.optionalStepSubmit = optionalStepSubmit;
        }

        public void setup()
        {
            tester.gotoPage("/plugins/servlet/project-config/" + "MKY" + "/" + tab);
            tester.clickLink(linkId);
            tester.selectOption(inputName, inputValue);
            if (optionalStepSubmit != null)
            {
                tester.submit(optionalStepSubmit);
            }
        }
    }
}
