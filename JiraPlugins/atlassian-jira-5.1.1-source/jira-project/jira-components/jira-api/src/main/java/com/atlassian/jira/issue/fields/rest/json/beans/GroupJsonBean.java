package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.crowd.embedded.api.Group;
import com.google.common.base.Function;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collection;

import static com.google.common.collect.Collections2.transform;

/**
 * @since v5.0
 */
public class GroupJsonBean
{
    @JsonProperty
    private String name;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public static Collection<GroupJsonBean> shortBeans(final Collection<Group> Groups, final JiraBaseUrls urls)
    {
        return transform(Groups, new Function<Group, GroupJsonBean>()
        {
            @Override
            public GroupJsonBean apply(Group from)
            {
                return shortBean(from, urls);
            }
        });
    }

    /**
     *
     * @return null if the input is null
     */
    public static GroupJsonBean shortBean(final Group group, final JiraBaseUrls urls)
    {
        if (group == null)
        {
            return null;
        }
        final GroupJsonBean bean = new GroupJsonBean();
        bean.name = group.getName();
        return bean;
    }
}

