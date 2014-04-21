/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.portal;

import com.atlassian.configurable.ObjectConfigurationProperty;
import com.atlassian.configurable.ObjectDescriptor;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

public class ProjectObjectDescriptor implements ObjectDescriptor
{
    public String getDescription(Map properties, Map values)
    {
        ObjectConfigurationProperty projectid = (ObjectConfigurationProperty) properties.get("projectid");
        ObjectConfigurationProperty projectinfo = (ObjectConfigurationProperty) properties.get("projectinfo");
        I18nHelper i18n = new I18nBean();
        String text = i18n.getText("portlet.project.display.name");
        text = StringUtils.replace(text, "{0}", projectid.get(((String[]) values.get("projectid"))[0]).toString());
        text = StringUtils.replace(text, "{1}", i18n.getText(projectinfo.get(((String[]) values.get("projectinfo"))[0]).toString()));
        return text;
    }

    // Validate the properties
    public Map validateProperties(Map values)
    {
        // No validation required - return original values
        return values;
    }
}
