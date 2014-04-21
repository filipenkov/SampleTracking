package com.atlassian.jira.web.action.issue;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.issue.IssueViewEvent;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.util.IssueWebPanelRenderUtil;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.FieldRenderingContext;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.pager.NextPreviousPager;
import com.atlassian.jira.issue.pager.PagerManager;
import com.atlassian.jira.issue.util.IssueOperationsBarUtil;
import com.atlassian.jira.plugin.issueview.IssueViewModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webresource.JiraWebResourceManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.JiraVelocityHelper;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.component.ModuleWebComponent;
import com.atlassian.jira.web.component.issuesummary.IssueSummaryLayoutBean;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.query.order.SearchSort;
import com.atlassian.util.profiling.UtilTimerStack;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;
import webwork.action.ServletActionContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.web.action.AjaxHeaders.isPjaxRequest;
import static com.atlassian.jira.web.action.AjaxHeaders.requestUsernameMatches;

@SuppressWarnings ({ "UnusedDeclaration" })
public class ViewIssue extends AddComment implements OperationContext
{

    private static final String DEFAULT_ISSUE_ATTACHMENTS_ORDER = "asc";
    private static final String DEFAULT_ISSUE_ATTACHMENTS_SORTBY = "fileName";

    private final PagerManager pagerManager;
    private final PluginAccessor pluginAccessor;
    private final CommentManager commentManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final RendererManager rendererManager;
    private final WebResourceManager webResourceManager;
    private final SimpleLinkManager simpleLinkManager;
    private final WebInterfaceManager webInterfaceManager;

    private String issuetype;

    String attachmentOrder = null;
    String attachmentSortBy = null;

    // used for sorting sub-task issues
    private SearchSort sorter;
    private Long currentSubTaskSequence;
    private Long subTaskSequence;
    private Boolean isEditable;
    private IssueOperationsBarUtil issueOperationsBarUtil;
    private Map<String, Object> webPanelParams;
    private final ModuleWebComponent moduleWebComponent;
    private final FeatureManager featureManager;
    private final AvatarService avatarService;
    private final EventPublisher eventPublisher;
    private final ApplicationProperties applicationProperties;
    private final UserPickerSearchService userPickerSearchService;
    private IssueWebPanelRenderUtil issueWebPanelRenderUtil;

    public ViewIssue(final SubTaskManager subTaskManager, final PluginAccessor pluginAccessor,
            final FieldManager fieldManager, final FieldScreenRendererFactory fieldScreenRendererFactory,
            final FieldLayoutManager fieldLayoutManager, final RendererManager rendererManager,
            final CommentManager commentManager, final ProjectRoleManager projectRoleManager,
            final CommentService commentService,
            final PagerManager pagerManager, WebResourceManager webResourceManager, SimpleLinkManager simpleLinkManager,
            final WebInterfaceManager webInterfaceManager, final PermissionManager permissionManager,
            ModuleWebComponent moduleWebComponent, UserUtil userUtil, FeatureManager featureManager,
            AvatarService avatarService, EventPublisher eventPublisher, final ApplicationProperties applicationProperties, final UserPickerSearchService userPickerSearchService)
    {
        super(subTaskManager, fieldManager, fieldScreenRendererFactory, projectRoleManager, commentService, permissionManager, userUtil);
        this.pluginAccessor = pluginAccessor;
        this.commentManager = commentManager;
        this.pagerManager = pagerManager;
        this.fieldLayoutManager = fieldLayoutManager;
        this.rendererManager = rendererManager;
        this.webResourceManager = webResourceManager;
        this.simpleLinkManager = simpleLinkManager;
        this.webInterfaceManager = webInterfaceManager;
        this.moduleWebComponent = moduleWebComponent;
        this.featureManager = featureManager;
        this.avatarService = avatarService;
        this.eventPublisher = eventPublisher;
        this.applicationProperties = applicationProperties;
        this.userPickerSearchService = userPickerSearchService;
    }

    @Override
    protected void doValidation()
    {}

