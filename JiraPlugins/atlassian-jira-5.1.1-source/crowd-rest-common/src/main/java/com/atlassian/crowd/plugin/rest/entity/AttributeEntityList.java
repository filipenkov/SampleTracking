package com.atlassian.crowd.plugin.rest.entity;

import com.atlassian.plugins.rest.common.Link;
import com.atlassian.plugins.rest.common.expand.entity.ListWrapper;
import com.atlassian.plugins.rest.common.expand.entity.ListWrapperCallback;
import com.atlassian.plugins.rest.common.expand.parameter.Indexes;
import com.google.common.collect.ImmutableList;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Contains a list of <tt>AttributeEntity</tt>s.
 *
 * @since v2.1
 */
@XmlRootElement (name = "attributes")
@XmlAccessorType(XmlAccessType.FIELD)
public class AttributeEntityList implements ListWrapper<AttributeEntity>, Iterable<AttributeEntity>
{
    @XmlElements(@XmlElement (name = "attribute"))
    private final List<AttributeEntity> attributes;

    @XmlElement (name = "link")
    private final Link link;

    /**
     * JAXB requires a no-arg constructor.
     */
    private AttributeEntityList()
    {
        attributes = new ArrayList<AttributeEntity>();
        link = null;
    }

    public AttributeEntityList(final List<AttributeEntity> attributes, final Link link)
    {
        this.attributes = ImmutableList.copyOf(checkNotNull(attributes));
        this.link = link;
    }

    public int size()
    {
        return attributes.size();
    }

    public boolean isEmpty()
    {
        return attributes.isEmpty();
    }

    public AttributeEntity get(final int index)
    {
        return attributes.get(index);
    }

    public Iterator<AttributeEntity> iterator()
    {
        return attributes.iterator();
    }

    public Link getLink()
    {
        return link;
    }

    public ListWrapperCallback<AttributeEntity> getCallback()
    {
        return new ListWrapperCallback<AttributeEntity>()
        {
            public List<AttributeEntity> getItems(final Indexes indexes)
            {
                return attributes; // attributes should already be filled in by the UserEntityExpander.
            }
        };
    }
}
