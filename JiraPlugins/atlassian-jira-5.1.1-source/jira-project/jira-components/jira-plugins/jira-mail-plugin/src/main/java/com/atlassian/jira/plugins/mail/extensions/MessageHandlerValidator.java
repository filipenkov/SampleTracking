package com.atlassian.jira.plugins.mail.extensions;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.util.ErrorCollection;

/**
 * Provides optional validation of required JIRA state to use given MessageHandler
 * This is used while an administrator creates a new handlers.
 * Any class implementing this can provide additional pre-requirements (prerequisites) for the handler
 * to be useful in given JIRA instance - e.g. whether there are any projects, users, attachments enabled, etc.
 * Validators should be stateless and thus threadsafe. There is only up to one object instatiated per
 * declaration of message-handler module and this validator instance is served to any caller.
 *
 * @since v5.0
 */
@ExperimentalApi
public interface MessageHandlerValidator
{
    /**
     * if any error messages (retrieved via {@link com.atlassian.jira.util.ErrorCollection#getErrorMessages()})
     * are present, they will be displayed in message handler/service configuration UI and will block
     * the user from proceeding to the next configuration screen.
     * Note that per-field error (getError() and addError) and reasons will be ignored.
     *
     * @return potentially non-empy list of error messages - if using the handler given current JIRA state is not possible.
     */
    ErrorCollection validate();
}
