package com.atlassian.jira.rest.v2.issue.project;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueTypeJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.ProjectJsonBean;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.rest.v2.issue.IssueTypeBeanBuilder;
import com.atlassian.jira.rest.v2.issue.ProjectResource;
import com.atlassian.jira.rest.v2.issue.ResourceUriBuilder;
import com.atlassian.jira.rest.v2.issue.UserBean;
import com.atlassian.jira.rest.v2.issue.UserBeanBuilder;
import com.atlassian.jira.rest.v2.issue.component.ComponentBean;
import com.atlassian.jira.rest.v2.issue.version.VersionBean;
import com.atlassian.jira.rest.v2.issue.version.VersionBeanFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Collections2.transform;

/**
 * @since v4.4
 */
public class ProjectBeanFactoryImpl implements ProjectBeanFactory
{
    private final VersionBeanFactory versionBeanFactory;
    private final UserManager userManager;
    private JiraBaseUrls jiraBaseUrls;
    private final ResourceUriBuilder uriBuilder;
    private final UriInfo uriInfo;
    private final ProjectRoleService projectRoleService;
    private final JiraAuthenticationContext authenticationContext;

    public ProjectBeanFactoryImpl(VersionBeanFactory versionBeanFactory, UriInfo uriInfo,
            ResourceUriBuilder uriBuilder, ProjectRoleService projectRoleService, JiraAuthenticationContext authenticationContext,
            UserManager userManager, JiraBaseUrls jiraBaseUrls)
    {
        this.versionBeanFactory = versionBeanFactory;

        //This is proxied to report the current request URI. Go spring.
        this.uriInfo = uriInfo;
        this.uriBuilder = uriBuilder;
        this.projectRoleService = projectRoleService;
        this.authenticationContext = authenticationContext;
        this.userManager = userManager;
        this.jiraBaseUrls = jiraBaseUrls;
    }

    public ProjectBean fullProject(final Project project)
    {
        ProjectBeanBuilder builder = shortProjectBuilder(project);

        builder.name(project.getName()).description(project.getDescription());
        String leadUserName  = project.getLeadUserName();
        final User user = userManager.getUserEvenWhenUnknown(leadUserName);
        builder.lead(user);
        builder.components(project.getProjectComponents());
        builder.url(project.getUrl());
        builder.versions(project.getVersions());
        builder.assigneeType(project.getAssigneeType());

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final Collection<ProjectRole> projectRoles = projectRoleService.getProjectRoles(authenticationContext.getLoggedInUser(), errorCollection);
        if (!errorCollection.hasAnyErrors())
        {
            for (ProjectRole projectRole : projectRoles)
            {
                final URI uri = uriInfo.getBaseUriBuilder().path(ProjectRoleResource.class).path(projectRole.getId().toString()).build(project.getKey());
                builder.role(projectRole.getName(), uri);
            }
        }

        // no lazyness
        builder.issueTypes(Lists.<IssueTypeJsonBean>newArrayList(transform(project.getIssueTypes(), new Function<IssueType, IssueTypeJsonBean>()
        {
            public IssueTypeJsonBean apply(IssueType issueType)
            {
                return new IssueTypeBeanBuilder().jiraBaseUrls(jiraBaseUrls).issueType(issueType).context(uriInfo).buildShort();
            }
        })));

        return builder.build();
    }

    private ProjectBeanBuilder shortProjectBuilder(Project project)
    {
        return new ProjectBeanBuilder()
                .self(createSelfLink(project))
                .key(project.getKey())
                .name(project.getName())
                .id(project.getId())
                .avatarUrls(ProjectJsonBean.getAvatarUrls(project));
    }

    private URI createSelfLink(Project project)
    {
        return uriBuilder.build(uriInfo, ProjectResource.class, project.getKey());
    }

    private class ProjectBeanBuilder
    {
        private URI self;
        private String id;
        private String key;
        private String name;
        private String description;
        private UserBean lead;
        private Collection<ComponentBean> components;
        private String url;
        private Long assigneeType;
        private Collection<VersionBean> versions;
        private Collection<IssueTypeJsonBean> issueTypes;
        private Map<String, URI> roles = new HashMap<String, URI>();
        private Map<String, String> avatarUrls = new HashMap<String, String>();

        public ProjectBeanBuilder()
        {
        }

        public ProjectBeanBuilder self(URI self)
        {
            this.self = self;
            return this;
        }

        public ProjectBeanBuilder role(final String name, final URI uri)
        {
            roles.put(name, uri);
            return this;
        }

        public ProjectBeanBuilder id(Long id)
        {
            this.id = id == null ? null : id.toString();
            return this;
        }

        public ProjectBeanBuilder key(String key)
        {
            this.key = key;
            return this;
        }

        public ProjectBeanBuilder issueTypes(Collection<IssueTypeJsonBean> types)
        {
            this.issueTypes = types;
            return this;
        }

        public ProjectBeanBuilder description(String description)
        {
            this.description = description;
            return this;
        }

        public ProjectBeanBuilder lead(User lead)
        {
            this.lead = new UserBeanBuilder().user(lead).context(uriInfo).buildShort();
            return this;
        }

        public ProjectBeanBuilder assigneeType(Long assigneeType)
        {
            this.assigneeType = assigneeType;
            return this;
        }

        public ProjectBeanBuilder components(Collection<? extends ProjectComponent> components)
        {
            this.components = ComponentBean.asBeans(components, uriInfo);
            return this;
        }

        public ProjectBeanBuilder url(String url)
        {
            this.url = StringUtils.stripToNull(url);
            return this;
        }

        public ProjectBeanBuilder versions(Collection<? extends Version> versions)
        {
            this.versions = versionBeanFactory.createVersionBeans(versions);
            return this;
        }

        public ProjectBeanBuilder name(String name)
        {
            this.name = name;
            return this;
        }

        public ProjectBeanBuilder avatarUrls(Map<String, String> avatarUrls)
        {
            this.avatarUrls = avatarUrls;
            return this;
        }

        public ProjectBean build()
        {
            return new ProjectBean(self, id, key, name, description, lead, assigneeType, url, components, versions, issueTypes, roles, avatarUrls);
        }
    }

}
