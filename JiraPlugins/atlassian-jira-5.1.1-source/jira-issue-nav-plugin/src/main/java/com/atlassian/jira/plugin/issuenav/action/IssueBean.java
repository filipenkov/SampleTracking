package com.atlassian.jira.plugin.issuenav.action;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.rest.json.beans.FieldHtmlBean;
import com.atlassian.jira.issue.fields.rest.json.beans.ProjectJsonBean;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.plugin.issuenav.viewissue.webpanel.IssueWebPanelsBean;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.OpsbarBean;
import com.google.common.collect.Maps;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;

/**
 * Representation of issue for returning to client
 *
 * @since v5.0.3
 */
@XmlRootElement
public class IssueBean
{
    @XmlElement
    private Long id;

    @XmlElement
    private String key;

    @XmlElement
    private String summary;

    @XmlElement
    private OpsbarBean operations;

    @XmlElement
    private IssueProjectBean project;

    @XmlElement
    private IssueStatusBean status;

    @XmlElement
    private IssueBean parent;

    private IssueBean() {}

    public IssueBean(final Issue issue)
    {
        this.id = issue.getId();
        this.key = issue.getKey();
        this.summary = issue.getSummary();
    }

    public IssueBean(final Issue issue, final Project project, final Status status, OpsbarBean operations)
    {
        this(issue);
        this.operations = operations;
        this.project = new IssueProjectBean(project);
        this.status = new IssueStatusBean(status);

        Issue parent = issue.getParentObject();
        if (null != parent)
        {
            this.parent = new IssueBean(parent);
        }
    }

    public Long getId()
    {
        return id;
    }

    public String getKey()
    {
        return key;
    }

    public String getSummary()
    {
        return summary;
    }

    public OpsbarBean getOperations()
    {
        return operations;
    }

    public IssueProjectBean getProject()
    {
        return project;
    }

    public IssueStatusBean getStatus()
    {
        return status;
    }

    public IssueBean getParent()
    {
        return parent;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("id", id).
                append("key", key).
                append("operations", operations).
                toString();
    }

    @XmlRootElement
    public static class IssueProjectBean
    {
        @XmlElement (name = "id")
        private Long id;

        @XmlElement (name = "key")
        private String key;

        @XmlElement (name = "name")
        private String name;

        @XmlElement (name = "avatarUrls")
        private Map<String, String> avatarUrls;

        public IssueProjectBean(Project project)
        {
            this.id = project.getId();
            this.key = project.getKey();
            this.name = project.getName();
            this.avatarUrls = ProjectJsonBean.getAvatarUrls(project);
        }

        public Long getId()
        {
            return id;
        }

        public String getKey()
        {
            return key;
        }

        public String getName()
        {
            return name;
        }

        public Map<String, String> getAvatarUrls()
        {
            return avatarUrls;
        }
    }

    @XmlRootElement
    public static class IssueStatusBean
    {
        @XmlElement (name = "description")
        private String description;

        @XmlElement (name = "iconUrl")
        private String iconUrl;

        @XmlElement (name = "name")
        private String name;

        @XmlElement (name = "id")
        private String id;

        public IssueStatusBean(Status status)
        {
            this.description = status.getDescription();
            this.iconUrl = status.getIconUrl();
            this.name = status.getName();
            this.id = status.getId();
        }

        public String getDescription()
        {
            return description;
        }

        public String getIconUrl()
        {
            return iconUrl;
        }

        public String getName()
        {
            return name;
        }

        public String getId()
        {
            return id;
        }
    }
}
