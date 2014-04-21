package com.atlassian.jira.pageobjects.project.workflow;

import com.atlassian.jira.pageobjects.components.DropDown;
import com.atlassian.jira.pageobjects.pages.admin.ConfigureScreen;
import com.atlassian.jira.pageobjects.pages.admin.WorkflowDesignerPage;
import com.atlassian.jira.pageobjects.pages.admin.workflow.EditWorkflowScheme;
import com.atlassian.jira.pageobjects.pages.admin.workflow.SelectWorkflowScheme;
import com.atlassian.jira.pageobjects.project.AbstractProjectConfigPageTab;
import com.atlassian.jira.pageobjects.project.ProjectSharedBy;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Page object for the workflows tab.
 *
 * @since v4.4
 */
public class WorkflowsPageTab extends AbstractProjectConfigPageTab
{
    private final String projectKey;
    private static final String SCHEME_NAME_ID = "project-config-workflows-scheme-name";
    private static final String EDIT_LINK_ID = "project-config-workflows-scheme-edit";
    private static final String CHANGE_LINK_ID = "project-config-workflows-scheme-change";

    @ElementBy (id = "project-config-panel-workflows")
    private PageElement container;

    private PageElement schemeName;
    private PageElement schemeEditLink;
    private PageElement schemeChangeLink;
    private DropDown dropDown;

    @Init
    public void initialise()
    {
        dropDown = pageBinder.bind(DropDown.class, By.id("project-config-tab-actions"), By.id("project-config-tab-actions-list"));
        schemeName = elementFinder.find(By.id(SCHEME_NAME_ID));
        schemeEditLink = elementFinder.find(By.id(EDIT_LINK_ID));
        schemeChangeLink = elementFinder.find(By.id(CHANGE_LINK_ID));
    }

    public WorkflowsPageTab(String projectKey)
    {
        this.projectKey = projectKey;
    }

    public boolean isSchemeLinked()
    {
        return dropDown.hasItemById(EDIT_LINK_ID);
    }

    public boolean isSchemeChangeAvailable()
    {
        return dropDown.hasItemById(CHANGE_LINK_ID);
    }

    public EditWorkflowScheme gotoEditScheme()
    {
        final String schemeId = schemeEditLink.getAttribute("data-id");
        return dropDown.openAndClick(By.id(EDIT_LINK_ID), EditWorkflowScheme.class, Long.valueOf(schemeId));
    }

    public SelectWorkflowScheme gotoSelectScheme()
    {
        final String projectId = schemeChangeLink.getAttribute("data-id");
        return dropDown.openAndClick(By.id(CHANGE_LINK_ID), SelectWorkflowScheme.class, Long.valueOf(projectId));
    }

    public String getSchemeName()
    {
        return schemeName.getText();
    }

    public List<WorkflowPanel> getWorkflowPanels()
    {
        List<PageElement> all = container.findAll(By.cssSelector("div.project-config-webpanel"));
        List<WorkflowPanel> panels = Lists.newArrayList();
        for (PageElement element : all)
        {
            panels.add(pageBinder.bind(WorkflowPanel.class, element));
        }
        return panels;
    }

    @Override
    public String getUrl()
    {
        return String.format("/plugins/servlet/project-config/%s/workflows", projectKey);
    }

    @Override
    public TimedCondition isAt()
    {
        return container.timed().isPresent();
    }

    public static class WorkflowPanel
    {
        private final PageElement container;

        private PageElement editElement;

        @Inject
        private PageBinder binder;

        @Inject
        private PageElementFinder finder;

        public WorkflowPanel(PageElement container)
        {
            this.container = container;
        }

        @Init
        public void init()
        {
            editElement = container.find(By.cssSelector("a.project-config-workflow-edit"));
        }

        public boolean hasEditWorkflowLink()
        {
            return editElement.isPresent();
        }

        public String getEditWorkflowLink()
        {
            assertTrue("Edit workflow link is not present.", hasEditWorkflowLink());
            return editElement.getAttribute("href");
        }

        public WorkflowDesignerPage gotoEditWorkflow()
        {
            assertTrue("Edit workflow link is not present.", hasEditWorkflowLink());
            editElement.click();
            return binder.bind(WorkflowDesignerPage.class, getWorkflowName(), false);
        }

        public boolean isCollapsed()
        {
            return container.hasClass("collapsed");
        }

        public boolean toggleCollapsed()
        {
            PageElement element = container.find(By.cssSelector("h3 span.project-config-workflow-name"));
            assertTrue("Unable to find toggle heading.", element.isPresent());
            element.click();
            return isCollapsed();
        }

