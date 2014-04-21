/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.portal;

import com.atlassian.configurable.ObjectConfigurationProperty;
import com.atlassian.configurable.ObjectDescriptor;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

public class ProjectCategoriesDescriptor implements ObjectDescriptor
{
    private static final String PROJECT_CATEGORY_ID_KEY = "projectcategoryid";

    public String getDescription(Map properties, Map values)
    {
        ObjectConfigurationProperty projectcategory = (ObjectConfigurationProperty) properties.get("projectcategoryid");

        I18nHelper i18n = new I18nBean();
        if (values == null || !values.containsKey("projectcategoryid"))
        {
            return i18n.getText("gadget.projects.display.name.all");
        }
        else
        {
            String[] projectCategoryStringArray = (String[]) values.get("projectcategoryid");

            if (projectCategoryStringArray.length == 0 || !TextUtils.stringSet(projectCategoryStringArray[0]))
            {
                return i18n.getText("gadget.projects.display.name.all");
            }
            else
            {
                String text = i18n.getText("portlet.projects.display.name.category");
                return StringUtils.replace(text, "{0}", projectcategory.get(projectCategoryStringArray[0]).toString());
            }
        }
    }

    // Validate the properties defined in system-portlets-plugin.xml
    public Map validateProperties(Map values)
    {
        return values;
    }
}
