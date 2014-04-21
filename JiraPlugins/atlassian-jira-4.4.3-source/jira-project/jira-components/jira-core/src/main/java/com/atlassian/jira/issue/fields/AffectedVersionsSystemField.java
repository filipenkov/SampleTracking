package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.util.VersionHelperBean;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.handlers.AffectedVersionSearchHandlerFactory;
import com.atlassian.jira.issue.statistics.RaisedInVersionStatisticsMapper;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.velocity.VelocityManager;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class AffectedVersionsSystemField extends AbstractVersionsSystemField
{
    private static final String AFFECTED_VERSIONS_NAME_KEY = "issue.field.affectsversions";
    private final RaisedInVersionStatisticsMapper raisedInVersionStatsMapper;

    public AffectedVersionsSystemField(VelocityManager velocityManager, ApplicationProperties applicationProperties,
                                       VersionManager versionManager, PermissionManager permissionManager,
                                       JiraAuthenticationContext authenticationContext,
                                       RaisedInVersionStatisticsMapper raisedInVersionStatsMapper,
                                       VersionHelperBean versionHelperBean,
                                       AffectedVersionSearchHandlerFactory searchHandlerFactory)
    {
        super(IssueFieldConstants.AFFECTED_VERSIONS, AFFECTED_VERSIONS_NAME_KEY, velocityManager, applicationProperties, versionManager, permissionManager, authenticationContext, versionHelperBean, searchHandlerFactory);
        this.raisedInVersionStatsMapper = raisedInVersionStatsMapper;
    }

    public boolean isShown(Issue issue)
    {
        // Affected Versions field is not protected by any permission
        return true;
    }

    protected Collection getCurrentVersions(Issue issue)
    {
        return issue.getAffectedVersions();
    }

    protected String getArchivedVersionsFieldTitle()
    {
        return "issue.field.archived.affectsversions";
    }

    protected String getArchivedVersionsFieldSearchParam()
    {
        return "version";
    }

    protected boolean getUnreleasedVersionsFirst()
    {
        return false;
    }

    protected void addFieldRequiredErrorMessage(Issue issue, ErrorCollection errorCollection, I18nHelper i18n)
    {
        if (getPossibleVersions(issue.getProject(), false).isEmpty())
        {
            errorCollection.addErrorMessage(i18n.getText("createissue.error.versions.required", i18n.getText(getNameKey()), issue.getProject().getString("name")));
        }
        else
        {
            errorCollection.addError(getId(), i18n.getText("issue.field.required", i18n.getText(getNameKey())));
        }
    }

    public String getColumnHeadingKey()
    {
        return "issue.column.heading.affectsversions";
    }

    public LuceneFieldSorter getSorter()
    {
        return raisedInVersionStatsMapper;
    }

    protected String getIssueRelationName()
    {
        return IssueRelationConstants.VERSION;
    }

    protected String getChangeItemFieldName()
    {
        return "Version";
    }

    protected String getModifiedWithoutPermissionErrorMessage(I18nHelper i18n)
    {
        throw new UnsupportedOperationException("Affected Versions field is not protected by permission.");
    }

    public Object getDefaultValue(Issue issue)
    {
        return Collections.EMPTY_LIST;
    }

    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        if (fieldValueHolder.containsKey(getId()))
        {
            Collection affectedVersions = (Collection) getValueFromParams(fieldValueHolder);
            issue.setAffectedVersions(affectedVersions);
        }
    }

    public void removeValueFromIssueObject(MutableIssue issue)
    {
        issue.setAffectedVersions(Collections.EMPTY_LIST);
    }

    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        return true;
    }
}
