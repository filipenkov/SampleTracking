package com.atlassian.administration.quicksearch.rest;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashSet;
import java.util.Set;

/**
* Admin link REST bean.
*
* @since 1.0
*/
@XmlRootElement
public class LinkBean
{
    @XmlAttribute String key;
    @XmlAttribute String linkUrl;
    @XmlAttribute String label;
    @XmlElement Set<String> aliases;

    public LinkBean(String key, String linkUrl, String label /*,String section*/, Set<String> aliases) {
        this.key = key;
        this.linkUrl = linkUrl;
        this.label = label;
        this.aliases = aliases;
    }

    public String key()
    {
        return key;
    }

    public String linkUrl()
    {
        return linkUrl;
    }

    public String label()
    {
        return label;
    }

    public Set<String> aliases()
    {
        return new LinkedHashSet<String>(aliases);
    }
}
