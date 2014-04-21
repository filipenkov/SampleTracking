package com.atlassian.jira.issue.fields.util;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollection.Reason;
import com.atlassian.jira.util.I18nHelper;

import java.util.Collection;
import java.util.Iterator;

public class VersionHelperBean
{
    public static final Long UNKNOWN_VERSION_ID = new Long(-1);
    public static final Long UNRELEASED_VERSION_ID = new Long(-2);
    public static final Long RELEASED_VERSION_ID = new Long(-3);

    private VersionManager versionManager;


    public VersionHelperBean(VersionManager versionManager)
    {
        this.versionManager = versionManager;
    }

    public boolean validateVersionIds(Collection versionIds, ErrorCollection errorCollection, I18nHelper i18n, String fieldId)
    {
        boolean valid = true;
        if (versionIds != null)
        {
            if (versionIds.size() > 1)
            {
                if (versionIds.contains(UNKNOWN_VERSION_ID))
                {
                    errorCollection.addError(fieldId, i18n.getText("issue.field.versions.noneselectedwithother"), Reason.VALIDATION_FAILED);
                    valid = false;
                }
            }

            for (Iterator iterator = versionIds.iterator(); iterator.hasNext();)
            {
                final Object o = iterator.next();

                final Long l = getVersionIdAsLong(o);

                // TODO: Should this check for (l < 0). See comment in validateVersionForProject().
                if (l < -1)
                {
                    errorCollection.addError(fieldId, i18n.getText("issue.field.versions.releasedunreleasedselected"), Reason.VALIDATION_FAILED);
                    valid = false;
                }
            }
        }
        return valid;
    }

    public void validateVersionForProject(final Collection versionIds, final Project project, final ErrorCollection errorCollection, final I18nHelper i18n, String fieldId)
    {
        if (versionIds != null && project != null)
        {
            final Long projectId = project.getId();
            StringBuilder sb = null;
            for (Object versionId : versionIds)
            {
                final Long id = getVersionIdAsLong(versionId);
                if (id == -1)
                {
                    // Unknown should ahve been validated earlier
                    // TODO: It looks like validateVersionIds() checks for < -1. Is this incorrect?
                    return;
                }
                final Version version = versionManager.getVersion(id);
                if (version == null)
                {
                    errorCollection.addError(fieldId, i18n.getText("issue.field.versions.invalid.version.id", id), Reason.VALIDATION_FAILED);
                    return;
                }
                final Long versionProjectId = version.getProjectObject().getId();

                // JRA-20184: Only check on the ProjectID
                if (!versionProjectId.equals(projectId))
                {
                    if (sb == null)
                    {
                        sb = new StringBuilder(version.getName()).append("(").append(version.getId()).append(")");
                    }
                    else
                    {
                        sb.append(", ").append(version.getName()).append("(").append(version.getId()).append(")");
                    }
                }
            }
            if (sb != null)
            {
                errorCollection.addError(fieldId, i18n.getText("issue.field.versions.versions.not.valid.for.project", sb.toString(), project.getName()), Reason.VALIDATION_FAILED);
            }
        }
    }

    private Long getVersionIdAsLong(Object o)
    {
        Long l;
        if (o instanceof String)
        {
            l = new Long((String) o);
        }
        else
        {
            l = (Long) o;
        }
        return l;
    }

}
