package com.atlassian.jira.dev.backdoor;

import javax.xml.bind.annotation.XmlRootElement;

/**
* Stores state for a {@link com.atlassian.jira.issue.search.SearchRequest} representation being passed via REST.
*
* @since v5.0
*/
@XmlRootElement
public class SearchRequestBean
{
    public String username;
    public String searchJql;
    public String searchName;
    public String searchDescription;
    public String jsonShareString;
    public boolean favourite;
    public Long favouriteCount;
}
