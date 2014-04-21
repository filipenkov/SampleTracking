package com.atlassian.jira.plugin.viewissue;

import com.atlassian.jira.bean.SubTask;
import com.atlassian.jira.bean.SubTaskBean;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.IssueUtils;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutStorageException;
import com.atlassian.jira.issue.fields.option.Option;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculator;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculatorFactory;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestSession;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.component.IssueTableLayoutBean;
import com.atlassian.jira.web.component.IssueTableWebComponent;
import com.atlassian.jira.web.component.TableLayoutFactory;
import com.atlassian.jira.web.util.SubTaskQuickCreationConfig;
import com.atlassian.jira.web.util.SubTaskQuickCreationWebComponent;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.user.User;
import org.apache.commons.lang.StringUtils;
import webwork.action.Action;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Context Provider for the subtask section on view issue.  Is Cacheable.
 *
 * @since v4.4
 */
public class SubTasksContxtProvider implements CacheableContextProvider
{
    private final SubTaskManager subTaskManager;
    private final JiraAuthenticationContext authenticationContext;
    private final ApplicationProperties applicationProperties;
    private final FieldManager fieldManager;
    private final PermissionManager permissionManager;
    private final IssueManager issueManager;
    private final IssueFactory issueFactory;
    private final VelocityManager velocityManager;
    private final SubTaskQuickCreationConfig subTaskQuickCreationConfig;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final AggregateTimeTrackingCalculatorFactory aggregateTimeTrackingCalculatorFactory;
    private final TableLayoutFactory tableLayoutFactory;

    public SubTasksContxtProvider(SubTaskManager subTaskManager, JiraAuthenticationContext authenticationContext,
            ApplicationProperties applicationProperties, FieldManager fieldManager, PermissionManager permissionManager,
            IssueManager issueManager, IssueFactory issueFactory, VelocityManager velocityManager, SubTaskQuickCreationConfig subTaskQuickCreationConfig,
            VelocityRequestContextFactory velocityRequestContextFactory, AggregateTimeTrackingCalculatorFactory aggregateTimeTrackingCalculatorFactory,
            TableLayoutFactory tableLayoutFactory)
    {
        this.subTaskManager = subTaskManager;
        this.authenticationContext = authenticationContext;
        this.applicationProperties = applicationProperties;
        this.fieldManager = fieldManager;
        this.permissionManager = permissionManager;
        this.issueManager = issueManager;
        this.issueFactory = issueFactory;
        this.velocityManager = velocityManager;
        this.subTaskQuickCreationConfig = subTaskQuickCreationConfig;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.aggregateTimeTrackingCalculatorFactory = aggregateTimeTrackingCalculatorFactory;
        this.tableLayoutFactory = tableLayoutFactory;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }


    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        final Issue issue = (Issue) context.get("issue");
        final User user = authenticationContext.getUser();

        return issue.getId() + "/" + (user == null ? "" : user.getName());
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final MapBuilder<String, Object> paramsBuilder = MapBuilder.newBuilder(context);

        final Issue issue = (Issue) context.get("issue");
        final Action action = (Action) context.get("action");
        final Project project = issue.getProjectObject();
        final User user = authenticationContext.getUser();

        @SuppressWarnings ("unchecked")
        final Collection<Option> subTaskOptions = fieldManager.getIssueTypeField().getOptionsForIssue(issue, true);
        final boolean subTasksEnabled = subTaskManager.isSubTasksEnabled();
        final boolean isSubTask = issue.isSubTask();
        final boolean editable = isEditable(issue, user);
        final boolean canCreate = permissionManager.hasPermission(Permissions.CREATE_ISSUE, project, user);
        final boolean showQuickCreate = subTasksEnabled && !isSubTask && editable && canCreate && (subTaskOptions != null) && !subTaskOptions.isEmpty();

        final SubTaskBean subTaskBean = getSubTaskBean(issue, context);


        final SubTaskQuickCreationWebComponent quickCreateForm = new SubTaskQuickCreationWebComponent(issue, action, issueFactory, subTaskManager, fieldManager, velocityManager, authenticationContext, applicationProperties, subTaskQuickCreationConfig);

        final VelocityRequestContext requestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();
        final String selectedIssueId = requestContext.getRequestParameter("selectedIssueId");


        paramsBuilder.add("hasSubTasks", !subTaskBean.getSubTasks(getSubTaskView()).isEmpty());
        paramsBuilder.add("showQuickCreate", showQuickCreate);
        paramsBuilder.add("quickCreateForm", quickCreateForm);
        paramsBuilder.add("selectedIssueId", selectedIssueId);
        paramsBuilder.add("subTaskTable", new SubTaskTableRenderer(issue, user, context));

