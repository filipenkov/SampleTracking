package com.atlassian.jira.pageobjects.dialogs.quickedit;

import com.atlassian.jira.pageobjects.components.fields.AssigneeField;
import com.atlassian.jira.pageobjects.components.fields.SingleSelect;
import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.pageobjects.util.TraceContext;
import com.atlassian.jira.pageobjects.util.Tracer;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.SelectElement;
import com.google.inject.Inject;
import org.openqa.selenium.By;

import java.util.List;
import java.util.Map;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;

/**
 * @since v5.0
 */
public class EditIssueDialog extends AbstractIssueDialog
{

    @Inject
    PageBinder pageBinder;

    @Inject
    private PageElementFinder pageElementFinder;

    @Inject
    private TraceContext traceContext;

    public EditIssueDialog()
    {
        super("edit-issue-dialog");
    }



    @Override
    public EditIssueDialog switchToCustomMode()
    {
        openFieldPicker().switchToCustomMode();
        return this;
    }

    @Override
    public EditIssueDialog removeFields(String... fields)
    {
        openFieldPicker().removeFields(fields);
        return this;
    }

    @Override
    public EditIssueDialog addFields(String... fields)
    {
        openFieldPicker().addFields(fields);
        return this;
    }

    @Override
    public EditIssueDialog switchToFullMode()
    {
        openFieldPicker().switchToFullMode();
        return this;
    }

    @Override
    public EditIssueDialog fill(String id, String value)
    {
        FormDialog.setElement(find(By.id(id)), value);
        return this;
    }

    public <P> P submit(Class<P> pageClass, Object... args)
    {
        this.submit(By.id("edit-issue-submit"));
        return binder.bind(pageClass, args);
    }

    public boolean submit()
    {
        return super.submit(By.id("edit-issue-submit"));
    }

    public ViewIssuePage submitExpectingViewIssue(String issueKey)
    {
        Tracer tracer = traceContext.checkpoint();
        ViewIssuePage viewIssuePage = submit(ViewIssuePage.class, issueKey);
        return viewIssuePage.waitForAjaxRefresh(tracer);
    }

    @Override
    protected void waitWhileSubmitting()
    {
        PageElement loading = locator.find(By.cssSelector("#edit-issue-dialog .submitting"));
        waitUntilFalse(loading.timed().isPresent());
    }
    
    public EditIssueDialog setAssignee(String newAssignee)
    {
        AssigneeField assigneeField = pageBinder.bind(AssigneeField.class);
        assigneeField.setAssignee(newAssignee);
        return this;
    }
    
    public EditIssueDialog setPriority(String newPriority)
    {
        pageElementFinder.find(By.id("priority-field")).clear().type(newPriority);
        if (pageElementFinder.find(By.cssSelector(".ajs-layer.active")).find(By.cssSelector("li.active")).isVisible())
        {
            pageElementFinder.find(By.cssSelector(".ajs-layer.active")).find(By.cssSelector("li.active")).click();
        }
        return this;
    }
    
    public EditIssueDialog setIssueType(String issueType)
    {
        pageElementFinder.find(By.id("issuetype-field")).clear().type(issueType);
        if (pageElementFinder.find(By.cssSelector(".ajs-layer.active")).find(By.cssSelector("li.active")).isVisible())
        {
            pageElementFinder.find(By.cssSelector(".ajs-layer.active")).find(By.cssSelector("li.active")).click();
        }
        return this;
    }

    public EditIssueDialog setOriginalEstimate(String originalEstimate)
    {
        return fill("timetracking_originalestimate", originalEstimate);
    }

    public EditIssueDialog setTimeSpent(String timeSpent)
    {
        return fill("log-work-time-logged", timeSpent);
    }

    public void setFields(Map<String, String> fields)
    {
        for (String field : fields.keySet())
        {
            fill(field, fields.get(field));
        }
    }

}
