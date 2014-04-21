package com.atlassian.jira.crowd.embedded;

import com.atlassian.crowd.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;

/**
 * Delegate to JIRA's I18Helper.
 *
 * @since v4.3
 */
public class CrowdDelegatingI18Helper implements I18nHelper
{

    @Override
    public String getText(String key)
    {
        com.atlassian.jira.util.I18nHelper i18nBean = new I18nBean();
        return i18nBean.getText(key);
    }

    @Override
    public String getText(String key, String value1)
    {
        com.atlassian.jira.util.I18nHelper i18nBean = new I18nBean();
        return i18nBean.getText(key, value1);
    }

    @Override
    public String getText(String key, String value1, String value2)
    {
        com.atlassian.jira.util.I18nHelper i18nBean = new I18nBean();
        return i18nBean.getText(key, value1, value2);
    }

    @Override
    public String getText(String key, Object parameters)
    {
        com.atlassian.jira.util.I18nHelper i18nBean = new I18nBean();
        return i18nBean.getText(key, parameters);
    }

    @Override
    public String getUnescapedText(String key)
    {
        com.atlassian.jira.util.I18nHelper i18nBean = new I18nBean();
        return i18nBean.getUnescapedText(key);
    }
}
