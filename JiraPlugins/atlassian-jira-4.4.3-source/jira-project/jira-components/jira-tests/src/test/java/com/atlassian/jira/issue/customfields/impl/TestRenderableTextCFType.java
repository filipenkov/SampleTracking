package com.atlassian.jira.issue.customfields.impl;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.imports.project.customfield.NoTransformationCustomFieldImporter;
import com.atlassian.jira.local.ListeningTestCase;

/**
 * @since v3.13
 */
public class TestRenderableTextCFType extends ListeningTestCase
{
    @Test
    public void testGetProjectImporter() throws Exception
    {
        RenderableTextCFType renderableTextCFType = new RenderableTextCFType(null, null, null);
        assertTrue(renderableTextCFType.getProjectImporter() instanceof NoTransformationCustomFieldImporter);
    }

}
