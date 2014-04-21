package com.atlassian.jira.pageobjects.project.versions;

import com.atlassian.jira.pageobjects.components.MultiSelect;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.SelectElement;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.List;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Dialog for merging versions.
 *
 * @since v4.4
 */
public class MergeDialog
{
    @ElementBy (id = "versionsMergeDialog", timeoutType = TimeoutType.DIALOG_LOAD)
    private PageElement mergeDialog;

    @ElementBy (id = "project-config-versions-merge-form-no-versions")
    private PageElement noVersions;

    @ElementBy (id = "project-config-version-merge-form-submit")
    private PageElement submit;

    private MultiSelect idsToMerge;

    @ElementBy (name = "idMergeTo")
    private SelectElement idMergeTo;

    private PageElement loading;

    @Inject
    private PageElementFinder elementFinder;

    @Inject
    private PageBinder pageBinder;

    @Init
    public void initialize()
    {
        idsToMerge = elementFinder.find(By.id("idsToMerge-multi-select")).isPresent() ?
            pageBinder.bind(MultiSelect.class, "idsToMerge") : null;

        //The MergeDialog adds this while the submit is occuring. It is removed after successful submit or error.
        loading = elementFinder.find(By.cssSelector(".throbber.loading"), TimeoutType.DIALOG_LOAD);
    }

    @WaitUntil
    public void ready()
    {
        waitUntilTrue(mergeDialog.timed().hasClass("aui-dialog-content-ready"));
    }

    public boolean hasWarning()
    {
        return mergeDialog.find(getWarningSelector()).isPresent();
    }

    public String getWarningText()
    {
        return mergeDialog.find(getWarningSelector()).getText();
    }

    public boolean hasNoVersions()
    {
        return noVersions.isPresent();
    }

    public String getNoVersionsText()
    {
        return noVersions.getText();
    }

    public MergeDialog merge(final String targetVersion, final String... sourceVersions)
    {
        idMergeTo.select(Options.text(targetVersion));

        for (final String sourceVersion : sourceVersions)
        {
            idsToMerge.add(sourceVersion);
        }

        return this;
    }

    public void submit()
    {
        submit.click();
        waitUntilFalse(loading.timed().isPresent());
    }

    public List<String> getErrorMessages()
    {
        final List<String> errorMessages = Lists.newArrayList();
        final List<PageElement> errors = mergeDialog.findAll(By.className("error-list-item"));
        for (final PageElement error : errors)
        {
            errorMessages.add(error.getText());
        }
        return errorMessages;
    }

    public boolean hasErrorMessages()
    {
        return mergeDialog.find(getErrorSelector()).isPresent();
    }

    private static By getWarningSelector()
    {
        return By.cssSelector(".aui-message.warning");
    }

    private static By getErrorSelector()
    {
        return By.cssSelector(".aui-message.error");
    }
}
