package com.atlassian.jira.pageobjects.project.versions.operations;

import com.atlassian.jira.pageobjects.navigator.IssueNavigatorResults;
import com.atlassian.jira.pageobjects.project.versions.VersionPageTab;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.pageobjects.elements.query.Poller.by;
import static org.hamcrest.Matchers.is;

/**
 * @since v4.4
 */
public class DeleteOperation
{
    private static String DIALOG_SELECTOR = "#version-%s-delete-dialog.aui-dialog-content-ready";
    private final String versionId;
    private String dialogSelector;

    protected PageElement dialog;
    protected PageElement swapAffectVersion;
    protected PageElement swapAffectVersionSelect;
    protected PageElement removeAffectVersion;
    protected PageElement affectsCount;
    protected PageElement swapFixVersion;
    protected PageElement swapFixVersionSelect;
    protected PageElement removeFixVersion;
    protected PageElement fixCount;
    protected PageElement submitButton;
    protected PageElement infoMessage;
    private String projectKey;


    @Inject
    private PageBinder binder;

    @Inject
    private AtlassianWebDriver driver;

    @Inject
    private PageElementFinder elementFinder;

    public DeleteOperation(final String versionId)
    {
        this.versionId = versionId;
        dialogSelector = String.format(DIALOG_SELECTOR, versionId);
    }

    @WaitUntil
    public void dialogOpen()
    {
        elementFinder.find(By.cssSelector(dialogSelector)).isPresent();
    }

    @Init
    public void getElements()
    {
        dialog = elementFinder.find(By.cssSelector(dialogSelector));
        swapAffectVersion = dialog.find(By.id("affects-swap"));
        swapAffectVersionSelect = dialog.find(By.name("moveAffectedIssuesTo"));
        removeAffectVersion = dialog.find(By.id("affects-remove"));
        affectsCount = dialog.find(By.id("affects-count"));
        swapFixVersion = dialog.find(By.id("fix-swap"));
        swapFixVersionSelect = dialog.find(By.name("moveFixIssuesTo"));
        removeFixVersion = dialog.find(By.id("fix-remove"));
        fixCount = dialog.find(By.id("fix-count"));
        submitButton = dialog.find(By.id("submit"));
        infoMessage = dialog.find(By.cssSelector(".aui-message.info"));
        projectKey = elementFinder.find(By.name("projectKey")).getAttribute("content");
    }

    public Map<String, Boolean> getOperations()
    {
        Map<String, Boolean> operationMap = new HashMap<String, Boolean>();

        operationMap.put("swapAffectVersion", swapAffectVersion.isPresent());
        operationMap.put("removeAffectVersion", removeAffectVersion.isPresent());
        operationMap.put("swapFixVersion", swapFixVersion.isPresent());
        operationMap.put("removeFixVersion", removeFixVersion.isPresent());

        return operationMap;
    }

    public int getAffectsCount()
    {
        return Integer.parseInt(affectsCount.getText());
    }

    public int getFixCount()
    {
        return Integer.parseInt(fixCount.getText());
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


    public DeleteOperation setFixToSwapVersion(final String versionName)
    {
        selectOption(swapFixVersionSelect, versionName);
        return this;
    }

    // Flakey
    public int getAffectsIssuesCountInNavigator() throws InterruptedException
    {

        final String mainWindowId = driver.getWindowHandle();

        affectsCount.click();

        for (String handle : driver.getWindowHandles())
        {
            if (!handle.equals(mainWindowId))
            {
                driver.switchTo().window(handle);
            }
        }

        final int resultCount = binder.bind(IssueNavigatorResults.class).getTotalCount();

        driver.close();

        driver.switchTo().window(mainWindowId);
        
        return resultCount;
    }

    // Flakey
    public int getFixIssuesCountInNavigator() throws InterruptedException
    {

        final String mainWindowId = driver.getWindowHandle();

        fixCount.click();



        for (String handle : driver.getWindowHandles())
        {
            if (!handle.equals(mainWindowId))
            {
                driver.switchTo().window(handle);
            }
        }

        final int resultCount = binder.bind(IssueNavigatorResults.class).getTotalCount();

        driver.close();

        driver.switchTo().window(mainWindowId);

        return resultCount;
    }

    public DeleteOperation setFixToRemoveVersion()
    {
        removeFixVersion.click();
        return this;
    }

    public DeleteOperation setAffectsToSwapVersion(final String versionName)
    {
        selectOption(swapAffectVersionSelect, versionName);
        return this;
    }

    public DeleteOperation setAffectsToRemoveVersion()
    {
        removeAffectVersion.click();
        return this;
    }

    public VersionPageTab submit()
    {
        submitButton.click();
        Conditions.not(dialog.timed().isPresent());
        Poller.waitUntil(dialog.timed().isPresent(), is(false), by(5000));
        return binder.bind(VersionPageTab.class, projectKey);
    }

    private DeleteOperation selectOption(final PageElement select, final String optionText)
    {
        List<PageElement> options = select.findAll(By.tagName("option"));

        for (PageElement option : options)
        {
            if (option.getText().equals(optionText))
            {
                option.select();
                break;
            }
        }

        return this;
    }
}