    @Override
    protected String doExecute() throws Exception
    {
        try
        {
            final GenericValue issue = getIssue();
            if (issue != null)
            {
                //set the selected project to be current project
                if (getProject() != null)
                {
                    setSelectedProjectId(getProject().getLong("id"));
                }

                String issueKey = issue.getString("key");

                ((JiraWebResourceManager)webResourceManager).putMetadata("can-edit-watchers", Boolean.toString(isCanEditWatcherList()));
                ((JiraWebResourceManager)webResourceManager).putMetadata("can-search-users", Boolean.toString(userPickerSearchService.canPerformAjaxSearch(getLoggedInUser())));
                ((JiraWebResourceManager)webResourceManager).putMetadata("issue-key", issueKey);
                ((JiraWebResourceManager)webResourceManager).putMetadata("issue-key", issueKey);
                String defaultAvatarUrl = avatarService.getAvatarURL(getLoggedInUser(), null, Avatar.Size.SMALL).toString();
                ((JiraWebResourceManager)webResourceManager).putMetadata("default-avatar-url", defaultAvatarUrl);

                UtilTimerStack.push("Updating Pager for viewing issue:" + issueKey);
                pagerManager.updatePager(getNextPreviousPager(), getSearchRequest(), getLoggedInUser(), issueKey);
                UtilTimerStack.pop("Updating Pager for viewing issue:" + issueKey);
            }

            // only render the issue tab for PJAX requests
            if (isPjaxRequest(request))
            {
                if (requestUsernameMatches(request, getLoggedInUser()))
                {
                    return "issueTabOnly";
                }

                return forceRedirect(request.getRequestURI());
            }


            if (!useKickAss())
            {
                webResourceManager.requireResource("jira.webresources:viewissue");
            }
            webResourceManager.requireResource("jira.webresources:jira-fields");
            if (useKickAss())
            {
                webResourceManager.requireResource("com.atlassian.jira.jira-issue-nav-plugin:standalone-issue");
            }

            eventPublisher.publish(new IssueViewEvent(getIssueObject().getId()));
            return SUCCESS;
        }
        catch (final IssueNotFoundException ex)
        {
            addErrorMessage(getText("admin.errors.issues.issue.does.not.exist"));
            return ISSUE_NOT_FOUND_RESULT;
        }
        catch (final IssuePermissionException ex)
        {
            addErrorMessage(getText("admin.errors.issues.no.browse.permission"));
            return PERMISSION_VIOLATION_RESULT;
        }
    }

    public boolean useKickAss()
    {
        return pluginAccessor.isPluginEnabled("com.atlassian.jira.jira-issue-nav-plugin") &&
                !applicationProperties.getOption(APKeys.JIRA_OPTION_DISABLE_INLINE_EDIT);
    }

    public NextPreviousPager getNextPreviousPager()
    {
        return pagerManager.getPager();
    }

    public CommentManager getCommentManager()
    {
        return commentManager;
    }

    // Check permission to edit watcher list
    private boolean isCanEditWatcherList() throws GenericEntityException
    {
        return (permissionManager.hasPermission(Permissions.MANAGE_WATCHER_LIST, getIssueObject(), getLoggedInUser()));
    }

    /**
     * Determines whether the current user can work on this issue
     *
     * @return true if the current user can work on this issue, false otherwise
     */
    public boolean isWorkable()
    {
        return isHasIssuePermission(Permissions.WORK_ISSUE, getIssue());
    }

    public boolean isWorkflowAllowsEdit()
    {
        return isWorkflowAllowsEdit(getIssueObject());
    }

    /**
     * Return Boolean.TRUE if this is the ViewIssue page (so the UI can customise itself).
     * <p/>
     * Any subclass should probably return FALSE.
     *
     * @return {@link Boolean#TRUE}
     */
    public Boolean isViewIssue()
    {
        return Boolean.TRUE;
    }

    public IssueWebPanelRenderUtil getRenderUtil()
    {
        if(issueWebPanelRenderUtil == null)
        {
            issueWebPanelRenderUtil = new IssueWebPanelRenderUtil(getLoggedInUser(),
                    getIssueObject(), this, this.webInterfaceManager, this.moduleWebComponent);
        }
        return issueWebPanelRenderUtil;
    }

