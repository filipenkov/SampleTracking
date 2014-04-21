package com.atlassian.jira.webtests.ztests.i18n;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.TableCellLocator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.xml.sax.SAXException;

import java.io.IOException;

@WebTest ({ Category.FUNC_TEST, Category.I18N })
public class TestI18n500Page extends FuncTestCase
{
    public static final String USERNAME_NON_SYS_ADMIN = "admin_non_sysadmin";
    public static final String PASSWORD_NON_SYS_ADMIN = "admin_non_sysadmin";

    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreI18nData("TestI18n.xml");
    }

    @Override
    public void tearDownTest()
    {
        administration.generalConfiguration().setJiraLocaleToSystemDefault();
        super.tearDownTest();
    }

    public void testI18nNonSystemAdministratorDoesntSeeFilePaths()
    {
        navigation.login(BOB_USERNAME, BOB_PASSWORD);
        tester.gotoPage("/500page.jsp");
        assertions.getTextAssertions().assertTextSequence(new WebPageLocator(tester), new String[] {
                "ID de serveur",
                "Contacter votre administrateur pour d\u00e9couvrir cette valeur de la propri\u00e9t\u00e9.",
                "Chemins d'acc\u00e8s de fichiers:",
                "R\u00e9pertoire de travail en cours",
                "Contacter votre administrateur de syst\u00e8me pour d\u00e9couvrir cette valeur de la propri\u00e9t\u00e9.",
                "Arguments d'entr\u00e9e de JVM",
                "Contacter votre administrateur de syst\u00e8me pour d\u00e9couvrir cette valeur de la propri\u00e9t\u00e9."
        });

        assertions.getTextAssertions().assertTextNotPresent(new WebPageLocator(tester), "-Xmx"); // this shouldn't be present during tests for non sysadmin user
        navigation.login(USERNAME_NON_SYS_ADMIN, PASSWORD_NON_SYS_ADMIN);
        tester.gotoPage("/500page.jsp");
        assertions.getTextAssertions().assertTextSequence(new WebPageLocator(tester), new String[] {
                "ID de serveur",
                "ABN9-RZYJ-WI2T-37UF", // admins can see server ids
                "Chemins d'acc\u00e8s de fichiers:",
                "Contacter votre administrateur de syst\u00e8me pour d\u00e9couvrir les informations de chemin de fichier.",
                "R\u00e9pertoire de travail en cours",
                "Contacter votre administrateur de syst\u00e8me pour d\u00e9couvrir cette valeur de la propri\u00e9t\u00e9.",
                "Arguments d'entr\u00e9e de JVM",
                "Contacter votre administrateur de syst\u00e8me pour d\u00e9couvrir cette valeur de la propri\u00e9t\u00e9."
        });
        assertions.getTextAssertions().assertTextNotPresent(new WebPageLocator(tester), "-Xmx"); // this shouldn't be present during tests for non sysadmin user
    }

    public void testI18nSystemAdministratorCanSeeSysAdminOnlyProperties() throws SAXException, IOException
    {

        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        tester.gotoPage("/500page.jsp");

        final String responseText = tester.getDialog().getResponseText();
        assertions.getTextAssertions().assertTextPresent(responseText,"\u00e9");
        assertions.getTextAssertions().assertTextPresent(responseText,"Chemins d'acc\u00e8s de fichiers");

        assertions.getTextAssertions().assertTextNotPresent(new WebPageLocator(tester), "Contacter votre administrateur de syst\u00e8me pour d\u00e9couvrir les informations de chemin de fichier.");
        assertions.getTextAssertions().assertTextNotPresent(new WebPageLocator(tester), "Contacter votre administrateur de syst\u00e8me pour d\u00e9couvrir cette valeur de la propri\u00e9t\u00e9.");
        assertions.getTextAssertions().assertTextSequence(new WebPageLocator(tester), new String[] { "ID de serveur", "ABN9-RZYJ-WI2T-37UF" });



        assertions.getTextAssertions().assertTextSequence(new WebPageLocator(tester), new String[] { "Chemins d'acc\u00e8s de fichiers", "entityengine.xml", "atlassian-jira.log" });
        assertions.getTextAssertions().assertTextPresent(new WebPageLocator(tester), "Arguments d'entr\u00e9e de JVM");

        // Checking Jdk version changes page to system info page.
        tester.gotoPage("/500page.jsp");
        assertions.getTextAssertions().assertTextPresent(new WebPageLocator(tester), "-D");
        assertions.getTextAssertions().assertTextPresent(new WebPageLocator(tester), "R\u00e9pertoire de travail en cours");
    }

    /**
     * A user with no predefined language gets the language options in the system's default language
     */
    public void testShowsLanguageListInDefaultLanguage()
    {
        administration.restoreData("TestUserProfileI18n.xml");

        administration.generalConfiguration().setJiraLocale("Deutsch (Deutschland)");

        tester.gotoPage("/500page.jsp");

        // assert that the page defaults to German
        final int lastRow = page.getHtmlTable("language-info").getRowCount() - 1;
        text.assertTextPresent(new TableCellLocator(tester, "language-info", lastRow, 1), "Deutsch (Deutschland)");
        text.assertTextPresent(new TableCellLocator(tester, "language-info", lastRow - 1, 1), "Deutsch (Deutschland)");
    }

    /**
     * A user with a language preference that is different from the system's language gets the list of languages in his preferred language.
     */
    public void testShowsLanguageListInTheUsersLanguage()
    {
        administration.restoreData("TestUserProfileI18n.xml");

        // set the system locale to something other than English just to be different
        administration.generalConfiguration().setJiraLocale("Deutsch (Deutschland)");

        navigation.login(FRED_USERNAME);

        tester.gotoPage("/500page.jsp");

        // assert that the page defaults to Spanish
        final int lastRow = page.getHtmlTable("language-info").getRowCount() - 1;
        text.assertTextPresent(new TableCellLocator(tester, "language-info", lastRow, 1), "alem\u00e1n (Alemania)");
        text.assertTextPresent(new TableCellLocator(tester, "language-info", lastRow - 1, 1), "espa\u00f1ol (Espa\u00f1a)");
    }
}

