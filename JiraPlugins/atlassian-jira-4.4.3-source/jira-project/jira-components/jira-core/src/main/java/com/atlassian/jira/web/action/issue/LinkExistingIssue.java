package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.issuelink.IssueLinkService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.link.IssueLinkDisplayHelper;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.plugin.webresource.WebResourceManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LinkExistingIssue extends AbstractCommentableIssue implements OperationContext
{
    private String[] linkKey;
    private String linkDesc;
    private Collection<String> linkDescs;

    private IssueLinkType issueLinkType;

    final private List<MutableIssue> issues = new ArrayList<MutableIssue>();
    private final IssueLinkTypeManager issueLinkTypeManager;
    private final IssueLinkService issueLinkService;
    private final IssueLinkDisplayHelper issueLinkDisplayHelper;
    private final UserHistoryManager userHistoryManager;

    public LinkExistingIssue(
            final IssueLinkManager issueLinkManager,
            final IssueLinkTypeManager issueLinkTypeManager,
            final SubTaskManager subTaskManager,
            final FieldManager fieldManager,
            final FieldScreenRendererFactory fieldScreenRendererFactory,
            final ProjectRoleManager projectRoleManager,
            final CommentService commentService,
            final UserHistoryManager userHistoryManager,
            final IssueLinkService issueLinkService)
    {
        super(issueLinkManager, subTaskManager, fieldScreenRendererFactory, fieldManager, projectRoleManager, commentService);
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.issueLinkService = issueLinkService;
        this.userHistoryManager = userHistoryManager;
        this.issueLinkDisplayHelper = new IssueLinkDisplayHelper(userHistoryManager, getLoggedInUser());

        ComponentManager.getComponent(WebResourceManager.class).requireResource("jira.webresources:jira-fields");
    }

    protected void doValidation()
    {
        try
        {
            if (!isHasIssuePermission(Permissions.LINK_ISSUE, getIssue()))
            {
                addErrorMessage(getText("linkissue.error.nopermission"));
            }

            super.doValidation(); // validate comment

            if (linkKey != null && linkKey.length > 0)
            {
                for (String key : linkKey)
                {
                    final MutableIssue issue = ComponentManager.getComponent(IssueManager.class).getIssueObject(key);
                    if (issue == null)
                    {
                        addError("linkKey", getText("linkissue.error.notexist", key));
                    }
                    else if (key.equals(getIssue().getString("key")))
                    {
                        addError("linkKey", getText("linkissue.error.selflink"));
                    }
                    else
                    {
                        issues.add(issue);
                    }

                }
            }
            else
            {
                addError("linkKey", getText("linkissue.error.keyrequired"));
            }

            if (!getLinkDescs().contains(linkDesc))
            {
                addError("linkDesc", getText("linkissue.error.invalidlinkdesc"));
            }
            else if (getIssueLinkType().isSystemLinkType())
            {
                // Check that the chosen link type is not a system link type.
                // This should not happen - the system should not present a user with the description
                // of a system link type to chose
                addError("linkDesc", getText("linkissue.error.systemlink"));
            }
        }
        catch (IssueNotFoundException e)
        {
            // Do nothing as error added above
        }
        catch (IssuePermissionException e)
        {
            // Do nothing as error added above
        }
    }

    public String doDefault() throws Exception
    {
        try
        {
            getIssueObject();
        }
        catch (IssueNotFoundException e)
        {
            // Error is added above
            return ERROR;
        }
        catch (IssuePermissionException e)
        {
            return ERROR;
        }

        return super.doDefault();

    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        IssueLinkType linkType = getIssueLinkType();

        boolean addLocalComment = true;
        for (final MutableIssue issue : issues)
        {
            linkIssue(linkType, issue, addLocalComment);
            addLocalComment = false;
        }

        if (isInlineDialogMode())
        {
            return returnCompleteWithInlineRedirect("/browse/" + getIssue().getString("key") + "#linkingmodule");
        }

        return returnComplete("/browse/" + getIssue().getString("key") + "#linkingmodule");
    }

    private void linkIssue(IssueLinkType linkType, MutableIssue destinationIssue, boolean addCommentToLocalIssue)
    {
        try
        {
            if (linkDesc.equals(linkType.getOutward()))
            {
                getIssueLinkManager().createIssueLink(getIssue().getLong("id"), destinationIssue.getId(), linkType.getId(), null, getLoggedInUser());
            }
            else
            {
                getIssueLinkManager().createIssueLink(destinationIssue.getId(), getIssue().getLong("id"), linkType.getId(), null, getLoggedInUser());
            }
            userHistoryManager.addItemToHistory(UserHistoryItem.ISSUELINKTYPE, getLoggedInUser(), String.valueOf(linkType.getId()), linkDesc);

            if (addCommentToLocalIssue)
            {
                createComment(getIssueObject());
            }
            createComment(destinationIssue);

            // Reset the fields as comment has been persisted to the db.
            getIssueObject().resetModifiedFields();
        }
        catch (Exception e)
        {
            log.error("Error occurred creating link: " + e, e);
            addErrorMessage(getText("admin.errors.issues.an.error.occured", e));
        }
    }

    private IssueLinkType getIssueLinkType()
    {
        if (issueLinkType == null)
        {
            for (IssueLinkType linkType : issueLinkTypeManager.getIssueLinkTypes())
            {
                if (linkDesc.equals(linkType.getOutward()) || linkDesc.equals(linkType.getInward()))
                {
                    issueLinkType = linkType;
                    break;
                }
            }
        }

        return issueLinkType;
    }

    public String[] getLinkKey()
    {
        return linkKey;
    }

    public void setLinkKey(String[] linkKey)
    {
        this.linkKey = linkKey;
    }

    public String getLinkDesc()
    {
        return linkDesc;
    }

    public void setLinkDesc(String linkDesc)
    {
        this.linkDesc = linkDesc;
    }

    public Collection getLinkDescs()
    {
        if (linkDescs == null)
        {
            linkDescs = issueLinkDisplayHelper.getSortedIssueLinkTypes(issueLinkService.getIssueLinkTypes());
        }

        return linkDescs;
    }

    public String getLastUsedLinkType()
    {
        return issueLinkDisplayHelper.getLastUsedLinkType();
    }

    public List<MutableIssue> getCurrentValue()
    {
        return issues;

    }

    @Override
    public Map<String, Object> getDisplayParams()
    {
        final Map<String, Object> displayParams = new HashMap<String, Object>(super.getDisplayParams());
        displayParams.put("theme", "aui");
        return displayParams;
    }
}
