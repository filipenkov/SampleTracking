package com.atlassian.jira.whatsnew.constraints;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.whatsnew.access.WhatsNewAccess;
import com.atlassian.studio.haup.api.SupportedApplication;
import com.atlassian.studio.haup.api.UserApplicationAccessService;

/**
 * <p>Specifies an {@link WhatsNewAccess.Constraint} that restricts access to the What's New Feature in JIRA to only
 * JIRA Users.</p>
 *
 * <p>JIRA Access is determined through the {@link UserApplicationAccessService}.</p>
 *
 * <p>Anonymous access is <em>not</em> restricted by this constraint.</p>
 *
 * @see UserApplicationAccessService
 */
public class HasJiraOnDemandAccess implements WhatsNewAccess.Constraint
{
    private final UserApplicationAccessService userApplicationAccessService;

    public HasJiraOnDemandAccess(final UserApplicationAccessService userApplicationAccessService)
    {
        this.userApplicationAccessService = userApplicationAccessService;
    }

    /**
     * Whether this constraint has been met for the specified user. Anonymous users meet this constraint specification
     *
     * @param user {@inheritDoc}
     * @return {@inheritDoc}
     */
    public boolean apply(final User user)
    {
        return user == null || userApplicationAccessService.hasAccess(user.getName(), SupportedApplication.JIRA);
    }
}
