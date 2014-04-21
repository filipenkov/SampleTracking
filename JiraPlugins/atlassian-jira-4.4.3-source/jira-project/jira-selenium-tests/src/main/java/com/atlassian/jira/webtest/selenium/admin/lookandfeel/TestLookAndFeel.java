package com.atlassian.jira.webtest.selenium.admin.lookandfeel;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.form.FormParameterUtil;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;

/**
*
 * @since v4.0
 */
@WebTest({Category.SELENIUM_TEST })
public class TestLookAndFeel extends JiraSeleniumTest
{
    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestIssueTypeSchemes.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().gotoAdmin();
        getNavigator().clickAndWaitForPageLoad("lookandfeel");
        getNavigator().clickAndWaitForPageLoad("editlookandfeel");
        getNavigator().click("logo-option-jira");
        client.clickButton("Update", true);
        getNavigator().clickAndWaitForPageLoad("editlookandfeel");
    }

    public void testColorPicker () throws InterruptedException
    {
        String currentHexVal = client.getValue("name=topTextColour");
        client.click("colorpicker-topTextColour");
        client.selectWindow("colorpicker");
        assertThat.elementPresentByTimeout("colorVal", 5000);
        if (!client.getValue("name=colorVal").equals(currentHexVal)) {
            throw new RuntimeException("Expected field colorVal to be '" + currentHexVal + "' but recieved " + client.getValue("colorVal"));
        }
        client.click("//img[@alt='#cc00cc']", false);
        String newHexValue = client.getValue("name=colorVal");
        if (!newHexValue.equals("#cc00cc")) {
            throw new RuntimeException("Expected field colorVal to be #cc00cc but recieved " + newHexValue);
        }
        client.click("name=OK", false);
        client.selectWindow("");
        if (!client.getValue("name=topTextColour").equals(newHexValue)) {
            throw new RuntimeException();
        }
        currentHexVal = client.getValue("name=topTextColour");
        client.click("colorpicker-topTextColour");
        client.selectWindow("colorpicker");
        assertThat.elementPresentByTimeout("colorVal", 5000);
        client.click("//img[@alt='#cc9966']", false);
        newHexValue = client.getValue("name=colorVal");
        if (!newHexValue.equals("#cc9966")) {
            throw new RuntimeException("Expected field colorVal to be #cc00cc but recieved " + newHexValue);
        }
        client.click("name=Cancel", false);
        client.selectWindow("");
        if (client.getValue("name=topTextColour").equals(newHexValue)) {
            throw new RuntimeException();
        }
    }

    public void testInvalidMimeTypeLogoUpload()
    {
        String filePath = getEnvironmentData().getXMLDataLocation().getAbsolutePath() + "/TestIssueTypeSchemes.xml";
        client.clickElementWithCss("#logo-option-upload");
        client.typeInElementWithCss("#logo_file",filePath);
        client.clickButton("Update", true);
        assertThat.elementPresent("jquery=.aui-message.error");
        assertThat.elementHasText("jquery=.aui-message.error", "Failed to upload image from file TestIssueTypeSchemes.xml.");
        assertThat.elementHasText("jquery=.aui-message.error", "The MIME type text/xml is unsupported.");
    }


    public void testInvalidMimeTypeFaviconUpload()
    {
        String filePath = getEnvironmentData().getXMLDataLocation().getAbsolutePath() + "/TestIssueTypeSchemes.xml";
        client.clickElementWithCss("#favicon-option-upload");
        client.typeInElementWithCss("#favicon_file",filePath);
        client.clickButton("Update", true);
        assertThat.elementPresent("jquery=.aui-message.error");
        assertThat.elementHasText("jquery=.aui-message.error", "Failed to upload image from file TestIssueTypeSchemes.xml.");
        assertThat.elementHasText("jquery=.aui-message.error", "The MIME type text/xml is unsupported.");
    }

    public void testRelativeUrlUpload()
    {
        client.clickElementWithCss("#logo-option-url");
        client.typeInElementWithCss("#logo_url","/images/intro-gadget.png");
        client.clickButton("Update", true);
        client.waitForPageToLoad();
        assertThat.elementPresent("jquery=img[src$='/jira-logo-scaled.png']");
    }

    public void testRelativeUrlUploadWithNoslash()
    {
        client.clickElementWithCss("#logo-option-url");
        client.typeInElementWithCss("#logo_url","images/intro-gadget.png");
        client.clickButton("Update", true);
        client.waitForPageToLoad();
        assertThat.elementPresent("jquery=img[src$='/jira-logo-scaled.png']");
    }
}