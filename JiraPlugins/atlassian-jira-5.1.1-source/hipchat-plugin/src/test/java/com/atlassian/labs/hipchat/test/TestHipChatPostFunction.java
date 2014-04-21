package com.atlassian.labs.hipchat.test;


import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.status.MockStatus;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.labs.hipchat.HipChatApiClient;
import com.atlassian.labs.hipchat.components.ConfigurationManager;
import com.atlassian.labs.hipchat.test.mock.HipChatPostFunctionTestOverrider;
import com.atlassian.labs.hipchat.test.mock.MoreAnswers;
import com.atlassian.labs.jira.workflow.HipChatPostFunction;
import com.atlassian.labs.jira.workflow.HipChatPostFunctionFactory;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.executor.ThreadLocalDelegateExecutorFactory;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.MoreExecutors;
import com.opensymphony.workflow.WorkflowException;
import com.opensymphony.workflow.loader.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.ExecutorService;

public class TestHipChatPostFunction {

    private static final int ENDSTEPID = Integer.MAX_VALUE;
    public static final Integer ACTIONID = 1;

    // Mocks
    private ApplicationProperties applicationProperties;
    private ThreadLocalDelegateExecutorFactory executorFactory;
    private TemplateRenderer templateRenderer;
    private HipChatApiClient hipChatApiClient;
    private ConfigurationManager configurationManager;

    @Before
    public void setUp() throws Exception {
        applicationProperties = Mockito.mock(ApplicationProperties.class);
        executorFactory = Mockito.mock(ThreadLocalDelegateExecutorFactory.class);
        templateRenderer = Mockito.mock(TemplateRenderer.class);
        hipChatApiClient = Mockito.mock(HipChatApiClient.class);
        configurationManager = Mockito.mock(ConfigurationManager.class);

        Mockito.when(applicationProperties.getBaseUrl()).thenReturn("/fake/base/url/");
        Mockito.when(executorFactory.createExecutor(Mockito.<ExecutorService>any())).thenReturn(MoreExecutors.sameThreadExecutor());
        Mockito.when(executorFactory.createRunnable((Runnable) Mockito.notNull())).thenAnswer(MoreAnswers.firstArg());

        Mockito.doAnswer(new Answer() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ((Writer) invocation.getArguments()[2]).write("fake rendered template");
                return null;
            }
        }).when(templateRenderer).render(Mockito.anyString(), Mockito.anyMap(), Mockito.<Writer>anyObject());

        Mockito.when(configurationManager.getHipChatApiToken()).thenReturn("authtoken");
    }

    @Test
    public void testExecute() throws SearchException, WorkflowException, IOException, ResponseException {
        Issue originalIssue = Mockito.mock(Issue.class);
        Mockito.when(originalIssue.getStatusObject()).thenReturn(new MockStatus(null, "fake-status-name"));

        postFunction(true, true).execute(
                ImmutableMap.of("issue", mockIssue("ABC-123"), "descriptor", workflowDescriptor(), "actionId", ACTIONID, "originalissueobject", originalIssue),
                ImmutableMap.of(HipChatPostFunctionFactory.JQL_FILTER_PARAM, "fake-jql", HipChatPostFunctionFactory.ROOMS_TO_NOTIFY_CSV_IDS_PARAM, "1,2,3", "username", "fake-username"),
                null);

        Mockito.verify(hipChatApiClient, Mockito.times(3)).notifyRoom(Mockito.anyString(), Mockito.eq("fake rendered template"));
    }

    @Test
    public void testExecuteIssueWithoutKey() throws SearchException, WorkflowException, IOException, ResponseException {
        Issue originalIssue = Mockito.mock(Issue.class);
        Mockito.when(originalIssue.getStatusObject()).thenReturn(new MockStatus(null, "fake-status-name"));

        postFunction(false, true).execute(
                ImmutableMap.of("issue", mockIssue(null), "descriptor", workflowDescriptor(), "actionId", ACTIONID, "originalissueobject", originalIssue),
                ImmutableMap.of(HipChatPostFunctionFactory.JQL_FILTER_PARAM, "fake-jql", HipChatPostFunctionFactory.ROOMS_TO_NOTIFY_CSV_IDS_PARAM, "1,2,3", "username", "fake-username"),
                null);

        // Should not cause an exception anymore, but send error message as notifications with red background
        Mockito.verify(hipChatApiClient, Mockito.times(3)).notifyRoom(Mockito.anyString(), Mockito.eq("fake rendered template"), Mockito.eq(HipChatApiClient.BackgroundColour.RED));
    }

    private WorkflowDescriptor workflowDescriptor() {
        WorkflowDescriptor workflowDescriptor = DescriptorFactory.getFactory().createWorkflowDescriptor();
        StepDescriptor stepDescriptor = DescriptorFactory.getFactory().createStepDescriptor();

        stepDescriptor.setId(ENDSTEPID);
        workflowDescriptor.addStep(stepDescriptor);

        ActionDescriptor actionDescriptor = DescriptorFactory.getFactory().createActionDescriptor();
        actionDescriptor.setId(ACTIONID);
        ResultDescriptor resultDescriptor = DescriptorFactory.getFactory().createResultDescriptor();
        resultDescriptor.setStep(ENDSTEPID);
        actionDescriptor.setUnconditionalResult(resultDescriptor);

        workflowDescriptor.addGlobalAction(actionDescriptor);
        return workflowDescriptor;
    }

    private MockIssue mockIssue(String key) {
        MockIssue issue = new MockIssue();
        issue.setKey(key);
        return issue;
    }

    private HipChatPostFunction postFunction(boolean issuePresent, boolean jqlMatches) {
        return new HipChatPostFunctionTestOverrider(applicationProperties, null, hipChatApiClient, executorFactory, templateRenderer, configurationManager, issuePresent, jqlMatches);
    }
}