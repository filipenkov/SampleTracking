package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.JiraUrlCodec;
import com.google.common.base.Function;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Collections2.transform;
/**
 * @since 5.0
 */
public class ProjectJsonBean
{
    @JsonProperty
    private String self;

    @JsonProperty
    private String id;

    @JsonProperty
    private String key;

    @JsonProperty
    private String name;

    @JsonProperty
    private Map<String, String> avatarUrls;


    public String getSelf()
    {
        return self;
    }

    public String getId()
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

    public static ProjectJsonBean shortBean(Project project, final JiraBaseUrls urls)
    {
        if (project == null)
        {
            return null;
        }

        final ProjectJsonBean bean = new ProjectJsonBean();
        bean.self = urls.restApi2BaseUrl() + "project/" + JiraUrlCodec.encode(project.getKey());
        bean.id = project.getId().toString();
        bean.key = project.getKey();
        bean.name = project.getName();
        bean.avatarUrls = getAvatarUrls(project);
        return bean;
    }

    public static Map<String, String> getAvatarUrls(final Project project)
    {
        AvatarService avatarService = ComponentAccessor.getAvatarService();
        final Avatar avatar = project.getAvatar();
        if (avatar == null) return null;

        final Map<String, String> avatarUrls = new HashMap<String, String>();
        avatarUrls.put("16x16", avatarService.getProjectAvatarAbsoluteURL(project, Avatar.Size.SMALL).toString());
        avatarUrls.put("48x48", avatarService.getProjectAvatarAbsoluteURL(project, Avatar.Size.LARGE).toString());
        return avatarUrls;
    }

    public static Collection<ProjectJsonBean> shortBeans(final Collection<Project> allowedValues, final JiraBaseUrls baseUrls)
    {
        return transform(allowedValues, new Function<Project, ProjectJsonBean>()
        {
            @Override
            public ProjectJsonBean apply(Project from)
            {
                return ProjectJsonBean.shortBean(from, baseUrls);
            }
        });

    }
}


