package com.atlassian.jira.imports.project.transformer;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.external.beans.ExternalVoter;

/**
 * @since v3.13
 */
public class TestVoterTransformerImpl extends ListeningTestCase
{
    @Test
    public void testTransform() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getIssueMapper().mapValue("12", "13");

        ExternalVoter externalVoter = new ExternalVoter();
        externalVoter.setVoter("admin");
        externalVoter.setIssueId("12");

        VoterTransformerImpl voterTransformer = new VoterTransformerImpl();
        final ExternalVoter transformedVoter = voterTransformer.transform(projectImportMapper, externalVoter);
        assertEquals("13", transformedVoter.getIssueId());
        assertEquals("admin", externalVoter.getVoter());
    }

    @Test
    public void testTransformNoMappedIssueId() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        ExternalVoter externalVoter = new ExternalVoter();
        externalVoter.setVoter("admin");
        externalVoter.setIssueId("12");

        VoterTransformerImpl voterTransformer = new VoterTransformerImpl();
        assertNull(voterTransformer.transform(projectImportMapper, externalVoter).getIssueId());
    }
    
}
