package com.atlassian.jira.rest.v2.issue.project;

import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.rest.v2.issue.component.ComponentBean;
import com.atlassian.jira.rest.v2.issue.Examples;
import com.atlassian.jira.rest.v2.issue.IssueTypeBean;
import com.atlassian.jira.rest.v2.issue.UserBean;
import com.atlassian.jira.rest.v2.issue.version.VersionBean;
import com.atlassian.jira.util.collect.MapBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

/**
 * @since 4.2
 */
@SuppressWarnings ( { "UnusedDeclaration" })
@XmlRootElement (name="project")
public class ProjectBean
{
    /**
     * Project bean example used in auto-generated documentation.
     */
    public static final ProjectBean DOC_EXAMPLE;
    static
    {
        ProjectBean project = new ProjectBean();
        project.key = "EX";
        project.name = "Example";
        project.self = Examples.restURI("project/" + project.key);
        project.description = "This project was created as an example for REST.";
        project.lead = UserBean.SHORT_DOC_EXAMPLE;
        project.components = singletonList(ComponentBean.DOC_EXAMPLE);
        project.url = Examples.jiraURI("browse", project.key).toString();
        project.assigneeType = AssigneeType.PROJECT_LEAD;
        project.versions = Collections.emptyList();
        project.roles = MapBuilder.<String, URI>newBuilder()
                .add("Developers", Examples.restURI("project", project.key, "role", "10000"))
                .toMap();

        DOC_EXAMPLE = project;
    }

    public static final ProjectBean SHORT_DOC_EXAMPLE_1;
    static
    {
        ProjectBean project = new ProjectBean();
        project.key = "EX";
        project.self = Examples.restURI("project/" + project.key);
        project.name = "Example";

        SHORT_DOC_EXAMPLE_1 = project;
    }

    public static final ProjectBean SHORT_DOC_EXAMPLE_2;
    static
    {
        ProjectBean project = new ProjectBean();
        project.key = "ABC";
        project.self = Examples.restURI("project/" + project.key);
        project.name = "Alphabetical";

        SHORT_DOC_EXAMPLE_2 = project;
    }
    public static final List<ProjectBean> PROJECTS_EXAMPLE;
    static
    {
        PROJECTS_EXAMPLE = new ArrayList<ProjectBean>();
        PROJECTS_EXAMPLE.add(SHORT_DOC_EXAMPLE_1);
        PROJECTS_EXAMPLE.add(SHORT_DOC_EXAMPLE_2);
    }

    @XmlElement
    private URI self;

    @XmlElement
    private String key;

    @XmlElement
    private String description;

    @XmlElement
    private UserBean lead;

    @XmlElement
    private Collection<ComponentBean> components;

    @XmlElement
    private Collection<IssueTypeBean> issueTypes;

    @XmlElement
    private String url;

    @XmlElement
    private AssigneeType assigneeType;

    @XmlElement
    private Collection<VersionBean> versions;

    @XmlElement
    private String name;

    @XmlElement
    private Map<String, URI> roles;

    ProjectBean(URI self, String key, String name, String description, UserBean lead, long assigneeType,  String url, Collection<ComponentBean> components,
            Collection<VersionBean> versions, Collection<IssueTypeBean> issueTypes, final Map<String, URI> roles)
    {
        this.self = self;
        this.key = key;
        this.description = description;
        this.lead = lead;
        this.components = components;
        this.url = url;
        this.assigneeType = AssigneeType.getAssigneeType(assigneeType);
        this.versions = versions;
        this.name = name;
        this.issueTypes = issueTypes;
        this.roles = roles;
    }

    public ProjectBean() {}

    public String getKey()
    {
        return key;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public enum AssigneeType
    {
        PROJECT_LEAD (AssigneeTypes.PROJECT_LEAD),
        UNASSIGNED (AssigneeTypes.UNASSIGNED);

        private final long id;

        AssigneeType(long id)
        {
            this.id = id;
        }

        public long getId()
        {
            return id;
        }

        static AssigneeType getAssigneeType(long assigneeType)
        {
            switch ((short) assigneeType)
            {
                case (short) AssigneeTypes.PROJECT_LEAD : return PROJECT_LEAD;
                case (short) AssigneeTypes.UNASSIGNED : return UNASSIGNED;
            }
            return null;
        }
    }


}


