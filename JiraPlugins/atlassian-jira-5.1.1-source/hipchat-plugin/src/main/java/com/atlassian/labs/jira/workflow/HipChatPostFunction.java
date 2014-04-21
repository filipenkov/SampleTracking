package com.atlassian.labs.jira.workflow;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.atlassian.labs.hipchat.HipChatApiClient;
import com.atlassian.labs.hipchat.components.ConfigurationManager;
import com.atlassian.query.Query;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.executor.ThreadLocalDelegateExecutorFactory;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HipChatPostFunction extends AbstractJiraFunctionProvider {

    public static final String NOTIFICATION_TEMPLATE_PATH = "/templates/postfunctions/hip-chat-notification.vm";
    public static final String CONFIG_ERROR_NOTIFICATION_TEMPLATE_PATH = "/templates/postfunctions/hip-chat-notification-error.vm";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final HipChatApiClient hipChatApiClient;
    private final SearchService searchService;
    private final ApplicationProperties applicationProperties;
    private final ThreadLocalDelegateExecutorFactory threadLocalDelegateExecutorFactory;
    private final Executor threadLocalExecutor;
    private final TemplateRenderer templateRenderer;
    private final ConfigurationManager configurationManager;

    public HipChatPostFunction(ApplicationProperties applicationProperties, SearchService searchService,
                               HipChatApiClient hipChatApiClient, ThreadLocalDelegateExecutorFactory threadLocalDelegateExecutorFactory, TemplateRenderer templateRenderer, ConfigurationManager configurationManager) {
        this.applicationProperties = applicationProperties;
        this.hipChatApiClient = hipChatApiClient;
        this.searchService = searchService;
        this.threadLocalDelegateExecutorFactory = threadLocalDelegateExecutorFactory;
        this.templateRenderer = templateRenderer;
        this.configurationManager = configurationManager;
        this.threadLocalExecutor = threadLocalDelegateExecutorFactory.createExecutor(
                Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat(getClass().getSimpleName() + "-pool-thread-%d").build()));
    }

    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        if (Strings.isNullOrEmpty(configurationManager.getHipChatApiToken())) {
            return;
        }

        Issue issue = getIssue(transientVars);

        WorkflowDescriptor descriptor = (WorkflowDescriptor) transientVars.get("descriptor");
        Integer actionId = (Integer) transientVars.get("actionId");
        ActionDescriptor action = descriptor.getAction(actionId);
        Issue originalIssue = (Issue) transientVars.get("originalissueobject");
        String firstStepName = "";
        if (originalIssue != null) {
            Status status = originalIssue.getStatusObject();
            firstStepName = status.getName();
        }

        String actionName = action.getName();
        StepDescriptor endStep = descriptor.getStep(action.getUnconditionalResult().getStep());

        Iterable<String> roomsToNotifyIds = Splitter.on(",").omitEmptyStrings().split(Strings.nullToEmpty((String) args.get(HipChatPostFunctionFactory.ROOMS_TO_NOTIFY_CSV_IDS_PARAM)));

        if (roomsToNotifyIds.iterator().hasNext()) {
            String jql = (String) args.get(HipChatPostFunctionFactory.JQL_FILTER_PARAM);

            try {
                User caller = getCaller(transientVars, args);
                NotificationDto notificationDto = new NotificationDto(applicationProperties.getBaseUrl(), issue,
                        caller, firstStepName, endStep, actionName);
                if (!isIssuePresent(issue, caller)) {
                    // No issue key, or issue not indexed yet: an error message will be displayed in the notification
                    sendErrorNotification(roomsToNotifyIds, notificationDto);
                } else if (Strings.isNullOrEmpty(jql) || matchesJql(jql, issue, caller)) {
                    sendNotification(roomsToNotifyIds, notificationDto);
                }
            } catch (SearchException e) {
                throw new WorkflowException(e);
            } catch (IOException e) {
                throw new WorkflowException(e);
            }
        }
    }

    private void sendNotification(Iterable<String> roomsToNotifyIds, NotificationDto notificationDto) throws IOException {
        StringWriter messageWriter = new StringWriter();
        templateRenderer.render(NOTIFICATION_TEMPLATE_PATH, ImmutableMap.<String, Object>of("dto", notificationDto), messageWriter);

        threadLocalExecutor.execute(threadLocalDelegateExecutorFactory.createRunnable(
                new SendNotificationRunnable(hipChatApiClient, roomsToNotifyIds, messageWriter.toString(), logger, false)));
    }

    private void sendErrorNotification(Iterable<String> roomsToNotifyIds, NotificationDto notificationDto) throws IOException {
        StringWriter messageWriter = new StringWriter();
        templateRenderer.render(CONFIG_ERROR_NOTIFICATION_TEMPLATE_PATH, ImmutableMap.<String, Object>of("dto", notificationDto), messageWriter);

        threadLocalExecutor.execute(threadLocalDelegateExecutorFactory.createRunnable(
                new SendNotificationRunnable(hipChatApiClient, roomsToNotifyIds, messageWriter.toString(), logger, true)));
    }

    // protected to allow unit testing
    protected boolean matchesJql(String jql, Issue issue, User caller) throws SearchException {
        SearchService.ParseResult parseResult = searchService.parseQuery(caller, jql);
        if (parseResult.isValid()) {
            Query query = JqlQueryBuilder.newBuilder(parseResult.getQuery())
                    .where()
                    .and()
                    .issue()
                    .eq(issue.getKey())
                    .buildQuery();

            return searchService.searchCount(caller, query) > 0;

        }

        return false;
    }

    /**
     * Verifies that the issue is present in the index. If the HipChat post function is used on the create issue
     * transition, this might not be the case if the post function is not the last one in the list, so we need to detect
     * this situation.
     * @param issue the issue to check
     * @param caller the user triggering the post function
     * @return true if the issue was found in the index
     * @throws SearchException
     */
    protected boolean isIssuePresent(Issue issue, User caller) throws SearchException {
        if (issue == null || Strings.isNullOrEmpty(issue.getKey())) {
            return false;
        }

        Query query = JqlQueryBuilder.newBuilder().where().issue().eq(issue.getKey()).buildQuery();

        return searchService.searchCount(caller, query) > 0;
    }

    private static class SendNotificationRunnable implements Runnable {

        private final HipChatApiClient hipChatApiClient;
        private final Iterable<String> roomsToNotifyIds;
        private final String message;
        private final Logger logger;
        private final boolean error;

        public SendNotificationRunnable(HipChatApiClient hipChatApiClient, Iterable<String> roomsToNotifyIds, String message, Logger logger, boolean error) {
            this.hipChatApiClient = hipChatApiClient;
            this.roomsToNotifyIds = roomsToNotifyIds;
            this.message = message;
            this.logger = logger;
            this.error = error;
        }

        @Override
        public void run() {
            for (String roomsToNotifyId : roomsToNotifyIds) {
                try {
                    if (error) {
                        hipChatApiClient.notifyRoom(roomsToNotifyId, message, HipChatApiClient.BackgroundColour.RED);
                    } else {
                        hipChatApiClient.notifyRoom(roomsToNotifyId, message);
                    }
                } catch (ResponseException e) {
                    logger.error("Can not notify the HipCHat room " + roomsToNotifyId + " " + e.getMessage());
                }
            }
        }
    }

    public static class NotificationDto {

        private final String baseUrl;
        private final Issue issue;
        private final User actor;
        private final String firstStepName;
        private final StepDescriptor endStep;
        private final String actionName;

        public NotificationDto(String baseUrl, Issue issue, User actor, String firstStepName, StepDescriptor endStep, String actionName) {
            this.baseUrl = baseUrl;
            this.issue = issue;
            this.actor = actor;
            this.firstStepName = firstStepName;
            this.endStep = endStep;
            this.actionName = actionName;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public Issue getIssue() {
            return issue;
        }

        public User getActor() {
            return actor;
        }

        public String getFirstStepName() {
            return firstStepName;
        }

        public StepDescriptor getEndStep() {
            return endStep;
        }

        public String getActionName() {
            return actionName;
        }
    }
}