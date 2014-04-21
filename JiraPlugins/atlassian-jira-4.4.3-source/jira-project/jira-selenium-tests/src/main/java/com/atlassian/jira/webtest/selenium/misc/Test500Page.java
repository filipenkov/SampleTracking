package com.atlassian.jira.webtest.selenium.misc;

import com.atlassian.jira.functest.framework.assertions.TextAssertions;
import com.atlassian.jira.functest.framework.assertions.TextAssertionsImpl;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;

/**
 * This test is an extension of the Test500Page Functional test for international testing. It needs to be a Selenium
 * test as there appears to be a bug in httpunit that reads non-ascii characters incorrectly.
 *
 * @since v4.0
 */
@WebTest({Category.SELENIUM_TEST })
public class Test500Page extends JiraSeleniumTest
{
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin";

    public static final String USERNAME_BOB = "bob";
    public static final String PASSWORD_BOB = "bob";

    public static final String USERNAME_NON_SYS_ADMIN = "admin_non_sysadmin";
    public static final String PASSWORD_NON_SYS_ADMIN = "admin_non_sysadmin";
    private TextAssertions textAssertions;

    public void onSetUp()
    {
        super.onSetUp();
        textAssertions = new TextAssertionsImpl();
        restoreI18nData("Test500FrenchPage.xml");
    }

    public void testI18nNonSystemAdministratorDoesntSeeFilePaths()
    {
        getNavigator().login(USERNAME_BOB, PASSWORD_BOB);
        getNavigator().gotoPage("/500page.jsp", true);
        textAssertions.assertTextSequence(this.getSeleniumClient().getBodyText(), new String[] {
                "ID de serveur",
                "Contacter votre administrateur pour d\u00e9couvrir cette valeur de la propri\u00e9t\u00e9.",
                "Chemins d'acc\u00e8s de fichiers:",
                "R\u00e9pertoire de travail en cours",
                "Contacter votre administrateur de syst\u00e8me pour d\u00e9couvrir cette valeur de la propri\u00e9t\u00e9.",
                "Arguments d'entr\u00e9e de JVM",
                "Contacter votre administrateur de syst\u00e8me pour d\u00e9couvrir cette valeur de la propri\u00e9t\u00e9."
        });

        textAssertions.assertTextNotPresent(this.getSeleniumClient().getBodyText(), "-Xmx"); // this shouldn't be present during tests for non sysadmin user
        getNavigator().login(USERNAME_NON_SYS_ADMIN, PASSWORD_NON_SYS_ADMIN);
        getNavigator().gotoPage("/500page.jsp", true);
        textAssertions.assertTextSequence(this.getSeleniumClient().getBodyText(), new String[] {
                "ID de serveur",
                "ABN9-RZYJ-WI2T-37UF", // admins can see server ids
                "Chemins d'acc\u00e8s de fichiers:",
                "Contacter votre administrateur de syst\u00e8me pour d\u00e9couvrir les informations de chemin de fichier.",
                "R\u00e9pertoire de travail en cours",
                "Contacter votre administrateur de syst\u00e8me pour d\u00e9couvrir cette valeur de la propri\u00e9t\u00e9.",
                "Arguments d'entr\u00e9e de JVM",
                "Contacter votre administrateur de syst\u00e8me pour d\u00e9couvrir cette valeur de la propri\u00e9t\u00e9."
        });
        textAssertions.assertTextNotPresent(this.getSeleniumClient().getBodyText(), "-Xmx"); // this shouldn't be present during tests for non sysadmin user
    }

    public void testI18nSystemAdministratorCanSeeSysAdminOnlyProperties()
    {
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().gotoPage("/500page.jsp", true);
        textAssertions.assertTextNotPresent(this.getSeleniumClient().getBodyText(), "Contacter votre administrateur de syst\u00e8me pour d\u00e9couvrir les informations de chemin de fichier.");
        textAssertions.assertTextNotPresent(this.getSeleniumClient().getBodyText(), "Contacter votre administrateur de syst\u00e8me pour d\u00e9couvrir cette valeur de la propri\u00e9t\u00e9.");
        textAssertions.assertTextSequence(this.getSeleniumClient().getBodyText(), new String[] { "ID de serveur", "ABN9-RZYJ-WI2T-37UF" });

        textAssertions.assertTextSequence(this.getSeleniumClient().getBodyText(), new String[] { "Chemins d'acc\u00e8s de fichiers", "entityengine.xml", "atlassian-jira.log" });
        textAssertions.assertTextPresent(this.getSeleniumClient().getBodyText(), "Arguments d'entr\u00e9e de JVM");

        // Checking Jdk version changes page to system info page.
        final boolean isBeforeJdk15 = isBeforeJdk15();
        getNavigator().gotoPage("/500page.jsp", true);
        if (!isBeforeJdk15)
        {
            textAssertions.assertTextPresent(this.getSeleniumClient().getBodyText(), "-D");
        }

        textAssertions.assertTextPresent(this.getSeleniumClient().getBodyText(), "R\u00e9pertoire de travail en cours");
    }
}
