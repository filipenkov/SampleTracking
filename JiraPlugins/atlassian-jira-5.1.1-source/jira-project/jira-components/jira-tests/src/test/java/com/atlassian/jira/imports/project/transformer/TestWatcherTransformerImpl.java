package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.external.beans.ExternalWatcher;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;

/**
 * @since v3.13
 */
public class TestWatcherTransformerImpl extends ListeningTestCase
{
    @Test
    public void testTransform() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getIssueMapper().mapValue("12", "13");

        ExternalWatcher externalWatcher = new ExternalWatcher();
        externalWatcher.setWatcher("admin");
        externalWatcher.setIssueId("12");

        WatcherTransformerImpl watcherTransformer = new WatcherTransformerImpl();
        final ExternalWatcher transformedWatcher = watcherTransformer.transform(projectImportMapper, externalWatcher);
        assertEquals("13", transformedWatcher.getIssueId());
        assertEquals("admin", externalWatcher.getWatcher());
    }

    @Test
    public void testTransformNoMappedIssueId() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        ExternalWatcher externalWatcher = new ExternalWatcher();
        externalWatcher.setWatcher("admin");
        externalWatcher.setIssueId("12");

        WatcherTransformerImpl watcherTransformer = new WatcherTransformerImpl();
        assertNull(watcherTransformer.transform(projectImportMapper, externalWatcher).getIssueId());
    }
}
