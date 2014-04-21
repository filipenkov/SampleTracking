package com.atlassian.jira.plugin.viewissue;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.fields.util.FieldPredicates;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.plugin.profile.UserFormatManager;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.plugin.PluginParseException;
import com.opensymphony.user.User;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Context Provider for the People Block pnn view issue
 *
 * @since v4.4
 */
public class PeopleBlockContextProvider implements CacheableContextProvider
{
    private final ApplicationProperties applicationProperties;
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authenticationContext;
    private final WatcherManager watcherManager;
    private final UserFormatManager userFormatManager;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final FieldScreenRendererFactory fieldScreenRendererFactory;
    private final VoteManager voteManager;

    public PeopleBlockContextProvider(ApplicationProperties applicationProperties, PermissionManager permissionManager,
            JiraAuthenticationContext authenticationContext, WatcherManager watcherManager,
            UserFormatManager userFormatManager, FieldVisibilityManager fieldVisibilityManager,
            FieldScreenRendererFactory fieldScreenRendererFactory, VoteManager voteManager) {
        this.applicationProperties = applicationProperties;
        this.permissionManager = permissionManager;
        this.authenticationContext = authenticationContext;
        this.watcherManager = watcherManager;
        this.userFormatManager = userFormatManager;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.voteManager = voteManager;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final Issue issue = (Issue) context.get("issue");
        final User user = authenticationContext.getUser();
        final Action action = (Action) context.get("action");

        final MapBuilder<String, Object> paramsBuilder = MapBuilder.newBuilder(context);

        paramsBuilder.add("issue", issue);
        paramsBuilder.add("peopleComponent", this);
        paramsBuilder.add("assigneeVisible", isAssigneeVisible(issue));
        paramsBuilder.add("reporterVisible", isReporterVisible(issue));
        List<String> watchers = watcherManager.getCurrentWatcherUsernames(issue);
        paramsBuilder.add("watchers", watchers);
        paramsBuilder.add("watching", user != null && watchers.contains(user.getName()));
        paramsBuilder.add("voting", voteManager.hasVoted(user, issue));
        paramsBuilder.add("isResolved", issue.getResolutionObject() != null);
        final String reporterId = issue.getReporterId();
        paramsBuilder.add("isCurrentUserReporter", StringUtils.isNotBlank(reporterId) && user != null && reporterId.equals(user.getName()));
        //need to be logged in in order to toggle watching/voting for an issue
        paramsBuilder.add("isLoggedIn", user != null);
        paramsBuilder.add("votingEnabled", applicationProperties.getOption(APKeys.JIRA_OPTION_VOTING));
        paramsBuilder.add("watchingEnabled", applicationProperties.getOption(APKeys.JIRA_OPTION_WATCHING));
        paramsBuilder.add("canManageWatcherList", permissionManager.hasPermission(Permissions.MANAGE_WATCHER_LIST, issue, user));
        paramsBuilder.add("canViewVotersAndWatchers", permissionManager.hasPermission(Permissions.VIEW_VOTERS_AND_WATCHERS, issue, user));
        paramsBuilder.add("userCustomFields", createUserFieldHelpers(issue, user, action));

        return paramsBuilder.toMap();
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        final Issue issue = (Issue) context.get("issue");
        final User user = authenticationContext.getUser();

        return issue.getId() + "/" + (user == null ? "" : user.getName());
    }

    private List<CustomFieldHelper> createUserFieldHelpers(final Issue issue, final com.atlassian.crowd.embedded.api.User remoteUser, final Action action)
    {
        final FieldScreenRenderer screenRenderer = fieldScreenRendererFactory.getFieldScreenRenderer(remoteUser, issue,
                IssueOperations.VIEW_ISSUE_OPERATION, FieldPredicates.isCustomUserField());
        final List<FieldScreenRenderLayoutItem> fieldScreenRenderLayoutItems = screenRenderer.getAllScreenRenderItems();
        final List<CustomFieldHelper> userCustomFields = new ArrayList<CustomFieldHelper>();
        for (final FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem : fieldScreenRenderLayoutItems)
        {
            userCustomFields.add(new CustomFieldHelper(fieldScreenRenderLayoutItem, action, issue));
        }
        return userCustomFields;
    }

