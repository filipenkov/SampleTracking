package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.pager.NextPreviousPager;
import com.atlassian.jira.issue.pager.PagerManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.trackback.TrackbackManager;
import com.atlassian.trackback.Trackback;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class ManageTrackbacks extends AbstractIssueSelectAction
{
    private final TrackbackManager trackbackManager;

    private Long trackbackId;
    private boolean confirm;
    private Collection trackbacks;
    Collection affectedVersions;
    Collection components;
    Collection fixVersions;
    PagerManager pagerManager;
    private Issue parentIssueObject = null;

    public ManageTrackbacks(TrackbackManager trackbackManager, final PagerManager pagerManager)
    {
        this.trackbackManager = trackbackManager;
        this.pagerManager = pagerManager;
    }

    public String doDelete() throws Exception
    {
        if (!isCanDeleteTrackbacks())
            return ERROR;

        if (confirm)
        {
            trackbackManager.deleteTrackback(trackbackId);
            return getRedirect("ManageTrackbacks.jspa?id=" + getIssue().get("id"));
        }
        else
        {
            // No confirmation supplied - ask for one
            return INPUT;
        }
    }


    @Override
    protected String doExecute() throws Exception
    {
        final NextPreviousPager pager = getNextPreviousPager();

        if (pager != null)
        {
            final Issue issue = getIssueObject();
            pager.update(getSearchRequest(), getRemoteUser(), issue == null ? null : issue.getKey());
        }
        return super.doExecute();
    }

    /**
     * Checks if the issue is a subtask. This is used by issue_headertable.jsp
     * @return true if issue is a subtask, false otherwise
     */
    public boolean isSubTask()
    {
        final Issue issue = getIssueObject();
        return issue != null && issue.isSubTask();
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

    public Long getTrackbackId()
    {
        return trackbackId;
    }

    public void setTrackbackId(Long trackbackId)
    {
        this.trackbackId = trackbackId;
    }

    public boolean isConfirm()
    {
        return confirm;
    }

    public void setConfirm(boolean confirm)
    {
        this.confirm = confirm;
    }

    public boolean isCanDeleteTrackbacks()
    {
        return isHasIssuePermission(Permissions.DELETE_ISSUE, getIssue());
    }

    public Collection getTrackbacks() throws GenericEntityException
    {
        if (trackbacks == null)
            trackbacks = trackbackManager.getTrackbacksForIssue(getIssue());
        return trackbacks;
    }

    public Trackback getTrackback(Long id) throws GenericEntityException
    {
        return trackbackManager.getTrackback(id);
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


    public NextPreviousPager getNextPreviousPager()
    {
        return pagerManager.getPager();
    }
}
