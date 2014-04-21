/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.portal;

import com.atlassian.configurable.ObjectDescriptor;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;

import java.util.Map;

public class ProjectTableCategoriesDescriptor implements ObjectDescriptor
{
    public String getDescription(Map properties, Map values)
    {
        I18nHelper i18n = new I18nBean();
        return i18n.getText("portlet.projecttable.display.name");
    }

    // Validate the properties defined in system-portlets-plugin.xml
    public Map validateProperties(Map values)
    {
        return values;
    }
}
