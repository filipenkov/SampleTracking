package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.rest.api.expand.SimpleListWrapper;
import com.atlassian.plugins.rest.common.expand.Expandable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.List;
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
    private String avatarUrl;

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

    public UserBean(URI self, String name, String displayName, boolean active, String avatarUrl)
    {
        this(self, name, displayName, active, null, null, avatarUrl, null);
    }

    public UserBean(URI self, String name, String displayName, boolean active, String emailAddress, List<GroupBean> groups, String avatarUrl, TimeZone timeZone)
    {
        this.name = name;
        this.self = self;
        this.displayName = displayName;
        this.active = active;
        this.emailAddress = emailAddress;
        this.groups = groups != null ? SimpleListWrapper.of(groups) : null;
        this.avatarUrl = avatarUrl;
        this.timeZone = timeZone != null ? timeZone.getID() : null;
    }

    public String getName()
    {
        return name;
    }

    public static final UserBean DOC_EXAMPLE = new UserBean();
    public static final UserBean SHORT_DOC_EXAMPLE = new UserBean();
    static
    {
        DOC_EXAMPLE.self = Examples.restURI("user?username=fred");
        DOC_EXAMPLE.name = "fred";
        DOC_EXAMPLE.emailAddress = "fred@example.com";
        DOC_EXAMPLE.displayName = "Fred F. User";
        DOC_EXAMPLE.active = true;
        DOC_EXAMPLE.avatarUrl = "http://example.com:8080/jira/secure/useravatar?size=large&ownerId=fred";
        DOC_EXAMPLE.groups = SimpleListWrapper.of(
                new GroupBean("jira-user"),
                new GroupBean("jira-admin"),
                new GroupBean("important")
        );
        DOC_EXAMPLE.timeZone = "Australia/Sydney";

        SHORT_DOC_EXAMPLE.self = DOC_EXAMPLE.self;
        SHORT_DOC_EXAMPLE.name = DOC_EXAMPLE.name;
        SHORT_DOC_EXAMPLE.displayName = DOC_EXAMPLE.displayName;
    }
}

