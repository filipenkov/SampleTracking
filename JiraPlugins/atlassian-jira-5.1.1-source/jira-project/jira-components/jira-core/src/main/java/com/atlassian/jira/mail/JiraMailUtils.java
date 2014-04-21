package com.atlassian.jira.mail;

import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.core.util.StringUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.ProjectKeys;

import static com.atlassian.jira.user.util.Users.isAnonymous;

/**
 * Helper methods for common mail related operations.
 */
public class JiraMailUtils
{
    /**
     * @deprecated Use {@link com.atlassian.mail.server.MailServerManager#isDefaultSMTPMailServerDefined()} instead.
     * Since 5.0
     */
    @Deprecated
    public static boolean isHasMailServer()
    {
        return (ComponentAccessor.getMailServerManager().getDefaultSMTPMailServer() != null);
    }

    /**
     * Returns the specified email address of the issue's project.
     * <p/>
     * @param issue The issue to be notified about
     * @return The email address of the issue's project
     */
    static String getProjectEmailFromIssue(final Issue issue)
    {
        return OFBizPropertyUtils.getPropertySet(issue.getProject()).getString(ProjectKeys.EMAIL_SENDER);
    }

    /**
     * Returns the sender's name in the format specified by {@link com.atlassian.jira.config.properties.APKeys#EMAIL_FROMHEADER_FORMAT} ('Joe Bloggs (JIRA)' usually).
     * <p/>
     * @param sender The user sending the email
     * @return The sender's name in the specified format
     */
    static String getFromNameForUser(final User sender)
    {
        String from = ComponentAccessor.getApplicationProperties().getDefaultBackedString(APKeys.EMAIL_FROMHEADER_FORMAT);
        if (from == null)
        {
            return null;
        }

        String name;

        if (isAnonymous(sender))
        {
            name = "Anonymous";
        }
        else
        {
            try
            {
                final String fullName = sender.getDisplayName();
                if (org.apache.commons.lang.StringUtils.isBlank(fullName))
                {
                    name = sender.getName();
                }
                else
                {
                    name = fullName;
                }
            }
            catch (final Exception exception)
            {
                // this should never fail, but incase it does we don't want to imply it was a anonymous sender.
                try
                {
                    name = sender.getName();
                }
                catch (final Exception exception2)
                {
                    name = "";
                }
            }
        }

        String email;
        try
        {
            email = (sender != null ? sender.getEmailAddress() : "");
        }
        catch (final Exception exception)
        {
            email = "";
        }
        final String hostname = ((sender != null) && (email != null) ? email.substring(email.indexOf("@") + 1) : "");

        from = StringUtils.replaceAll(from, "${fullname}", name);
        from = StringUtils.replaceAll(from, "${email}", email);
        from = StringUtils.replaceAll(from, "${email.hostname}", hostname);
        return from;
    }
}
