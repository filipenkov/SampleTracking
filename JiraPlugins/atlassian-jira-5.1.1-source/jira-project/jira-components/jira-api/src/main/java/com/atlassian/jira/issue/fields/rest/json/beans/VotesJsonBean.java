package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.crowd.embedded.api.User;
import com.google.common.base.Function;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collection;

import static com.google.common.collect.Collections2.transform;

/**
 * @since v5.0
 */
public class VotesJsonBean
{
    @JsonProperty
    private String self;

    @JsonProperty
    private long votes;

    private boolean hasVoted;

    // This will either be a Collection<UserBean> or an ErrorCollection explaining that you don't have permission
    // to view the voters for this issue.
    @JsonProperty
    private Collection<UserJsonBean> voters;

    public String getSelf()
    {
        return self;
    }

    public void setSelf(String self)
    {
        this.self = self;
    }

    public long getVotes()
    {
        return votes;
    }

    public void setVotes(long votes)
    {
        this.votes = votes;
    }

    public boolean isHasVoted()
    {
        return hasVoted;
    }

    public void setHasVoted(boolean hasVoted)
    {
        this.hasVoted = hasVoted;
    }

    public Collection<UserJsonBean> getVoters()
    {
        return voters;
    }

    public void setVoters(Collection<UserJsonBean> voters)
    {
        this.voters = voters;
    }

    /**
     *
     * @return null if the input is null
     */
    public static VotesJsonBean shortBean(final String issueKey, final long votes, final boolean hasVoted, final JiraBaseUrls urls)
    {
        final VotesJsonBean bean = new VotesJsonBean();
        bean.self = urls.restApi2BaseUrl() + "issue/" + issueKey +  "/votes";
        bean.hasVoted = hasVoted;
        bean.votes = votes;

        return bean;
    }

    /**
     *
     * @return null if the input is null
     */
    public static VotesJsonBean fullBean(final String issueKey, final long votes, final boolean hasVoted, Collection<User> voters, final JiraBaseUrls urls)
    {
        final VotesJsonBean bean = shortBean(issueKey, votes, hasVoted, urls);

        bean.voters = transform(voters, new Function<User, UserJsonBean>()
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
