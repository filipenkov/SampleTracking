package com.atlassian.jira.plugin.viewissue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.LinkCollection;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.plugin.PluginParseException;

import java.util.List;
import java.util.Map;

/**
 * Context Provider for the Link block on the view issue page.
 *
 * @since v4.4
 */
public class LinkBlockContextProvider implements CacheableContextProvider
{
    private final IssueLinkManager issueLinkManager;
    private final JiraAuthenticationContext authenticationContext;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final PermissionManager permissionManager;

    public LinkBlockContextProvider(IssueLinkManager issueLinkManager, JiraAuthenticationContext authenticationContext,
            FieldVisibilityManager fieldVisibilityManager, PermissionManager permissionManager)
    {
        this.issueLinkManager = issueLinkManager;
        this.authenticationContext = authenticationContext;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.permissionManager = permissionManager;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final MapBuilder<String, Object> paramsBuilder = MapBuilder.newBuilder(context);

        final Issue issue = (Issue) context.get("issue");
        final User loggedInUser = authenticationContext.getLoggedInUser();
        final I18nHelper i18n = authenticationContext.getI18nHelper();


        final LinkCollection linkCollection = issueLinkManager.getLinkCollection(issue, loggedInUser);

        final CollectionBuilder<SimpleLinkedIssueType> issueLinkTypes = CollectionBuilder.newBuilder();

        for (IssueLinkType issueLinkType : linkCollection.getLinkTypes())
        {
            final List<Issue> outwardIssues = linkCollection.getOutwardIssues(issueLinkType.getName());
            final List<Issue> inwardIssues = linkCollection.getInwardIssues(issueLinkType.getName());
            issueLinkTypes.add(new SimpleLinkedIssueType(issueLinkType, convertIssues(outwardIssues), convertIssues(inwardIssues), i18n));
        }

        paramsBuilder.add("linkTypes", issueLinkTypes.asList());
        paramsBuilder.add("hasLinks", linkCollection.isDisplayLinkPanel());
        paramsBuilder.add("canLink", permissionManager.hasPermission(Permissions.LINK_ISSUE, issue, loggedInUser));

        return paramsBuilder.toMap();
    }


    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        final Issue issue = (Issue) context.get("issue");
        final User user = authenticationContext.getLoggedInUser();

        return issue.getId() + "/" + (user == null ? "" : user.getName());
    }

    private List<SimpleLinkedIssue> convertIssues(List<Issue> issues)
    {
        final CollectionBuilder<SimpleLinkedIssue> linkedIssues = CollectionBuilder.newBuilder();
        if (issues != null)
        {
            for (Issue issue : issues)
            {
                linkedIssues.add(new SimpleLinkedIssue(issue, fieldVisibilityManager));
            }
        }

        return linkedIssues.asList();
    }


    public static class SimpleLinkedIssueType
    {
        private final IssueLinkType linkType;
        private final List<SimpleLinkedIssue> outwardLinks;
        private final List<SimpleLinkedIssue> inwardLinks;
        private final I18nHelper i18n;

        public SimpleLinkedIssueType(IssueLinkType linkType, List<SimpleLinkedIssue> outwardLinks, List<SimpleLinkedIssue> inwardLinks, I18nHelper i18n)
        {
            this.linkType = linkType;
            this.outwardLinks = outwardLinks;
            this.inwardLinks = inwardLinks;
            this.i18n = i18n;
        }

        public Long getId()
        {
            return linkType.getId();
        }

        public String getName()
        {
            return linkType.getName();
        }

        public String getOutwardDescriptionHtml()
        {
            return i18n.getText("common.concepts.linkDescription", "<strong>" + linkType.getOutward() + "</strong>");
        }

        public boolean hasOutwardLinks()
        {
            return outwardLinks != null && !outwardLinks.isEmpty();
        }

        public List<SimpleLinkedIssue> getOutwardLinks()
        {
            return outwardLinks;
        }

        public String getInwardDescriptionHtml()
        {
            return i18n.getText("common.concepts.linkDescription", "<strong>" + linkType.getInward() + "</strong>");
        }

        public List<SimpleLinkedIssue> getInwardLinks()
        {
            return inwardLinks;
        }

        public boolean hasInwardLinks()
        {
            return inwardLinks != null && !inwardLinks.isEmpty();
        }
    }

    public static class SimpleLinkedIssue
    {
        private final Issue linkedIssue;
        private final FieldVisibilityManager fieldVisibilityManager;

        public SimpleLinkedIssue(Issue linkedIssue, FieldVisibilityManager fieldVisibilityManager)
        {
            this.linkedIssue = linkedIssue;
            this.fieldVisibilityManager = fieldVisibilityManager;
        }

        public Long getId()
        {
            return linkedIssue.getId();
        }

        public String getSummary()
        {
            return linkedIssue.getSummary();
        }

        public Priority getPriority()
        {
            if (fieldVisibilityManager.isFieldHidden("priority", linkedIssue))
            {
                return null;
            }
            return linkedIssue.getPriorityObject();
        }

        public Status getStatus()
        {
            return linkedIssue.getStatusObject();
        }

        public Resolution getResolution()
        {
            return linkedIssue.getResolutionObject();
        }

        public String getKey()
        {
            return linkedIssue.getKey();
        }
    }
}
