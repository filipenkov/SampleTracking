package com.atlassian.administration.quicksearch.rest;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * REST section bean.
 *
 * @since 1.0
 */
@XmlRootElement
public class SectionBean extends LocationBean
{
    @XmlAttribute
    private String label;
    @XmlAttribute private String location;

    public SectionBean(String key, String label, String location, List<SectionBean> sections, List<LinkBean> items) {
        super(key, items, sections);
        this.label = label;
        this.location = location;
    }

    public String label()
    {
        return label;
    }

    public String location()
    {
        return location;
    }


}