        public List<String> getIssueTypes()
        {
            List<PageElement> types = container.findAll(By.cssSelector(".project-config-workflow-issuetypes li .project-config-issuetype-name"));
            List<String> stringTypes = Lists.newArrayList();
            for (PageElement type : types)
            {
                stringTypes.add(type.getText());
            }
            return stringTypes;
        }

        public boolean isDefaultWorkflow()
        {
            return container.find(By.cssSelector(".status-default")).isPresent();
        }

        public String getWorkflowName()
        {
            PageElement element = container.find(By.cssSelector(".project-config-workflow-name"));
            if (element.isPresent())
            {
                return element.getText();
            }
            else
            {
                return null;
            }
        }

        public List<String> getProjects()
        {
            PageElement sharedElement = container.find(By.cssSelector(".shared-by"));
            if (sharedElement.isPresent())
            {
                ProjectSharedBy sharedBy = binder.bind(ProjectSharedBy.class, sharedElement);
                return sharedBy.getProjects();
            }
            else
            {
                return Collections.emptyList();
            }
        }

        public List<WorkflowTransition> getTransitions()
        {
            if(isCollapsed())
            {
                container.find(By.cssSelector(".project-config-workflow-name")).click();
                assertFalse(isCollapsed());
            }

            List<PageElement> rows = container.findAll(By.cssSelector(".project-config-datatable tbody tr"));
            List<WorkflowTransition> transtions = Lists.newArrayListWithExpectedSize(rows.size());
            String sourceStatus = null;
            int sourceStatusCount = 0;

            for (PageElement element : rows)
            {
                int column = 0;
                List<PageElement> cols = element.findAll(By.cssSelector("td"));

                if (sourceStatusCount <= 0)
                {
                    PageElement status = cols.get(column);
                    sourceStatus = status.getText();
                    String rowspan = StringUtils.trimToNull(status.getAttribute("rowspan"));
                    if (rowspan == null)
                    {
                        sourceStatusCount = 1;
                    }
                    else
                    {
                        sourceStatusCount = Integer.parseInt(rowspan);
                    }

                    column += 1;
                }
                else
                {
                    column = 0;
                }

                //Transition name
                PageElement transElement = cols.get(column);
                PageElement tranNameElement = transElement.find(By.cssSelector(".project-config-transname"));
                if (!tranNameElement.isPresent())
                {
                    //Looks like we have a transition with outgoing elements.
                    transtions.add(binder.bind(WorkflowTransition.class, sourceStatus, null, null, null));
                }
                else
                {
                    String transitionName = tranNameElement.getText();
                    //Screen name
                    PageElement screenElement = transElement.find(By.cssSelector(".project-config-screen"));
                    if (!screenElement.isPresent())
                    {
                        screenElement = null;
                    }

                    //Target status.
                    column += 2;
                    String targetStatus = cols.get(column).getText();
                    transtions.add(binder.bind(WorkflowTransition.class, sourceStatus, transitionName, screenElement, targetStatus));
                }
                sourceStatusCount--;
            }
            return transtions;
        }
    }

    public static class WorkflowTransition
    {
        private final String name;
        private final PageElement screenElement;
        private final String targetStatus;
        private final String sourceStatus;

        @Inject private PageBinder binder;

        public WorkflowTransition(String sourceStatus, String name, PageElement screenElement, String targetStatus)
        {
            this.name = name;
            this.targetStatus = targetStatus;
            this.screenElement = screenElement;
            this.sourceStatus = sourceStatus;
        }

        public String getSourceStatusName()
        {
            return sourceStatus;
        }

        public String getTransitionName()
        {
            return name;
        }

        public String getTargetStatusName()
        {
            return targetStatus;
        }

        public String getScreenName()
        {
            if (screenElement != null)
            {
                return screenElement.getText();
            }
            else
            {
                return null;
            }
        }

        public ConfigureScreen gotoConfigureScreen()
        {
            assertTrue("There is no screen link.", hasScreenLink());

            screenElement.click();
            return binder.bind(ConfigureScreen.class);
        }

        public String getScreenLink()
        {
            return screenElement != null ? screenElement.getAttribute("href") : null;
        }

        public boolean hasScreenLink()
        {
            return screenElement != null && StringUtils.isNotBlank(screenElement.getAttribute("href"));
        }

        public boolean hasScreen()
        {
            return screenElement != null;
        }
    }
}
