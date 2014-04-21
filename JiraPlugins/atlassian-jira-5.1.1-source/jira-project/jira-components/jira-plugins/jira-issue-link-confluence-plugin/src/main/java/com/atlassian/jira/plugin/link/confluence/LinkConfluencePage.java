package com.atlassian.jira.plugin.link.confluence;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.application.confluence.ConfluenceApplicationType;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.issue.link.RemoteIssueLinkBuilder;
import com.atlassian.jira.plugin.link.applinks.RemoteResponse;
import com.atlassian.jira.plugin.link.confluence.service.ConfluenceGlobalIdFactoryImpl;
import com.atlassian.jira.plugin.link.confluence.service.ConfluencePageService;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.UriMatcher;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.web.action.issue.AbstractIssueLinkAction;
import com.atlassian.sal.api.net.ResponseException;
import com.google.common.collect.ImmutableList;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;

public class LinkConfluencePage extends AbstractIssueLinkAction
{
    private static final String TITLE = "Wiki Page";
    private static final String RELATIONSHIP = "Wiki Page";

    private String pageUrl;
    private ApplicationLink appLink;
    private Collection<ApplicationLink> confluenceAppLinks;

    private final ApplicationLinkService applicationLinkService;
    private final ConfluencePageService confluencePageService;

    public LinkConfluencePage(
            final SubTaskManager subTaskManager,
            final FieldScreenRendererFactory fieldScreenRendererFactory,
            final FieldManager fieldManager,
            final ProjectRoleManager projectRoleManager,
            final CommentService commentService,
            final UserUtil userUtil,
            final RemoteIssueLinkService remoteIssueLinkService,
            final EventPublisher eventPublisher,
            final ApplicationLinkService applicationLinkService,
            final ConfluencePageService confluencePageService)
    {
        super(subTaskManager, fieldScreenRendererFactory, fieldManager, projectRoleManager, commentService, userUtil, remoteIssueLinkService, eventPublisher);
        this.applicationLinkService = applicationLinkService;
        this.confluencePageService = confluencePageService;
    }

    @Override
    public boolean isValidToView()
    {
        return super.isValidToView()
                && !getConfluenceAppLinks().isEmpty();
    }

    protected void doValidation()
    {
        // Validate comment and permissions
        super.doValidation();

        // Make sure the URL belongs to an application link
        appLink = validatePageUrl(pageUrl);

        String pageId = null;
        if (!hasAnyErrors())
        {
            pageId = getPageId(pageUrl, appLink);
        }

        if (!hasAnyErrors())
        {
            if (pageId == null)
            {
                addErrorMessage(getText("addconfluencelink.error.pageid.notfound"));
            }
        }

        if (!hasAnyErrors())
        {
            final String globalId = ConfluenceGlobalIdFactoryImpl.encode(appLink.getId(), pageId);

            final RemoteIssueLink remoteIssueLink = new RemoteIssueLinkBuilder()
                    .issueId(getIssue().getLong("id"))
                    .url(buildPageUrl(appLink, pageId))
                    .title(TITLE)
                    .globalId(globalId)
                    .relationship(RELATIONSHIP)
                    .applicationType(RemoteIssueLink.APPLICATION_TYPE_CONFLUENCE)
                    .applicationName(appLink.getName())
                    .build();

            validationResult = remoteIssueLinkService.validateCreate(getLoggedInUser(), remoteIssueLink);

            if (!validationResult.isValid())
            {
                mapErrors(validationResult.getErrorCollection());
                addErrorCollection(validationResult.getErrorCollection());
            }
        }
    }

    private static String buildPageUrl(final ApplicationLink appLink, final String pageId)
    {
        return new UrlBuilder(appLink.getDisplayUrl().toASCIIString())
                .addPathUnsafe("pages/viewpage.action")
                .addParameter("pageId", pageId)
                .asUrlString();
    }

