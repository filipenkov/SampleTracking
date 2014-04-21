package com.atlassian.jira.collector.plugin.components;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;

import java.util.List;
import javax.ws.rs.core.Response;

public interface ErrorLog
{
    enum ErrorType {
        DELETED, DISABLED, UNKNOWN_ERROR;

        public static ErrorType from(Response.Status status)
        {
            switch(status.getStatusCode()) {
                case 404:
                    return DELETED;
                case 403:
                    return DISABLED;
                default:
                    return UNKNOWN_ERROR;
            }
        }
    }

    void logError(final Project project, final String collectorId, final String fullName, final String email, final String sourceUrl, ErrorType type);

    List<String> getFormattedErrors(final Project project, final User remoteUser);

    void clearErrors(final Project project);
}
