package com.atlassian.jira.issue.fields.rest;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueRefJsonBean;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.ErrorCollection;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;

import javax.annotation.Nonnull;

import java.util.regex.Pattern;

import static com.atlassian.jira.user.util.Users.isAnonymous;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Finds issues by id or key.
 *
 * @since v5.0
 */
public class IssueFinderImpl implements IssueFinder
{
    private static final Logger LOG = Logger.getLogger(IssueFinder.class);

    private final Pattern ISSUE_ID_PATTERN = Pattern.compile("^[1-9]\\d{0,17}$");

    private final JiraAuthenticationContext authContext;
    private final IssueManager issueManager;
    private final ChangeHistoryManager changeHistoryManager;
    private final PermissionManager permissionManager;

    public IssueFinderImpl(JiraAuthenticationContext authContext, IssueManager issueManager, ChangeHistoryManager changeHistoryManager, PermissionManager permissionManager)
    {
        this.authContext = authContext;
        this.issueManager = issueManager;
        this.changeHistoryManager = changeHistoryManager;
        this.permissionManager = permissionManager;
    }

    @Override
    public MutableIssue findIssue(@Nonnull IssueRefJsonBean issueRef, @Nonnull ErrorCollection errorCollection)
    {
        if (isNotBlank(issueRef.id()) && isIssueId(issueRef.id()))
        {
            return findIssueById(Long.parseLong(issueRef.id()), errorCollection);
        }

        if (isNotBlank(issueRef.key()))
        {
            return findIssueByKey(issueRef.key(), errorCollection);
        }

        errorCollection.addErrorMessage(authContext.getI18nHelper().getText("rest.issue.key.or.id.required"));
        return null;
    }

    @Override
    public MutableIssue findIssue(@Nonnull String issueIdOrKey, @Nonnull ErrorCollection errorCollection)
    {
        if (isIssueId(issueIdOrKey))
        {
            return findIssueById(Long.parseLong(issueIdOrKey), errorCollection);
        }
        return findIssueByKey(issueIdOrKey, errorCollection);
    }

    private boolean isIssueId(String issueIdOrKey) {
        return ISSUE_ID_PATTERN.matcher(issueIdOrKey).matches();
    }

    public MutableIssue findIssueById(@Nonnull Long id, @Nonnull ErrorCollection errorCollection)
    {
        MutableIssue issue = issueManager.getIssueObject(id);
        return checkIssuePermission(errorCollection, issue);
    }

    public MutableIssue findIssueByKey(@Nonnull String key, @Nonnull ErrorCollection errorCollection)
    {
        MutableIssue issue = issueManager.getIssueObject(key);

        if (issue == null)
        {
            key = key.toUpperCase();
            issue = issueManager.getIssueObject(key);
        }

        if (issue == null)
        {
            try
            {
                issue = (MutableIssue) changeHistoryManager.findMovedIssue(key);
            }
            catch (GenericEntityException e)
            {
                LOG.info("problem finding moved issue", e);
            }
        }


        return checkIssuePermission(errorCollection, issue);
    }

    private MutableIssue checkIssuePermission(ErrorCollection errorCollection, MutableIssue issue)
    {
        if (issue == null)
        {
            errorCollection.addErrorMessage(authContext.getI18nHelper().getText("issue.does.not.exist.title"), ErrorCollection.Reason.NOT_FOUND);
            return null;
        }

        final User user = authContext.getLoggedInUser();

        if (!permissionManager.hasPermission(Permissions.BROWSE, issue, user))
        {
            errorCollection.addErrorMessage(authContext.getI18nHelper().getText("admin.errors.issues.no.permission.to.see"));
            if (isAnonymous(user))
            {
                errorCollection.addErrorMessage(authContext.getI18nHelper().getText("login.required.title"), ErrorCollection.Reason.NOT_LOGGED_IN);
            }
            else
            {
                errorCollection.addReason(ErrorCollection.Reason.FORBIDDEN);
            }
            return null;
        }
        else
        {
            return issue;
        }

    }
}
