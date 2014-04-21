/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.translation;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.util.TextUtils;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

public class TranslationManagerImpl implements TranslationManager
{
    public static final String JIRA_ISSUETYPE_TRANSLATION_PREFIX = "jira.translation.issuetype";
    public static final String JIRA_PRIORITY_TRANSLATION_PREFIX = "jira.translation.priority";
    public static final String JIRA_RESOLUTION_TRANSLATION_PREFIX = "jira.translation.resolution";
    public static final String JIRA_STATUS_TRANSLATION_PREFIX = "jira.translation.status";

    public static final String NONE = "None";

    private final Map translationPrefixMap;
    private JiraAuthenticationContext authenticationContext;

    public TranslationManagerImpl(JiraAuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
        translationPrefixMap = new HashMap();
        translationPrefixMap.put("IssueType", JIRA_ISSUETYPE_TRANSLATION_PREFIX);
        translationPrefixMap.put("Priority", JIRA_PRIORITY_TRANSLATION_PREFIX);
        translationPrefixMap.put("Resolution", JIRA_RESOLUTION_TRANSLATION_PREFIX);
        translationPrefixMap.put("Status", JIRA_STATUS_TRANSLATION_PREFIX);
    }

    /**
     * Retrieves the installed locales in the user's language.
     * @return A map containing the installed locales indexed by each locale's string representation.
     */
    public Map getInstalledLocales()
    {
        Map locales = new ListOrderedMap();
        final List installedLocales = ManagerFactory.getJiraLocaleUtils().getInstalledLocales();
        for (int i = 0; i < installedLocales.size(); i++)
        {
            Locale locale = (Locale) installedLocales.get(i);
            locales.put(locale.toString(), locale.getDisplayName(authenticationContext.getLocale()));
        }

        return locales;
    }

    public String getTranslatedNameFromString(String translationString)
    {
        StringTokenizer st = new StringTokenizer(translationString, "\n");
        if (st.countTokens() == 2)
        {
            return (String) st.nextElement();
        }
        return null;
    }

    public String getTranslatedDescriptionFromString(String translationString)
    {
        StringTokenizer st = new StringTokenizer(translationString, "\n");
        String extractedDesc = null;
        while (st.hasMoreElements())
        {
            extractedDesc = (String) st.nextElement();
        }
        return extractedDesc;
    }

    /**
     * Extract the desired string (name/description) from the specified issue constant.
     * <p>
     * If a system defined translation does not exist, the property file associated with the i18nHelper is checked.
     *
     * @param issueConstant
     * @param name      boolean - fetch name or description
     * @param locale    used to check if system defined property exists
     * @param i18n      the i18nHelper to use to retrieve the translation from property files if no defined within system
     * @return String   translated issue constant name or description
     */
    public String getIssueConstantTranslation(IssueConstant issueConstant, boolean name, String locale, I18nHelper i18n)
    {
        PropertySet ps = issueConstant.getPropertySet();

        String issueConstantType = issueConstant.getGenericValue().getEntityName();
        String translationPrefix = getTranslationPrefix(issueConstantType);

        String translationString = ps.getString(translationPrefix + issueConstant.getId() + "." + locale);

        if (!TextUtils.stringSet(translationString))
        {
            if(TextUtils.stringSet(translationPrefix))
            {
                // try to fall back to the properties file
                String propKey = translationPrefix + "." + makeNameIntoProperty(issueConstant.getName()) + "." + ((name) ? "name" : "desc");

                // Use the specified i18nHelper or the one contained in the context.
                if (i18n == null)
                {
                    translationString = authenticationContext.getI18nHelper().getText(propKey);
                }
                else
                {
                    translationString = i18n.getText(propKey);
                }

                if(translationString != null && translationString.indexOf(translationPrefix) == -1)
                {
                    return translationString;
                }
            }

            // No translation exists - return the default name/description
            if (name)
                return issueConstant.getName();
            else
                return issueConstant.getDescription();
        }
        else
        {
            if (name)
                return getTranslatedNameFromString(translationString);
            else
                return getTranslatedDescriptionFromString(translationString);
        }
    }

    // Extract the desired string (name/description) from the specified issue constant
    public String getIssueConstantTranslation(IssueConstant issueConstant, boolean name, String locale)
    {
        return getIssueConstantTranslation(issueConstant, name, locale, null);
    }

    public boolean hasLocaleTranslation(IssueConstant issueConstant, String locale)
    {
        PropertySet ps = issueConstant.getPropertySet();

        String issueConstantType = issueConstant.getGenericValue().getEntityName();
        String translationPrefix = getTranslationPrefix(issueConstantType);

        String translationString = ps.getString(translationPrefix + issueConstant.getId() + "." + locale);

        return TextUtils.stringSet(translationString);
    }

    private String makeNameIntoProperty(String issueConstantName)
    {
        return StringUtils.deleteWhitespace(issueConstantName).toLowerCase();
    }

    private String getTranslationPrefix(String issueConstantType)
    {
        return (String) translationPrefixMap.get(issueConstantType);
    }

    // Extract the desired string (name/description) from the specified issue constant
    public String getIssueConstantTranslation(IssueConstant issueConstant, boolean name, Locale locale)
    {
        return getIssueConstantTranslation(issueConstant, name, locale.toString());
    }

    public void setIssueConstantTranslation(IssueConstant issueConstant, String issueConstantPrefix, Locale locale, String translatedName, String translatedDesc)
    {
        PropertySet ps = issueConstant.getPropertySet();
        String issueConstantLocaleKey = issueConstantPrefix + issueConstant.getId() + "." + locale;

        // Set property
        if (TextUtils.stringSet(translatedName) && TextUtils.stringSet(translatedDesc))
            ps.setString(issueConstantLocaleKey, translatedName + "\n" + translatedDesc);
    }

    public void deleteIssueConstantTranslation(IssueConstant issueConstant, String issueConstantPrefix, Locale locale)
    {
        PropertySet ps = issueConstant.getPropertySet();
        String issueConstantLocaleKey = issueConstantPrefix + issueConstant.getId() + "." + locale;

        if (ps.exists(issueConstantLocaleKey))
            ps.remove(issueConstantLocaleKey);
    }
}
