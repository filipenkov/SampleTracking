package com.atlassian.labs.hipchat.test.mock;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.user.MockUser;
import com.atlassian.labs.hipchat.HipChatApiClient;
import com.atlassian.labs.hipchat.components.ConfigurationManager;
import com.atlassian.labs.jira.workflow.HipChatPostFunction;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.executor.ThreadLocalDelegateExecutorFactory;
import com.atlassian.templaterenderer.TemplateRenderer;

import java.util.Map;

/**
 * Overrides some methods for test purpose to avoid calls to not really static helpers (that use {@link com.atlassian.jira.component.ComponentAccessor})
 */
public class HipChatPostFunctionTestOverrider extends HipChatPostFunction {
    private final boolean issuePresent;
    private final boolean jqlMatches;

    public HipChatPostFunctionTestOverrider(ApplicationProperties applicationProperties, SearchService searchService, HipChatApiClient hipChatApiClient,
                                            ThreadLocalDelegateExecutorFactory threadLocalDelegateExecutorFactory, TemplateRenderer templateRenderer, ConfigurationManager configurationManager,
                                            boolean issuePresent, boolean jqlMatches) {
        super(applicationProperties, searchService, hipChatApiClient, threadLocalDelegateExecutorFactory, templateRenderer, configurationManager);
        this.issuePresent = issuePresent;
        this.jqlMatches = jqlMatches;
    }

    @Override
    protected User getCaller(Map transientVars, Map args) {
        return new MockUser((String) args.get("username"));
    }

    @Override
    protected boolean matchesJql(String jql, Issue issue, User caller) throws SearchException {
        return jqlMatches;
    }

    @Override
    protected boolean isIssuePresent(Issue issue, User caller) throws SearchException {
        return issuePresent;
    }
}
