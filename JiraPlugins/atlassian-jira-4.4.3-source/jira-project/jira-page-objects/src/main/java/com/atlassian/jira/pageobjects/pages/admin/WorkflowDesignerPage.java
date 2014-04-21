package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import javax.inject.Inject;
import java.util.List;

import static com.atlassian.pageobjects.elements.query.Conditions.and;
import static com.atlassian.pageobjects.elements.query.Poller.by;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntil;
import static org.hamcrest.Matchers.containsString;

/**
 * Represents the workflow designer page for a particular workflow!
 *
 * @since v4.4
 */
public class WorkflowDesignerPage extends AbstractJiraPage
{
    private static final String EDIT_TRIGGER = "edit-workflow-trigger";
    private static final String PUBLISH_TRIGGER = "publish-workflow-trigger";
    private static final String EDIT_WORKFLOW_FORM_ID = "edit-workflow";
    private static final String PUBLISH_WORKFLOW_FORM_ID = "publish-workflow";
    private static final String URI_TEMPLATE = "/secure/admin/WorkflowDesigner.jspa?wfName=%s&workflowMode=%s";

    private final String uri;

    @ElementBy (id = "jira", timeoutType = TimeoutType.SLOW_PAGE_LOAD)
    private PageElement rootElement;
    @ElementBy (className = "workflow-name", timeoutType = TimeoutType.SLOW_PAGE_LOAD)
    private PageElement workflowHeader;
    @ElementBy (className = "workflow-description", timeoutType = TimeoutType.SLOW_PAGE_LOAD)
    private PageElement workflowDescription;
    @ElementBy (className = "item-header", timeoutType = TimeoutType.SLOW_PAGE_LOAD)
    private PageElement itemHeader;

    public WorkflowDesignerPage(final String workflowName, final boolean isDraft)
    {
        this.uri = String.format(URI_TEMPLATE, workflowName, isDraft ? "draft" : "live");
    }

    @Override
    public TimedCondition isAt()
    {
        return and(rootElement.timed().isPresent(), workflowHeader.timed().isPresent());
    }

    @Override
    public String getUrl()
    {
        return uri;
    }

    public String getWorkflowHeader()
    {
        return workflowHeader.getText();
    }

    public String getWorkflowDescription()
    {
        return workflowDescription.getText();
    }

    public boolean isEditable()
    {
        return itemHeader.find(By.id(EDIT_TRIGGER)).isPresent();
    }

    public boolean isPublishable()
    {
        return itemHeader.find(By.id(PUBLISH_TRIGGER)).isPresent();
    }

    public boolean isNameEditable()
    {
        try
        {
            //open the edit workflow dialog
            itemHeader.find(By.id(EDIT_TRIGGER)).click();
            driver.waitUntilElementIsVisible(By.id(EDIT_WORKFLOW_FORM_ID));

            return driver.elementIsVisible(By.name("newWorkflowName"));
        }
        finally
        {
            //close the dialog
            driver.findElement(By.id("edit-workflow-cancel")).click();
            driver.waitUntilElementIsNotVisible(By.id(EDIT_WORKFLOW_FORM_ID));
        }
    }

    public EditDialog editDialog()
    {
        return pageBinder.bind(EditDialog.class);
    }

    public void publish(boolean createBackup)
    {
        itemHeader.find(By.id(PUBLISH_TRIGGER)).click();
        driver.waitUntilElementIsVisible(By.id(PUBLISH_WORKFLOW_FORM_ID));

        if (createBackup)
        {
            driver.findElement(By.id("publish-workflow-true")).click();
        }
        else
        {
            driver.findElement(By.id("publish-workflow-false")).click();
        }
        driver.findElement(By.id("publish-workflow-submit")).click();
        driver.waitUntilElementIsLocated(By.className("item-header"));
    }

    public static class EditDialog
    {
        @Inject
        private AtlassianWebDriver driver;
        @Inject
        private PageElementFinder elementFinder;

        private PageElement workflowName;
        private PageElement workflowDescription;
        private PageElement submit;

        public EditDialog open()
        {
            //open the edit workflow dialog
            elementFinder.find(By.id(EDIT_TRIGGER)).click();
            driver.waitUntilElementIsVisible(By.id(EDIT_WORKFLOW_FORM_ID));
            isVisible();

            workflowName = elementFinder.find(By.name("newWorkflowName"));
            workflowDescription = elementFinder.find(By.name("description"));
            submit = elementFinder.find(By.id("edit-workflow-submit"));

            return this;
        }

        public EditDialog edit(String newWorkflowName, String newWorkflowDescription)
        {
            if (workflowName.isVisible())
            {
                workflowName.clear().type(newWorkflowName);
            }
            workflowDescription.clear().type(newWorkflowDescription);
            return this;
        }

        public boolean isVisible()
        {
            return driver.elementIsVisible(By.id(EDIT_WORKFLOW_FORM_ID));
        }

        public String submitWithError()
        {
            submit.click();
            driver.waitUntilElementIsVisible(By.className("error"));
            final StringBuilder errorMsg = new StringBuilder();
            List<WebElement> errors = driver.findElements(By.className("error"));
            for (WebElement error : errors)
            {
                errorMsg.append(error.getText());
            }
            return errorMsg.toString();
        }

        public void submit(String newWorkflowName)
        {
            submit.click();
            waitUntil(elementFinder.find(By.id("issue_header_summary")).timed().getText(),
                    containsString(newWorkflowName), by(30000));
        }

    }

}
