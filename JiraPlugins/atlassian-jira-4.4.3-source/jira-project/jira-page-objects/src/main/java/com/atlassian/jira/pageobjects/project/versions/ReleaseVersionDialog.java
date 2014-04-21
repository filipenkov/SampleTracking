package com.atlassian.jira.pageobjects.project.versions;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.SelectElement;
import com.atlassian.pageobjects.elements.WebDriverSelectElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.atlassian.webdriver.utils.by.ByJquery;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.by;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static org.hamcrest.Matchers.is;

/**
 * Dialog for releasing a version
 *
 * @since v4.4
 */
public class ReleaseVersionDialog
{
    @Inject
    private AtlassianWebDriver driver;

    @Inject
    PageBinder pageBinder;

    private PageElement releaseDate;

    private PageElement submit;

    private PageElement ignore;
    private PageElement ignoreLabel;

    private PageElement move;
    private PageElement moveLabel;

    private PageElement unresolvedMessage;
    private PageElement unresolvedIssuesLink;

    private PageElement releaseDateError;

    private SelectElement unfixedIssuesVersion;

    @ElementBy(cssSelector = "#version-release-dialog.aui-dialog-content-ready", timeoutType = TimeoutType.DIALOG_LOAD)
    private PageElement dialog;

    @Inject
    private Timeouts timeouts;
    @Inject
    private PageElementFinder elementFinder;

    @Init
    public void initialize()
    {
        waitUntilTrue(dialog.timed().isPresent());
        releaseDate = dialog.find(By.id("project-config-version-release-form-release-date-field"));
        submit = dialog.find(By.id("project-config-version-release-form-submit"));
        ignore = dialog.find(By.id("unresolved-ignore"));
        ignoreLabel = dialog.find(By.id("unresolved-ignore-label"));
        move = dialog.find(By.id("unresolved-move"));
        moveLabel = dialog.find(By.id("unresolved-move-label"));
        unresolvedMessage = dialog.find(By.id("unresolved-message"));
        unresolvedIssuesLink = dialog.find(By.id("unresolved-issues-link"));
        releaseDateError = dialog.find(By.id("project-config-versions-release-form-release-date-error"));
        unfixedIssuesVersion = pageBinder.bind(WebDriverSelectElement.class, By.name("moveUnfixedIssuesTo"));
    }

    public ReleaseVersionDialog setReleaseDate(final String date)
    {
        releaseDate.type(date);
        return this;
    }

    public boolean hasUnresolvedIssues()
    {
        return unresolvedIssuesLink.isPresent();
    }

    public String unresolvedIssueCountText()
    {
        return unresolvedIssuesLink.getText();
    }

    public String unresolvedIssueLinkUrl()
    {
        return unresolvedIssuesLink.getAttribute("href");
    }

    public ReleaseVersionDialog ignoreUnresolvedIssues()
    {
        ignore.select();
        return this;
    }

    public ReleaseVersionDialog moveUnresolvedIssues(final String version)
    {
        move.select();
        unfixedIssuesVersion.select(Options.text(version));
        return this;
    }

    public void submit()
    {
        submit.click();
        PageElement loading = elementFinder.find(By.cssSelector("#version-release-dialog.aui-dialog-content-ready .loading"));
        Poller.waitUntil(loading.timed().isPresent(), is(false), by(8000));
    }

    public TimedQuery<Boolean> isClosed()
    {
        return Conditions.not(dialog.timed().isPresent());
    }

    private static By getDialogLocator()
    {
        return By.className("aui-dialog-content-ready");
    }

    public boolean hasMoveOption()
    {
        return move.isPresent();
    }

    public boolean hasIgnoreOption()
    {
        return ignore.isPresent();
    }

    public boolean hasUnresolvedMessage()
    {
        return unresolvedMessage.isPresent();
    }

    public String getUnresolvedMessage()
    {
        return unresolvedMessage.getText();
    }

    public String getIgnoreOptionLabelText()
    {
        return ignoreLabel.getText();
    }

    public boolean hasReleaseDateErrorMessage()
    {
        return releaseDateError.isPresent();
    }
}
