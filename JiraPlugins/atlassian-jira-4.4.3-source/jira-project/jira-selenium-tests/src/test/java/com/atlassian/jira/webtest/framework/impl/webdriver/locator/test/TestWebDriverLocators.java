package com.atlassian.jira.webtest.framework.impl.webdriver.locator.test;

import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.core.locator.Locators;
import com.atlassian.jira.webtest.framework.impl.webdriver.core.DefaultWebDriverContext;
import com.atlassian.jira.webtest.framework.impl.webdriver.core.WebDriverContext;
import com.atlassian.jira.webtest.framework.impl.webdriver.core.WebDriverPageObjectFactory;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Test WebDriver locators on a real web page.
 *
 * @since v4.2
 */
public class TestWebDriverLocators extends TestCase
{
    private static final Logger log = Logger.getLogger(TestWebDriverLocators.class);

    private static final String TEST_HTML_RESOURCE_NAME = "test.html";
    private static final String TEST_HTML_FILE_NAME = "webdriver-test.html";

    private File pageFile;
    private WebDriver webDriver;
    private WebDriverContext context;
    private WebDriverPageObjectFactory factory;

    @Override
    protected void setUp() throws Exception
    {
        copyPageToTemp();
        webDriver = new HtmlUnitDriver();
        context = new DefaultWebDriverContext(webDriver);
        factory = new WebDriverPageObjectFactory(context);
        webDriver.get("file://" + pageFile.getAbsolutePath());
    }

    private void copyPageToTemp() throws IOException
    {
        InputStream is = getClass().getResourceAsStream(TEST_HTML_RESOURCE_NAME);
        assertNotNull(is);
        pageFile = new File(TEST_HTML_FILE_NAME);
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(pageFile);
            IOUtils.copy(is, fos);
        }
        finally
        {
            IOUtils.closeQuietly(fos);
        }
        log.info("Created temp file: " + pageFile.getAbsolutePath());
    }

    @Override
    protected void tearDown() throws Exception
    {
        FileUtils.deleteQuietly(pageFile);
    }

    public void testSimpleOperations()
    {
        Locator inputLocator = factory.createLocator(Locators.ID, "input");
        assertTrue(inputLocator.element().isPresent().by(100));
        assertTrue(inputLocator.element().isVisible().by(100));
        assertFalse(inputLocator.element().containsText("some").by(400));

        Locator labelLocator = factory.createLocator(Locators.CLASS, "labelClass");
        assertTrue(labelLocator.element().isPresent().by(100));
        assertTrue(labelLocator.element().isVisible().by(100));
        assertTrue(labelLocator.element().containsText("Some").by(100));

        Locator notExisting = factory.createLocator(Locators.XPATH, "//form");
        assertFalse(notExisting.element().isPresent().by(400));
        assertFalse(notExisting.element().isVisible().by(400));
    }

    public void testCssLocator()
    {
        log.warn("Disabled until CSS support for HTMLUnit provided");
//        Locator existing = factory.createLocator(Locators.CSS, "#input");
//        assertTrue(existing.element().isPresent().by(400));
//        assertTrue(existing.element().isVisible().by(400));
//
//        Locator notExisting = factory.createLocator(Locators.CSS, ".nowaaay");
//        assertFalse(notExisting.element().isPresent().by(400));
//        assertFalse(notExisting.element().isVisible().by(400));
    }
}
