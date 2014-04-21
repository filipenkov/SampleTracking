package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.GenericType;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonAutoDetect;

import java.util.List;

/**
 * Use this class from func/selenium/page-object tests that need to manipulate SearchRequests.
 *
 * See SearchRequestBackdoor for the code this plugs into at the back-end.
 *
 * @since v5.0
 */
public class SearchRequestControl extends BackdoorControl<SearchRequestControl>
{
    private static final GenericType<List<SearchBean>> SEARCH_LIST_TYPE = new GenericType<List<SearchBean>>(){};

    public SearchRequestControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    /**
     * Creates a SearchRequest with the specified query and returns the id of the object created. The share level of
     * the filter will be Private.
     *
     * @param username  the user to save the search for
     * @param searchJql the JQL of the search
     * @param searchName the name of the search
     * @param searchDescription the description of the search
     *
     * @return String id of the SearchRequest created
     */
    public String createFilter(String username, String searchJql, String searchName, String searchDescription)
    {
        return createFilter(username, searchJql, searchName, searchDescription, "");
    }

    /**
     * Creates a SearchRequest with the specified query and returns the id of the object created. The share level of
     * the filter will be determined by the JSON share string. See SharePermissionUtils for how the JSON is handled.
     *
     * @param username  the user to save the search for
     * @param searchJql the JQL of the search
     * @param searchName the name of the search
     * @param searchDescription the description of the search
     * @param jsonShareString a JSON array of permissions to apply to the saved search
     *
     * @return String id of the SearchRequest created
     */
    public String createFilter(String username, String searchJql, String searchName, String searchDescription,
            String jsonShareString)
    {
        SearchBean searchBean = new SearchBean(username, searchJql, searchName, searchDescription, jsonShareString);
        return post(createResource().path("filter"), searchBean, String.class);
    }

    public List<SearchBean> getOwnedFilters(String username)
    {
        return get(createResource().path("filter").path("my").queryParam("username", username), SEARCH_LIST_TYPE);
    }
    
    @JsonAutoDetect
    public static class SearchBean
    {
        public String username;
        public String searchJql;
        public String searchName;
        public String searchDescription;
        public String jsonShareString;
        public boolean favourite;
        public long favouriteCount;

        public SearchBean()
        {
        }
        
        public SearchBean(String username, String searchJql, String searchName, String searchDescription,
                String jsonShareString)
        {
            this.username = username;
            this.searchJql = searchJql;
            this.searchName = searchName;
            this.searchDescription = searchDescription;
            this.jsonShareString = jsonShareString;
        }

        public String getUsername()
        {
            return username;
        }

        public SearchBean setUsername(String username)
        {
            this.username = username;
            return this;
        }

        public String getSearchJql()
        {
            return searchJql;
        }

        public SearchBean setSearchJql(String searchJql)
        {
            this.searchJql = searchJql;
            return this;
        }

        public String getSearchName()
        {
            return searchName;
        }

        public SearchBean setSearchName(String searchName)
        {
            this.searchName = searchName;
            return this;
        }

        public String getSearchDescription()
        {
            return searchDescription;
        }

        public SearchBean setSearchDescription(String searchDescription)
        {
            this.searchDescription = searchDescription;
            return this;
        }

        public String getJsonShareString()
        {
            return jsonShareString;
        }

        public SearchBean setJsonShareString(String jsonShareString)
        {
            this.jsonShareString = jsonShareString;
            return this;
        }

        public boolean isFavourite()
        {
            return favourite;
        }

        public SearchBean setFavourite(boolean favourite)
        {
            this.favourite = favourite;
            return this;
        }

        public long getFavouriteCount()
        {
            return favouriteCount;
        }

        public SearchBean setFavouriteCount(long favouriteCount)
        {
            this.favouriteCount = favouriteCount;
            return this;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
