package com.atlassian.administration.quicksearch.rest;

import com.google.common.collect.ImmutableList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Main REST response bean.
 *
 * @since 1.o
 */
@XmlRootElement
public class LocationBean
{

    @XmlAttribute
    private String key;

    @XmlElement
    private List<LinkBean> items;

    @XmlElement
    private List<SectionBean> sections;

    public LocationBean(String key, List<LinkBean> items, List<SectionBean> sections)
    {
        this.key = key;
        this.items = items;
        this.sections = sections;
    }

    public String key()
    {
        return key;
    }

    public List<LinkBean> links()
    {
        return ImmutableList.<LinkBean>builder().addAll(items).build();
    }

    public List<SectionBean> sections()
    {
        return ImmutableList.<SectionBean>builder().addAll(sections).build();
    }
}