        return paramsBuilder.toMap();
    }

    private boolean isEditable(Issue issue, User user)
    {
        return permissionManager.hasPermission(Permissions.EDIT_ISSUE, issue, user) && issueManager.isEditable(issue);
    }

    /*
    * This is cached because this is very expensive to calculate and is calculated in few areas per request. E.g. View Issue (subtask block)
    */
    private AggregateTimeTrackingBean getAggregates(Issue issue, Map<String, Object> context)
    {
        final HttpServletRequest request = getRequest(context);
        if (request != null)
        {
            AggregateTimeTrackingBean aggregates = (AggregateTimeTrackingBean) request.getAttribute(AggregateTimeTrackingBean.AGG_TIMETRACKING + issue.getId());
            if (aggregates == null)
            {
                final AggregateTimeTrackingCalculator calculator = aggregateTimeTrackingCalculatorFactory.getCalculator(issue);
                aggregates = calculator.getAggregates(issue);
                request.setAttribute(AggregateTimeTrackingBean.AGG_TIMETRACKING + issue.getId(), aggregates);
            }
            return aggregates;
        }

        final AggregateTimeTrackingCalculator calculator = aggregateTimeTrackingCalculatorFactory.getCalculator(issue);
        return calculator.getAggregates(issue);

    }

    private SubTaskBean getSubTaskBean(Issue issue, Map<String, Object> context)
    {
        final HttpServletRequest request = getRequest(context);
        if (request != null)
        {
            SubTaskBean subtaskBean = (SubTaskBean) request.getAttribute("atl.jira.subtask.bean." + issue.getKey());
            if (subtaskBean != null)
            {
                return subtaskBean;
            }
            subtaskBean = subTaskManager.getSubTaskBean(issue.getGenericValue(), authenticationContext.getUser());
            request.setAttribute("atl.jira.subtask.bean." + issue.getKey(), subtaskBean);
            return subtaskBean;
        }

        return subTaskManager.getSubTaskBean(issue.getGenericValue(), authenticationContext.getUser());
    }

    private String getSubTaskView()
    {
        final VelocityRequestSession session = velocityRequestContextFactory.getJiraVelocityRequestContext().getSession();

        final String subTaskView = (String) session.getAttribute(SessionKeys.SUB_TASK_VIEW);
        return StringUtils.isNotBlank(subTaskView) ? subTaskView : SubTaskBean.SUB_TASK_VIEW_DEFAULT;

    }

    private String getTableHtml(Issue issue, User user, Map<String, Object> context)
    {
        AggregateTimeTrackingBean aggregateTTBean = getAggregates(issue, context);


        SubTaskBean subTaskBean = getSubTaskBean(issue, context);
        String subTaskView = getSubTaskView();
        Collection<SubTask> issues = subTaskBean.getSubTasks(subTaskView);
        List issueObjects = new ArrayList();
        boolean atLeastOneIssueHasTimeTrackingData = false;
        // get the subtask Issue object out of each SubTask and calculate if we need to display timetracking progress
        for (SubTask subTask : issues)
        {
            Issue subTaskIssue = subTask.getSubTaskIssueObject();
            atLeastOneIssueHasTimeTrackingData = atLeastOneIssueHasTimeTrackingData || IssueUtils.hasTimeTracking(subTaskIssue);
            issueObjects.add(subTaskIssue);
        }

        IssueTableWebComponent issueTable = new IssueTableWebComponent();
        IssueTableLayoutBean layout = null;
        try
        {
            layout = tableLayoutFactory.getSubTaskIssuesLayout(user, issue, subTaskBean, subTaskView, atLeastOneIssueHasTimeTrackingData);
        }
        catch (ColumnLayoutStorageException e)
        {
            throw new RuntimeException(e);
        }
        catch (FieldException e)
        {
            throw new RuntimeException(e);
        }

        layout.addCellDisplayParam("aggTTBean", aggregateTTBean);
        return issueTable.getHtml(layout, issueObjects, null);
    }

    public class SubTaskTableRenderer
    {
        private final Issue issue;
        private final User user;
        private final Map<String, Object> context;


        public SubTaskTableRenderer(Issue issue, User user, Map<String, Object> context)
        {
            this.user = user;
            this.issue = issue;
            this.context = context;
        }

        public String getHtml()
        {
            return getTableHtml(issue, user, context);

        }
    }

    protected HttpServletRequest getRequest(Map<String, Object> context)
    {
        return ExecutingHttpRequest.get();
    }

}
