package com.atlassian.jira.issue.fields.option;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class FieldConfigSchemeOption extends AbstractOption
{
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    private FieldConfigScheme fieldConfigScheme;
    private List childOptions = Collections.EMPTY_LIST;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors
    public FieldConfigSchemeOption(FieldConfigScheme fieldConfigScheme, Collection childOptions)
    {
        this.fieldConfigScheme = fieldConfigScheme;
        if (childOptions != null)
        {
            this.childOptions = EasyList.build(childOptions);
        }
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    public String getId()
    {
        return fieldConfigScheme != null ? fieldConfigScheme.getId().toString() : null;
    }

    public Long getFieldConfigurationId()
    {
        if(fieldConfigScheme == null || fieldConfigScheme.getOneAndOnlyConfig() == null)
        {
            return null;
        }
        return fieldConfigScheme.getOneAndOnlyConfig().getId();
    }

    public String getName()
    {
        return fieldConfigScheme != null ? fieldConfigScheme.getName() : null;
    }

    public String getDescription()
    {
        return fieldConfigScheme != null ? fieldConfigScheme.getDescription() : null;
    }

    public List getChildOptions()
    {
        return childOptions;
    }

    public String getProjects()
    {
        if (fieldConfigScheme != null)
        {
            StringBuffer sb = new StringBuffer();
            if (!fieldConfigScheme.isGlobal())
            {
                for (Iterator iterator = fieldConfigScheme.getAssociatedProjects().iterator(); iterator.hasNext();)
                {
                    GenericValue project = (GenericValue) iterator.next();
                    sb.append(project.getString("name"));
                    if (iterator.hasNext())
                    {
                        sb.append(", ");
                    }
                }
            }
            else
            {
                sb.append("Default scheme (unlisted projects)");
            }
            
            return sb.toString();
        }
        else
        {
            return "";
        }
    }
    // -------------------------------------------------------------------------------------------------- Helper Methods
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final FieldConfigSchemeOption that = (FieldConfigSchemeOption) o;

        if (fieldConfigScheme != null ? !fieldConfigScheme.equals(that.fieldConfigScheme) : that.fieldConfigScheme != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return (fieldConfigScheme != null ? fieldConfigScheme.hashCode() : 491);
    }
}
