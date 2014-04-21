package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.priority.Priority;

import java.net.URI;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @since v4.2
 */
@XmlRootElement (name = "priority")
public class PriorityBean
{
    static final PriorityBean DOC_EXAMPLE;
    static
    {
        PriorityBean priority = new PriorityBean();
        priority.self = Examples.restURI("priority/3");
        priority.name = "Major";
        priority.statusColor = "#009900";
        priority.description = "Major loss of function.";
        priority.iconUrl = Examples.jiraURI("images/icons/priority_major.gif").toString();

        DOC_EXAMPLE = priority;
    }

    @XmlElement
    private URI self;

    @XmlElement
    private String statusColor;

    @XmlElement
    private String description;

    @XmlElement
    private String iconUrl;

    @XmlElement
    private String name;

    PriorityBean() {}

    public static PriorityBean shortBean(final Priority priority, final UriInfo uriInfo)
    {
        final PriorityBean bean = new PriorityBean();
        bean.self = uriInfo.getBaseUriBuilder().path(PriorityResource.class).path(priority.getId()).build();
        bean.name = priority.getNameTranslation();

        return bean;
    }

    public static PriorityBean fullBean(final Priority priority, final UriInfo uriInfo, final String baseUrl)
    {
        final PriorityBean bean = shortBean(priority, uriInfo);
        bean.statusColor = priority.getStatusColor();
        bean.description = priority.getDescTranslation();
        bean.iconUrl = baseUrl + priority.getIconUrl();

        return bean;
    }
}
