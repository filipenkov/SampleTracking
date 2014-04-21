package com.atlassian.jira.webtest.selenium.issue;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
* Util for testing JIRA action menus.
*
* @since v4.2
*/
class ActionItem
{
    private String title;
    private String url;

    ActionItem(String title, String url)
    {
        this.title = title;
        this.url = url;
    }

    String getTitle()
    {
        return title;
    }

    String getUrl()
    {
        return url;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
