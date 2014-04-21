package com.atlassian.crowd.integration.rest.entity;

import com.atlassian.crowd.embedded.api.Attributes;

import javax.xml.bind.annotation.*;
import java.util.*;

/**
 * Contains a list of <tt>MultiValuedAttributeEntity</tt>s.
 *
 * @since v2.1
 */
@XmlRootElement (name = "attributes")
@XmlAccessorType(XmlAccessType.FIELD)
public class MultiValuedAttributeEntityList implements Attributes, Iterable<MultiValuedAttributeEntity>
{
    @XmlElements(@XmlElement (name = "attribute"))
    private final List<MultiValuedAttributeEntity> attributes;

    /**
     * JAXB requires a no-arg constructor.
     */
    private MultiValuedAttributeEntityList()
    {
        attributes = new ArrayList<MultiValuedAttributeEntity>();
    }

    public MultiValuedAttributeEntityList(final List<MultiValuedAttributeEntity> attributes)
    {
        this.attributes = new ArrayList<MultiValuedAttributeEntity>(attributes);
    }

    public int size()
    {
        return attributes.size();
    }

    public Set<String> getValues(String key)
    {
        return asMap().get(key);
    }

    public String getValue(String key)
    {
        MultiValuedAttributeEntity attribute = null;
        for (MultiValuedAttributeEntity attr : attributes)
        {
            if (attr.getName().equals(key))
            {
                attribute = attr;
                break;
            }
        }

        if (attribute == null || attribute.getValues() == null || attribute.getValues().isEmpty())
        {
            return null;
        }

        return attribute.getValues().iterator().next();
    }

    public Set<String> getKeys()
    {
        return asMap().keySet();
    }

    public boolean isEmpty()
    {
        return attributes.isEmpty();
    }

    public MultiValuedAttributeEntity get(final int index)
    {
        return attributes.get(index);
    }

    public Iterator<MultiValuedAttributeEntity> iterator()
    {
        return attributes.iterator();
    }

    private Map<String, Set<String>> asMap()
    {
        final Map<String, Set<String>> map = new HashMap<String, Set<String>>(attributes.size());
        for (MultiValuedAttributeEntity attributeEntity : attributes)
        {
            map.put(attributeEntity.getName(), new HashSet<String>(attributeEntity.getValues()));
        }
        return map;
    }
}
