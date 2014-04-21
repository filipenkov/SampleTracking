package com.atlassian.jira.bc.subtask.conversion;

/**
 * Service class to reveal all business logic in converting a sub-task to an issue, including validation.
 * This business component should be used by all clients: web, rpc-soap, jelly, etc.
 * No additional methods needed.
 */
public interface SubTaskToIssueConversionService extends IssueConversionService
{

}
