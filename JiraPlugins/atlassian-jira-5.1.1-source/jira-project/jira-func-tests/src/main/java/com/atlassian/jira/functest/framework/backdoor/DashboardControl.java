package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.GenericType;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonAutoDetect;

import java.util.List;

/**
 * @since v5.0.3
 */
public class DashboardControl extends BackdoorControl<DashboardControl>
{
    private static final GenericType<List<Dashboard>> DASHBOARD_LIST_GENERIC_TYPE = new GenericType<List<Dashboard>>(){};

    public DashboardControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }
    
    public List<Dashboard> getOwnedDashboard(String username)
    {
        return get(createResource().path("dashboard").path("my").queryParam("username", username), DASHBOARD_LIST_GENERIC_TYPE);
    }

    @JsonAutoDetect
    public static class Dashboard
    {
        private long id;
        private String name;
        private String owner;
        private String description;
        private long favouriteCount;
        private boolean favourite;

        public Dashboard()
        {
        }

        public Long getId()
        {
            return id;
        }

        public Dashboard setId(Long id)
        {
            this.id = id;
            return this;
        }

        public String getName()
        {
            return name;
        }

        public Dashboard setName(String name)
        {
            this.name = name;
            return this;
        }

        public String getOwner()
        {
            return owner;
        }

        public Dashboard setOwner(String owner)
        {
            this.owner = owner;
            return this;
        }

        public String getDescription()
        {
            return description;
        }

        public Dashboard setDescription(String description)
        {
            this.description = description;
            return this;
        }

        public long getFavouriteCount()
        {
            return favouriteCount;
        }

        public Dashboard setFavouriteCount(long favouriteCount)
        {
            this.favouriteCount = favouriteCount;
            return this;
        }

        public boolean isFavourite()
        {
            return favourite;
        }

        public Dashboard setFavourite(boolean favourite)
        {
            this.favourite = favourite;
            return this;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
