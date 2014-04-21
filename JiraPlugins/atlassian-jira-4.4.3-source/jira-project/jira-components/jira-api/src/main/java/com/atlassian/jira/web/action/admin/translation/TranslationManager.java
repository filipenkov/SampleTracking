/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.translation;

import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.util.I18nHelper;

import java.util.Locale;
import java.util.Map;

public interface TranslationManager
{
    public Map getInstalledLocales();

    public String getTranslatedNameFromString(String translationString);

    public String getTranslatedDescriptionFromString(String translationString);

    public String getIssueConstantTranslation(IssueConstant issueConstant, boolean name, String locale);

    public String getIssueConstantTranslation(IssueConstant issueConstant, boolean name, String locale, I18nHelper i18n);

    public String getIssueConstantTranslation(IssueConstant issueConstant, boolean name, Locale locale);

    public boolean hasLocaleTranslation(IssueConstant issueConstant, String locale);

    public void setIssueConstantTranslation(IssueConstant issueConstant, String issueConstantPrefix, Locale locale, String translatedName, String translatedDesc);

    public void deleteIssueConstantTranslation(IssueConstant issueConstant, String issueConstantPrefix, Locale locale);
}
