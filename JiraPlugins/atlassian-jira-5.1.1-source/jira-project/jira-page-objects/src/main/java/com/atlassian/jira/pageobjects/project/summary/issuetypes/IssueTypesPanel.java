package com.atlassian.jira.pageobjects.project.summary.issuetypes;

import com.atlassian.jira.pageobjects.project.summary.AbstractSummaryPanel;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.google.common.collect.Lists;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.List;

/**
 * Represents the issue types panel in the project configuration page.
 *
 * @since v4.4
 */
public class IssueTypesPanel extends AbstractSummaryPanel
{
    @Inject
    private PageBinder pageBinder;

    @ElementBy(id = "project-config-webpanel-summary-issuetypes")
    private PageElement issueTypesSummaryPanel;

    public List<IssueTypeListItem> issueTypes()
    {
        final List<IssueTypeListItem> issueTypeListItems = Lists.newArrayList();
        final List<PageElement> issueTypeElements = issueTypesSummaryPanel.findAll(getIssueTypeListLocator());

        for (final PageElement issueTypeElement : issueTypeElements)
        {
            final String issueTypeName = issueTypeElement.find(getIssueTypeNameLocator()).getText();
            final String issueTypeImage = issueTypeElement.find(By.cssSelector("img")).getAttribute("src");
            final boolean isSubTask =
                    issueTypeElement.find(By.className("project-config-issuetype-subtask")).isPresent();
            final boolean isDefaultIssueType =
                    issueTypeElement.find(By.className("project-config-list-default")).isPresent();

            issueTypeListItems.add(new IssueTypeListItem(issueTypeName, issueTypeImage, isSubTask, isDefaultIssueType));
        }

        return issueTypeListItems;
    }

    public boolean isIssueTypeSchemeEditLinkPresent()
    {
        return issueTypesSummaryPanel.find(getSchemeLinkLocator()).isPresent();
    }

    public String getIssueTypeTabLinkText()
    {
        return issueTypesSummaryPanel.find(getSchemeLinkLocator()).getText();
    }

    public String getIssueTypeTabUrl()
    {
        return issueTypesSummaryPanel.find(getSchemeLinkLocator()).getAttribute("href");
    }

    public String getNoIssueTypesMessage()
    {
        return issueTypesSummaryPanel.find(getNoIssueTypesMessageLocator()).getText();
    }

    private static By getSchemeLinkLocator()
    {
        return By.cssSelector(".project-config-summary-scheme > a");
    }

    private static By getNoIssueTypesMessageLocator()
    {
        return By.cssSelector(".project-config-list-empty span");
    }

    private static By getIssueTypeNameLocator()
    {
        return By.cssSelector(".project-config-issuetype-name");
    }

    private static By getIssueTypeListLocator()
    {
        return By.cssSelector(".project-config-list > li");
    }

    /**
     * Represents an item in an issue types scheme as shown in the issue types panel on the project configuration summary
     * page
     *
     * @since v4.4
     */
    public static class IssueTypeListItem
    {
        private final String issueTypeName;
        private final String issueTypeImage;
        private final boolean subTask;
        private final boolean defaultIssueType;

        public IssueTypeListItem(final String issueTypeName, final String issueTypeImage, boolean subTask,
                boolean defaultIssueType)
        {
            this.issueTypeName = issueTypeName;
            this.issueTypeImage = issueTypeImage;
            this.subTask = subTask;
            this.defaultIssueType = defaultIssueType;
        }

        public String getIssueTypeName()
        {
            return issueTypeName;
        }

        public String getIssueTypeImage()
        {
            return issueTypeImage;
        }

        public boolean isSubTask()
        {
            return subTask;
        }

        public boolean isDefaultIssueType()
        {
            return defaultIssueType;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            IssueTypeListItem that = (IssueTypeListItem) o;

            if (defaultIssueType != that.defaultIssueType) { return false; }
            if (subTask != that.subTask) { return false; }
            if (issueTypeImage != null ? !issueTypeImage.equals(that.issueTypeImage) : that.issueTypeImage != null)
            { return false; }
            if (issueTypeName != null ? !issueTypeName.equals(that.issueTypeName) : that.issueTypeName != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = issueTypeName != null ? issueTypeName.hashCode() : 0;
            result = 31 * result + (issueTypeImage != null ? issueTypeImage.hashCode() : 0);
            result = 31 * result + (subTask ? 1 : 0);
            result = 31 * result + (defaultIssueType ? 1 : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
