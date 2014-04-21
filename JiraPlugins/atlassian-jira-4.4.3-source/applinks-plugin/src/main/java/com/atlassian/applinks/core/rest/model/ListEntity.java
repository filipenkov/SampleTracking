package com.atlassian.applinks.core.rest.model;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * @since 3.0
 */
@XmlRootElement (name = "list")
@XmlSeeAlso({LinkAndAuthProviderEntity.class})
public class ListEntity<T>
{
    @XmlElement (name = "list")
    private List<T> list;

    public ListEntity()
    {
    }

    public ListEntity(List<T> list)
    {
        this.list = list;
    }

    public List<T> getList()
    {
        return list;
    }
}
