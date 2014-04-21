package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.rest.bind.DateTimeAdapter;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.user.util.UserManager;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

/**
 * @since v4.2
 */
@XmlRootElement (name="worklog")
public class WorklogBean
{
    @XmlElement
    private URI self;

    @XmlElement
    private URI issue;

    @XmlElement
    private UserBean author;

    @XmlElement
    private UserBean updateAuthor;

    @XmlElement
    private String comment;

    @XmlJavaTypeAdapter (DateTimeAdapter.class)
    private Date created;

    @XmlJavaTypeAdapter (DateTimeAdapter.class)
    private Date updated;

    @XmlElement
    private VisibilityBean visibility;

    @XmlJavaTypeAdapter (DateTimeAdapter.class)
    private Date started;

    @XmlElement
    private Long minutesSpent;

    public static List<WorklogBean> asBeans(final List<Worklog> worklogs, final UriInfo uriInfo, final UserManager userManager)
    {
        return Lists.newArrayList(Iterables.transform(worklogs, new Function<Worklog, WorklogBean>() {
            public WorklogBean apply(@Nullable Worklog from)
            {
                return getWorklog(from, uriInfo, userManager);
            }
        }));
    }
    
    public static WorklogBean getWorklog(final Worklog log, final UriInfo uriInfo, final UserManager userManager)
    {
        final WorklogBean bean = new WorklogBean();
        bean.self = uriInfo.getBaseUriBuilder().path(WorklogResource.class).path(log.getId().toString()).build();
        bean.author = getUserBean(uriInfo, log.getAuthor(), userManager);
        bean.comment = log.getComment();
        bean.updateAuthor = getUserBean(uriInfo, log.getUpdateAuthor(), userManager);
        bean.created = log.getCreated();
        bean.updated = log.getUpdated();
        bean.started = log.getStartDate();
        bean.minutesSpent = log.getTimeSpent() / 60; // REST always reports in minutes
        bean.issue = uriInfo.getBaseUriBuilder().path(IssueResource.class).path(log.getIssue().getKey()).build();


        final String groupLevel = log.getGroupLevel();
        final ProjectRole roleLevel = log.getRoleLevel();

        if (groupLevel != null)
        {
            bean.visibility = new VisibilityBean(VisibilityType.GROUP, groupLevel);
        }
        else if (roleLevel != null)
        {
            bean.visibility = new VisibilityBean(VisibilityType.ROLE, roleLevel.getName());
        }
        return bean;
    }

    /**
     * Returns a UserBean for the user with the given name. If the user does not exist, the returned bean contains only
     * the username and no more info.
     *
     * @param uriInfo a UriInfo
     * @param username a String containing a user name
     * @param userManager Manager for users
     * @return a UserBean
     */
    protected static UserBean getUserBean(UriInfo uriInfo, String username, final UserManager userManager)
    {
        return new UserBeanBuilder().user(username, userManager).context(uriInfo).buildShort();
    }


    static final WorklogBean DOC_EXAMPLE;
    static {
        try
        {
            DOC_EXAMPLE = new WorklogBean();
            DOC_EXAMPLE.self = new URI("http://www.example.com/jira/rest/api/2.0/worklog/10000");
            DOC_EXAMPLE.author = UserBean.SHORT_DOC_EXAMPLE;
            DOC_EXAMPLE.updateAuthor = UserBean.SHORT_DOC_EXAMPLE;
            DOC_EXAMPLE.comment = "I did some work here.";
            DOC_EXAMPLE.visibility = new VisibilityBean(VisibilityType.GROUP, "jira-developers");
            DOC_EXAMPLE.started = new Date();
            DOC_EXAMPLE.minutesSpent = 180L;
        }
        catch (URISyntaxException impossible)
        {
            throw new RuntimeException(impossible);
        }
    }
}
