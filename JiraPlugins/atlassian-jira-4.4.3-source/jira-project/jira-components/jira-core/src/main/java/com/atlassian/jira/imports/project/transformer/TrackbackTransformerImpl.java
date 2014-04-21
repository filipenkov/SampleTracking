package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.external.beans.ExternalTrackback;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;

/**
 * @since v3.13
 */
public class TrackbackTransformerImpl implements TrackbackTransformer
{
    public ExternalTrackback transform(final ProjectImportMapper projectImportMapper, final ExternalTrackback trackback)
    {
        final String newIssueId = projectImportMapper.getIssueMapper().getMappedId(trackback.getIssueId());
        return new ExternalTrackback(null, newIssueId, trackback.getUrl(), trackback.getBlogName(), trackback.getExcerpt(), trackback.getTitle(),
            trackback.getCreated());
    }
}
