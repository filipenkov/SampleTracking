package com.atlassian.jira.rest.v2.issue.project;

import com.atlassian.jira.rest.v2.issue.Examples;
import com.atlassian.jira.util.collect.CollectionBuilder;

import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.Collection;

/**
* @since v4.4
*/
@SuppressWarnings ( { "UnusedDeclaration" })
@XmlRootElement (name="projectRole")
public class ProjectRoleBean
{
    @XmlElement
    URI self;

    @XmlElement
    String name;

    @XmlElement
    Long id;

    @XmlElement
    String description;

    @XmlElement
    Collection<RoleActorBean> actors;

    public static class Builder
    {
        String name;
        Long id;
        String description;
        Collection<RoleActorBean> actors;

        Builder() {}

        public Builder name(final String name)
        {
            this.name = name;
            return this;
        }

        public Builder id(final Long id)
        {
            this.id = id;
            return this;
        }

        public Builder description(final String description)
        {
            this.description = description;
            return this;
        }

        public Builder actors(final Collection<RoleActorBean> actors)
        {
            this.actors = actors;
            return this;
        }

        public ProjectRoleBean build(final String projectKey, final UriInfo uriInfo)
        {
            final ProjectRoleBean bean = new ProjectRoleBean();
            bean.self = uriInfo.getBaseUriBuilder().path(ProjectRoleResource.class).path(id.toString()).build(projectKey);
            bean.name = name;
            bean.id = id;
            bean.description = description;
            bean.actors = actors;
            return bean;
        }

        public static Builder newBuilder()
        {
            return new Builder();
        }
    }

    public static final ProjectRoleBean DOC_EXAMPLE;
    static
    {
        DOC_EXAMPLE = new ProjectRoleBean();
        long id = 10360;
        DOC_EXAMPLE.self = Examples.restURI("project/MKY/role/" + id);
        DOC_EXAMPLE.id = id;
        DOC_EXAMPLE.name = "Developers";
        DOC_EXAMPLE.description = "A project role that represents developers in a project";
        DOC_EXAMPLE.actors = CollectionBuilder.list(RoleActorBean.DOC_EXAMPLE);
    }
}
