package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.base.Function;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.Collections2.transform;

/**
 * @since v5.0
 */
public class UserJsonBean
{
    @JsonProperty
    private String self;

    @JsonProperty
    private String name;

    @JsonProperty
    private String emailAddress;

    @JsonProperty
    private Map<String, String> avatarUrls;

    @JsonProperty
    private String displayName;

    @JsonProperty
    private boolean active;

    public String getSelf()
    {
        return self;
    }

    public void setSelf(String self)
    {
        this.self = self;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getEmailAddress()
    {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress)
    {
        this.emailAddress = emailAddress;
    }

    public Map<String, String> getAvatarUrls()
    {
        return avatarUrls;
    }

    public void setAvatarUrls(Map<String, String> avatarUrls)
    {
        this.avatarUrls = avatarUrls;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public boolean isActive()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    public static Collection<UserJsonBean> shortBeans(final Collection<User> users, final JiraBaseUrls urls)
    {
        if (users == null)
        {
            return null;
        }
        return transform(users, new Function<User, UserJsonBean>()
        {
            @Override
            public UserJsonBean apply(User from)
            {
                return shortBean(from, urls);
            }
        });
    }

    /**
     *
     * @return null if the input is null
     */
    public static UserJsonBean shortBean(final User user, final JiraBaseUrls urls)
    {
        if (user == null)
        {
            return null;
        }
        final UserJsonBean bean = new UserJsonBean();
        bean.self = urls.restApi2BaseUrl() + "user?username=" + JiraUrlCodec.encode(user.getName());
        bean.name = user.getName();
        bean.displayName = user.getDisplayName();
        bean.emailAddress = user.getEmailAddress();
        bean.active  = user.isActive();
        bean.avatarUrls = getAvatarURLs(user);
        return bean;
    }

    private static Map<String, String> getAvatarURLs(User user)
    {
        final AvatarService avatarService = ComponentAccessor.getAvatarService();

        return MapBuilder.<String, String>newBuilder()
                .add("16x16", avatarService.getAvatarAbsoluteURL(user, user.getName(), Avatar.Size.SMALL).toString())
                .add("48x48", avatarService.getAvatarAbsoluteURL(user, user.getName(), Avatar.Size.LARGE).toString())
                .toMap();
    }


    public static final UserJsonBean USER_DOC_EXAMPLE = new UserJsonBean();
    public static final UserJsonBean USER_SHORT_DOC_EXAMPLE = new UserJsonBean();
    static
    {
        USER_DOC_EXAMPLE.setSelf("http://www.example.com/jira/rest/api/2.0/user?username=fred");
        USER_DOC_EXAMPLE.setName("fred");
        USER_DOC_EXAMPLE.setEmailAddress("fred@example.com");
        USER_DOC_EXAMPLE.setDisplayName("Fred F. User");
        USER_DOC_EXAMPLE.setActive(true);
        USER_DOC_EXAMPLE.setAvatarUrls(MapBuilder.<String, String>newBuilder()
                .add("16x16", "http://www.example.com/jira/secure/useravatar?size=small&ownerId=fred")
                .add("48x48", "http://www.example.com/jira/secure/useravatar?size=large&ownerId=fred")
                .toMap());

        USER_SHORT_DOC_EXAMPLE.setSelf(USER_DOC_EXAMPLE.getSelf());
        USER_SHORT_DOC_EXAMPLE.setName(USER_DOC_EXAMPLE.getName());
        USER_SHORT_DOC_EXAMPLE.setDisplayName(USER_DOC_EXAMPLE.getDisplayName());
    }
}

