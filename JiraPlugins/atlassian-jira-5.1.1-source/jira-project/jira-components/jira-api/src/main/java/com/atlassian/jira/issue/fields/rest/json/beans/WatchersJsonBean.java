package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.crowd.embedded.api.User;
import com.google.common.base.Function;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collection;

import static com.google.common.collect.Collections2.transform;

/**
 * @since v5.0
 */
public class WatchersJsonBean
{
    @JsonProperty
    private String self;

    @JsonProperty
    private long watchCount;

    @JsonProperty("isWatching")
    private boolean watching;

    // This will either be a Collection<UserBean> or an ErrorCollection explaining that you don't have permission
    // to view the watcherrs for this issue.
    @JsonProperty
    private Collection<UserJsonBean> watchers;

    public String getSelf()
    {
        return self;
    }

    public void setSelf(String self)
    {
        this.self = self;
    }

    public long getWatchCount()
    {
        return watchCount;
    }

    public void setWatchCount(long watchCount)
    {
        this.watchCount = watchCount;
    }

    @JsonIgnore
    public boolean isWatching()
    {
        return watching;
    }

    public void setWatching(boolean watching)
    {
        this.watching = watching;
    }

    public Collection<UserJsonBean> getWatchers()
    {
        return watchers;
    }

    public void setWatchers(Collection<UserJsonBean> watchers)
    {
        this.watchers = watchers;
    }

    /**
     *
     * @return null if the input is null
     */
    public static WatchersJsonBean shortBean(final String issueKey, final long watchers, final boolean isWatching, final JiraBaseUrls urls)
    {
        final WatchersJsonBean bean = new WatchersJsonBean();
        bean.self = urls.restApi2BaseUrl() + "issue/" + issueKey +  "/watchers";
        bean.watching = isWatching;
        bean.watchCount = watchers;

        return bean;
    }

    /**
     *
     * @return null if the input is null
     */
    public static WatchersJsonBean fullBean(final String issueKey, final long watchers, final boolean isWatching, Collection<User> watcherrs, final JiraBaseUrls urls)
    {
        final WatchersJsonBean bean = shortBean(issueKey, watchers, isWatching, urls);

        bean.watchers = transform(watcherrs, new Function<User, UserJsonBean>()
        {
            @Override
            public UserJsonBean apply(User from)
            {
                return UserJsonBean.shortBean(from, urls);
            }
        });
        
        return bean;
    }

}
