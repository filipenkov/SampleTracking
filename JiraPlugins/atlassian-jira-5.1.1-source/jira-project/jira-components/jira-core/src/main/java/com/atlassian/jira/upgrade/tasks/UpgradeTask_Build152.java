package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.util.JiraUtilsBean;
import com.atlassian.jira.web.action.admin.EditAnnouncementBanner;
import com.opensymphony.util.TextUtils;

public class UpgradeTask_Build152 extends AbstractUpgradeTask
{
    private final ApplicationProperties applicationProperties;
    private final JiraUtilsBean jiraUtilsBean;

    public UpgradeTask_Build152(ApplicationProperties applicationProperties, JiraUtilsBean jiraUtilsBean)
    {
        super(false);
        this.applicationProperties = applicationProperties;
        this.jiraUtilsBean = jiraUtilsBean;
    }

    public String getBuildNumber()
    {
        return "152";
    }

    public String getShortDescription()
    {
        return "Set application property for visibility level of announcement banner";
    }

    /**
     * Set the visibility of the alert announcement header to PUBLIC if a header exists and JIRA is set in PUBLIC mode.
     * Otherwise, set the visibility to PRIVATE.
     *
     * @throws Exception
     * @param setupMode
     */
    public void doUpgrade(boolean setupMode) throws Exception
    {
        String alertHeader = applicationProperties.getDefaultBackedText(APKeys.JIRA_ALERT_HEADER);

        if (TextUtils.stringSet(alertHeader))
        {
            if (jiraUtilsBean.isPublicMode())
            {
                applicationProperties.setString(APKeys.JIRA_ALERT_HEADER_VISIBILITY, EditAnnouncementBanner.PUBLIC_BANNER);
            }
            else
            {
                applicationProperties.setString(APKeys.JIRA_ALERT_HEADER_VISIBILITY, EditAnnouncementBanner.PRIVATE_BANNER);
            }
        }
    }
}
