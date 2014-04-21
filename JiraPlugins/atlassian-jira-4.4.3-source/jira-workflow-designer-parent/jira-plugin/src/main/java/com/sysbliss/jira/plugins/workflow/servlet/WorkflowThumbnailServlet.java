package com.sysbliss.jira.plugins.workflow.servlet;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.sysbliss.jira.plugins.workflow.manager.WorkflowImageManager;
import com.sysbliss.jira.plugins.workflow.manager.WorkflowImageParams;
import com.sysbliss.jira.plugins.workflow.manager.WorkflowThumbnailParams;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet used to return a thumbnail for a given workflow based on name. The servlet will generate an image if needed
 * and cache it for subsequent requests.
 * <p/>
 * URL Parameters: --------------- workflowName: The name of the workflow (required) mode: live|draft (defaults to live)
 * width: width of the image (defaults to 600) height: height of the image (defaults to 800) maintainAspect: determines
 * if the resulting image dimensions maintain the aspect ratio of the generated graph or uses the exact width and height
 * passed in. (true|false defaults to false/exact dimensions) stepId: The id of the step to highlight. (optional
 * defaults to -1/no highlight) showLabels: true|false toggles edge labels on or off. (defaults to false)
 * <p/>
 * Examples: --------- Basic image with dimensions of 400 x 600 http://localhost:2990/jira/plugins/servlet/workflow/thumbnail/getThumbnail?workflowName=jira&width=400&height=600
 * <p/>
 * Same image with labels turned on http://localhost:2990/jira/plugins/servlet/workflow/thumbnail/getThumbnail?workflowName=jira&width=400&height=600&showLabels=true
 * <p/>
 * The kitchen sink http://localhost:2990/jira/plugins/servlet/workflow/thumbnail/getThumbnail?workflowName=jira&mode=live&&width=400&height=600&maintainAspect=true&stepId=5&showLabels=true
 */
public class WorkflowThumbnailServlet extends HttpServlet
{

    public static Logger log = Logger.getLogger(WorkflowThumbnailServlet.class);

    public static final String CONTENTTYPE_PNG = "image/png";
    public static final String PARAM_WORKFLOW_NAME = "workflowName";
    public static final String PARAM_WORKFLOW_MODE = "mode";
    public static final String PARAM_WIDTH = "width";
    public static final String PARAM_HEIGHT = "height";
    public static final String PARAM_MAINTAIN_ASPECT = "maintainAspect";
    public static final String PARAM_STEP_ID = "stepId";
    public static final String PARAM_SHOW_LABELS = "showLabels";

    public static final String DEFAULT_WORKFLOW_MODE = "live";
    public static final String DEFAULT_WIDTH = "600";
    public static final String DEFAULT_HEIGHT = "800";
    public static final String DEFAULT_MAINTAIN_ASPECT = "false";
    public static final String DEFAULT_SHOW_LABELS = "true";
    public static final String DEFAULT_STEP_ID = "-1";
    public static final String FULL = "full";

    private final WorkflowService workflowService;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final WorkflowImageManager workflowImageManager;
    private final WorkflowSchemeManager workflowSchemeManager;
    private final ProjectService projectService;
    private final PermissionManager permissionManager;

