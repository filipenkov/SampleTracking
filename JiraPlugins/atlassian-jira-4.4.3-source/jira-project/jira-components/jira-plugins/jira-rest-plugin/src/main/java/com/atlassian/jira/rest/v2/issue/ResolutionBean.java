package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.resolution.Resolution;

import java.net.URI;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
* @since v4.2
*/
@XmlRootElement (name="resolution")
public class ResolutionBean
{
    static final ResolutionBean DOC_EXAMPLE;
    static
    {
        ResolutionBean resolution = new ResolutionBean();
        resolution.self = Examples.restURI("resolution/1");
        resolution.name = "Fixed";
        resolution.description = "A fix for this issue is checked into the tree and tested.";
        resolution.iconUrl = Examples.jiraURI("images/icons/status_resolved.gif").toString();
        
        DOC_EXAMPLE = resolution;
    }

    @XmlElement
    private URI self;

    @XmlElement
    private String description;

    @XmlElement
    private String iconUrl;

    @XmlElement
    private String name;

    public ResolutionBean() {}

    public static ResolutionBean shortBean(final Resolution resolution, final UriInfo uriInfo)
    {
        final ResolutionBean bean = new ResolutionBean();

        bean.self = uriInfo.getBaseUriBuilder().path(ResolutionResource.class).path(resolution.getId()).build();
        bean.name = resolution.getNameTranslation();

        return bean;
    }

    public static ResolutionBean fullBean(final Resolution resolution, final UriInfo uriInfo)
    {
        final ResolutionBean bean = shortBean(resolution, uriInfo);

        bean.description = resolution.getDescTranslation();
        bean.iconUrl = resolution.getIconUrl();

        return bean;
    }
}
