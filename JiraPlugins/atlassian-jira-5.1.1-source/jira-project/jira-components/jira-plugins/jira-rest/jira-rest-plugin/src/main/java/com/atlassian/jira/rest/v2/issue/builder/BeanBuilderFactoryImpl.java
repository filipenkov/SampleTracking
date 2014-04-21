package com.atlassian.jira.rest.v2.issue.builder;

import com.atlassian.jira.bc.config.ConstantsService;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueLinksBeanBuilderFactory;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.issue.thumbnail.ThumbnailManager;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.v2.issue.AttachmentBeanBuilder;
import com.atlassian.jira.rest.v2.issue.CreateMetaBeanBuilder;
import com.atlassian.jira.rest.v2.issue.EditMetaBeanBuilder;
import com.atlassian.jira.rest.v2.issue.IncludedFields;
import com.atlassian.jira.rest.v2.issue.IssueBeanBuilder;
import com.atlassian.jira.rest.v2.issue.OpsbarBeanBuilder;
import com.atlassian.jira.rest.v2.issue.RemoteIssueLinkBeanBuilder;
import com.atlassian.jira.rest.v2.issue.ResourceUriBuilder;
import com.atlassian.jira.rest.v2.issue.TransitionMetaBeanBuilder;
import com.atlassian.jira.rest.v2.issue.context.ContextI18n;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.rest.v2.issue.project.ProjectBeanFactory;
import com.atlassian.jira.rest.v2.issue.version.VersionBeanFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.workflow.IssueWorkflowManager;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.plugin.PluginAccessor;

import java.net.URI;

import static com.atlassian.jira.rest.v2.issue.VelocityRequestContextFactories.getBaseURI;

/**
 * Default implementation for BeanBuilderFactory.
 *
 * @since v4.2
 */
public class BeanBuilderFactoryImpl implements BeanBuilderFactory
{
    private final UserManager userManager;
    private final ThumbnailManager thumbnailManager;
    private final VersionBeanFactory versionBeanFactory;
    private final ProjectBeanFactory projectBeanFactory;
    private final FieldLayoutManager fieldLayoutManager;
    private final JiraAuthenticationContext authContext;
    private final FieldManager fieldManager;
    private final ResourceUriBuilder uriBuilder;
    private final ContextUriInfo contextUriInfo;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final ProjectManager projectManager;
    private final ConstantsService constantsService;
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final PermissionManager permissionManager;
    private final FieldScreenRendererFactory fieldScreenRendererFactory;
    private final IssueManager issueManager;
    private final JiraBaseUrls baseUrls;
    private final IssueLinksBeanBuilderFactory issueLinkBeanBuilderFactory;
    private final IssueWorkflowManager issueWorkflowManager;
    private final WorkflowManager workflowManager;
    private final StatusManager statusManager;
    private final IssueFactory issueFactory;
    private final ChangeHistoryManager changeHistoryManager;
    private final ApplicationProperties applicationProperties;
    private final SimpleLinkManager simpleLinkManager;
    private final ContextI18n i18nHelper;
    private final PluginAccessor pluginAccessor;

    public BeanBuilderFactoryImpl(
            final UserManager userManager,
            final ThumbnailManager thumbnailManager,
            final VersionBeanFactory versionBeanFactory,
            final ProjectBeanFactory projectBeanFactory,
            final FieldLayoutManager fieldLayoutManager,
            final JiraAuthenticationContext authContext,
            final FieldManager fieldManager,
            final ResourceUriBuilder uriBuilder,
            final ContextUriInfo contextUriInfo,
            final VelocityRequestContextFactory velocityRequestContextFactory,
            final ProjectManager projectManager,
            final ConstantsService constantsService,
            final IssueTypeSchemeManager issueTypeSchemeManager,
            final PermissionManager permissionManager,
            final FieldScreenRendererFactory fieldScreenRendererFactory,
            final IssueManager issueManager, final JiraBaseUrls baseUrls,
            final IssueLinksBeanBuilderFactory issueLinkBeanBuilderFactory,
            final IssueWorkflowManager issueWorkflowManager,
            final WorkflowManager workflowManager,
            final StatusManager statusManager, IssueFactory issueFactory, ChangeHistoryManager changeHistoryManager,
            final ApplicationProperties applicationProperties,
            final SimpleLinkManager simpleLinkManager,
            final ContextI18n i18nHelper,
            final PluginAccessor pluginAccessor)
    {
        this.userManager = userManager;
        this.thumbnailManager = thumbnailManager;
        this.versionBeanFactory = versionBeanFactory;
        this.projectBeanFactory = projectBeanFactory;
        this.fieldLayoutManager = fieldLayoutManager;
        this.authContext = authContext;
        this.fieldManager = fieldManager;
        this.uriBuilder = uriBuilder;
        this.contextUriInfo = contextUriInfo;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.projectManager = projectManager;
        this.constantsService = constantsService;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.permissionManager = permissionManager;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.issueManager = issueManager;
        this.baseUrls = baseUrls;
        this.issueLinkBeanBuilderFactory = issueLinkBeanBuilderFactory;
        this.issueWorkflowManager = issueWorkflowManager;
        this.workflowManager = workflowManager;
        this.statusManager = statusManager;
        this.issueFactory = issueFactory;
        this.changeHistoryManager = changeHistoryManager;
        this.applicationProperties = applicationProperties;
        this.simpleLinkManager = simpleLinkManager;
        this.i18nHelper = i18nHelper;
        this.pluginAccessor = pluginAccessor;
    }

