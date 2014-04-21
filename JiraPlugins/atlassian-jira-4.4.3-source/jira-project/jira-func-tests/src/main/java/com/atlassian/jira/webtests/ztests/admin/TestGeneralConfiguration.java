package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.locator.TableCellLocator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

/**
 * Func test of editing application properties.
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.BROWSING })
public class TestGeneralConfiguration extends JIRAWebTest
{
    public TestGeneralConfiguration(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreBlankInstance();
    }

    public void testAjaxIssuePicker()
    {
        gotoAdmin();
        clickLink("general_configuration");
        //enabled by default
        assertTableCellHasText("table-options_table", 10, 1, "ON");

        //lets disable it
        clickLinkWithText("Edit Configuration");
        checkCheckbox("ajaxIssuePicker", "false");
        submit("Update");
        assertTableCellHasText("table-options_table", 10, 1, "OFF");
    }

    public void testAjaxUserPicker()
    {
        gotoAdmin();
        clickLink("general_configuration");
        //enabled by default
        assertTableCellHasText("table-options_table", 11, 1, "ON");

        //lets disable it
        clickLinkWithText("Edit Configuration");
        checkCheckbox("ajaxUserPicker", "false");
        submit("Update");
        assertTableCellHasText("table-options_table", 11, 1, "OFF");
    }

    /**
     * JRA-13435: Test to ensure that the last slash will always be stripped from
     * the baseUrl.  This is to ensure we don't endup with URLs such as
     * http://jira.atlassian.com//browse/HSP-1.
     *
     * Also tests that whitespace surrounding the base url is stripped.
     */
    public void testBaseUrlNormalised()
    {
        gotoAdmin();
        clickLink("general_configuration");

        clickLinkWithText("Edit Configuration");
        setFormElement("baseURL", "http://example.url.com:8090/");
        submit("Update");
        //ensure trailing slash was stripped
        assertTextPresent("http://example.url.com:8090");
        assertTextNotPresent("http://example.url.com:8090/");

        clickLinkWithText("Edit Configuration");
        setFormElement("baseURL", "http://example.url.com:8090/jira/");
        submit("Update");
        //ensure trailing slash was stripped
        assertTextPresent("http://example.url.com:8090/jira");
        assertTextNotPresent("http://example.url.com:8090/jira/");

        //finally check we can use a URL that doesn't have a slash at all.
        clickLinkWithText("Edit Configuration");
        setFormElement("baseURL", "http://example.url.com:8090");
        submit("Update");
        //ensure trailing slash was stripped
        assertTextPresent("http://example.url.com:8090");
        assertTextNotPresent("http://example.url.com:8090/");

        // Check whitespace is stripped.
        clickLinkWithText("Edit Configuration");
        setFormElement("baseURL", "\thttp://example.url.com:8090/ ");
        submit("Update");
        // Ensure whitespace and slash have been stripped.
        assertTextPresent("http://example.url.com:8090");
        assertTextNotPresent("\thttp://example.url.com:8090/ ");
    }

    public void testMimeSnifferOptions() {
        gotoAdmin();
        clickLink("general_configuration");
        assertTextPresent("Work around Internet Explorer security hole");

        clickLinkWithText("Edit Configuration");
        setFormElement("ieMimeSniffer", "secure");
        submit("Update");
        assertTextPresent("Secure: forced download of attachments for all browsers");

        clickLinkWithText("Edit Configuration");
        setFormElement("ieMimeSniffer", "insecure");
        submit("Update");
        assertTextPresent("Insecure: inline display of attachments");

        clickLinkWithText("Edit Configuration");
        setFormElement("ieMimeSniffer", "workaround");
        submit("Update");
        assertTextPresent("Work around Internet Explorer security hole");

        // hack url
        tester.gotoPage(page.addXsrfToken("/secure/admin/jira/EditApplicationProperties.jspa?title=jWebTest+JIRA+installation&mode=public&captcha=false&baseURL=http%3A%2F%2Flocalhost%3A8080%2Fjira&emailFromHeaderFormat=%24%7Bfullname%7D+%28JIRA%29&introduction=&encoding=UTF-8&language=english&defaultLocale=-1&voting=true&watching=true&allowUnassigned=false&externalUM=false&logoutConfirm=never&useGzip=false&allowRpc=false&emailVisibility=show&groupVisibility=true&excludePrecedenceHeader=false&ajaxIssuePicker=true&ajaxUserPicker=true&Update=Update"));
        assertTextPresent("The MIME sniffing policy option is required.");
        tester.gotoPage(page.addXsrfToken("/secure/admin/jira/EditApplicationProperties.jspa?title=jWebTest+JIRA+installation&mode=public&captcha=false&baseURL=http%3A%2F%2Flocalhost%3A8080%2Fjira&emailFromHeaderFormat=%24%7Bfullname%7D+%28JIRA%29&introduction=&encoding=UTF-8&language=english&defaultLocale=-1&voting=true&watching=true&allowUnassigned=false&externalUM=false&logoutConfirm=never&useGzip=false&allowRpc=false&emailVisibility=show&groupVisibility=true&excludePrecedenceHeader=false&ajaxIssuePicker=true&ajaxUserPicker=true&ieMimeSniffer=_WRONGARSE%26copy;&Update=Update"));
        assertTextPresent("The given value for MIME sniffing policy is invalid: _WRONGARSE&amp;copy;");
    }

    /**
     * A user with no predefined language gets the language options in the system's default language
     */
    public void testShowsLanguageListInDefaultLanguage()
    {
        administration.restoreData("TestUserProfileI18n.xml");

        administration.generalConfiguration().setJiraLocale("German (Germany)");

        navigation.gotoAdminSection("general_configuration");

        // assert that the page defaults to German
        final int secondLastRow = page.getHtmlTable("table-language-info").getRowCount() - 2;
        text.assertTextPresent(new TableCellLocator(tester, "table-language-info", secondLastRow, 1), "Deutsch (Deutschland)");
        text.assertTextPresent(new TableCellLocator(tester, "table-language-info", secondLastRow - 1, 1), "Deutsch (Deutschland)");

        // check edit as well
        tester.gotoPage("secure/admin/jira/EditApplicationProperties!default.jspa");

        text.assertTextSequence(new WebPageLocator(tester), "Installierte Sprachen", "Deutsch (Deutschland)", "Standardsprache");
        assertions.getJiraFormAssertions().assertSelectElementHasOptionSelected("defaultLocale", "Deutsch (Deutschland)");
    }

    /**
     * A user with a language preference that is different from the system's language gets the list of languages in his preferred language.
     */
    public void testShowsLanguageListInTheUsersLanguage()
    {
        administration.restoreData("TestUserProfileI18n.xml");

        // set the system locale to something other than English just to be different
        administration.generalConfiguration().setJiraLocale("German (Germany)");

        navigation.login(FRED_USERNAME);

        navigation.gotoAdminSection("general_configuration");

        // assert that the page defaults to German
        final int secondLastRow = page.getHtmlTable("table-language-info").getRowCount() - 2;
        text.assertTextPresent(new TableCellLocator(tester, "table-language-info", secondLastRow, 1), "alem\u00e1n (Alemania)");
        text.assertTextPresent(new TableCellLocator(tester, "table-language-info", secondLastRow - 1, 1), "espa\u00f1ol (Espa\u00f1a)");

        // check edit as well
        tester.gotoPage("secure/admin/jira/EditApplicationProperties!default.jspa");

        text.assertTextSequence(new WebPageLocator(tester), "Lenguajes instalados", "espa\u00f1ol (Espa\u00f1a)", "Lenguaje por defecto");
        assertions.getJiraFormAssertions().assertSelectElementHasOptionSelected("defaultLocale", "alem\u00e1n (Alemania)");
    }


    public void testMaxAuthattempts()
    {
        gotoAdmin();
        clickLink("general_configuration");
        //enabled by default to 3
        text.assertTextSequence(xpath("//table//tr[@id='maximumAuthenticationAttemptsAllowed']"), "Maximum Authentication Attempts Allowed", "3");        

        //lets disable it
        clickLinkWithText("Edit Configuration");
        tester.setFormElement("maximumAuthenticationAttemptsAllowed", "xzl");
        submit("Update");
        text.assertTextPresent(xpath("//form[@name='jiraform']//span[@class='errMsg']"), "You must specify a number or leave it blank");

        tester.setFormElement("maximumAuthenticationAttemptsAllowed", "0");
        submit("Update");
        text.assertTextPresent(xpath("//form[@name='jiraform']//span[@class='errMsg']"), "You cannot set the maximum authentication attempts to zero or less");
        
        tester.setFormElement("maximumAuthenticationAttemptsAllowed", "-1");
        submit("Update");
        text.assertTextPresent(xpath("//form[@name='jiraform']//span[@class='errMsg']"), "You cannot set the maximum authentication attempts to zero or less");

        tester.setFormElement("maximumAuthenticationAttemptsAllowed", "10");
        submit("Update");
        text.assertTextSequence(xpath("//table//tr[@id='maximumAuthenticationAttemptsAllowed']"), "Maximum Authentication Attempts Allowed", "10");

    }

}