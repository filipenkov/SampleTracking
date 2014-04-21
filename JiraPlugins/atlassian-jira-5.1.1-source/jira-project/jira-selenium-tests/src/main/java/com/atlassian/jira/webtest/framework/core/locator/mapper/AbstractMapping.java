package com.atlassian.jira.webtest.framework.core.locator.mapper;

import com.atlassian.jira.webtest.framework.core.locator.LocatorData;
import com.atlassian.jira.webtest.framework.core.locator.LocatorType;

import static com.atlassian.jira.util.dbc.Assertions.notNull;


/**
 * Abstract implementation of {@link LocatorMapping}. Holds suported parent and child locator type and defines
 * {@link #combine(com.atlassian.jira.webtest.framework.core.locator.LocatorData, com.atlassian.jira.webtest.framework.core.locator.LocatorData)}
 * as a template method ensuring locator type compatibility of the provided arguments.
 *
 * @since v4.3
 */
public abstract class AbstractMapping implements LocatorMapping
{
    private final LocatorType parentType;
    private final LocatorType childType;

    protected AbstractMapping(LocatorType parentType, LocatorType childType)
    {
        this.parentType = notNull("parentType", parentType);
        this.childType = notNull("childType", childType);
    }

    public LocatorType parentType()
    {
        return parentType;
    }

    public LocatorType childType()
    {
        return childType;
    }

    public boolean supportsParent(LocatorData parent)
    {
        return parentType().equals(parent.type());
    }

    public boolean supportsChild(LocatorData child)
    {
        return childType().equals(child.type());
    }

    public final LocatorData combine(LocatorData parent, LocatorData child)
    {
        if (!supportsParent(parent))
        {
            throw new IllegalArgumentException("Invalid parent type <" + parent.type()
                    + ">. This mapping only supports <" + parentType() + ">");
        }
        if (!supportsChild(child))
        {
            throw new IllegalArgumentException("Invalid child type <" + child.type()
                    + ">. This mapping only supports <" + childType() + ">");
        }
        return doCombine(parent.value(), child.value());
    }

    /**
     * Do the actual combination of locators. This method is onlu given string locator values, as it is already ensured
     * that parent type and child type are compatible with those supported by this mapping, so {@link #parentType()}
     * and {@link #childType()} may be used to determine them.
     *
     * @param parentValue parent locator value
     * @param childValue child locator value
     * @return new locator data representing a combined locator
     */
    protected abstract LocatorData doCombine(final String parentValue, final String childValue);

}