    private void mapErrors(final ErrorCollection errorCollection)
    {
        // Convert field errors to error messages so that they will appear on the page
        // Hide the field name (key), as this will mean nothing to users
        for (final Map.Entry<String, String> entry : errorCollection.getErrors().entrySet())
        {
            if ("globalId".equals(entry.getKey()))
            {
                // Give a more meaningful message when a duplicate link exists
                errorCollection.addErrorMessage(getText("addconfluencelink.error.duplicate"));
            }
            else
            {
                errorCollection.addErrorMessage(entry.getValue());
            }
        }
    }

    private ApplicationLink validatePageUrl(final String pageUrl)
    {
        if (StringUtils.isBlank(pageUrl))
        {
            addError("pageUrl", getText("addconfluencelink.error.url.required"));
            return null;
        }

        final URI pageUri;
        try
        {
            pageUri = new URI(pageUrl);
        }
        catch (URISyntaxException e)
        {
            addError("pageUrl", getText("addconfluencelink.error.url.invalid"));
            return null;
        }

        final ApplicationLink appLink = getAppLink(pageUri);
        if (appLink == null)
        {
            addErrorMessage(getText("addconfluencelink.error.no.matching.app.link", "<a href='#' class='confluence-search-trigger'>", "</a>"));
        }

        return appLink;
    }

    private ApplicationLink getAppLink(final URI pageUri)
    {
        // TODO sort by length to cover case where mulitple base URLs match, e.g. something.com and something.com/confluence
        for (final ApplicationLink appLink : getConfluenceAppLinks())
        {
            if (UriMatcher.isBaseEqual(appLink.getDisplayUrl(), pageUri))
            {
                return appLink;
            }
        }

        return null;
    }

    private String getPageId(final String pageUrl, final ApplicationLink appLink)
    {
        // Always use a GET to fetch the pageId (see: JRADEV-8435)
        try
        {
            final RemoteResponse<String> response = confluencePageService.getPageId(appLink, pageUrl);
            if (response.isSuccessful())
            {
                return response.getEntity();
            }

            switch (response.getStatusCode())
            {
                case HttpStatus.SC_FORBIDDEN:
                {
                    addErrorMessage(getText("addconfluencelink.error.page.forbidden"));
                    break;
                }
                case HttpStatus.SC_UNAUTHORIZED:
                {
                    handleCredentialsRequired();
                    break;
                }
                default:
                {
                    addErrorMessage(getText("addconfluencelink.error.pageid.notfound"));
                    log.error("Invalid response from getting the pageId: " + response.getStatusCode() + ": " + response.getStatusText());
                }
            }
        }
        catch (final CredentialsRequiredException e)
        {
            handleCredentialsRequired();
        }
        catch (final ResponseException e)
        {
            addErrorMessage(getText("addconfluencelink.error.pageid.notfound"));
            log.error("Invalid response from getting the pageId: " + e.getMessage());
        }

        return null;
    }

    public String doDefault() throws Exception
    {
        // Set default value
        pageUrl = "http://";

        return super.doDefault();
    }

    @RequiresXsrfCheck
    protected String doExecute()
    {
        final RemoteIssueLinkService.RemoteIssueLinkResult result = createLink();

        if (!result.isValid())
        {
            addErrorCollection(result.getErrorCollection());
            return ERROR;
        }

        createComment();

        return returnComplete(getRedirectUrl());
    }

    @SuppressWarnings("unused")
    public String getPageUrl()
    {
        return pageUrl;
    }

    @SuppressWarnings("unused")
    public void setPageUrl(String pageUrl)
    {
        this.pageUrl = pageUrl;
    }

    @SuppressWarnings("unused")
    public String getAppId()
    {
        if (appLink != null)
        {
            return appLink.getId().get();
        }

        return "";
    }

    @SuppressWarnings("unused")
    public Collection<ApplicationLink> getConfluenceAppLinks()
    {
        if (confluenceAppLinks == null)
        {
            final Iterable<ApplicationLink> iterable = applicationLinkService.getApplicationLinks(ConfluenceApplicationType.class);
            confluenceAppLinks = ImmutableList.copyOf(iterable);
        }

        return confluenceAppLinks;
    }
}
