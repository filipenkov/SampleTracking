package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.jira.imports.project.customfield.NoTransformationCustomFieldImporter;

/**
 * @since v3.13
 */
public class TestURLCFType extends ListeningTestCase
{
    @Test
    public void testGetProjectImporter() throws Exception
    {
        URLCFType urlcfType = new URLCFType(null, null);
        assertTrue(urlcfType.getProjectImporter() instanceof NoTransformationCustomFieldImporter);
    }

    // JRA-14998 - URLs should be trimmed before validating
    @Test
    public void testUrlIsTrimmed() throws Exception
    {
        URLCFType urlcfType = new URLCFType(null, null);
        assertEquals("http://www.atlassian.com", urlcfType.getSingularObjectFromString("  http://www.atlassian.com  "));
        assertEquals("http://www.atlassian.com", urlcfType.getSingularObjectFromString("http://www.atlassian.com"));
    }
}