    public String renderActivityModule()
    {
        List<WebPanelModuleDescriptor> webPanels = ComponentAccessor.getComponentOfType(PluginAccessor.class).getEnabledModuleDescriptorsByClass(WebPanelModuleDescriptor.class);
        for (WebPanelModuleDescriptor webPanel : webPanels)
        {
            if ("com.atlassian.jira.jira-view-issue-plugin:activitymodule".equals(webPanel.getCompleteKey()))
            {
                return webPanel.getModule().getHtml(getRenderUtil().getWebPanelContext());
            }
        }

        return "";
    }

    public SearchSort getSorter()
    {
        if (sorter == null)
        {
            sorter = new SearchSort(NavigableField.ORDER_DESCENDING, IssueFieldConstants.ISSUE_KEY);
        }
        return sorter;
    }

    public String doMoveIssueLink() throws Exception
    {
        final Long id = getId();

        if (!isAllowedReorderSubTasks())
        {
            addErrorMessage(getText("admin.errors.cannot.reorder.subtasks"));
        }

        if (id == null)
        {
            log.error("Cannot move sub-task when no parent issue exists.");
            addErrorMessage(getText("admin.errors.viewissue.no.parent.exists"));
            return ERROR;
        }

        if (getCurrentSubTaskSequence() == null)
        {
            log.error("Cannot move sub-task when current sequence is unset.");
            addErrorMessage(getText("admin.errors.viewissue.no.sequence.unset"));
            return ERROR;
        }

        if (getSubTaskSequence() == null)
        {
            log.error("Cannot move sub-task when sequence is unset.");
            addErrorMessage(getText("admin.errors.viewissue.no.sequence.unset"));
            return ERROR;
        }

        getSubTaskManager().moveSubTask(getIssue(), getCurrentSubTaskSequence(), getSubTaskSequence());

        return getRedirect("/browse/" + getIssue().getString("key"));
    }

    public boolean isAllowedReorderSubTasks()
    {
        return isHasIssuePermission(Permissions.EDIT_ISSUE, getIssue());
    }

    public void setSubTaskView(final String subTaskView)
    {
        if (!StringUtils.isBlank(subTaskView))
        {
            // Record in the session
            ActionContext.getSession().put(SessionKeys.SUB_TASK_VIEW, subTaskView);
        }
    }

    public Long getCurrentSubTaskSequence()
    {
        return currentSubTaskSequence;
    }

    public void setCurrentSubTaskSequence(final Long currentSubTaskSequence)
    {
        this.currentSubTaskSequence = currentSubTaskSequence;
    }

    public Long getSubTaskSequence()
    {
        return subTaskSequence;
    }

    public void setSubTaskSequence(final Long subTaskSequence)
    {
        this.subTaskSequence = subTaskSequence;
    }

    public GenericValue getAssignIn()
    {
        return getProject();
    }

    public JiraWorkflow getJiraWorkflow() throws WorkflowException
    {
        return ManagerFactory.getWorkflowManager().getWorkflow(getIssue());
    }

    public void setIssuetype(final String issuetypeId)
    {
        issuetype = issuetypeId;
    }

    public String getIssuetype()
    {
        return issuetype;
    }

    @Override
    public boolean isSubTask()
    {
        return getSubTaskManager().isSubTask(getIssue());
    }

    public String getAttachmentSortBy()
    {
        if (attachmentSortBy == null)
        {
            attachmentSortBy = (String) ActionContext.getSession().get(SessionKeys.VIEWISSUE_ATTACHMENT_SORTBY);
        }

        if (attachmentSortBy == null)
        {
            attachmentSortBy = DEFAULT_ISSUE_ATTACHMENTS_SORTBY;
        }
        return attachmentSortBy;
    }

    public void setAttachmentSortBy(final String attachmentSortBy)
    {
        if (!StringUtils.isBlank(attachmentSortBy) && !attachmentSortBy.equals(DEFAULT_ISSUE_ATTACHMENTS_SORTBY))
        {
            this.attachmentSortBy = attachmentSortBy;
            ActionContext.getSession().put(SessionKeys.VIEWISSUE_ATTACHMENT_SORTBY, this.attachmentSortBy);
        }
        else
        {
            ActionContext.getSession().put(SessionKeys.VIEWISSUE_ATTACHMENT_SORTBY, null);
        }
    }

