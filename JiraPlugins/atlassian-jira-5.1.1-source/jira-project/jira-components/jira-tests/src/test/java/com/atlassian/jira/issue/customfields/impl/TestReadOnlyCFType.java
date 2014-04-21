package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.jira.imports.project.customfield.NoTransformationCustomFieldImporter;

/**
 * @since v3.13
 */
public class TestReadOnlyCFType extends ListeningTestCase
{
    @Test
    public void testGetProjectImporter() throws Exception
    {
        ReadOnlyCFType readOnlyCFType = new ReadOnlyCFType(null, null);
        assertTrue(readOnlyCFType.getProjectImporter() instanceof NoTransformationCustomFieldImporter);
    }

}
