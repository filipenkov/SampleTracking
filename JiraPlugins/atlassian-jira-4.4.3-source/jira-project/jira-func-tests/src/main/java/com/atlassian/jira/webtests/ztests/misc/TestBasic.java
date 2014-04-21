package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

/**
 * Test some basic operations in JIRA in German.
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestBasic extends JIRAWebTest
{
    private static final String GERMAN_NEW_FEATURE = "Neue Funktion";    

    public TestBasic(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreBlankInstance();
    }

    public void testI18NDates()
    {
        setLocaleTo("German (Germany)");

        // create an issue with a valid due date
        createIssueInGermanWithDueDate("25/Dez/05");
        assertTextPresent("Es liegen noch keine Kommentare zu diesem Vorgang vor.");

        // create an issue with an invalid due date
        createIssueInGermanWithDueDate("25/Dec/05");
        assertTextPresent("Datum eingegeben. Geben Sie das Datum im Format");

    }

    public void testIssueConstantTranslations()
    {
        setLocaleTo("German (Germany)");
        // reset the translation to blank
        updateBugTranslationWith("", "");

        // browse to the admin section and make sure that we see the issue constants as translated
        browseToCustomFieldAdd();

        assertTextPresent("Alle Vorgangstypen");
        assertTextPresent("Bug");
        assertTextPresent("Verbesserung");
        assertTextPresent(GERMAN_NEW_FEATURE);
        assertTextPresent("Aufgabe");

        // add a translation via the GUI, confirm present in place of the default properties
        updateBugTranslationWith("bugenzee", "bugenzee desc");

        browseToCustomFieldAdd();
        assertTextPresent("bugenzee");
        assertTextNotPresent("Fehler");
    }

    private void updateBugTranslationWith(String name, String desc)
    {
        gotoAdmin();
        clickLink("issue_types");
        clickLink("translate_link");
        setWorkingForm("update");
        setFormElement("jira.translation.Vorgangstyp.1.name", name);
        setFormElement("jira.translation.Vorgangstyp.1.desc", desc);
        submit();
    }

    private void browseToCustomFieldAdd()
    {
        gotoAdmin();
        clickLink("view_custom_fields");
        clickLink("add_custom_fields");
        checkCheckbox("fieldType", "com.atlassian.jira.plugin.system.customfieldtypes:textarea");
        submit(BUTTON_NAME_NEXT);
    }

    private void createIssueInGermanWithDueDate(String dueDate)
    {
        navigation.issue().goToCreateIssueForm(PROJECT_HOMOSAP,GERMAN_NEW_FEATURE);
        setWorkingForm("issue-create");
        setFormElement("duedate", dueDate);
        setFormElement("summary", "test issue");
        assertFormElementHasValue("issue-create", "issue-create-submit", "Erstellen");
        submit("Create");
    }

    private void setLocaleTo(String localeName)
    {

        gotoPage("/secure/UpdateUserPreferences!default.jspa?username=admin");
        selectOption("userLocale", localeName);
        setWorkingForm("update-user-preferences");
        submit();
    }

    private void resetLocaleToEnglishFromGerman()
    {
        clickLinkWithText("Profil");
        clickLinkWithText("Bearbeiten der Einstellungen");
        selectMultiOptionByValue("userLocale", "-1");
        submit("Aktualisieren");
    }
}

/*
    THIS SHOULD BE A TEST THAT WE CAN INCLUDE BUT EITHER JWEBUNIT OR HTTPUNIT IS MISSINTERPRETING
    THE \u00e4 character so the string compares for the created projects, versions, and components
    are failing.

    private final String COMPONENT_NAME_WITH_UM = "Component \u00e4";
    private final String VERSION_NAME_WITH_UM = "Version \u00e4";
    private final String PROJECT_NAME_WITH_UM = "Project \u00e4";
    private final String PROJECT_NAME_WITH_UM_KEY = "UMM";

    public void testI18NIssueNavigatorElements()
    {
        try
        {
            // add a second project to force the projects to show in the issue navigator
            if (projectExists(PROJECT_HOMOSAP))
            {
                log("Project: " + PROJECT_HOMOSAP + " exists");
            }
            else
            {
                addProject(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "", ADMIN_USERNAME, "project for homsapiens.");
            }

            if (projectExists(PROJECT_NAME_WITH_UM))
            {
                deleteProject(PROJECT_NAME_WITH_UM);
            }
            String projectId = addProject(PROJECT_NAME_WITH_UM, PROJECT_NAME_WITH_UM_KEY, "", ADMIN_USERNAME, "project for umlaat tests.");
            // create a component with a german umlaat
            addComponent(PROJECT_NAME_WITH_UM, COMPONENT_NAME_WITH_UM);
            // create a version witha  german umlaat
            addVersion(PROJECT_NAME_WITH_UM, VERSION_NAME_WITH_UM, "");

            // do this after creating all the stuff we need since lots of links names will change when in German
            setLocaleTo("German (Germany)");

            clickLink("find_link");

            // make sure that the project name is displayed correctly
            assertTextPresent(PROJECT_NAME_WITH_UM);

            // check that the issue types are showing correctly
            assertTextPresentAfterText("Vorgangstyp", "Vorg\u00e4nge");

            // make the components and versions show up
            setFormElement("pid", projectId);
            submit("show");

            assertTextPresent(COMPONENT_NAME_WITH_UM);
            assertTextPresent(VERSION_NAME_WITH_UM);

        }
        finally
        {
            resetLocaleToEnglishFromGerman();
            deleteProject(PROJECT_HOMOSAP);
            deleteProject(PROJECT_NAME_WITH_UM);
        }
    }

 */
