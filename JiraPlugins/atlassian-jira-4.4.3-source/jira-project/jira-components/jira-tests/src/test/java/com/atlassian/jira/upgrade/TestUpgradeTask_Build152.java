package com.atlassian.jira.upgrade;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build152;
import com.atlassian.jira.web.action.admin.EditAnnouncementBanner;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.MockJiraUtilsBean;

public class TestUpgradeTask_Build152 extends LegacyJiraMockTestCase
{
    private MockApplicationProperties applicationProperties;
    private MockJiraUtilsBean jiraUtilsBean;
    private UpgradeTask_Build152 upgradeTask_build152;

    public TestUpgradeTask_Build152(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        applicationProperties = new MockApplicationProperties();
        jiraUtilsBean = new MockJiraUtilsBean();
        upgradeTask_build152 = new UpgradeTask_Build152(applicationProperties, jiraUtilsBean);
    }

    public void testBannerVisibilityPrivateMode() throws Exception
    {
        applicationProperties.setText(APKeys.JIRA_ALERT_HEADER, "Paint It Black");
        jiraUtilsBean.setPublicMode(false);
        upgradeTask_build152.doUpgrade(false);
        assertEquals(EditAnnouncementBanner.PRIVATE_BANNER, applicationProperties.getDefaultBackedString(APKeys.JIRA_ALERT_HEADER_VISIBILITY));
    }

    public void testBannerVisibilityPublicMode() throws Exception
    {
        applicationProperties.setText(APKeys.JIRA_ALERT_HEADER, "Paint It Green");
        jiraUtilsBean.setPublicMode(true);
        upgradeTask_build152.doUpgrade(false);
        assertTrue(jiraUtilsBean.isPublicMode());
        assertEquals(EditAnnouncementBanner.PUBLIC_BANNER, applicationProperties.getDefaultBackedString(APKeys.JIRA_ALERT_HEADER_VISIBILITY));
    }
}
