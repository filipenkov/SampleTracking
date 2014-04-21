package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.rest.api.expand.SimpleListWrapper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugins.rest.common.expand.Expandable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * @since v4.2
 */
@XmlRootElement (name = "user")
public class UserBean
{
    @XmlElement
    private URI self;

    @XmlElement
    private String name;

    @XmlElement
    private String emailAddress;

    @XmlElement
    private Map<String, URI> avatarUrls;

    @XmlElement
    private String displayName;

    @XmlElement
    private boolean active;

    @XmlElement
    private String timeZone;

    @Expandable
    @XmlElement
    private SimpleListWrapper<GroupBean> groups;

    @XmlAttribute(name = "expand")
    private String expand;

    UserBean()
    {
        // empty
    }

    public UserBean(URI self, String name, String displayName, boolean active, Map<String, URI> avatarUrls)
    {
        this(self, name, displayName, active, null, null, avatarUrls, null);
    }

    public UserBean(URI self, String name, String displayName, boolean active, String emailAddress, List<GroupBean> groups, Map<String, URI> avatarUrls, TimeZone timeZone)
    {
        this.name = name;
        this.self = self;
        this.displayName = displayName;
        this.active = active;
        this.emailAddress = emailAddress;
        this.groups = groups != null ? SimpleListWrapper.of(groups) : null;
        this.avatarUrls = avatarUrls;
        this.timeZone = timeZone != null ? timeZone.getID() : null;
    }

    public String getName()
    {
        return name;
    }

    public static final UserBean DOC_EXAMPLE = new UserBean();
    public static final List<UserBean> DOC_EXAMPLE_LIST = new ArrayList<UserBean>();
    public static final UserBean SHORT_DOC_EXAMPLE = new UserBean();
    public static final UserBean SHORT_DOC_EXAMPLE_2 = new UserBean();
    public static final Map REF_DOC_EXAMPLE;
    static
    {
        DOC_EXAMPLE.self = Examples.restURI("user?username=fred");
        DOC_EXAMPLE.name = "fred";
        DOC_EXAMPLE.emailAddress = "fred@example.com";
        DOC_EXAMPLE.displayName = "Fred F. User";
        DOC_EXAMPLE.active = true;
        DOC_EXAMPLE.avatarUrls = MapBuilder.<String, URI>newBuilder()
                .add("16x16", Examples.jiraURI("secure/useravatar?size=small&ownerId=fred"))
                .add("48x48", Examples.jiraURI("secure/useravatar?size=large&ownerId=fred"))
                .toMap();
        DOC_EXAMPLE.groups = SimpleListWrapper.of(
                new GroupBean("jira-user"),
                new GroupBean("jira-admin"),
                new GroupBean("important")
        );
        DOC_EXAMPLE.timeZone = "Australia/Sydney";

        SHORT_DOC_EXAMPLE.self = DOC_EXAMPLE.self;
        SHORT_DOC_EXAMPLE.name = DOC_EXAMPLE.name;
        SHORT_DOC_EXAMPLE.displayName = DOC_EXAMPLE.displayName;
        SHORT_DOC_EXAMPLE.avatarUrls = MapBuilder.<String, URI>newBuilder()
                .add("16x16", Examples.jiraURI("secure/useravatar?size=small&ownerId=fred"))
                .add("48x48", Examples.jiraURI("secure/useravatar?size=large&ownerId=fred"))
                .toMap();
        SHORT_DOC_EXAMPLE_2.self = Examples.restURI("user?username=andrew");
        SHORT_DOC_EXAMPLE_2.name = "andrew";
        SHORT_DOC_EXAMPLE_2.displayName = "Andrew Anderson";
        SHORT_DOC_EXAMPLE_2.avatarUrls = MapBuilder.<String, URI>newBuilder()
                .add("16x16", Examples.jiraURI("secure/useravatar?size=small&ownerId=andrew"))
                .add("48x48", Examples.jiraURI("secure/useravatar?size=large&ownerId=andrew"))
                .toMap();
        REF_DOC_EXAMPLE = MapBuilder.<String, String>newBuilder().add("name", "harry").toMap();

        DOC_EXAMPLE_LIST.add(SHORT_DOC_EXAMPLE);
        DOC_EXAMPLE_LIST.add(SHORT_DOC_EXAMPLE_2);
    }
}

