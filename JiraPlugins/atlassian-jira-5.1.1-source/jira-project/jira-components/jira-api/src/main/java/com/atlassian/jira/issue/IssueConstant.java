/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.util.I18nHelper;
import com.opensymphony.module.propertyset.PropertySet;
import org.ofbiz.core.entity.GenericValue;

import java.util.Locale;

/**
 * Abstraction to represent any of the various constants like {@link com.atlassian.jira.issue.resolution.Resolution},
 * {@link com.atlassian.jira.issue.status.Status} etc.
 */
@PublicApi
public interface IssueConstant extends Comparable
{
    GenericValue getGenericValue();

    String getId();

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    Long getSequence();

    void setSequence(Long sequence);

    String getIconUrl();

    /**
     * Returns the HTML-escaped URL for this issue constant.
     *
     * @return a String containing an HTML-escaped icon URL
     * @see #getIconUrl()
     */
    String getIconUrlHtml();

    void setIconUrl(String iconURL);

    // Retrieve name translation in current locale
    String getNameTranslation();

    // Retrieve desc translation in current locale
    String getDescTranslation();

    // Retrieve name translation in specified locale
    String getNameTranslation(String locale);

    // Retrieve desc translation in specified locale
    String getDescTranslation(String locale);

    String getNameTranslation(I18nHelper i18n);

    String getDescTranslation(I18nHelper i18n);

    void setTranslation(String translatedName, String translatedDesc, String issueConstantPrefix, Locale locale);

    void deleteTranslation(String issueConstantPrefix, Locale locale);

    PropertySet getPropertySet();
}
