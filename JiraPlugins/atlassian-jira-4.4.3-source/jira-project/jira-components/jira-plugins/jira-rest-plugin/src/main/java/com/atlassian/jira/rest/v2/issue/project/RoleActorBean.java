package com.atlassian.jira.rest.v2.issue.project;

import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.security.roles.RoleActor;

import java.net.URI;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
* @since v4.4
*/
@SuppressWarnings ( { "UnusedDeclaration" })
@XmlRootElement (name="projectRoleActor")
public class RoleActorBean
{
    @XmlElement
    Long id;

    @XmlElement
    String displayName;

    @XmlElement
    String type;

    @XmlElement
    String name;

    @XmlElement
    URI avatarUrl;

    public static RoleActorBean convert(final RoleActor actor)
    {
        final RoleActorBean bean = new RoleActorBean();
        bean.id = actor.getId();
        bean.displayName = actor.getDescriptor();
        bean.type = actor.getType();
        bean.name = actor.getParameter();

        return bean;
    }

    public String getName()
    {
        return name;
    }

    public void setAvatarUrl(final URI avatarUrl)
    {
        this.avatarUrl = avatarUrl;
    }

    public static final RoleActorBean DOC_EXAMPLE;
    static
    {
        DOC_EXAMPLE = new RoleActorBean();
        DOC_EXAMPLE.id = 10240L;
        DOC_EXAMPLE.displayName = "jira-developers";
        DOC_EXAMPLE.type = "atlassian-group-role-actor";
        DOC_EXAMPLE.name = "jira-developers";
    }
}
