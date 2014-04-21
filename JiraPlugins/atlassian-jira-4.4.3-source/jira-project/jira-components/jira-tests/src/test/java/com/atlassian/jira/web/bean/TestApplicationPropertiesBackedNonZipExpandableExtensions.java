package com.atlassian.jira.web.bean;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;

/**
 * Responsible for holding unit tests for {@link ApplicationPropertiesBackedNonZipExpandableExtensions}
 *
 * @since v4.2
 */
public class TestApplicationPropertiesBackedNonZipExpandableExtensions extends MockControllerTestCase
{
    ApplicationProperties mockApplicationProperties;

    ApplicationPropertiesBackedNonZipExpandableExtensions nonZipExpandableExtensions;

    @Before
    public void setUp() throws Exception
    {
        mockApplicationProperties = getMock(ApplicationProperties.class);
    }

    @Test
    public void testContainsDoesNotAcceptNullExtensions() throws Exception
    {
        boolean thrown = false;

        nonZipExpandableExtensions = instantiate(ApplicationPropertiesBackedNonZipExpandableExtensions.class);

        try
        {
            nonZipExpandableExtensions.contains(null);
        }
        catch (IllegalArgumentException expectedExceptions)
        {
            thrown = true;
        }
        if (!thrown)
        {
            fail("ApplicationPropertiesBackedNonZipExpandableExtensions.contains(String extension) "
                    + "should not accept a null value for the extension.");
        }
    }

    @Test
    public void testContainsUsesADefaultExtensionsListWhenTheApplicationPropertyIsNotDefined() throws Exception
    {
        expect(mockApplicationProperties.
                getDefaultBackedString(APKeys.JIRA_ATTACHMENT_DO_NOT_EXPAND_AS_ZIP_EXTENSIONS_LIST)).
                andReturn(null).times(2);

        nonZipExpandableExtensions = instantiate(ApplicationPropertiesBackedNonZipExpandableExtensions.class);

        assertTrue(nonZipExpandableExtensions.contains("docx"));
        assertFalse(nonZipExpandableExtensions.contains("jar"));
    }

    @Test
    public void testContainsPrefersTheExtensionsSpecifiedInTheJiraApplicationPropertyWhenItIsSpecified()
    {
        expect(mockApplicationProperties.
                getDefaultBackedString(APKeys.JIRA_ATTACHMENT_DO_NOT_EXPAND_AS_ZIP_EXTENSIONS_LIST)).andReturn("xlsx").anyTimes();

        nonZipExpandableExtensions = instantiate(ApplicationPropertiesBackedNonZipExpandableExtensions.class);

        assertTrue(nonZipExpandableExtensions.contains("xlsx"));
        assertFalse(nonZipExpandableExtensions.contains("docx"));
    }

    @Test
    public void testContainsHandlesWhiteSpaceInTheExtensionsList()
    {
        expect(mockApplicationProperties.
                getDefaultBackedString(APKeys.JIRA_ATTACHMENT_DO_NOT_EXPAND_AS_ZIP_EXTENSIONS_LIST)).andReturn("xlsx , docx,   pptx").anyTimes();

        nonZipExpandableExtensions = instantiate(ApplicationPropertiesBackedNonZipExpandableExtensions.class);

        assertTrue(nonZipExpandableExtensions.contains("xlsx"));
        assertTrue(nonZipExpandableExtensions.contains("docx"));
        assertTrue(nonZipExpandableExtensions.contains("pptx"));
    }

    @Test
    public void testContainsComparesExtensionInACaseInsensitiveManner()
    {
        expect(mockApplicationProperties.
                getDefaultBackedString(APKeys.JIRA_ATTACHMENT_DO_NOT_EXPAND_AS_ZIP_EXTENSIONS_LIST)).andReturn("xlsx , docx,   pptx").anyTimes();

        nonZipExpandableExtensions = instantiate(ApplicationPropertiesBackedNonZipExpandableExtensions.class);

        assertTrue(nonZipExpandableExtensions.contains("xLSx"));
        assertTrue(nonZipExpandableExtensions.contains("docX"));
        assertTrue(nonZipExpandableExtensions.contains("Pptx"));
    }

    @Test
    public void testContainsIsAbleToUseAnEmptyApplicationPropertyToEnableTurningOffSelectiveZipFileExpansion()
            throws Exception
    {
        expect(mockApplicationProperties.
                getDefaultBackedString(APKeys.JIRA_ATTACHMENT_DO_NOT_EXPAND_AS_ZIP_EXTENSIONS_LIST)).andReturn("").anyTimes();

        nonZipExpandableExtensions = instantiate(ApplicationPropertiesBackedNonZipExpandableExtensions.class);

        assertFalse(nonZipExpandableExtensions.contains("xlsx"));
        assertFalse(nonZipExpandableExtensions.contains("docx"));
    }
}
