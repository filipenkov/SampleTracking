package com.atlassian.jira.plugin.issuenav;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager;
import com.atlassian.jira.plugin.webresource.JiraWebResourceManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.component.jql.AutoCompleteJsonGenerator;
import com.atlassian.plugin.webresource.WebResourceManager;

public class IssueNavAction extends JiraWebActionSupport
{
    private static final String ISSUE = "issue";
    private final JiraWebResourceManager wrm;
    private final ApplicationProperties applicationProperties;
    private final KeyboardShortcutManager keyboardShortcutManager;
    private final AutoCompleteJsonGenerator autoCompleteJsonGenerator;
    private final GroupManager groupManager;
    private final IssueService issueService;
    private String issueKey;

    public IssueNavAction(final ApplicationProperties applicationProperties,
            final KeyboardShortcutManager keyboardShortcutManager, final GroupManager groupManager, final IssueService issueService)
    {
        this.groupManager = groupManager;
        this.issueService = issueService;
        this.wrm = (JiraWebResourceManager) ComponentManager.getComponentInstanceOfType(WebResourceManager.class);
        this.applicationProperties = applicationProperties;
        this.keyboardShortcutManager = keyboardShortcutManager;
        this.autoCompleteJsonGenerator = ComponentManager.getComponentInstanceOfType(AutoCompleteJsonGenerator.class);
    }

    @Override
    public String doDefault() throws Exception
    {
        if (null == issueKey)
        {
            return doNav();
        }
        else
        {
            return doIssue();
        }
    }
    
    private String doNav() throws Exception
    {
        addAtlassianStaffData();
        keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
        wrm.requireResource("com.atlassian.jira.jira-issue-nav-plugin:standalone-issue-nav");

        return SUCCESS;
    }
    
    public String doIssue() throws Exception
    {
        addAtlassianStaffData();
        wrm.requireResource("com.atlassian.jira.jira-issue-nav-plugin:standalone-issue");
        IssueService.IssueResult result = issueService.getIssue(getLoggedInUser(), getIssueKey());
        final MutableIssue issue = result.getIssue();
        wrm.putMetadata("issueId", issue.getId().toString());
        wrm.putMetadata("issueKey", issue.getKey());
        return ISSUE;
    }

    private void addAtlassianStaffData()
    {
        final User loggedInUser = getLoggedInUser();
        final Group staff = groupManager.getGroup(KickassRedirectFilter.ATLASSIAN_STAFF);
        boolean isAtlassianStaff = loggedInUser != null && staff != null && groupManager.isUserInGroup(loggedInUser, staff);

        wrm.putMetadata("isAtlassianStaff", Boolean.toString(isAtlassianStaff));
    }

    public String getVisibleFieldNamesJson() throws JSONException
    {
        return autoCompleteJsonGenerator.getVisibleFieldNamesJson(getLoggedInUser(), getLocale());
    }

    public String getVisibleFunctionNamesJson() throws JSONException
    {
        return autoCompleteJsonGenerator.getVisibleFunctionNamesJson(getLoggedInUser(), getLocale());
    }

    public String getJqlReservedWordsJson() throws JSONException
    {
        return autoCompleteJsonGenerator.getJqlReservedWordsJson();
    }

    public boolean isAutocompleteEnabledForThisRequest()
    {
        // if the user is not logged in then they do not have autocomplete disabled
        return !isAutocompleteDisabled() && !isAutocompleteDisabledForUser();
    }

    private boolean isAutocompleteDisabled()
    {
        return applicationProperties.getOption(APKeys.JIRA_JQL_AUTOCOMPLETE_DISABLED);
    }

    private boolean isAutocompleteDisabledForUser()
    {
        // if the user is not logged in then they do not have autocomplete disabled
        return getLoggedInUser() != null && getUserPreferences().getBoolean(PreferenceKeys.USER_JQL_AUTOCOMPLETE_DISABLED);
    }

    public String getIssueKey()
    {
        return issueKey;
    }

    public void setIssueKey(String issueKey)
    {
        this.issueKey = issueKey;
    }
}
