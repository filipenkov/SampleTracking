package com.atlassian.jira.pageobjects.project.versions;

import com.atlassian.jira.pageobjects.project.versions.operations.VersionOperationDropdown;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.atlassian.webdriver.utils.by.ByJquery;
import org.openqa.selenium.By;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.by;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntil;
import static org.hamcrest.Matchers.is;

/**
 * @since v4.4
 */
public class DefaultVersion implements Version
{
    private final String id;
    private String rowId;
    private PageElement version;
    private PageElement nameCell;
    private PageElement descriptionCell;
    private PageElement releaseDateCell;
    private PageElement operationsCell;

    @Inject
    private AtlassianWebDriver driver;

    @Inject
    private PageBinder pageBinder;

    @Inject
    private Timeouts timeouts;

    @Inject
    private PageElementFinder elementFinder;

    public DefaultVersion(final String id)
    {
        this.id = id;
        this.rowId = "version-" + id + "-row";
    }

    @Init
    public void initialise()
    {
        version = elementFinder.find(By.id(this.rowId), TimeoutType.AJAX_ACTION);
        nameCell = version.find(By.className("project-config-version-name"));
        descriptionCell = version.find(By.className("project-config-version-description"));
        releaseDateCell = version.find(By.className("project-config-version-release-date"));
        operationsCell = version.find(By.className("project-config-operations"));
    }

    @Override
    public String getName()
    {
        return nameCell.getText();
    }

    @Override
    public String getDescription()
    {
        return descriptionCell.getText();
    }

    @Override
    public Date getReleaseDate()
    {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MMM/yy");
        try
        {
            return dateFormat.parse(releaseDateCell.getText());
        }
        catch (ParseException e)
        {
            throw new RuntimeException("Release date no longer in expected format.", e);
        }
    }

    public EditVersionForm edit(final String fieldName)
    {
        version.find(ByJquery.$(".aui-restfultable-editable[data-field-name=" + fieldName + "]")).click();
        return pageBinder.bind(EditVersionForm.class, By.id(version.getAttribute("id")));
    }

    @Override
    public boolean isOverdue()
    {
        return version.hasClass("project-config-version-overdue");
    }

    @Override
    public boolean isArchived()
    {
        return version.hasClass("project-config-version-archived");
    }

    @Override
    public boolean isReleased()
    {
        return version.hasClass("project-config-version-released");
    }

    public VersionOperationDropdown openOperationsCog()
    {
        // Egregarious hack, since the mouseover hack in the JavascriptUtils doesn't work right
        driver.executeScript("jQuery('#" + version.getAttribute("id") + "').addClass('aui-restfultable-active')");

        PageElement operationsCog = version.find(getOperationsCogLocator());
        
        operationsCog.javascript().mouse().mouseover();

        waitUntil("Tried to open the version operations cog, but couldn't find the trigger on the page",
                operationsCog.timed().isVisible(), is(true), by(timeouts.timeoutFor(TimeoutType.AJAX_ACTION)));

        operationsCog.click();

        waitUntil("Tried to open the version operations cog, but couldn't trigger the menu to be active",
                operationsCog.timed().hasClass("active"), is(true), by(timeouts.timeoutFor(TimeoutType.AJAX_ACTION)));


        return pageBinder.bind(VersionOperationDropdown.class, id);
    }

    public TimedQuery<Boolean> hasFinishedVersionOperation()
    {
        return Conditions.not(version.timed().hasClass("loading"));
    }

    private static By getOperationsCogLocator()
    {
        return By.className("project-config-operations-trigger");
    }

}
