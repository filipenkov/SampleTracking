package com.atlassian.jira.pageobjects.dialogs.quickedit;

import com.atlassian.jira.pageobjects.components.fields.SingleSelect;
import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.jira.pageobjects.framework.fields.CustomField;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.SelectElement;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;

/**
 * Quick Create Issue Dialog
 *
 * @since v5.0
 */
public class CreateIssueDialog extends AbstractIssueDialog
{

    public enum Type
    {
        SUBTASK,
        ISSUE
    }

    @Inject
    protected AtlassianWebDriver driver;

    @Inject
    protected PageElementFinder pageElementFinder;
    
    @ElementBy (className = "qf-field-project")
    protected PageElement projectSingleSelectEl;

    @ElementBy (className = "qf-field-issuetype")
    protected PageElement issueTypeSingleSelectEl;


    protected SingleSelect issueTypeSingleSelect;
    protected SingleSelect projectSingleSelect;

    @Init
    protected void createControls()
    {
        // subtask dialog doesn't have one
        if (projectSingleSelectEl.isPresent())
        {
            projectSingleSelect = binder.bind(SingleSelect.class, projectSingleSelectEl);
        }

        issueTypeSingleSelect = binder.bind(SingleSelect.class, issueTypeSingleSelectEl);
    }

    @WaitUntil
    private void issueTypeSelectsReady()
    {
        issueTypeSingleSelectEl.timed().isPresent();
    }

    public CreateIssueDialog(final Type type)
    {
        super(type == Type.SUBTASK ? "create-subtask-dialog" : "create-issue-dialog");
    }

    @Override
    public CreateIssueDialog switchToCustomMode()
    {
        openFieldPicker().switchToCustomMode();
        waitWhileSubmitting();
        return this;
    }

    @Override
    public CreateIssueDialog removeFields(String... fields)
    {
        openFieldPicker().removeFields(fields);
        return this;
    }

    @Override
    public CreateIssueDialog addFields(String... fields)
    {
        openFieldPicker().addFields(fields);
        return this;
    }

    @Override
    public CreateIssueDialog switchToFullMode()
    {
        openFieldPicker().switchToFullMode();
        waitWhileSubmitting();
        return this;
    }

    @Override
    public CreateIssueDialog fill(String id, String value)
    {
        FormDialog.setElement(find(By.id(id)), value);
        return this;
    }

    public CreateIssueDialog setPriority(String priority)
    {
        pageElementFinder.find(By.id("priority-field")).clear().type(priority);
        if (pageElementFinder.find(By.cssSelector(".ajs-layer.active")).find(By.cssSelector("li.active")).isVisible())
        {
            pageElementFinder.find(By.cssSelector(".ajs-layer.active")).find(By.cssSelector("li.active")).click();
        }
        return this;
    }
    
    public CreateIssueDialog selectProject(String name)
    {
        projectSingleSelect.select(name);
        issueTypeSingleSelectEl.timed().hasAttribute("data-project", name);
        waitWhileSubmitting();
        return this;
    }

    public CreateIssueDialog selectIssueType(String name)
    {
        issueTypeSingleSelect.select(name);
        waitWhileSubmitting();
        // Dialog might need to redraw
        waitUntilFalse(getDialogElement().find(By.className("loading")).timed().isVisible());
        return this;
    }

    public CreateIssueDialog checkCreateMultiple()
    {
        final PageElement createAnother = find(By.id("qf-create-another"));

        final String checked = createAnother.getAttribute("checked");
        if (checked == null || !checked.equals("true"))
        {
            createAnother.click();
        }

        return this;
    }

    public CreateIssueDialog uncheckCreateMultiple()
    {
        final PageElement createAnother = find(By.id("qf-create-another"));
        final String checked = createAnother.getAttribute("checked");

        if (checked != null && createAnother.getAttribute("checked").equals("true"))
        {
            createAnother.click();
        }

        return this;
    }

    public <T extends CustomField> T getCustomField(String customFieldId, Class<T> customFieldType)
    {
        return binder.bind(customFieldType, form, customFieldId);
    }

    public PageElement getCustomFieldElement(String customFieldId)
    {
        return form.find(By.id(customFieldId));
    }

    public String getProject()
    {
       return projectSingleSelect.getValue();
    }

    public String getIssueType()
    {
        return issueTypeSingleSelect.getValue();
    }

    /**
     * Accept a dirty form warning in an alert (if there is one)
     *
     * @return false if no dirty form warning was shown, true otherwise
     */
    public boolean acceptDirtyFormWarning()
    {
        try
        {
            final Alert alert = driver.switchTo().alert();
            final String alertText = alert.getText();
            if (!alertText.equals("false"))
            {
                //looks like we got an alert. Lets accept it and return true if it was a dirty forms alert.
                alert.accept();
                return alertText.contains("changes will be lost");
            }
            //didn't get an alert at all. Boo!
            return false;
        }
        catch (NoAlertPresentException e)
        {
            return false;
        }
        finally
        {
            driver.switchTo().defaultContent();
        }
    }

    public List<String> getIssueTypes()
    {
        List<String> issueTypes = new ArrayList<String>();
        final List<PageElement> options = getDialogElement().findAll(By.cssSelector("#issuetype option"));
        for (PageElement option : options)
        {
            if (option.getValue() != null && !option.getValue().isEmpty())
            {
                issueTypes.add(StringUtils.trim(option.getAttribute("innerHTML")));
            }
        }
        return issueTypes;
    }

    public <P> P submit(Class<P> pageClass, Object... args)
    {
        this.submit(By.id("create-issue-submit"));
        return binder.bind(pageClass, args);
    }
}
