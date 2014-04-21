package com.atlassian.streams.jira;

import java.net.URI;

import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.streams.api.common.uri.Uris;

import static com.google.common.base.Preconditions.checkNotNull;

public class UriProvider
{
    private static final String ISSUE_URI = "/browse/";

    private final ApplicationProperties applicationProperties;
    private final WebResourceManager webResourceManager;

    UriProvider(ApplicationProperties applicationProperties, WebResourceManager webResourceManager)
    {
        this.applicationProperties = checkNotNull(applicationProperties, "applicationProperties");
        this.webResourceManager = checkNotNull(webResourceManager, "webResourceManager");
    }

    public URI getIssueUri(JiraActivityItem activityItem)
    {
        return getIssueUri(activityItem.getIssue());
    }

    public URI getIssueUri(Issue issue)
    {
        return getIssueUri(issue.getKey());
    }

    public URI getIssueUri(String issueKey)
    {
        return URI.create(getIssueUriStr(issueKey));
    }

    private String getIssueUriStr(String issueKey)
    {
        return applicationProperties.getBaseUrl() + ISSUE_URI + issueKey;
    }

    public URI getIssueCommentUri(Comment comment)
    {
        return URI.create(getIssueUriStr(comment.getIssue().getKey()) +
            "?focusedCommentId=" + comment.getId() +
            "&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-" + comment.getId());
    }

    public URI getAttachmentUri(Attachment attachment)
    {
        return URI.create(applicationProperties.getBaseUrl() + "/secure/attachment/" +
            attachment.getId() + "/" + Uris.encode(attachment.getFilename()));
    }

    public URI getThumbnailUri(Thumbnail thumbnail)
    {
        return URI.create(applicationProperties.getBaseUrl() + "/secure/thumbnail/" +
            thumbnail.getAttachmentId() + "/" + Uris.encode(thumbnail.getFilename()));
    }

    public URI getComponentUri(Issue issue, ProjectComponent component)
    {
        return getComponentUri(issue.getProjectObject(), component);
    }

    public URI getComponentUri(Project project, ProjectComponent component)
    {
        return getComponentUri(project.getKey(), component.getId());
    }

    public URI getComponentUri(String projectKey, Long componentId)
    {
        return URI.create(applicationProperties.getBaseUrl() + "/browse/" + projectKey +
                "/component/" + componentId.toString());
    }

    public URI getFixForVersionUri(Version version)
    {
        return getFixForVersionUri(version.getProjectObject().getKey(), version.getId());
    }

    public URI getFixForVersionUri(String projectKey, Long versionId)
    {
        return URI.create(applicationProperties.getBaseUrl() + "/browse/" + projectKey +
                "/fixforversion/" + versionId.toString());
    }

    public URI getWikiRendererCssUri()
    {
        return URI.create(webResourceManager.getStaticPluginResource("jira.webresources:global-static", "wiki-renderer.css", UrlMode.ABSOLUTE)).normalize();
    }

    public URI getBrokenThumbnailUri()
    {
        return URI.create(applicationProperties.getBaseUrl() + "/images/broken_thumbnail.png");
    }
}
