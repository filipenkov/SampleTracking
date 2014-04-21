package com.atlassian.jira.bc;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;

/**
 * This is a context that provides information to calls to the JIRA service layer.
 */
public interface JiraServiceContext
{
    /**
     * Gets an error collection. This should be used to report any human-readable errors that occur in a JIRA
     * service method call.
     *
     * @return errorCollection
     */
    ErrorCollection getErrorCollection();

    /**
     * Gets the {@link com.opensymphony.user.User} who has invoked the JIRA service method.
     *
     * @return user who is performing the operation
     * @deprecated Use {@link #getLoggedInUser()}. Since 4.4
     */
    com.opensymphony.user.User getUser();

    /**
     * Returns the User who has invoked the JIRA service method.
     *
     * @return user who is performing the operation (can be null).
     */
    User getLoggedInUser();

    /**
     * Get an I18nHelper for localising text.
     * 
     * @return an I18nHelper for localising text.
     * @since v3.13
     */
    I18nHelper getI18nBean();
}
