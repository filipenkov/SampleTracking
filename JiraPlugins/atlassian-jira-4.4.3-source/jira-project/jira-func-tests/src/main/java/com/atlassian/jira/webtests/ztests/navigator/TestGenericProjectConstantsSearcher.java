package com.atlassian.jira.webtests.ztests.navigator;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import org.xml.sax.SAXException;

@WebTest ({ Category.FUNC_TEST, Category.PROJECTS })
public class TestGenericProjectConstantsSearcher extends JIRAWebTest
{
    public static final String VERSION_ONE_VALUE = "10000";
    public static final String VERSION_FOUR_VALUE = "10001";
    public static final String COMPONENT_ONE_VALUE = "10000";
    public static final String COMPONENT_TWO_VALUE = "10001";
    public static final String NO_VERSION_VALUE = "-1";
    public static final String UNRELEASED_VERSION_VALUE = "-2";
    public static final String NO_COMPONENT_VALUE = "-1";
    public static final String NO_VERSIONS = "no versions";
    public static final String UNRELEASED_VERSIONS = "all unreleased versions";
    public static final String NO_COMPONENTS = "no components";

    public TestGenericProjectConstantsSearcher(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestGenericProjectConstantsSearcher.xml");

    }

    public void tearDown()
    {
        restoreBlankInstance();
        super.tearDown();
    }

    public void testLinkMultipleFixForVersions() throws SAXException
    {
        clickLink("find_link");
        selectMultiOptionByValue("fixfor", VERSION_ONE_VALUE);
        selectMultiOptionByValue("fixfor", VERSION_FOUR_VALUE);
        tester.submit("show");
        tester.clickLink("viewfilter");


        assertLinkPresentWithText(VERSION_NAME_ONE);
        assertLinkPresentWithText(VERSION_NAME_FOUR);

        clickLinkWithText(VERSION_NAME_ONE);
        assertEquals("homosapien: New Version 1 - jWebTest JIRA installation", getDialog().getResponse().getTitle().trim());

    }

    public void testLinkMultipleComponents() throws SAXException
    {
        clickLink("find_link");
        selectMultiOptionByValue("component", COMPONENT_ONE_VALUE);
        selectMultiOptionByValue("component", COMPONENT_TWO_VALUE);
        tester.submit("show");
        tester.clickLink("viewfilter");


        assertLinkPresentWithText(COMPONENT_NAME_ONE);
        assertLinkPresentWithText(COMPONENT_NAME_TWO);

        clickLinkWithText(COMPONENT_NAME_ONE);
        assertEquals("homosapien: New Component 1 - jWebTest JIRA installation", getDialog().getResponse().getTitle().trim());
    }

    public void testNoLinkMultipleAffectsVersions()
    {
        clickLink("find_link");
        selectMultiOptionByValue("version", VERSION_ONE_VALUE);
        selectMultiOptionByValue("version", VERSION_FOUR_VALUE);
        tester.submit("show");
        tester.clickLink("viewfilter");


        assertTextPresent(VERSION_NAME_ONE);
        assertLinkNotPresentWithText(VERSION_NAME_ONE);
        assertTextPresent(VERSION_NAME_FOUR);
        assertLinkNotPresentWithText(VERSION_NAME_FOUR);
    }

    public void testNoLinkUnreleasedNoComponentsAndVersions()
    {
        clickLink("find_link");
        selectMultiOptionByValue("fixfor", NO_VERSION_VALUE);
        selectMultiOptionByValue("fixfor", UNRELEASED_VERSION_VALUE);
        selectMultiOptionByValue("component", NO_COMPONENT_VALUE);
        selectMultiOptionByValue("version", NO_VERSION_VALUE);
        selectMultiOptionByValue("version", UNRELEASED_VERSION_VALUE);
        tester.submit("show");
        tester.clickLink("viewfilter");


        assertTextPresent(NO_VERSIONS);
        assertLinkNotPresentWithText(NO_VERSIONS);
        assertTextPresent(UNRELEASED_VERSIONS);
        assertLinkNotPresentWithText(UNRELEASED_VERSIONS);
        assertTextPresent(NO_COMPONENTS);
        assertLinkNotPresentWithText(NO_COMPONENTS);
    }
}
