package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.attachment.AttachmentService;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.pager.NextPreviousPager;
import com.atlassian.jira.issue.pager.PagerManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class ManageAttachments extends AbstractCommentableIssue
{
    private static final String ATTACH_FILE_ISSUE_OPERATION_KEY = "com.atlassian.jira.plugin.system.issueoperations:attach-file";
    private static final String ATTACH_SCREENSHOT_ISSUE_OPERATION_KEY = "com.atlassian.jira.plugin.system.issueoperations:attach-screenshot";

    private final AttachmentService attachmentService;
    private final PluginAccessor pluginAccessor;
    private final ApplicationProperties applicationProperties;
    private final PagerManager pagerManager;

    Collection affectedVersions;
    Collection components;
    Collection fixVersions;
    private Issue parentIssueObject = null;

    public ManageAttachments(IssueLinkManager issueLinkManager, SubTaskManager subTaskManager,
                             AttachmentService attachmentService, PluginAccessor pluginAccessor,
                             final ApplicationProperties applicationProperties, FieldScreenRendererFactory fieldScreenRendererFactory,
                             FieldManager fieldManager, ProjectRoleManager projectRoleManager, CommentService commentService,
                             final PagerManager pagerManager)
    {
        super(issueLinkManager, subTaskManager, fieldScreenRendererFactory, fieldManager, projectRoleManager, commentService);
        this.attachmentService = attachmentService;
        this.pluginAccessor = pluginAccessor;
        this.applicationProperties = applicationProperties;
        this.pagerManager = pagerManager;
    }

    @Override
    protected void doValidation()
    {
    }

    @Override
    protected String doExecute() throws Exception
    {
        final Issue issue;
        try
        {
            issue = getIssueObject();
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

        final NextPreviousPager pager = getNextPreviousPager();

        if (pager != null)
        {
            pager.update(getSearchRequest(), getRemoteUser(), issue == null ? null : issue.getKey());
        }
        return super.doExecute();
    }

    public boolean isScreenshotAttachable()
    {
        /*
          Test whether the user is on Windows or OSX. No other operating system (that I know about) is able to paste
          images into the clipboard such that they are available to a Java applet. So we should not make the
          'attach screenshot' link available to users that are not on Windows. Note, that here we are testing
          whether the user's browser is running on Windows - NOT whether the app server that runs JIRA webapp is
          running on Windows.

          Also check whether the applet is enabled

          JRA-12403 - check that the attach-file issue operation plugin is visible
        */
        return isIssueOperationShowable(ATTACH_SCREENSHOT_ISSUE_OPERATION_KEY) &&
                attachmentService.canAttachScreenshots(getJiraServiceContext(), getIssueObject());
    }

    public boolean isAttachable()
    {
        //JRA-12409 - you can only attach a file to the issue if the user has permission to create an attachment and the
        // issue is in an editable workflow state.
        // JRA-12403 - check that the attach-file issue operation plugin is visible
        return isIssueOperationShowable(ATTACH_FILE_ISSUE_OPERATION_KEY) &&
                attachmentService.canCreateAttachments(getJiraServiceContext(), getIssueObject());
    }

    public Collection /*<GenericValue>*/ getComponents() throws Exception
    {
        if (getIssueObject() != null && components == null)
        {
            components = getIssueObject().getComponents();
        }

        return components;
    }

    public Collection /*<GenericValue>*/ getAffectedVersions() throws Exception
    {
        if (getIssueObject() != null && affectedVersions == null)
        {
            affectedVersions = new ArrayList();
            // TODO: return the actual Version objects instead of GenericValues, when Issue.getComponents() gets its act together
            for (Iterator iterator = getIssueObject().getAffectedVersions().iterator(); iterator.hasNext();)
            {
                Version version = (Version) iterator.next();
                affectedVersions.add(version.getGenericValue());
            }
        }

        return affectedVersions;
    }

    public Collection /*<GenericValue>*/ getFixVersions() throws Exception
    {
        if (getIssueObject() != null && fixVersions == null)
        {
            fixVersions = new ArrayList();
            // TODO: return the actual Version objects instead of GenericValues, when Issue.getComponents() gets its act together
            for (Iterator iterator = getIssueObject().getFixVersions().iterator(); iterator.hasNext();)
            {
                Version version = (Version) iterator.next();
                fixVersions.add(version.getGenericValue());
            }
        }

        return fixVersions;
    }

    public boolean isHasDeleteAttachmentPermission(Long attachmentId)
    {
        // Do not call this with the action as the error collection, otherwise you will get permission errors for
        // those attachments you do not have permission to delete
        JiraServiceContext context = new JiraServiceContextImpl(getRemoteUser(), new SimpleErrorCollection());
        return attachmentService.canDeleteAttachment(context, attachmentId);
    }

    /**
     * Returns the parent of the current {@link Issue}
     *
     * @return the parent issue object
     */
    public Issue getParentIssueObject()
    {
        if (isSubTask())
        {
            if (parentIssueObject == null)
            {
                final Issue issue = getIssueObject();
                if (issue != null && issue.isSubTask() && issue.getParentObject() != null)
                {
                    parentIssueObject = issue.getParentObject();
                }
            }
        }

        return parentIssueObject;
    }

    public NextPreviousPager getNextPreviousPager()
    {
        return pagerManager.getPager();
    }

    private boolean isIssueOperationShowable(String issueOperationKey)
    {
        ModuleDescriptor moduleDescriptor = pluginAccessor.getEnabledPluginModule(issueOperationKey);
        return moduleDescriptor != null;
    }

    public boolean getZipSupport()
    {
        return getApplicationProperties().getOption(APKeys.JIRA_OPTION_ALLOW_ZIP_SUPPORT);
    }

}