    public WorkflowThumbnailServlet(WorkflowService workflowService, JiraAuthenticationContext jiraAuthenticationContext,
            WorkflowImageManager workflowImageManager, final PermissionManager permissionManager,
            final WorkflowSchemeManager workflowSchemeManager, final ProjectService projectService)
    {
        this.workflowService = workflowService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.workflowImageManager = workflowImageManager;
        this.workflowSchemeManager = workflowSchemeManager;
        this.projectService = projectService;
        this.permissionManager = permissionManager;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws javax.servlet.ServletException, java.io.IOException
    {
        getWorkflowImage(request, response);
    }

    public void getWorkflowImage(HttpServletRequest request, HttpServletResponse response) throws IOException
    {

        int returnStatus = HttpServletResponse.SC_OK;

        String workflowNameParam = request.getParameter(PARAM_WORKFLOW_NAME);
        String workflowModeParam = StringUtils.defaultString(request.getParameter(PARAM_WORKFLOW_MODE), DEFAULT_WORKFLOW_MODE);
        String widthParam = StringUtils.defaultString(request.getParameter(PARAM_WIDTH), DEFAULT_WIDTH);
        String heightParam = StringUtils.defaultString(request.getParameter(PARAM_HEIGHT), DEFAULT_HEIGHT);
        String maintainAspectParam = StringUtils.defaultString(request.getParameter(PARAM_MAINTAIN_ASPECT), DEFAULT_MAINTAIN_ASPECT);
        String stepIdParam = StringUtils.defaultString(request.getParameter(PARAM_STEP_ID), DEFAULT_STEP_ID);
        String showLabelsParam = StringUtils.defaultString(request.getParameter(PARAM_SHOW_LABELS), DEFAULT_SHOW_LABELS);


        if (!workflowModeParam.equals("live") && !workflowModeParam.equals("draft"))
        {
            workflowModeParam = DEFAULT_WORKFLOW_MODE;
        }

        if (!StringUtils.isNumeric(widthParam) && !FULL.equals(widthParam))
        {
            widthParam = DEFAULT_WIDTH;
        }

        if (!StringUtils.isNumeric(heightParam) && !FULL.equals(heightParam))
        {
            heightParam = DEFAULT_HEIGHT;
        }

        if (!StringUtils.isNumeric(stepIdParam))
        {
            stepIdParam = DEFAULT_STEP_ID;
        }

        int stepId = Integer.parseInt(stepIdParam);
        boolean showLabels = Boolean.parseBoolean(showLabelsParam);
        boolean maintainAspect = Boolean.parseBoolean(maintainAspectParam);

        int width = Integer.parseInt(DEFAULT_WIDTH);
        int height = Integer.parseInt(DEFAULT_HEIGHT);

        JiraWorkflow workflow;
        User user = jiraAuthenticationContext.getLoggedInUser();
        InputStream imageStream = null;

        if (StringUtils.isNotBlank(workflowNameParam))
        {
            final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(user);

            if (workflowModeParam.equals("draft"))
            {
                workflow = workflowService.getDraftWorkflow(jiraServiceContext, workflowNameParam);
            }
            else
            {
                workflow = workflowService.getWorkflow(jiraServiceContext, workflowNameParam);
            }

            if (workflow != null && hasPermissionToViewWorkflow(workflowNameParam, user))
            {
                try
                {
                    if (!FULL.equals(widthParam) && !FULL.equals(heightParam))
                    {
                        width = Integer.parseInt(widthParam);
                        height = Integer.parseInt(heightParam);
                        if (width < 20)
                        {
                            width = 20;
                        }

                        if (width > 3000)
                        {
                            width = 3000;
                        }

                        if (height < 20)
                        {
                            height = 20;
                        }

                        if (height > 3000)
                        {
                            height = 3000;
                        }

                        WorkflowThumbnailParams params = new WorkflowThumbnailParams.Builder(workflow).setStepId(stepId).setWidth(width).setHeight(height).setShowLabels(showLabels).setMaintainAspect(maintainAspect).build();
                        imageStream = workflowImageManager.getThumbnailStream(params);
                    }
                    else
                    {
                        WorkflowImageParams params = new WorkflowImageParams.Builder(workflow).setStepId(stepId).setShowLabels(showLabels).build();
                        imageStream = workflowImageManager.getFullImageStream(params);
                    }

                }
                catch (Exception e)
                {
                    log.error("Error getting workflow thumbnail, returning default image.", e);
                }

            }

        }

        //if we still don't have a stream, let's 404
        if (imageStream == null || imageStream.available() < 1)
        {
            returnStatus = HttpServletResponse.SC_NOT_FOUND;
        }

        response.setContentType(CONTENTTYPE_PNG);
        response.setStatus(returnStatus);

        if (HttpServletResponse.SC_OK == returnStatus)
        {
            OutputStream out = response.getOutputStream();

            IOUtils.copy(imageStream, out);

            imageStream.close();
            out.close();
        }
    }

    private boolean hasPermissionToViewWorkflow(final String workflowNameParam, final User user)
    {
        final List<Project> projectsForWorkflow = getProjectsForWorkflow(workflowNameParam, user);
        boolean hasPermissionToViewWorkflow = false;
        for (Project project : projectsForWorkflow)
        {
            //check if there's at least one project where the users has permission to view the workflow or
            //permission to admin the project.
            if (permissionManager.hasPermission(Permissions.VIEW_WORKFLOW_READONLY, project, user) ||
                    ProjectAction.VIEW_PROJECT.hasPermission(permissionManager, user, project))
            {
                hasPermissionToViewWorkflow = true;
                break;
            }
        }
        return hasPermissionToViewWorkflow;
    }

    //This is esentially copied from the 
    private List<Project> getProjectsForWorkflow(String workflowName, final User user)
    {
        final SetMultimap<String, Project> result = LinkedHashMultimap.create();
        for (final Project project : getAllProjects(user))
        {
            final Map<String, String> workflowMap = workflowSchemeManager.getWorkflowMap(project);
            String defaultWorkflow = workflowMap.get(null);
            if (defaultWorkflow == null)
            {
                defaultWorkflow = JiraWorkflow.DEFAULT_WORKFLOW_NAME;
            }

            for (final IssueType type : project.getIssueTypes())
            {
                String workflow = workflowMap.get(type.getId());
                if (workflow == null)
                {
                    workflow = defaultWorkflow;
                }
                if (workflowName.equals(workflow))
                {
                    result.put(workflow, project);
                }
            }
        }
        return Lists.newArrayList(result.get(workflowName));
    }

    private List<Project> getAllProjects(final User user)
    {
        ServiceOutcome<List<Project>> projectsForAction = projectService.getAllProjectsForAction(user, ProjectAction.VIEW_PROJECT);
        if (projectsForAction.isValid())
        {
            //Projects are already sorted apparently. So as long as we keep this order in our returned lists and maps
            //everything should remain sorted.
            return projectsForAction.getReturnedValue();
        }
        else
        {
            return Collections.emptyList();
        }
    }
}
