package com.atlassian.jira.pageobjects.project.versions;

import com.atlassian.jira.pageobjects.project.AbstractProjectConfigPageTab;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import org.openqa.selenium.By;
import org.openqa.selenium.RenderedWebElement;
import org.openqa.selenium.WebElement;

import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;

/**
 * @since v4.4
 */
public class VersionPageTab extends AbstractProjectConfigPageTab
{
    public static final String TAB_LINK_ID = "view_project_versions_tab";

    private static final String URI_TEMPLATE = "/plugins/servlet/project-config/%s/versions";

    private final String uri;
    private EditVersionForm createVersionForm;

    @Inject
    private PageElementFinder elementFinder;

    @Inject
    private Timeouts timeouts;

    @ElementBy (id = "project-config-panel-versions")
    private PageElement versionsPage;

    @ElementBy (id= "project-config-operations-merge")
    private PageElement mergeLink;

    public VersionPageTab(final String projectKey)
    {
        this.uri = String.format(URI_TEMPLATE, projectKey);
    }

    @Init
    public void initialise()
    {
        createVersionForm = pageBinder.bind(EditVersionForm.class, By.className("project-config-versions-add-fields"));
    }

    @Override
    public TimedCondition isAt()
    {
        return versionsPage.timed().isPresent();
    }

    public EditVersionForm getEditVersionForm()
    {
        return createVersionForm;
    }

    public PageElement getMergeLink() {
        return mergeLink;
    }

    public VersionPageTab closeServerErrorDialog()
    {
        elementFinder.find(By.id("server-error-dialog"))
                .find(By.className("cancel"))
                .click();

        return this;
    }

    public String getServerError()
    {
        return elementFinder.find(By.cssSelector("#server-error-dialog.aui-dialog-content-ready"))
                .find(By.className("aui-message"))
                .getText();
    }

    public Version getVersionByName(final String name)
    {
        final List<Version> versions = getVersions();
        for (final Version version : versions)
        {
            if(version.getName().equals(name))
            {
                return version;
            }
        }
        return null;
    }

    public List<Version> getVersions()
    {

        List<Version> versions = new ArrayList<Version>();

        if(versionsPage.find(By.className("jira-restfultable-no-entires")).timed()
                .isPresent().by(timeouts.timeoutFor(TimeoutType.DIALOG_LOAD)))
        {
            return versions;
        }

        final List<PageElement> versionElements = versionsPage.findAll(By.className("project-config-version"));

        for (PageElement versionElement : versionElements)
        {
            versions.add(pageBinder.bind(DefaultVersion.class, versionElement.getAttribute("data-id")));
        }

        return versions;
    }

    @Override
    public String getUrl()
    {
        return uri;
    }

    public void moveVersionAbove(final Version source, final Version target)
    {
        final RenderedWebElement sourceDragHandle = getDragHandle(source);
        final RenderedWebElement targetDragHandle = getDragHandle(target);

        // Note: Calculating heightOffset scrolls the window for some stupid reason.
        // The following is a sneaky hack to make sure WebDriver scrolls the window such that
        // both sourceDragHandle and targetDragHandle are contained within the viewport,
        // otherwise dragAndDropBy() won't have on-screen coords to drag and drop to.
        ((RenderedWebElement)driver.findElement(By.tagName("body"))).getSize();

        sourceDragHandle.dragAndDropOn(targetDragHandle);
    }

    public void moveVersionBelow(final Version source, final Version target)
    {
        final RenderedWebElement sourceDragHandle = getDragHandle(source);
        final RenderedWebElement targetDragHandle = getDragHandle(target);

        final RenderedWebElement targetRowElement = (RenderedWebElement)driver.findElement(By.id(getVersionRow(target.getName()).getAttribute("id")));
        int heightOffset = (int) (targetRowElement.getSize().getHeight() / 2.0);

        // Note: Calculating heightOffset scrolls the window for some stupid reason.
        // The following is a sneaky hack to make sure WebDriver scrolls the window such that
        // both sourceDragHandle and targetDragHandle are contained within the viewport,
        // otherwise dragAndDropBy() won't have on-screen coords to drag and drop to.
        ((RenderedWebElement)driver.findElement(By.tagName("body"))).getSize();

        Point sourceLocation = sourceDragHandle.getLocation();
        Point targetLocation = targetDragHandle.getLocation();

        sourceDragHandle.dragAndDropBy(0, targetLocation.y - sourceLocation.y + heightOffset);
    }

    private RenderedWebElement getDragHandle(Version source)
    {
        final PageElement sourceVersionRow = getVersionRow(source.getName());
        final WebElement sourceVersionRowWebElement = driver.findElement(By.id(sourceVersionRow.getAttribute("id")));
        return (RenderedWebElement) sourceVersionRowWebElement.findElement(By.className("jira-restfultable-draghandle"));
    }

    private PageElement getVersionRow(final String name)
    {
        final List<PageElement> versionElements = versionsPage.findAll(By.className("project-config-version"));

        for (PageElement versionElement : versionElements)
        {
            final DefaultVersion version = pageBinder.bind(DefaultVersion.class, versionElement.getAttribute("data-id"));
            if(version.getName().equals(name))
            {
                return versionElement;
            }
        }

        return null;

    }

    public MergeDialog openMergeDialog()
    {
        waitUntilFalse(isVersionsLoading());
        mergeLink.click();
        return pageBinder.bind(MergeDialog.class);
    }

    public TimedQuery<Boolean> isVersionsLoading()
    {
        return getVersionstable().timed().hasClass("loading");
    }

    private PageElement getVersionstable()
    {
        return elementFinder.find(By.id("project-config-versions-table"));
    }
}
