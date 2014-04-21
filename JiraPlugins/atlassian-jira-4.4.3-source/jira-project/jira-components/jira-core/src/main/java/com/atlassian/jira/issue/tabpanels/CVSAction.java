package com.atlassian.jira.issue.tabpanels;

import com.atlassian.core.user.UserUtils;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueAction;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor;
import com.atlassian.jira.vcs.cvsimpl.CVSCommit;
import com.opensymphony.user.EntityNotFoundException;
import com.opensymphony.user.User;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class CVSAction extends AbstractIssueAction
{
    private CVSCommit commit;
    private User user;

    public CVSAction(IssueTabPanelModuleDescriptor descriptor, CVSCommit commit)
    {
        super(descriptor);
        this.commit = commit;
    }

    public Date getTimePerformed()
    {
        return commit.getTimePerformed();
    }

    protected void populateVelocityParams(Map params)
    {
        params.put("cvsCommit", this);
    }

    //-------------------------------------------------------------------------------- Methods used by velocity template
    public String getUsername()
    {
       return commit.getUsername();
    }

    public User getUser()
    {
        if (user == null)
        {
            try
            {
                user = UserUtils.getUser(commit.getUsername());
            }
            catch (EntityNotFoundException e)
            {
            }
        }
        return user;
    }

    public String getFullName()
    {
        if (getUser() != null)
            return getUser().getDisplayName();
        return null;
    }

    public String getComment()
    {
        return commit.getComment();
    }

    public Collection getRevisions()
    {
        return commit.getRevisions();
    }

    public String getRepositoryName()
    {
        return commit.getRepositoryName();
    }

    public String getBranchName()
    {
        return commit.getBranchName();
    }

    public boolean hasRepositoryViewer()
    {
        return commit.hasRepositoryViewer();
    }

    public String getFileLink(String filePath)
    {
        return commit.getFileLink(filePath);
    }

    public String getRevisionLink(String filePath, String revision)
    {
        return commit.getRevisionLink(filePath, revision);
    }

    public String getDiffLink(String filePath, String revision)
    {
        return commit.getDiffLink(filePath, revision);
    }
}
