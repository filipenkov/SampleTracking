package com.atlassian.jira.webtest.unittests.selenium;

import com.atlassian.jira.functest.config.ConfigurationChecker;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Test to ensure that Selenium test XMLs are clean.
 *
 * @since v4.0
 */
public class TestSeleniumXmlBackups extends TestCase
{
    public void testXmlData() throws Exception
    {
        final ConfigurationChecker configurationChecker = ConfigurationChecker.createDefaultChecker(xmlsLocation());
        final ConfigurationChecker.CheckResult checkResult = configurationChecker.check();

        if (checkResult.hasErrors())
        {
            fail("Func Test XML contains errors. Check out https://extranet.atlassian.com/x/GAW7b for details on what to do.\n" + checkResult.getFormattedMessage());
        }
    }

    private File xmlsLocation() throws URISyntaxException {
        URL markerUrl = getClass().getClassLoader().getResource("xml/selenium_test_xml_resources");
        final File markerFile = new File(markerUrl.toURI());
        assertTrue(markerFile.exists());
        final File xml = markerFile.getParentFile();
        assertTrue(xml.isDirectory());
        assertTrue(FileUtils.listFiles(xml, new String[] { "xml" }, true).size() > 0);
        return markerFile.getParentFile();
    }
}
