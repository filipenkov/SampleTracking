package com.atlassian.jira.rest.v2.issue;

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @since v4.2
 */
@XmlRootElement (name = "status")
class StatusBean
{
    /**
     * Example status bean. JSON:
     * <p>
     * <pre>
     * {
     *   self: "http://localhost:8090/jira/rest/api/2.0/issueType/2",
     *   description: "A new feature of the product, which has yet to be developed.",
     *   iconUrl: "http://localhost:8090/jira/images/icons/newfeature.gif",
     *   name: "New Feature",
     *   subtask: false
     * }
     * <pre>
     */
    static final StatusBean DOC_EXAMPLE;
    static
    {
        try
        {
            DOC_EXAMPLE = StatusBean.fullBean(
                    "In Progress",
                    new URI("http://localhost:8090/jira/rest/api/2.0/status/10000"),
                    "http://localhost:8090/jira/images/icons/progress.gif",
                    "The issue is currently being worked on."
            );
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e); // never happens
        }
    }

    @XmlElement
    private URI self;

    @XmlElement
    private String description;

    @XmlElement
    private String iconUrl;

    @XmlElement
    private String name;

    public StatusBean() {}

    public StatusBean(String name, URI selfUri)
    {
    }

    public static StatusBean shortBean(final String name, final URI self)
    {
        final StatusBean bean = new StatusBean();
        bean.name = name;
        bean.self = self;

        return bean;
    }

    public static StatusBean fullBean(final String name, final URI self, final String iconUrl, final String description)
    {
        final StatusBean bean = shortBean(name, self);
        bean.description = description;
        bean.iconUrl = iconUrl;

        return bean;
    }

    public URI getSelf()
    {
        return self;
    }

    public String getDescription()
    {
        return description;
    }

    public String getIconUrl()
    {
        return iconUrl;
    }

    public String getName()
    {
        return name;
    }
}
