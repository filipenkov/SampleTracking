package com.atlassian.jira.pageobjects.pages.admin.issuetype;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.project.ProjectSharedBy;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.SelectElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.webdriver.utils.by.ByJquery;
import org.openqa.selenium.By;
import org.openqa.selenium.RenderedWebElement;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.awt.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents the Edit Issue Type Scheme page.
 *
 * @since v4.4
 */
public class EditIssueTypeSchemePage extends AbstractJiraPage
{

    /**
     * To reorder issue types, the issue type being moved needs to be dropped some distance below the top left of the
     * issue type it is dropped on. This distance can be expressed as a ratio of the height of the issue type it is
     * dropped on. This is that very ratio.
     *
     * Please note: This test is highly dependant on the width of the page. If the two drag boxes wrap then the test
     * breaks on bamboo due to the lower screen resolution
     */
    private static final double DROP_TARGET_RATIO = 0.75;

    private String uri;

    @ElementBy (id = "optionsContainer")
    private PageElement optionsContainer;

    @FindBy (id = "selectedOptions")
    private WebElement selectedOptions;

    @ElementBy (id = "submitSave")
    private PageElement submit;

    @ElementBy (id = "defaultOption_select")
    private SelectElement defaultOption;

    @ElementBy (className = "shared-by")
    private PageElement sharedBy;

    public EditIssueTypeSchemePage()
    {
    }

    public EditIssueTypeSchemePage(final String schemeId, final String projectId)
    {
        checkNotNull(schemeId);
        checkNotNull(projectId);
        this.uri = String.format("/secure/admin/ConfigureOptionSchemes!default.jspa?fieldId=issuetype&schemeId=%s&projectId=%s", schemeId, projectId);
    }


    public EditIssueTypeSchemePage(final Long schemeId)
    {
        checkNotNull(schemeId);
        this.uri = String.format("/secure/admin/ConfigureOptionSchemes!default.jspa?fieldId=issuetype&schemeId=%d", schemeId);
    }

    @Override
    public TimedCondition isAt()
    {
        return optionsContainer.timed().isPresent();
    }

    public String getUrl()
    {
        return uri;
    }

    public EditIssueTypeSchemePage moveWithinSelectedToBelow(final String sourceIssueType, final String targetIssueType)
    {
        RenderedWebElement source = (RenderedWebElement) getIssueTypeListItemFromSelectedOptions(sourceIssueType);
        RenderedWebElement target = (RenderedWebElement) getIssueTypeListItemFromSelectedOptions(targetIssueType);

        final double offset = target.getSize().getHeight() * DROP_TARGET_RATIO;

        dragAndDropWithOffset(source, target, (int) offset);
        return this;
    }

    public EditIssueTypeSchemePage moveWithinSelectedToAbove(final String sourceIssueType, final String targetIssueType)
    {
        RenderedWebElement source = (RenderedWebElement) getIssueTypeListItemFromSelectedOptions(sourceIssueType);
        RenderedWebElement target = (RenderedWebElement) getIssueTypeListItemFromSelectedOptions(targetIssueType);

        final double offset = target.getSize().getHeight() * DROP_TARGET_RATIO;

        dragAndDropWithOffset(source, target, (int) -offset);
        return this;
    }


    public EditIssueTypeSchemePage moveFromAvailableToBelowSelected(final String availableIssueType, final String selectedIssueType)
    {
        RenderedWebElement source = (RenderedWebElement) getIssueTypeListItemFromAvailableOptions(availableIssueType);
        RenderedWebElement target = (RenderedWebElement) getIssueTypeListItemFromSelectedOptions(selectedIssueType);

        final double offset = target.getSize().getHeight() * DROP_TARGET_RATIO;

        dragAndDropWithOffset(source, target, (int) offset);
        return this;
    }

    public EditIssueTypeSchemePage moveFromAvailableToAboveSelected(final String availableIssueType, final String selectedIssueType)
    {
        RenderedWebElement source = (RenderedWebElement) getIssueTypeListItemFromAvailableOptions(availableIssueType);
        RenderedWebElement target = (RenderedWebElement) getIssueTypeListItemFromSelectedOptions(selectedIssueType);

        final double offset = target.getSize().getHeight() * DROP_TARGET_RATIO;

        dragAndDropWithOffset(source, target, (int) -offset);
        return this;
    }

    public EditIssueTypeSchemePage moveFromSelectedToBelowAvailable(final String selectedIssueType, final String availableIssueType)
    {
        RenderedWebElement source = (RenderedWebElement) getIssueTypeListItemFromSelectedOptions(selectedIssueType);
        RenderedWebElement target = (RenderedWebElement) getIssueTypeListItemFromAvailableOptions(availableIssueType);

        final double offset = target.getSize().getHeight() * DROP_TARGET_RATIO;

        dragAndDropWithOffset(source, target, (int) offset);
        return this;
    }

    public EditIssueTypeSchemePage moveFromSelectedToAboveAvailable(final String selectedIssueType, final String availableIssueType)
    {
        RenderedWebElement source = (RenderedWebElement) getIssueTypeListItemFromSelectedOptions(selectedIssueType);
        RenderedWebElement target = (RenderedWebElement) getIssueTypeListItemFromAvailableOptions(availableIssueType);

        final double offset = target.getSize().getHeight() * DROP_TARGET_RATIO;

        dragAndDropWithOffset(source, target, (int) -offset);
        return this;
    }

    private void dragAndDropWithOffset(final RenderedWebElement source, final RenderedWebElement target, final int offset)
    {
        Point currentLocation = source.getLocation();
        Point destination = target.getLocation();

        // We need to ensure we have the source in view. An egrgarious hack to make sure we can do this.
        // Assumes that scrolling to the top of the page will have our source AND target in the viewable area
        ((RenderedWebElement) driver.findElement(By.tagName("body"))).getSize();

        source.dragAndDropBy(destination.x - currentLocation.x + 1, destination.y - currentLocation.y + offset);
    }

    /**
     * Makes an existing issue type the default for this issue type scheme
     * <p/>
     * You will need to call {@link #submitSave()} to commit your changes.
     *
     * @param issueTypeName the name of the issue type to make default
     * @return this page object so we can chain calls
     */
    public EditIssueTypeSchemePage makeDefault(final String issueTypeName)
    {
        defaultOption.select(Options.text(issueTypeName));
        return this;
    }

    /**
     * Commits the current changes. Note that this is currently broken when being accessed from the View Project page,
     * as well as from the Project Configuration plugin. You will need to retry the xsrf operation to handle the save
     * properly.
     */
    public void submitSave()
    {
        submit.click();
    }

    /**
     * Whether we are modifying the default scheme
     *
     * @return true if we are modifying the default scheme
     */
    public boolean isModifyingDefaultScheme()
    {
        return !optionsContainer.find(getAvailableOptionsLocator()).isPresent();
    }

    private WebElement availableOptions()
    {
        return driver.findElement(getAvailableOptionsLocator());
    }

    private static By getAvailableOptionsLocator()
    {
        return By.id("availableOptions");
    }

    private WebElement getIssueTypeListItemFromSelectedOptions(String sourceIssueType)
    {
        return selectedOptions
                .findElement(ByJquery.$("li:contains(\"" + sourceIssueType + "\")"));
    }

    private WebElement getIssueTypeListItemFromAvailableOptions(String availableIssueType)
    {
        return availableOptions()
                .findElement(ByJquery.$("li:contains(\"" + availableIssueType + "\")"));
    }

    public ProjectSharedBy getSharedBy()
    {
        return pageBinder.bind(ProjectSharedBy.class, sharedBy);
    }
}
