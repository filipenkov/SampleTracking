package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraAdminPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;


/**
 * Admin page for viewing workflows.
 *
 * @since v4.4
 */
public class WorkflowsPage extends AbstractJiraAdminPage
{

    @ElementBy (id = "workflows_table")
    private PageElement workflowsTable;

    @Override
    public String getUrl()
    {
        return "secure/admin/workflows/ListWorkflows.jspa";
    }

    @Override
    public TimedCondition isAt()
    {
        return workflowsTable.timed().isPresent();
    }

    @Override
    public String linkId()
    {
        return "workflows_section";
    }


    public WorkflowDesignerPage openDesigner(final String workflowName)
    {
        return openDesigner(workflowName, false);
    }

    public WorkflowDesignerPage openDesigner(final String workflowName, final boolean isDraft)
    {
        return pageBinder.navigateToAndBind(WorkflowDesignerPage.class, workflowName, isDraft);
    }

    public List<Workflow> getWorkflows()
    {
        final List<Workflow> ret = new ArrayList<Workflow>();
        List<PageElement> rows = workflowsTable.findAll(By.tagName("tr"));
        for (PageElement row : rows)
        {
            List<PageElement> columns = row.findAll(By.tagName("td"));
            //skip the header row
            if(columns.size() != 0)
            {
                ret.add(new Workflow(columns));
            }
        }
        return ret;
    }

    public static class Workflow
    {
        private final String name;
        private final String description;
        private final WorkflowStatus status;
        private final int numberOfSteps;

        public Workflow(List<PageElement> columns)
        {
            if (columns == null || columns.size() < 6)
            {
                throw new IllegalArgumentException("Not valid workflow row");
            }
            name = columns.get(0).find(By.className("boldTop")).getText();
            description = columns.get(1).getText();
            status = WorkflowStatus.fromString(columns.get(2).getText());
            numberOfSteps = Integer.parseInt(columns.get(4).getText());
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        public WorkflowStatus getStatus()
        {
            return status;
        }

        public int getNumberOfSteps()
        {
            return numberOfSteps;
        }
    }

    public enum WorkflowStatus
    {
        INACTIVE, ACTIVE, DRAFT;

        public static WorkflowStatus fromString(String value)
        {
            if (value.equalsIgnoreCase("Inactive"))
            {
                return INACTIVE;
            }
            else if (value.equalsIgnoreCase("Active"))
            {
                return ACTIVE;
            }
            else if (value.equalsIgnoreCase("Draft"))
            {
                return DRAFT;
            }
            return null;
        }
    }
}
