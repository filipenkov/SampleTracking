package com.atlassian.jira.web.action.sysbliss;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.ProjectWorkflowSchemeHelper;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.sysbliss.jira.plugins.workflow.BuildInfo;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.List;
import java.util.Map;


@WebSudoRequired
public class WorkflowDesignerAction extends JiraWebActionSupport
{
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authContext;
    private final I18nResolver il8n;
    private final ObjectMapper mapper;
    private final WorkflowManager workflowManager;
    private final WebResourceManager webResourceManager;
    private final ProjectWorkflowSchemeHelper helper;

    // This can't be called workflowName otherwise the RequestComponentManager tries to do funky stuff.
    private String wfName;
    private String workflowMode;
    private List<Project> usedIn;
    private JiraWorkflow workflow;

    public WorkflowDesignerAction(final PermissionManager permissionManager, final JiraAuthenticationContext jiraAuthenticationContext,
            final I18nResolver il8n, final WorkflowManager workflowManager, final WebResourceManager webResourceManager,
            final ProjectWorkflowSchemeHelper helper)
    {
        this.permissionManager = permissionManager;
        this.authContext = jiraAuthenticationContext;
        this.il8n = il8n;
        this.webResourceManager = webResourceManager;
        this.helper = helper;
        this.mapper = new ObjectMapper();
        this.workflowManager = workflowManager;
    }

    @Override
    public String execute() throws Exception
    {

        final User user = authContext.getLoggedInUser();
        if (user == null)
        {
            return "notloggedin";
        }


        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            return "nopermission";
        }

        if (StringUtils.isEmpty(wfName) || StringUtils.isEmpty(workflowMode) || !(workflowMode.equals(JiraWorkflow.LIVE) || workflowMode.equals(JiraWorkflow.DRAFT)))
        {
            return "invalidworkflowname";
        }

        if ((workflowMode.equals(JiraWorkflow.LIVE) && workflowManager.getWorkflow(wfName) == null)
                || (workflowMode.equals(JiraWorkflow.DRAFT) && workflowManager.getDraftWorkflow(wfName) == null))
        {
            return "invalidworkflowname";
        }

        webResourceManager.requireResource("com.atlassian.jira.plugins.jira-workflow-designer:jwdresources");
        webResourceManager.requireResource("com.atlassian.jira.plugins.jira-workflow-designer:jwdcss");
        webResourceManager.requireResource("com.atlassian.jira.plugins.jira-workflow-designer:jwd-topup");
        return SUCCESS;
    }

    public String getTranslationsAsJSON() throws Exception
    {
        Map<String, String> translations = il8n.getAllTranslationsForPrefix("workflow.designer.", authContext.getLocale());
        return StringEscapeUtils.escapeJavaScript(mapper.writeValueAsString(translations));
    }

    public String getPluginVersion()
    {
        return BuildInfo.PLUGIN_VERSION;
    }

    public String getPluginKey()
    {
        return BuildInfo.PLUGIN_KEY;
    }

    public String getWorkflowDescription()
    {
        String workflowDescription = null;

        if (wfName != null)
        {
            workflowDescription = getWorkflow().getDescription();
        }

        return workflowDescription;
    }

    public String getWfName()
    {
        return wfName;
    }

    public void setWfName(String wfName)
    {
        this.wfName = wfName;
    }

    public String getWorkflowMode()
    {
        return workflowMode;
    }

    public void setWorkflowMode(String workflowMode)
    {
        this.workflowMode = workflowMode;
    }

    public boolean isDraft()
    {
        return workflowMode.equals(JiraWorkflow.DRAFT);
    }

    public boolean isSystemWorkflow()
    {
        return getWorkflow().isSystemWorkflow();
    }

    public boolean isEditable()
    {
        final JiraWorkflow workflow = getWorkflow();
        return workflow != null && workflow.isEditable();
    }

    public HelpUtil.HelpPath getHelpLink()
    {
        HelpUtil helpUtil = HelpUtil.getInstance();
        HelpUtil.HelpPath path = helpUtil.getHelpPath("workflow_designer");
        return path;
    }

    public JiraWorkflow getWorkflow()
    {
        if (workflow == null)
        {
            if (workflowMode.equals(JiraWorkflow.LIVE))
            {
                workflow = workflowManager.getWorkflow(wfName);
            }
            else
            {
                workflow = workflowManager.getDraftWorkflow(wfName);
            }
        }

        return workflow;
    }

    public List<Project> getUsedIn()
    {
        if (usedIn == null)
        {
            usedIn = helper.getProjectsForWorkflow(wfName);
        }
        return usedIn;
    }

    public String getDraftReturnUrl(String workflowName)
    {
        return urlEncode("/secure/admin/WorkflowDesigner.jspa?wfName=" + urlEncode(workflowName) + "&workflowMode=draft");
    }

    public String urlEncode(String string)
    {
        return JiraUrlCodec.encode(string);
    }
}
