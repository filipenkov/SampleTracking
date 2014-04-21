package com.atlassian.jira.issue.customfields.config.helper;

import com.atlassian.core.util.collection.EasyList;

import java.util.List;

public class StylesConfigDescriptor implements BasicConfigDescriptor
{

    public String getTitle()
    {
        return "Edit styles";
    }

    public String getInstructions()
    {
        return "Fill in the field below to control the CSS behaviours of your custom field";
    }

    public List getConfigFields()
    {
        return EasyList.build(new BasicConfigFieldDescriptor()
        {
            public String getName()
            {
                return "Style";
            }

            public String getDescription()
            {
                return "Valid CSS styles. e.g. width:50%;color:red;";
            }

            public String getKey()
            {
                return "style";
            }
        });
    }
}
