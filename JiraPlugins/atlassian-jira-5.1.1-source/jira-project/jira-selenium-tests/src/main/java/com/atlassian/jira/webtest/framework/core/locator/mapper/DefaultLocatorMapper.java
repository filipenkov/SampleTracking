package com.atlassian.jira.webtest.framework.core.locator.mapper;

import com.atlassian.jira.webtest.framework.core.locator.LocatorData;
import com.atlassian.jira.webtest.framework.core.locator.LocatorType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * <p>
 * This class provides API for common conversions among locators, based solely on their types. It only supports
 * default locator types, as defined in {@link com.atlassian.jira.webtest.framework.core.locator.Locators}.
 *
 * <p>
 * This may be useful for implementations that don't define context-specific locator conversions (e.g. ones relying
 * on loctors' values or current test context).
 *
 * <p>
 * The conversions are biased towards CSS/jQuery locators, as opposed to xpath ones. E.g. given an ID parent locator
 * and an ID locator to nest, a CSS locator will be created as a result (and not an xpath one).
 *
 * <p>
 * This locator is also customizable by means of TODO. That means that custom mappings may be defined, either to
 * supplement, or replace the existing default ones.
 *
 * @since v4.3
 */
public class DefaultLocatorMapper
{
    private final List<LocatorMapping> mappings;


    public DefaultLocatorMapper(List<LocatorMapping> mappings) 
    {
        this.mappings = new CopyOnWriteArrayList<LocatorMapping>(notNull("mappings", mappings));
    }

    public DefaultLocatorMapper()
    {
        this(LocatorMappings.all());
    }

    /**
     * Add new mapping to this mapper. If a mapping for identical locator types is already maintained by this mapper,
     * it will be replaced. 
     *
     * @param mapping mapping to add
     * @return this mapper instance
     */
    public DefaultLocatorMapper addMapping(LocatorMapping mapping)
    {
        LocatorMapping existing = findEqual(mapping);
        if (existing != null)
        {
            mappings.remove(existing);
        }
        mappings.add(mapping);
        return this;
    }

    public DefaultLocatorMapper addMappings(List<LocatorMapping> mappings)
    {
        for (LocatorMapping mapping : mappings)
        {
            addMapping(mapping);
        }
        return this;
    }

    /**
     * Remove mapping for given <tt>parent</tt> and <tt>child</tt> types.
     *
     * @param parent parent type
     * @param child child type
     * @return this mapper instance
     */
    public DefaultLocatorMapper removeMapping(LocatorType parent, LocatorType child)
    {
        LocatorMapping toRemove = findFor(parent, child);
        if (toRemove != null)
        {
            mappings.remove(toRemove);
        }
        return this;
    }

    /**
     * Remove all mappings for given <tt>unwantedType</tt>, both in the parent
     * and child role.
     *
     * @param unwantedType unsupported locator type
     * @return this mapper instance
     */
    public DefaultLocatorMapper removeAllMappingsOf(LocatorType unwantedType)
    {
        List<LocatorMapping> toRemove = findFor(unwantedType);
        mappings.removeAll(toRemove);
        return this;
    }

    /**
     * Check if locators represented by <tt>parent</tt> and <tt>child</tt> are supported by this mapper.
     *
     * @param parent parent locator data
     * @param child child locator data
     * @return <code>true</code>, if <tt>parent</tt> and <tt>child</tt> are supported by this mapper, <code>false<code>
     * otherwise 
     */
    public boolean supports(LocatorData parent, LocatorData child)
    {
        return findFor(parent, child) != null;
    }


    public LocatorData combine(LocatorData parent, LocatorData child)
    {
        if (!supports(parent, child))
        {
            throw new IllegalArgumentException("No mapping for parent <" + parent + "> and child <" + child + ">");
        }
        return findFor(parent, child).combine(parent, child);
    }

    private LocatorMapping findEqual(LocatorMapping mapping)
    {
        for (LocatorMapping existing : mappings)
        {
            if (areEqual(existing, mapping))
            {
                return existing;
            }
        }
        return null;
    }

    private boolean areEqual(LocatorMapping one, LocatorMapping two)
    {
        return one.parentType().equals(two.parentType()) && one.childType().equals(two.childType());
    }

    private LocatorMapping findFor(LocatorData parent, LocatorData child)
    {
        for (LocatorMapping mapping : mappings)
        {
            if (mapping.supportsParent(parent) && mapping.supportsChild(child))
            {
                return mapping;
            }
        }
        return null;
    }

    private LocatorMapping findFor(LocatorType parent, LocatorType child)
    {
        for (LocatorMapping mapping : mappings)
        {
            if (mapping.parentType().equals(parent) && mapping.childType().equals(child))
            {
                return mapping;
            }
        }
        return null;
    }

    private List<LocatorMapping> findFor(LocatorType type)
    {
        List<LocatorMapping> answer = new ArrayList<LocatorMapping>();
        for (LocatorMapping mapping : mappings)
        {
            if (mapping.parentType().equals(type) || mapping.childType().equals(type))
            {
                answer.add(mapping);
            }
        }
        return answer;
    }
}
