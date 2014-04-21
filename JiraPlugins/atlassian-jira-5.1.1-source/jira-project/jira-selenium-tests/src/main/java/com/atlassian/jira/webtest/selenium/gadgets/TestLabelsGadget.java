package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Selenium test for the Assigned to Me Gadget.
 */
@WebTest({Category.SELENIUM_TEST })
public class TestLabelsGadget extends GadgetTest
{
    public void onSetUp()
    {
        super.onSetUp();
        addGadget("Labels Gadget", "Labels");
    }

    @Override
    protected void restoreGadgetData()
    {
        restoreData("TestLabelsGadget.xml");
    }

    public void testConfigureAndView()
    {
        _testDefault();
        _testChangeField();
        _testFieldWithNolabels();
    }

    private void _testDefault()
    {
        waitForGadgetConfiguration();
        submitGadgetConfig();
        waitForGadgetView("labels-content");
        waitFor(2500);
        
        assertThat.textPresent("A-Z");
        assertThat.textPresent("aa");
        assertThat.textPresent("bb");
        assertThat.textPresent("cc");
        assertThat.textPresent("dd");
        assertThat.textPresent("duffy");
        assertThat.textPresent("duck");
        assertThat.textPresent("mouse");
        assertThat.textPresent("mickey");
    }

    private void _testChangeField()
    {
        clickConfigButton();
        waitForGadgetConfiguration();
        getSeleniumClient().select("fieldId", "Epic");
        submitGadgetConfig();
        waitForGadgetView("labels-content");
        waitFor(2500);

        assertThat.textPresent("A-Z");
        assertThat.textPresent("john");
        assertThat.textPresent("lewis");
        assertThat.textPresent("marylyn");
        assertThat.textPresent("monroe");
    }

    private void _testFieldWithNolabels()
    {
        clickConfigButton();
        waitForGadgetConfiguration();
        getSeleniumClient().select("fieldId", "Stuff");
        submitGadgetConfig();
        waitForGadgetView("labels-content");
        waitFor(2500);

        assertThat.textPresent("No Labels Found");

        //now try a project with some labels for this field
        clickConfigButton();
        waitForGadgetConfiguration();
        getSeleniumClient().select("projectid", "monkey");
        submitGadgetConfig();
        waitForGadgetView("labels-content");
        waitFor(2500);

        assertThat.textPresent("A-Z");
        assertThat.textPresent("corn");
        assertThat.textPresent("rice");
    }

}