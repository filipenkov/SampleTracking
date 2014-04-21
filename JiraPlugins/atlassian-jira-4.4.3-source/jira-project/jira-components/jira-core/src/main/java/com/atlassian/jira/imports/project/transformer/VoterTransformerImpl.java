package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.external.beans.ExternalVoter;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;

/**
 * @since v3.13
 */
public class VoterTransformerImpl implements VoterTransformer
{
    public ExternalVoter transform(final ProjectImportMapper projectImportMapper, final ExternalVoter voter)
    {
        final String mappedId = projectImportMapper.getIssueMapper().getMappedId(voter.getIssueId());
        final ExternalVoter transformedVoter = new ExternalVoter();
        transformedVoter.setVoter(voter.getVoter());
        transformedVoter.setIssueId(mappedId);
        return transformedVoter;
    }
}
