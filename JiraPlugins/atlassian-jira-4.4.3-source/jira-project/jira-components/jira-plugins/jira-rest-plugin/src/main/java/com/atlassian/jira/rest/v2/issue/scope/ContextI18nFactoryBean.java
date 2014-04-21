package com.atlassian.jira.rest.v2.issue.scope;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.rest.v2.issue.context.ContextI18n;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * This factory bean creates ContextI18n instances.
 *
 * @since v4.2
 */
public class ContextI18nFactoryBean extends AbstractFactoryBean
{
    /**
     * A JiraAuthenticationContext.
     */
    private final JiraAuthenticationContext authContext;

    /**
     * Creates a new ContextI18nFactoryBean.
     *
     * @param authContext a JiraAuthenticationContext
     */
    public ContextI18nFactoryBean(JiraAuthenticationContext authContext)
    {
        this.authContext = authContext;
    }

    /**
     * Creates a new ContextI18n instance that delegates to the I18nHelper obtained from the JiraAuthenticationContext.
     *
     * @return a new ContextI18n
     */
    @Override
    protected ContextI18n createInstance()
    {
        I18nHelper i18n = authContext.getI18nHelper();
        if (i18n == null)
        {
            throw new IllegalStateException();
        }

        return new I18nWrapper(i18n);
    }

    /**
     * Returns ContextI18n.class
     *
     * @return ContextI18n.class
     */
    @Override
    public Class<ContextI18n> getObjectType()
    {
        return ContextI18n.class;
    }

    /**
     * Wrapper around an I18nHelper instance.
     */
    static class I18nWrapper implements ContextI18n
    {
        private final I18nHelper i18n;

        public I18nWrapper(I18nHelper i18n) {this.i18n = i18n;}

        public String getText(String key) {return i18n.getText(key);}

        public Locale getLocale() {return i18n.getLocale();}

        public String getUnescapedText(String key) {return i18n.getUnescapedText(key);}

        public String getText(String key, String value1) {return i18n.getText(key, value1);}

        public String getText(String key, String value1, String value2) {return i18n.getText(key, value1, value2);}

        public String getText(String key, String value1, String value2, String value3)
        {
            return i18n.getText(key, value1, value2, value3);
        }

        public String getText(String key, String value1, String value2, String value3, String value4)
        {
            return i18n.getText(key, value1, value2, value3, value4);
        }

        public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5, Object value6)
        {
            return i18n.getText(key, value1, value2, value3, value4, value5, value6);
        }

        public String getText(String key, String value1, String value2, String value3, String value4, String value5, String value6, String value7)
        {
            return i18n.getText(key, value1, value2, value3, value4, value5, value6, value7);
        }

        public String getText(String key, String value1, String value2, String value3, String value4, String value5, String value6, String value7, String value8, String value9)
        {
            return i18n.getText(key, value1, value2, value3, value4, value5, value6, value7, value8, value9);
        }

        public String getText(String key, Object value1, Object value2, Object value3)
        {
            return i18n.getText(key, EasyList.build(value1, value2, value3));
        }

        public String getText(String key, Object value1, Object value2, Object value3, Object value4)
        {
            return i18n.getText(key, EasyList.build(value1, value2, value3, value4));
        }

        public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5)
        {
            return i18n.getText(key, EasyList.build(value1, value2, value3, value4, value5));
        }

        public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5, Object value6, Object value7)
        {
            return i18n.getText(key, EasyList.build(value1, value2, value3, value4, value5, value6, value7));
        }

        public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5, Object value6, Object value7, Object value8)
        {
            return i18n.getText(key, EasyList.build(value1, value2, value3, value4, value5, value6, value7, value8));
        }

        public ResourceBundle getDefaultResourceBundle() {return i18n.getDefaultResourceBundle();}

        public String getText(String key, Object parameters) {return i18n.getText(key, parameters);}

        public Set<String> getKeysForPrefix(String prefix) {return i18n.getKeysForPrefix(prefix);}
    }
}