    public String getAttachmentOrder()
    {
        if (attachmentOrder == null)
        {
            attachmentOrder = (String) ActionContext.getSession().get(SessionKeys.VIEWISSUE_ATTACHMENT_ORDER);
        }

        if (attachmentOrder == null)
        {
            attachmentOrder = (DEFAULT_ISSUE_ATTACHMENTS_ORDER);
        }
        return attachmentOrder;
    }

    public void setAttachmentOrder(final String attachmentOrder)
    {
        if (!StringUtils.isBlank(attachmentOrder) && !attachmentOrder.equals(DEFAULT_ISSUE_ATTACHMENTS_ORDER))
        {
            this.attachmentOrder = attachmentOrder;
            ActionContext.getSession().put(SessionKeys.VIEWISSUE_ATTACHMENT_ORDER, this.attachmentOrder);
        }
        else
        // invalid value or "asc" specified, leave as default.
        {
            ActionContext.getSession().put(SessionKeys.VIEWISSUE_ATTACHMENT_ORDER, null);
        }
    }

    // \src\webapp\includes\panels\issue\view_customfields.jsp
    public String getCustomFieldHtml(final FieldLayoutItem fieldLayoutItem, final CustomField field, final Issue issue)
    {
        return field.getViewHtml(fieldLayoutItem, this, issue, EasyMap.build(FieldRenderingContext.ISSUE_VIEW, Boolean.TRUE));
    }

    public String getRenderedContent(final String fieldName, final String value, final Issue issue)
    {
        final FieldLayoutItem fieldLayoutItem = fieldLayoutManager.getFieldLayout(issue.getProjectObject(), issue.getIssueTypeObject().getId()).getFieldLayoutItem(fieldName);
        if (fieldLayoutItem != null)
        {
            return rendererManager.getRenderedContent(fieldLayoutItem.getRendererType(), value, issue.getIssueRenderContext());
        }
        return value;
    }

    public String getRenderedContentNoBreaks(final String fieldName, final String value, final Issue issue)
    {
        return new JiraVelocityHelper(null).removeHtmlBreaks(getRenderedContent(fieldName, value, issue));
    }

    /**
     * Return a layout bean specifically tailored for view issue.
     */
    @Override
    public IssueSummaryLayoutBean getLayoutBean()
    {
        return new IssueSummaryLayoutBean(true);
    }


    public boolean enableStalkerBar()
    {
        final boolean disabled = getApplicationProperties().getOption(APKeys.JIRA_OPTION_OPERATIONS_DISABLE);

        return !disabled;
    }

    public boolean showOpsBar()
    {
        // Lets assume they can at least see different views of an issue.
        return true;
    }

    public SimpleLink getEditOrLoginLink()
    {
        return getOpsBarUtil().getEditOrLoginLink(getIssueObject());
    }

    public IssueOperationsBarUtil getOpsBarUtil()
    {
        if (issueOperationsBarUtil == null)
        {
            final JiraHelper helper = getJiraHelper();
            issueOperationsBarUtil = new IssueOperationsBarUtil(helper, getLoggedInUser(), simpleLinkManager, 
                    getApplicationProperties(), getIssueManager(), getI18nHelper());
        }
        return issueOperationsBarUtil;
    }

    private JiraHelper getJiraHelper()
    {
        final MutableIssue issue = getIssueObject();
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("issue", issue);
        params.put("issueId", issue.getId());
        return new JiraHelper(ServletActionContext.getRequest(), issue.getProjectObject(), params);
    }

    public Collection<IssueViewModuleDescriptor> getIssueViews()
    {
        return pluginAccessor.getEnabledModuleDescriptorsByClass(IssueViewModuleDescriptor.class);
    }

    public List<SimpleLink> getToolLinks()
    {
        final List<SimpleLink> linksForSection = simpleLinkManager.getLinksForSection("jira.issue.tools", getLoggedInUser(), getJiraHelper());
        return linksForSection.size() > 0 ? linksForSection : null;
    }

    public String getUrlForIssueView(IssueViewModuleDescriptor descriptor)
    {
        return descriptor.getURLWithoutContextPath(getIssueObject().getKey());
    }
}
