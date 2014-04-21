package com.atlassian.jira.issue.customfields.impl;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.imports.project.customfield.UserCustomFieldImporter;

/**
 * @since v3.13
 */
public class TestMultiUserCFType extends ListeningTestCase
{
    @Test
    public void test()
    {
        MultiUserCFType multiUserCFType = new MultiUserCFType(null, null, null, null, null, null, null, null);
        assertTrue(multiUserCFType.getProjectImporter() instanceof UserCustomFieldImporter);
    }
}
