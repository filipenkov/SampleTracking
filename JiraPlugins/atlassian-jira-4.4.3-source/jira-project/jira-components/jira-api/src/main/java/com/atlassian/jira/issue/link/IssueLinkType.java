package com.atlassian.jira.issue.link;

import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.ofbiz.AbstractOfBizValueWrapper;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import org.ofbiz.core.entity.GenericValue;

public class IssueLinkType extends AbstractOfBizValueWrapper implements Comparable
{
    public static final String NAME_FIELD_NAME = "linkname";
    public static final String OUTWARD_FIELD_NAME = "outward";
    public static final String INWARD_FIELD_NAME = "inward";
    public static final String STYLE_FIELD_NAME = "style";

    /**
     * @TODO delete on confirming this is not in external use
     * @deprecated use IssueLinkType(GenericValue) we don't need the ILTM.
     * @param genericValue
     * @param issueLinkTypeManager
     */
    public IssueLinkType(GenericValue genericValue, IssueLinkTypeManager issueLinkTypeManager)
    {
        this(genericValue);
    }

    /**
     * Creates an IssueLinkType from the given GenericValue.
     * @param genericValue
     */
    public IssueLinkType(GenericValue genericValue)
    {
        super(genericValue);

        if (!OfBizDelegator.ISSUE_LINK_TYPE.equals(genericValue.getEntityName()))
        {
            throw new IllegalArgumentException("Entity must be an 'IssueLinkType', not '" + genericValue.getEntityName() + "'.");
        }
    }

    public Long getId()
    {
        return getGenericValue().getLong("id");
    }

    public String getName()
    {
        return getGenericValue().getString(NAME_FIELD_NAME);
    }

    public String getOutward()
    {
        return getGenericValue().getString(OUTWARD_FIELD_NAME);
    }

    public String getInward()
    {
        return getGenericValue().getString(INWARD_FIELD_NAME);
    }

    public String getStyle()
    {
        return getGenericValue().getString(STYLE_FIELD_NAME);
    }

    void setName(String name)
    {
        getGenericValue().set(NAME_FIELD_NAME, name);
    }

    void setOutward(String outward)
    {
        getGenericValue().set(OUTWARD_FIELD_NAME, outward);
    }

    void setInward(String inward)
    {
        getGenericValue().set(INWARD_FIELD_NAME, inward);
    }

    void setStyle(String style)
    {
        getGenericValue().set(STYLE_FIELD_NAME, style);
    }

    /**
     * Compare on name (in alphabetical order)
     *
     * @param o
     */
    public int compareTo(Object o)
    {
        if (o != null)
        {
            if (o instanceof IssueLinkType)
            {
                IssueLinkType other = (IssueLinkType) o;
                String otherName = other.getName();

                if (getName() == null && otherName == null)
                {
                    return 0;
                }
                else if (getName() != null && otherName != null)
                {
                    return getName().compareTo(otherName);
                }
                else if (otherName == null)
                {
                    return 1;
                }
                else
                {
                    return -1;
                }

            }
            else
            {
                throw new IllegalArgumentException("Object must be an IssueLinkType.");
            }
        }
        else
        {
            // If the object we are comparing to is null, this object should appear after it
            return 1;
        }

    }

    public boolean isSubTaskLinkType()
    {
        return SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE.equals(getStyle());
    }

    /**
     * Checks if this link type is a System Link type. System link types are used by JIRA to denote a special
     * relationship between issues. For example, a sub-task is linked ot its parent issue using a link that
     * is of a system link type.
     */
    public boolean isSystemLinkType()
    {
        return (getStyle() != null && getStyle().startsWith("jira_"));
    }
}