    private boolean isAssigneeVisible(Issue issue)
    {
        return isFieldVisible(issue, IssueFieldConstants.ASSIGNEE);
    }

    private boolean isReporterVisible(Issue issue)
    {
        return isFieldVisible(issue, IssueFieldConstants.REPORTER);
    }

    private boolean isFieldVisible(Issue issue, String field)
    {
        return !fieldVisibilityManager.isFieldHidden(issue.getProjectObject().getId(), field, issue.getIssueTypeObject().getId());
    }

    /**
     * Returns a HTML displayable string of the issues assignee.
     * if the issue:
     * <ul>
     * <li>is null, returns an empty string.</li>
     * <li>has an assignee username but no user with that username, returns just the username</li>
     * <li>has a valid assignee, returns the full name of the assignee wrapped in HTML link to the users profile</li>
     * <li>has no assignee, returns i18n of 'common.status.unassigned' key</li>
     * </ul>
     *
     * @param issue issue to get the assignee from
     * @return HTML displayable string of the issues assignee
     */
    @SuppressWarnings ({ "UnusedDeclaration" })
    public String getAssigneeDisplayHtml(Issue issue)
    {
        if (issue == null)
        {
            //if there is no issue then there is no assignee
            return "";
        }

        try
        {
            final com.atlassian.crowd.embedded.api.User assignee = issue.getAssignee();
            if (assignee != null)
            {
                return userFormatManager.formatUser(assignee.getName(), "profileLink", "issue_summary_assignee");
            }
        }
        catch (DataAccessException e)
        {
            //user does not exist, so just display the username
            return TextUtils.htmlEncode(issue.getAssigneeId());
        }
        //issue is unassigned
        return authenticationContext.getI18nHelper().getText("common.status.unassigned");
    }

    /**
     * Returns a HTML displayable string of the issues reporter.
     * if the issue:
     * <ul>
     * <li>is null, returns an empty string.</li>
     * <li>has an reporter username but no user with that username, returns just the username</li>
     * <li>has a valid reporter, returns the full name of the reporter wrapped in HTML link to the users profile</li>
     * <li>has no reporter, returns i18n of 'common.status.unassigned' key</li>
     * </ul>
     *
     * @param issue issue to get the reporter from
     * @return HTML displayable string of the issues reporter
     */
    @SuppressWarnings ({ "UnusedDeclaration" })
    public String getReporterDisplayHtml(Issue issue)
    {
        if (issue == null)
        {
            //if there is no issue then there is no reporter
            return "";
        }

        try
        {
            final com.atlassian.crowd.embedded.api.User reporter = issue.getReporter();
            if (reporter != null)
            {
                return userFormatManager.formatUser(reporter.getName(), "profileLink", "issue_summary_reporter");
            }
        }
        catch (DataAccessException e)
        {
            //user does not exist, so just display the username
            return TextUtils.htmlEncode(issue.getReporterId());
        }
        //issue is unassigned
        return authenticationContext.getI18nHelper().getText("common.words.anonymous");
    }

    /*
     * Simple helper class used by velocity to render user fields. Must be public so that velocity will access it.
     */
    public class CustomFieldHelper
    {
        private final FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem;
        private final Action action;
        private final Issue issue;
        private String nameEncoded;

        private CustomFieldHelper(final FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem, final Action action, final Issue issue)
        {
            this.fieldScreenRenderLayoutItem = fieldScreenRenderLayoutItem;
            this.action = action;
            this.issue = issue;
        }

        public String getHtml()
        {
            return fieldScreenRenderLayoutItem.getViewHtml(action, null, issue);
        }

        public String getName()
        {
            if (nameEncoded == null)
            {
                nameEncoded = TextUtils.htmlEncode(fieldScreenRenderLayoutItem.getOrderableField().getName());
            }
            return nameEncoded;
        }

        public String getId()
        {
            return fieldScreenRenderLayoutItem.getOrderableField().getId();
        }
    }
}