    /**
     * Returns a new AttachmentBeanBuilder.
     *
     * @param attachment an Attachment
     * @return an AttachmentBeanBuilder
     */
    @Override
    public AttachmentBeanBuilder newAttachmentBeanBuilder(final Attachment attachment)
    {
        return new AttachmentBeanBuilder(baseURI(), userManager, thumbnailManager, attachment); 
    }

    /**
     * Returns a new instance of an IssueBeanBuilder.
     *
     * @return an IssueBeanBuilder
     * @param issue
     * @param include
     */
    @Override
    public IssueBeanBuilder newIssueBeanBuilder(final Issue issue, IncludedFields include)
    {
        return new IssueBeanBuilder(fieldLayoutManager, authContext,
                fieldManager, uriBuilder, this,
                contextUriInfo, issue, include, issueLinkBeanBuilderFactory, issueWorkflowManager);
    }

    /**
     * Returns a new instance of an IssueLinkBeanBuilder.
     *
     * @return an IssueLinkBeanBuilder
     * @param issue
    @Override
    public IssueLinkBeanBuilder newIssueLinkBeanBuilder(final Issue issue)
    {
        return new IssueLinkBeanBuilder(applicationProperties, issueLinkManager, authContext, baseUrls, issue);
    }
     */

    /**
     * Returns a new instance of a CreateMetaBeanBuilder.
     *
     * @return a CreateMetaBeanBuilder
     */
    @Override
    public CreateMetaBeanBuilder newCreateMetaBeanBuilder()
    {
        return new CreateMetaBeanBuilder(authContext, projectManager, fieldLayoutManager,
                velocityRequestContextFactory, contextUriInfo, projectBeanFactory, issueTypeSchemeManager,
                permissionManager, versionBeanFactory, baseUrls, issueFactory, fieldScreenRendererFactory);
    }

    /**
     * Returns a new instance of a EditMetaBeanBuilder.
     *
     * @return a EditMetaBeanBuilder
     */
    @Override
    public EditMetaBeanBuilder newEditMetaBeanBuilder()
    {
        return new EditMetaBeanBuilder(authContext, fieldLayoutManager, velocityRequestContextFactory, contextUriInfo, versionBeanFactory, baseUrls, permissionManager, fieldScreenRendererFactory, fieldManager);
    }

    /**
     * Returns a new instance of a TransitionMetaBeanBuilder.
     *
     * @return a TransitionMetaBeanBuilder
     */
    @Override
    public TransitionMetaBeanBuilder newTransitionMetaBeanBuilder()
    {
        return new TransitionMetaBeanBuilder(fieldScreenRendererFactory, authContext, fieldLayoutManager, velocityRequestContextFactory, contextUriInfo, versionBeanFactory, baseUrls, workflowManager, statusManager);
    }

    @Override
    public OpsbarBeanBuilder newOpsbarBeanBuilder(final Issue issue)
    {
        return new OpsbarBeanBuilder(issue, applicationProperties, simpleLinkManager, authContext, i18nHelper, issueManager, pluginAccessor);
    }

    /**
     * Returns a new instance of a RemoteIssueLinkBeanBuilder.
     *
     * @param remoteIssueLink
     * @return a RemoteIssueLinkBeanBuilder
     */
    @Override
    public RemoteIssueLinkBeanBuilder newRemoteIssueLinkBeanBuilder(final RemoteIssueLink remoteIssueLink)
    {
        return new RemoteIssueLinkBeanBuilder(contextUriInfo, issueManager, remoteIssueLink);
    }

    @Override
    public ChangelogBeanBuilder newChangelogBeanBuilder()
    {
        return new ChangelogBeanBuilder(baseUrls, changeHistoryManager, authContext);
    }

    private URI baseURI()
    {
        return getBaseURI(velocityRequestContextFactory);
    }
}
