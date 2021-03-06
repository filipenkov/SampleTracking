package com.atlassian.jira.issue.fields;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.velocity.VelocityManager;
import org.apache.commons.lang.StringUtils;

import java.util.Comparator;
import java.util.Map;

public abstract class AbstractDurationSystemField extends NavigableFieldImpl
{
    public AbstractDurationSystemField(String id, String nameKey, String columnHeadingKey, String defaultSortOrder, Comparator comparator, VelocityManager velocityManager, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext)
    {
        super(id, nameKey, columnHeadingKey, defaultSortOrder, comparator, velocityManager, applicationProperties, authenticationContext);
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, getAuthenticationContext().getI18nHelper(), displayParams, issue);
        Long duration = getDuration(issue);
        if (duration != null)
        {
            String durationString;
            if (Boolean.TRUE.equals(displayParams.get(FieldRenderingContext.EXCEL_VIEW)))
            {
                durationString = duration.toString();
            }
            else
            {
                durationString = ComponentManager.getInstance().getJiraDurationUtils().getFormattedDuration(duration);
            }
            velocityParams.put("duration", durationString);
        }
        return renderTemplate("duration-columnview.vm", velocityParams);
    }

    protected abstract Long getDuration(Issue issue);

    public String prettyPrintChangeHistory(String changeHistory)
    {
        if (StringUtils.isNotBlank(changeHistory))
        {
            return ComponentManager.getInstance().getJiraDurationUtils().getFormattedDuration(new Long(changeHistory));
        }
        else
        {
            return super.prettyPrintChangeHistory(changeHistory);
        }
    }

    public String prettyPrintChangeHistory(String changeHistory, I18nHelper i18nHelper)
    {
        if (StringUtils.isNotBlank(changeHistory))
        {
            return ComponentManager.getInstance().getJiraDurationUtils().getFormattedDuration(new Long(changeHistory), i18nHelper.getLocale());
        }
        else
        {
            return super.prettyPrintChangeHistory(changeHistory);
        }
    }

}
