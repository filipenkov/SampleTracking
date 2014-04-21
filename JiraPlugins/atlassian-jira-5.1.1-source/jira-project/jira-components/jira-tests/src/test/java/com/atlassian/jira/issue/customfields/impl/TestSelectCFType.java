package com.atlassian.jira.issue.customfields.impl;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.imports.project.customfield.SelectCustomFieldImporter;

/**
 * @since v3.13
 */
public class TestSelectCFType extends ListeningTestCase
{
    @Test
    public void testGetProjectImporter() throws Exception
    {
        SelectCFType selectCFType = new SelectCFType(null, null, null, null);
        assertTrue(selectCFType.getProjectImporter() instanceof SelectCustomFieldImporter);
    }
}
