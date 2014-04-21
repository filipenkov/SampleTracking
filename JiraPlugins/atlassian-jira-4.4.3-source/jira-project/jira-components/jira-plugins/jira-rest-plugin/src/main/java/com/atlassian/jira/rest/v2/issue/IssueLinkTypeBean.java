package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.plugins.rest.common.util.RestUrlBuilder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 *
 * @since v4.3
 */
@XmlRootElement (name = "issueLinkType")
public class IssueLinkTypeBean
{
    @XmlElement
    private Long id;

    @XmlElement
    private String name;

    @XmlElement
    private String inward;

    @XmlElement
    private String outward;

    @XmlElement
    private URI self;


    static final IssueLinkTypeBean ISSUE_LINK_TYPE_EXAMPLE;
    static final IssueLinkTypeBean ISSUE_LINK_TYPE_EXAMPLE_2;

    static
    {
        ISSUE_LINK_TYPE_EXAMPLE = new IssueLinkTypeBean(1000l, "Duplicate", "Duplicated by", "Duplicates", Examples.restURI("/issueLinkType/1000"));
        ISSUE_LINK_TYPE_EXAMPLE_2 = new IssueLinkTypeBean(1010l, "Blocks", "Blocked by", "Blocks", Examples.restURI("/issueLinkType/1010"));
    }


    public IssueLinkTypeBean(Long id, String name, String inward, String outward, URI self)
    {
        this.id = id;
        this.name = name;
        this.inward = inward;
        this.outward = outward;
        this.self = self;
    }

    public IssueLinkTypeBean(){}

    public static IssueLinkTypeBean create(IssueLinkType issueLinkType, URI self)
    {
        return new IssueLinkTypeBean(issueLinkType.getId(), issueLinkType.getName(), issueLinkType.getInward(), issueLinkType.getOutward(), self);
    }

    
}
