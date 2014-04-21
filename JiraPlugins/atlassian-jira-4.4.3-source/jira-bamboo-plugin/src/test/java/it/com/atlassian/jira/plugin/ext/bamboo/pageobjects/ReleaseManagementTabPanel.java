package it.com.atlassian.jira.plugin.ext.bamboo.pageobjects;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.google.common.collect.Iterables;
import org.junit.Assert;
import org.openqa.selenium.By;

import java.util.concurrent.TimeUnit;

public class ReleaseManagementTabPanel extends AbstractJiraPage
{
    private static final String URI = "/browse/%s/fixforversion/%s?selectedTab=com.atlassian.jira.plugin.ext.bamboo:bamboo-release-tabpanel";

    private BrowseVersionSummaryPage browseVersionSummaryPage;

    public ReleaseManagementTabPanel()
    {
    }

    public ReleaseManagementTabPanel(final BrowseVersionSummaryPage browseVersionSummaryPage)
    {
        this.browseVersionSummaryPage = browseVersionSummaryPage;
    }

    public String getUrl()
    {
        final BrowsableVersion version = browseVersionSummaryPage.getBrowsableVersion();
        return String.format(URI, version.getProjectKey(), version.getId());
    }

    public BambooRelease executeNewRelease(String planKey)
    {
        elementFinder.find(By.id("runRelease"), TimeoutType.DIALOG_LOAD).click();

        PageElement releaseForm = elementFinder.find(By.id("release-form"));
        releaseForm.find(By.id("new-build")).click();
        final PageElement planSelectList = releaseForm.find(By.id("bamboo-plan"), TimeoutType.AJAX_ACTION);

        boolean selectedPlanKey = false;
        for (PageElement element : planSelectList.findAll(By.tagName("option"), TimeoutType.AJAX_ACTION))
        {
            if (element.getValue().equals(planKey))
            {
                element.select();
                selectedPlanKey = true;
                break;
            }
        }

        if (!selectedPlanKey)
        {
            throw new IllegalStateException("Could not find '" + planKey + "' in select list " + planSelectList.getAttribute("id"));
        }

        final PageElement bambooPlanStages = elementFinder.find(By.id("bamboo-plan-stages"));
        for (PageElement stageCheckbox : bambooPlanStages.findAll(By.tagName("input")))
        {
            if (stageCheckbox.isVisible())
            {
                stageCheckbox.click();
            }
        }

        releaseForm.find(By.id("release")).click();

        Assert.assertNotNull(elementFinder.find(By.id("bamboo-icon"), TimeoutType.AJAX_ACTION));

        return pageBinder.bind(BambooRelease.class, this);
    }

    public BambooRelease executeExistingBuildForRelease(String planKey)
    {
        elementFinder.find(By.id("runRelease"), TimeoutType.DIALOG_LOAD).click();

        PageElement releaseForm = elementFinder.find(By.id("release-form"));
        releaseForm.find(By.id("existing-build")).click();
        final PageElement planSelectList = releaseForm.find(By.id("bamboo-plan"), TimeoutType.AJAX_ACTION);

        boolean selectedPlanKey = false;
        for (PageElement element : planSelectList.findAll(By.tagName("option"), TimeoutType.AJAX_ACTION))
        {
            if (element.getValue().equals(planKey))
            {
                element.select();
                selectedPlanKey = true;
                break;
            }
        }

        if (!selectedPlanKey)
        {
            throw new IllegalStateException("Could not find '" + planKey + "' in select list " + planSelectList.getAttribute("id"));
        }

        PageElement pageElement = elementFinder.find(By.id("bamboo-build-results"), TimeoutType.AJAX_ACTION);
        Assert.assertNotNull(pageElement);

        //This seems to fix some odd webdriver/firefox bug where these elements dont turn up after the ajax action timeout.
        waitForFiveSeconds();

        PageElement releaseOption = Iterables.get(pageElement.findAll(By.tagName("input"), TimeoutType.AJAX_ACTION), 0);
        releaseOption.select();

        Assert.assertTrue(releaseOption.isSelected());

        PageElement stage1 = elementFinder.find(By.id("stage-1"), TimeoutType.AJAX_ACTION);
        Assert.assertNotNull(stage1);
        stage1.select();

        releaseForm.find(By.id("release")).click();

        Assert.assertNotNull(elementFinder.find(By.id("bamboo-icon"), TimeoutType.AJAX_ACTION));

        return pageBinder.bind(BambooRelease.class, this);
    }

    private void waitForFiveSeconds()
    {
        try
        {
            Thread.sleep(TimeUnit.SECONDS.toMillis(5));
        }
        catch (InterruptedException e)
        {

        }
    }
}
