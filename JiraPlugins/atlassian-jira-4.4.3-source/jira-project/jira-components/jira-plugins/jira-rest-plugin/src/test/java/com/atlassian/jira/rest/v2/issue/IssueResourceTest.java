package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.attachment.AttachmentService;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.vote.VoteService;
import com.atlassian.jira.bc.issue.watcher.WatcherService;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.jql.resolver.ResolverManager;
import com.atlassian.jira.rest.v2.issue.builder.BeanBuilderFactory;
import com.atlassian.jira.rest.v2.issue.context.ContextI18n;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.rest.v2.issue.customfield.CustomFieldOps;
import com.atlassian.jira.rest.v2.issue.project.ProjectBeanFactory;
import com.atlassian.jira.rest.v2.issue.version.VersionBeanFactory;
import com.atlassian.jira.rest.v2.issue.watcher.WatcherOps;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.workflow.WorkflowManager;
import junit.framework.TestCase;

import javax.ws.rs.core.UriInfo;

import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

/**
 * Abstract base class to take care of instantiating all of IssueResource's dependencies.
 *
 * @since v4.2
 */
public abstract class IssueResourceTest extends TestCase
{
    protected UriInfo uriInfo;
    protected Issue issue;
    protected ResourceUriBuilder uriBuilder;
    protected IssueBean bean;
    protected IssueService issueService;
    protected JiraAuthenticationContext authContext;
    protected CommentService commentService;
    protected FieldLayout fieldLayout;
    protected FieldLayoutManager fieldLayoutManager;
    protected VelocityRequestContext velocityRequestContext;
    protected VelocityRequestContextFactory velocityRequestContextFactory;
    protected WorkflowManager workflowManager;
    protected FieldScreenRendererFactory fieldScreenRendererFactory;
    protected UserManager userManager;
    protected AttachmentManager attachmentManager;
    protected ApplicationProperties applicationProperties;
    protected IssueLinkManager issueLinkManager;
    protected FieldManager fieldManager;
    protected RendererManager rendererManager;
    protected ProjectRoleManager projectRoleManager;
    protected IssueSecurityLevelManager issueSecurityLevelManager;
    protected WorklogService worklogService;
    protected ResolverManager resolverManager;
    protected CustomFieldOps customFieldOps;
    protected VoteService voteService;
    protected ContextI18n i18n;
    protected WatcherOps watcherOps;
    protected AttachmentService attachmentService;
    protected IssueUpdater issueUpdater;
    protected WatcherService watcherService;
    protected BeanBuilderFactory beanBuilderFactory;
    protected ContextUriInfo contextUriInfo;
    protected IssueManager issueManager;
    protected PermissionManager permissionManager;
    protected ProjectBeanFactory projectBeanFactory;
    protected VersionBeanFactory versionBeanFactory;

    @Override
    protected final void setUp() throws Exception
    {
        super.setUp();
        createDependencies();
        doSetUp();
    }

    /**
     * Perform per-test setup.
     */
    protected void doSetUp()
    {
        // empty
    }

    protected void replayMocks()
    {
        replay(uriInfo,
                issue,
                uriBuilder,
                bean,
                issueService,
                authContext,
                commentService,
                fieldLayout,
                fieldLayoutManager,
                velocityRequestContext,
                velocityRequestContextFactory,
                workflowManager,
                fieldScreenRendererFactory,
                userManager,
                attachmentManager,
                applicationProperties,
                issueLinkManager,
                fieldManager,
                rendererManager,
                projectRoleManager,
                issueSecurityLevelManager,
                worklogService,
                resolverManager,
                customFieldOps,
                voteService,
                i18n,
                watcherOps,
                attachmentService,
                issueUpdater,
                watcherService,
                beanBuilderFactory,
                contextUriInfo,
                issueManager,
                permissionManager,
                projectBeanFactory,
                versionBeanFactory
        );
    }

    /**
     * Creates all the mocks.
     */
    private void createDependencies()
    {
        uriInfo = createMock(UriInfo.class);
        issue = createMock(Issue.class);
        bean = createMock(IssueBean.class);
        uriBuilder = createMock(ResourceUriBuilder.class);
        issueService = createMock(IssueService.class);
        authContext = createMock(JiraAuthenticationContext.class);
        commentService = createMock(CommentService.class);
        fieldLayout = createMock(FieldLayout.class);
        fieldLayoutManager = createMock(FieldLayoutManager.class);
        velocityRequestContext = createMock(VelocityRequestContext.class);
        velocityRequestContextFactory = createMock(VelocityRequestContextFactory.class);
        workflowManager = createMock(WorkflowManager.class);
        fieldScreenRendererFactory = createMock(FieldScreenRendererFactory.class);
        userManager = createMock(UserManager.class);
        attachmentManager = createMock(AttachmentManager.class);
        applicationProperties = createMock(ApplicationProperties.class);
        issueLinkManager = createMock(IssueLinkManager.class);
        fieldManager = createMock(FieldManager.class);
        rendererManager = createMock(RendererManager.class);
        projectRoleManager = createMock(ProjectRoleManager.class);
        issueSecurityLevelManager = createMock(IssueSecurityLevelManager.class);
        worklogService = createMock(WorklogService.class);
        resolverManager = createMock(ResolverManager.class);
        customFieldOps = createMock(CustomFieldOps.class);
        voteService = createMock(VoteService.class);
        i18n = createMock(ContextI18n.class);
        watcherOps = createMock(WatcherOps.class);
        attachmentService = createMock(AttachmentService.class);
        issueUpdater = createMock(IssueUpdater.class);
        watcherService = createMock(WatcherService.class);
        beanBuilderFactory = createMock(BeanBuilderFactory.class);
        contextUriInfo = createMock(ContextUriInfo.class);
        issueManager = createMock(IssueManager.class);
        permissionManager = createMock(PermissionManager.class);
        versionBeanFactory = createMock(VersionBeanFactory.class);
        projectBeanFactory = createMock(ProjectBeanFactory.class);
    }

    protected IssueResource createIssueResource()
    {
        return new IssueResource(issueService, authContext, commentService, fieldLayoutManager, workflowManager,
                fieldScreenRendererFactory, userManager, attachmentManager, fieldManager, applicationProperties,
                issueLinkManager, uriBuilder, rendererManager, projectRoleManager, issueSecurityLevelManager,
                worklogService, resolverManager, customFieldOps, voteService, i18n, watcherOps, watcherService,
                beanBuilderFactory, contextUriInfo, issueManager, permissionManager, versionBeanFactory,
                projectBeanFactory);
    }
}
