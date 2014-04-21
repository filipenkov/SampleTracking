package com.atlassian.jira.web.action.issue;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.attachment.AttachmentService;
import com.atlassian.jira.bc.issue.attachment.FileNameBasedVersionedAttachmentsList;
import com.atlassian.jira.bc.issue.attachment.VersionedAttachmentsList;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.attachment.AttachmentCreationDateComparator;
import com.atlassian.jira.issue.attachment.AttachmentFileNameCreationDateComparator;
import com.atlassian.jira.issue.attachment.AttachmentZipKit;
import com.atlassian.jira.issue.attachment.AttachmentsCategoriser;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.FieldRenderingContext;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.fields.util.FieldPredicates;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.issue.pager.NextPreviousPager;
import com.atlassian.jira.issue.pager.PagerManager;
import com.atlassian.jira.issue.thumbnail.ThumbnailManager;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculator;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculatorFactory;
import com.atlassian.jira.issue.util.IssueOperationsBarUtil;
import com.atlassian.jira.plugin.issueview.IssueViewModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.util.AttachmentUtils;
import com.atlassian.jira.util.JiraVelocityHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.bean.NonZipExpandableExtensions;
import com.atlassian.jira.web.component.ModuleWebComponent;
import com.atlassian.jira.web.component.issuesummary.IssueSummaryBlock;
import com.atlassian.jira.web.component.issuesummary.IssueSummaryLayoutBean;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.query.order.SearchSort;
import com.atlassian.util.profiling.UtilTimerStack;
import com.google.common.collect.Lists;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;
import webwork.action.ServletActionContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewIssue extends AddComment implements OperationContext
{
    private static final String IMAGE_TYPE = "image";
    private static final String FILE_TYPE = "file";

    private static final String ORDER_DESC = "desc";
    private static final String SORTBY_DATE_TIME = "dateTime";
    private static final String DEFAULT_ISSUE_ATTACHMENTS_ORDER = "asc";
    private static final String DEFAULT_ISSUE_ATTACHMENTS_SORTBY = "fileName";

    private static final String JIRA_VIEW_ISSUE_INFO_CONTEXT = "atl.jira.view.issue.info.context";
    private static final String JIRA_VIEW_ISSUE_RIGHT_CONTEXT = "atl.jira.view.issue.right.context";
    private static final String JIRA_VIEW_ISSUE_LEFT_CONTEXT = "atl.jira.view.issue.left.context";

    private final PagerManager pagerManager;
    private final ThumbnailManager thumbnailManager;
    private final PluginAccessor pluginAccessor;
    private final CommentManager commentManager;
    private final AttachmentManager attachmentManager;
    private final AttachmentService attachmentService;
    private final FieldScreenRendererFactory fieldScreenRendererFactory;
    private final FieldLayoutManager fieldLayoutManager;
    private final RendererManager rendererManager;
    private final WebResourceManager webResourceManager;
    private final SimpleLinkManager simpleLinkManager;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final NonZipExpandableExtensions nonZipExpandableExtensions;
    private final WebInterfaceManager webInterfaceManager;
    private final AttachmentZipKit attachmentZipKit;
    private final AttachmentsCategoriser attachments;

    private String issuetype;
    private FieldScreenRenderer fieldScreenRenderer;

    String attachmentOrder = null;
    String attachmentSortBy = null;
    Collection<GenericValue> affectedVersions;
    Collection<GenericValue> components;
    Collection<GenericValue> fixVersions;

    // used for sorting sub-task issues
    private SearchSort sorter;
    private Long currentSubTaskSequence;
    private Long subTaskSequence;
    private Boolean isEditable;
    private IssueOperationsBarUtil issueOperationsBarUtil;
    private Map<String, Object> webPanelParams;
    private final ModuleWebComponent moduleWebComponent;

    private AggregateTimeTrackingBean aggregateTimeTrackingBean;
    private final AggregateTimeTrackingCalculatorFactory aggregateTimeTrackingCalculatorFactory;

    public ViewIssue(final ThumbnailManager thumbnailManager,
            final SubTaskManager subTaskManager, final IssueLinkManager issueLinkManager, final PluginAccessor pluginAccessor,
            final FieldManager fieldManager, final FieldScreenRendererFactory fieldScreenRendererFactory,
            final FieldLayoutManager fieldLayoutManager, final RendererManager rendererManager,
            final CommentManager commentManager, final ProjectRoleManager projectRoleManager,
            final CommentService commentService, final AttachmentManager attachmentManager, final AttachmentService attachmentService,
            final PagerManager pagerManager, WebResourceManager webResourceManager, SimpleLinkManager simpleLinkManager,
            final AttachmentZipKit attachmentZipKit, final NonZipExpandableExtensions nonZipExpandableExtensions,
            final WebInterfaceManager webInterfaceManager, final PermissionManager permissionManager, ModuleWebComponent moduleWebComponent, AggregateTimeTrackingCalculatorFactory aggregateTimeTrackingCalculatorFactory)
    {
        super(issueLinkManager, subTaskManager, fieldManager, fieldScreenRendererFactory, projectRoleManager, commentService, permissionManager);
        this.thumbnailManager = thumbnailManager;
        this.pluginAccessor = pluginAccessor;
        this.commentManager = commentManager;
        this.attachmentManager = attachmentManager;
        this.attachmentService = attachmentService;
        this.pagerManager = pagerManager;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.fieldLayoutManager = fieldLayoutManager;
        this.rendererManager = rendererManager;
        this.webResourceManager = webResourceManager;
        this.simpleLinkManager = simpleLinkManager;
        this.nonZipExpandableExtensions = nonZipExpandableExtensions;
        this.webInterfaceManager = webInterfaceManager;
        this.moduleWebComponent = moduleWebComponent;
        this.aggregateTimeTrackingCalculatorFactory = aggregateTimeTrackingCalculatorFactory;
        this.fieldVisibilityManager = ComponentManager.getComponentInstanceOfType(FieldVisibilityManager.class);
        this.attachmentZipKit = attachmentZipKit;
        this.attachments = new AttachmentsCategoriser(thumbnailManager, new ViewIssueAttachmentsSource());
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

                UtilTimerStack.push("Updating Pager for viewing issue:" + issue.getString("key"));
                pagerManager.updatePager(getNextPreviousPager(), getSearchRequest(), getLoggedInUser(),
                        issue.getString("key"));
                UtilTimerStack.pop("Updating Pager for viewing issue:" + issue.getString("key"));

            }
            webResourceManager.requireResource("jira.webresources:viewissue");
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

    public NextPreviousPager getNextPreviousPager()
    {
        return pagerManager.getPager();
    }

    public CommentManager getCommentManager()
    {
        return commentManager;
    }

    public Collection<GenericValue> getComponents() throws Exception
    {
        if ((getIssueObject() != null) && (components == null))
        {
            components = getIssueObject().getComponents();
        }
        return components;
    }

    public Collection<GenericValue> getAffectedVersions() throws Exception
    {
        if ((getIssueObject() != null) && (affectedVersions == null))
        {
            affectedVersions = new ArrayList<GenericValue>();
            // TODO: return the actual Version objects instead of GenericValues, when Issue.getComponents() gets its act together
            for (final Object element : getIssueObject().getAffectedVersions())
            {
                final Version version = (Version) element;
                affectedVersions.add(version.getGenericValue());
            }
        }
        return affectedVersions;
    }

    public Collection<GenericValue> getFixVersions() throws Exception
    {
        if ((getIssueObject() != null) && (fixVersions == null))
        {
            fixVersions = new ArrayList<GenericValue>();
            // TODO: return the actual Version objects instead of GenericValues, when Issue.getComponents() gets its act together
            for (final Object element : getIssueObject().getFixVersions())
            {
                final Version version = (Version) element;
                fixVersions.add(version.getGenericValue());
            }
        }
        return fixVersions;
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
     * Determines whether the current user can attach files or delete one or more files to this issue
     *
     * @return true if the current user can attach files or delete one or more files on this issue, false otherwise
     */
    public boolean isAttachable()
    {
        // JRA-13496 - build our own service context as we do not care about displaying the error message if
        // this call fails.
        final JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(getLoggedInUser(), new SimpleErrorCollection());
        return attachmentService.canManageAttachments(serviceContext, getIssueObject());
    }

    public boolean canCreateAttachments()
    {
        final JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(getLoggedInUser(), new SimpleErrorCollection());
        return attachmentService.canCreateAttachments(serviceContext, getIssueObject());
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

    public List<WebPanelModuleDescriptor> getInfoWebPanels()
    {
        return getWebPanels(JIRA_VIEW_ISSUE_INFO_CONTEXT);
    }

    public List<WebPanelModuleDescriptor> getRightWebPanels()
    {
        return getWebPanels(JIRA_VIEW_ISSUE_RIGHT_CONTEXT);
    }

    public List<WebPanelModuleDescriptor> getLeftWebPanels()
    {
        return getWebPanels(JIRA_VIEW_ISSUE_LEFT_CONTEXT);
    }

    public Map<String,Object> getWebPanelContext()
    {
        if (webPanelParams == null)
        {
            final Issue issue = getIssueObject();

            webPanelParams = new HashMap<String, Object>();
            webPanelParams.put("user", getRemoteUser());
            webPanelParams.put("project", issue.getProjectObject());
            webPanelParams.put("issue", issue);
            webPanelParams.put("action", this);
            final JiraHelper jiraHelper = new JiraHelper(request, issue.getProjectObject(), webPanelParams);
            webPanelParams.put("helper", jiraHelper);

        }
        return webPanelParams;
    }

    private List<WebPanelModuleDescriptor> getWebPanels(final String location)
    {
        return webInterfaceManager.getDisplayableWebPanelDescriptors(location, getWebPanelContext());
    }

    public String renderPanels(List<WebPanelModuleDescriptor> panels)
    {
        if (panels != null)
        {
            return moduleWebComponent.renderModules(getRemoteUser(), request, panels, getWebPanelContext());

        }

        return "";
    }

    public String renderHeadlessPanel(WebPanelModuleDescriptor panel)
    {
        return panel.getModule().getHtml(getWebPanelContext());
    }

    public Collection getThumbnails() throws Exception
    {
        return attachments.thumbnails();
    }

    public VersionedAttachmentsList getFileAttachments() throws Exception
    {
        return new FileNameBasedVersionedAttachmentsList(attachments.noThumbnailAttachments());
    }

    public VersionedAttachmentsList getImageAttachments() throws Exception
    {
        return new FileNameBasedVersionedAttachmentsList(attachments.thumbnailAttachments());
    }

    /**
     * Returns all of this issue's attachments, sorted by the specified order.
     *
     * @return a List of Attachment
     */
    List<Attachment> getAttachmentList()
    {
        Comparator<Attachment> attachmentComparator = SORTBY_DATE_TIME.equals(getAttachmentSortBy()) ?
                        new AttachmentCreationDateComparator() :
                        new AttachmentFileNameCreationDateComparator(getLocale());

        List<Attachment> attachments = Lists.newArrayList(attachmentManager.getAttachments(getIssueObject(), attachmentComparator));
        if (ORDER_DESC.equals(getAttachmentOrder()))
        {
            Collections.reverse(attachments);
        }

        return attachments;
    }

    public boolean isToolkitAvailable()
    {
        return thumbnailManager.checkToolkit(null);
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

    public boolean areAttachmentsEmpty() throws Exception
    {
        return getFileAttachments().asList().isEmpty() && getImageAttachments().asList().isEmpty();
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

    public List<FieldScreenRenderTab> getFieldScreenRenderTabs()
    {
        return getFieldScreenRenderer().getFieldScreenRenderTabs();
    }

    protected FieldScreenRenderer getFieldScreenRenderer()
    {
        if (fieldScreenRenderer == null)
        {
            fieldScreenRenderer = fieldScreenRendererFactory.getFieldScreenRenderer(getLoggedInUser(), getIssueObject(),
                    IssueOperations.VIEW_ISSUE_OPERATION, FieldPredicates.isStandardViewIssueCustomField());
        }
        return fieldScreenRenderer;
    }

    public int getSelectedTab()
    {
        // Always show the first tab
        return 1;
    }

    // \src\webapp\includes\panels\issue\view_customfields.jsp
    public String getCustomFieldHtml(final FieldLayoutItem fieldLayoutItem, final CustomField field, final Issue issue)
    {
        return field.getViewHtml(fieldLayoutItem, this, issue, EasyMap.build(FieldRenderingContext.ISSUE_VIEW, Boolean.TRUE));
    }

    public String getRenderedContent(final String fieldName, final String value, final Issue issue)
    {
        final FieldLayoutItem fieldLayoutItem = fieldLayoutManager.getFieldLayout(issue.getProject(), issue.getIssueTypeObject().getId()).getFieldLayoutItem(fieldName);
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

    /**
     * Gets the HTML that shows the environment field. This includes divs and a javascript enabled hide/show toggle
     * button.
     *
     * @return the HTML that shows the environment field.
     */
    public String getRenderedEnvironmentFieldValue()
    {
        final OrderableField environmentField = getOrderableField(IssueFieldConstants.ENVIRONMENT);
        final FieldLayoutItem fieldLayoutItem = getFieldScreenRendererLayoutItemForField(environmentField).getFieldLayoutItem();
        // JRA-16224 Cannot call getViewHtml() on FieldScreenRenderLayoutItem, because it will return "" if Environment is not included in the Screen Layout.
        return environmentField.getViewHtml(fieldLayoutItem, this, getIssueObject());
    }

        /**
     * Retrieve the {@link com.atlassian.jira.issue.util.AggregateTimeTrackingBean} for the current issue
     *
     * @return The AggregateTimeTrackingBean for this issue
     */
    public AggregateTimeTrackingBean getAggregateTimeTrackingBean()
    {
        if (aggregateTimeTrackingBean == null)
        {
            aggregateTimeTrackingBean = getCachedAggregateTimeTrackingBean();
        }
        return aggregateTimeTrackingBean;
    }

    /*
     * Caches the aggregate bean in the request as it is VERT expensive
     */
    private AggregateTimeTrackingBean getCachedAggregateTimeTrackingBean()
    {
        final Issue issue = getIssueObject();
        AggregateTimeTrackingBean aggregates = (AggregateTimeTrackingBean) request.getAttribute(AggregateTimeTrackingBean.AGG_TIMETRACKING + issue.getId());

        if (aggregates == null)
        {
            final AggregateTimeTrackingCalculator calculator = aggregateTimeTrackingCalculatorFactory.getCalculator(issue);
            aggregates = calculator.getAggregates(issue);
            request.setAttribute(AggregateTimeTrackingBean.AGG_TIMETRACKING + issue.getId(), aggregates);
        }

        return aggregates;
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

    public boolean showEdit()
    {
        if (isEditable == null)
        {
            final Issue issueObject = getIssueObject();
            isEditable = getPermissionManager().hasPermission(Permissions.EDIT_ISSUE, issueObject, getLoggedInUser()) && getIssueManager().isEditable(issueObject);
        }
        return isEditable;
    }

    public boolean showLogin()
    {
        if (getLoggedInUser() != null)
        {
            return false;
        }
        // We are only showing the login when no other buttons are available.
        if (showEdit())
        {
            return false;
        }

        final IssueOperationsBarUtil opsBarUtil = getOpsBarUtil();
        final List<SimpleLinkSection> groups = opsBarUtil.getGroups();
        for (SimpleLinkSection group : groups)
        {
            final List<SimpleLink> links = opsBarUtil.getPromotedLinks(group);
            if (!links.isEmpty())
            {
                return false;
            }
        }
        return true;
    }

    public IssueOperationsBarUtil getOpsBarUtil()
    {
        if (issueOperationsBarUtil == null)
        {
            final MutableIssue issue = getIssueObject();
            final Map<String, Object> params = new HashMap<String, Object>();
            params.put("issue", issue);
            params.put("issueId", issue.getId());
            params.put("display-context", "view-issue");
            final JiraHelper helper = new JiraHelper(ServletActionContext.getRequest(), issue.getProjectObject(), params);
            issueOperationsBarUtil = new IssueOperationsBarUtil(helper, getLoggedInUser(), simpleLinkManager, getApplicationProperties());
        }
        return issueOperationsBarUtil;
    }

    public String getSummaryHtml()
    {
        final IssueSummaryBlock issueSummary = ComponentManager.getComponentInstanceOfType(IssueSummaryBlock.class);
        return issueSummary.getHtml(getIssueObject(), this);
    }

    public Collection<IssueViewModuleDescriptor> getIssueViews()
    {
        return pluginAccessor.getEnabledModuleDescriptorsByClass(IssueViewModuleDescriptor.class);
    }

    public String getUrlForIssueView(IssueViewModuleDescriptor descriptor)
    {
        return descriptor.getURLWithoutContextPath(getIssueObject().getKey());
    }

    public boolean getZipSupport()
    {
        return getApplicationProperties().getOption(APKeys.JIRA_OPTION_ALLOW_ZIP_SUPPORT);
    }
    /**
     * Determines whether the specified attachment should be expanded as a zip file. Files are expanded if zip support 
     * is on, the file extension is not one of the extensions specified by {@link com.atlassian.jira.web.bean.NonZipExpandableExtensions}
     * and if the file represents a valid zip file.
     * @param attachment The attachment in play.
     * @return true if the the specified attachment should be expanded as a zip file; otherwise, false is returned.
     */
    public boolean shouldExpandAsZip(Attachment attachment)
    {
        final File attachmentFile = AttachmentUtils.getAttachmentFile(attachment);
        final String attachmentExtension = FilenameUtils.getExtension(attachmentFile.getName());

        return getZipSupport() && !nonZipExpandableExtensions.contains(attachmentExtension) && attachmentZipKit.isZip(attachmentFile);
    }

    public int getMaximumNumberOfZipEntriesToShow()
    {
        String maximumNumberOfZipEntriesToShowAsString = getApplicationProperties().getDefaultBackedString(APKeys.JIRA_ATTACHMENT_NUMBER_OF_ZIP_ENTRIES_TO_SHOW);
        int maximumNumberOfZipEntriesToShow = 30;
        try
        {
            maximumNumberOfZipEntriesToShow = Integer.parseInt(maximumNumberOfZipEntriesToShowAsString);
        }
        catch (NumberFormatException e)
        {
            //Ignoring error, we'll use the default of 30
        }
        return maximumNumberOfZipEntriesToShow;
    }

    /**
     * <p>Returns a list of zip entries for the specified attachment. The number of entries returned is limited to the
     * value of MAX_ZIP_ENTRIES.</p>
     *
     * <p>It is assumed that this attachment represents a valid zip file. In order to find this out, use
     * {@link com.atlassian.jira.web.action.issue.ViewIssue#shouldExpandAsZip(com.atlassian.jira.issue.attachment.Attachment)}.</p>
     *
     * @param attachment The attachment in play.
     * @return A {@link java.util.List} of {@link com.atlassian.jira.issue.attachment.AttachmentZipKit.AttachmentZipEntry}
     * for the specified attachment. Limited to {@link APKeys#JIRA_ATTACHMENT_NUMBER_OF_ZIP_ENTRIES_TO_SHOW}.
     */
    public AttachmentZipKit.AttachmentZipEntries getZipEntries(Attachment attachment)
    {
        try
        {
            File attachmentFile = AttachmentUtils.getAttachmentFile(attachment);
            return attachmentZipKit.listEntries(attachmentFile, getMaximumNumberOfZipEntriesToShow(), AttachmentZipKit.FileCriteria.ONLY_FILES);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    // allow attachment categoriser to use our attachment list
    private class ViewIssueAttachmentsSource implements AttachmentsCategoriser.Source
    {
        @Override
        public List<Attachment> getAttachments()
        {
            return getAttachmentList();
        }
    }
}
