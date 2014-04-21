package com.atlassian.jira.pageobjects.project.summary.workflows;

import com.atlassian.jira.pageobjects.project.summary.AbstractSummaryPanel;
import com.atlassian.jira.pageobjects.project.workflow.EditWorkflowDialog;
import com.atlassian.jira.pageobjects.project.workflow.WorkflowsPageTab;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Page object that can be used to parse the workflow panel on the project summary page.
 *
 * @since v4.4
 */
public class WorkflowPanel extends AbstractSummaryPanel
{
    @ElementBy (id = "project-config-webpanel-summary-workflows")
    private PageElement workflowsSummaryPanel;

    @Inject
    private PageBinder binder;

    public List<Workflow> getWorkflows()
    {
        final List<PageElement> elements = workflowsSummaryPanel.findAll(By.cssSelector(".project-config-list > li"));
        final List<Workflow> workflows = new ArrayList<Workflow>(elements.size());
        for (final PageElement element : elements)
        {
            workflows.add(binder.bind(Workflow.class, element));
        }
        return workflows;
    }

    public String getSchemeName()
    {
        PageElement element = getSchemeElement();
        if (element.isPresent())
        {
            return element.getText();
        }
        else
        {
            return null;
        }
    }

    public WorkflowsPageTab gotoWorkflowTab()
    {
        getSchemeElement().click();
        return binder.bind(WorkflowsPageTab.class, getProjectKey());
    }

    private PageElement getSchemeElement()
    {
        return workflowsSummaryPanel.find(By.cssSelector(".project-config-summary-scheme a"));
    }

    public String getSchemeLinkUrl()
    {
        return getSchemeElement().getAttribute("href");
    }

    public static class Workflow
    {
        @Inject
        private PageBinder pageBinder;

        private PageElement editLink;
        private String name;
        private boolean isDefault;
        private String url;

        public Workflow(PageElement root)
        {
            PageElement nameElement = root.find(By.className("project-config-workflow-name"));
            setName(nameElement.getText());
            PageElement link = root.find(By.cssSelector("a.project-config-workflow-name"));
            if (link.isPresent())
            {
                setUrl(link.getAttribute("href"));
            }
            PageElement defaultElement = root.find(By.className("project-config-list-default"));
            setDefault(defaultElement.isPresent());
            editLink = root.find(By.className("project-config-workflow-default"));
        }

        public String getName()
        {
            return name;
        }

        public boolean isDefault()
        {
            return isDefault;
        }

        public Workflow setName(String name)
        {
            this.name = StringUtils.trimToNull(name);
            return this;
        }

        public Workflow setDefault(boolean aDefault)
        {
            isDefault = aDefault;
            return this;
        }

        public String getUrl()
        {
            return url;
        }

        public Workflow setUrl(String url)
        {
            this.url = StringUtils.trimToNull(url);
            return this;
        }

        public boolean hasEditLink()
        {
            return editLink.isPresent();
        }

        public EditWorkflowDialog gotoEditWorkflowDialog()
        {
            return clickEditWorkflowAndBind(EditWorkflowDialog.class);
        }

        public <T> T clickEditWorkflowAndBind(Class<T> page, Object...args)
        {
            assertTrue("Edit workflow link is not present.", hasEditLink());

            editLink.click();
            return pageBinder.bind(page, args);
        }

        @Override
        public int hashCode()
        {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public boolean equals(Object obj)
        {
            return EqualsBuilder.reflectionEquals(this, obj);
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
