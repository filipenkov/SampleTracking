package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.form.FormParameterUtil;
import com.atlassian.jira.webtests.JIRAWebTest;
import org.w3c.dom.Node;

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

    @Override
    public void tearDown()
    {
        administration.generalConfiguration().setJiraLocaleToSystemDefault();
        super.tearDown();
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
        assertTextNotPresent("http://example.url.com:8090/");

        // Check that both whitespace and trailing slashes are stripped.
        clickLinkWithText("Edit Configuration");
        setFormElement("baseURL", "http://example.url.com:8090 /");
        submit("Update");
        assertTextPresent("http://example.url.com:8090");
        assertTextNotPresent("http://example.url.com:8090 /");
        assertTextNotPresent("http://example.url.com:8090 ");
    }

    public void testBaseUrlValidation()
    {
        String[] invalidURLs = {
                "",
                "http",
                "http://",
                "http://*&^%$#@",
                "http://example url.com:8090",
                "ldap://example.url.com:8090",
                "http://example.url.com:not_a_port",
                "http://example.url.com:8090/invalid path"
        };

        gotoAdmin();
        clickLink("general_configuration");
        clickLinkWithText("Edit Configuration");

        for (String URL : invalidURLs)
        {
            setFormElement("baseURL", URL);
            submit("Update");
            assertTextPresent("You must set a valid base URL.");
        }
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
     * Items in the language list should be localised to that same language
     * (e.g. "German" should be in German, "Japanese" in Japanese, etc.).
     */
    public void testLocalisesLanguageListItems()
    {
        administration.restoreData("TestUserProfileI18n.xml");
        tester.gotoPage("secure/admin/jira/EditApplicationProperties!default.jspa");

        String[] languageOptions = tester.getDialog().getOptionsFor("defaultLocale");
        String[] expectedOptions = new String[] {
                "English (Australia)",
                "Deutsch (Deutschland)",
                "\u65e5\u672c\u8a9e (\u65e5\u672c)"
        };

        // Just checking for equality won't work as the JVM's default language
        // is marked as the "default" and that could change between systems.
        for (String expectedOption : expectedOptions)
        {
            boolean found = false;
            for (String languageOption : languageOptions)
            {
                if (languageOption.indexOf(expectedOption) == 0)
                {
                    found = true;
                    break;
                }
            }

            if (!found)
            {
                fail();
            }
        }
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

    /**
     * JRA-23891 Test that a language not in the defaultLocales fails validation
     */
    public void testLocaleWhitelistValidation()
    {
        String[] invalidLocales = new String[] {
            "cs_CZ",
            "AF_ZA",
            "<script>alert()</script>"
        };
        administration.restoreData("TestUserProfileI18n.xml");
        getNavigation().gotoAdmin();
        clickLink("general_configuration");
        clickLinkWithText("Edit Configuration");
        // need to use FormParameterUtils as you can't submit invalid options through WebUnit
        for (String locale : invalidLocales)
        {
            FormParameterUtil formHelper = new FormParameterUtil(tester, "jiraform", "Update");
            formHelper.setFormElement("defaultLocale", locale);
            Node document = formHelper.submitForm();
            Locator locator = new XPathLocator(document,"//*[@class='errMsg']" );

            assertTrue(locator.getText().contains(String.format("Locale '%s' is not a valid locale.",locale)));
            tester.reset();
        }
    }

}