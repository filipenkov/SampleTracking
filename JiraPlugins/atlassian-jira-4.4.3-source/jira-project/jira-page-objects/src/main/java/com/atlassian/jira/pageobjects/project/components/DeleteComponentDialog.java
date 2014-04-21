package com.atlassian.jira.pageobjects.project.components;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Conditions;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.List;

import static com.atlassian.pageobjects.elements.query.Poller.by;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntil;
import static org.hamcrest.Matchers.is;

/**
 * @since v4.4
 */
public class DeleteComponentDialog
{
    private static String DIALOG_SELECTOR = "#component-%s-delete-dialog.aui-dialog-content-ready";
    private final String id;
    private String dialogSelector;
    private String projectKey;
    private PageElement dialog;
    private PageElement swapComponentRadio;
    private PageElement swapComponentSelect;
    private PageElement removeComponentRadio;
    private PageElement submitButton;
    private PageElement issueCount;
    private PageElement infoMessage;

    @Inject
    private PageBinder pageBinder;

    @Inject
    private PageElementFinder elementFinder;

    public DeleteComponentDialog(final String id)
    {
        this.id = id;
        dialogSelector = String.format(DIALOG_SELECTOR, id);
    }

    @WaitUntil
    public void waitForDialog()
    {
         elementFinder.find(By.cssSelector(dialogSelector)).isPresent();
    }

    @Init
    public void getElements()
    {
        dialog = elementFinder.find(By.cssSelector(dialogSelector));
        swapComponentRadio = dialog.find(By.id("component-swap"));
        swapComponentSelect = dialog.find(By.name("moveIssuesTo"));
        removeComponentRadio = dialog.find(By.id("component-remove"));
        submitButton = dialog.find(By.id("submit"));
        projectKey = elementFinder.find(By.name("projectKey")).getAttribute("content");
        issueCount = dialog.find(By.id("issue-count"));
        infoMessage = dialog.find(By.cssSelector(".aui-message.info"));
    }

    public String getInfoMessage()
    {
        if (infoMessage.isPresent())
        {
            return infoMessage.getText();
        }
        else
        {
            return null;
        }
    }

    public Boolean hasComponentSwapOperation()
    {
        return swapComponentRadio.isPresent();
    }

    public Boolean hasComponentRemoveOperation()
    {
        return removeComponentRadio.isPresent();
    }

    public DeleteComponentDialog setSwapComponent(final String name)
    {
        List<PageElement> options = swapComponentSelect.findAll(By.tagName("option"));

        for (PageElement option : options)
        {
            if (option.getText().equals(name))
            {
                option.select();
                break;
            }
        }

        return this;
    }

    public DeleteComponentDialog setRemoveComponent()
    {
        removeComponentRadio.click();
        return this;
    }

    public int getIssuesInComponentCount()
    {
        return Integer.parseInt(issueCount.getText());
    }

    public ComponentsPageTab submit()
    {
        submitButton.click();
        Conditions.not(dialog.timed().isPresent());
        waitUntil(dialog.timed().isPresent(), is(false), by(5000));
        return pageBinder.bind(ComponentsPageTab.class, projectKey);
    }

}
