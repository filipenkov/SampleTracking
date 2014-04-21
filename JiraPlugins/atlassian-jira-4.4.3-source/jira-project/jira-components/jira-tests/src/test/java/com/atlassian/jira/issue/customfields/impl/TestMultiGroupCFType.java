package com.atlassian.jira.issue.customfields.impl;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.imports.project.customfield.GroupCustomFieldImporter;

/**
 * @since v3.13
 */
public class TestMultiGroupCFType extends ListeningTestCase
{
    @Test
    public void testGetProjectImporter()
    {
        MultiGroupCFType multiGroupCFType = new MultiGroupCFType(null, null, null, null, null, null, null, null);
        assertTrue(multiGroupCFType.getProjectImporter() instanceof GroupCustomFieldImporter);
    }
}
