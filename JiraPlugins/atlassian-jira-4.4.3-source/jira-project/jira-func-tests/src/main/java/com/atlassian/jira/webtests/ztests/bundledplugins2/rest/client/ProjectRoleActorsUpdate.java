package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Map;

/**
 *
 *  A bean used when updating the role actors through the ProjectRoleResource
 * as we may not have enough information to fully populate a ProjectRoleBean when doing an update, hence only a reduced
 * set of data consisting of {actor-type -> actor-parameter} is required for this bean.
 *
 * @since v4.4
 */
@JsonSerialize (include = JsonSerialize.Inclusion.NON_NULL)
public class ProjectRoleActorsUpdate
{
    private Long id;
    private Map<String, String[]> categorisedActors;


    public ProjectRoleActorsUpdate(final Long id, final Map<String, String[]> categorisedActors) {
        this.id = id;
        this.categorisedActors = categorisedActors;
    }


    public Long getId()
    {
        return id;
    }

    public Map<String, String[]> getCategorisedActors()
    {
        return categorisedActors;
    }
}
