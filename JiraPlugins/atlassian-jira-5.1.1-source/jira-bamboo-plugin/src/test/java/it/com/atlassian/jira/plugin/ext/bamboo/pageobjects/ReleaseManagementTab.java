package it.com.atlassian.jira.plugin.ext.bamboo.pageobjects;

import com.atlassian.jira.pageobjects.pages.project.browseversion.BrowseVersionTab;
import com.atlassian.jira.plugin.ext.bamboo.model.BuildState;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Conditions.and;
import static com.atlassian.pageobjects.elements.query.Conditions.not;
import static com.atlassian.pageobjects.elements.query.Conditions.or;
import static com.atlassian.pageobjects.elements.query.Poller.by;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

public class ReleaseManagementTab implements BrowseVersionTab
{
    private static final String BAMBOO_ICON_SUCCESSFUL_PARTIAL = "bamboo-icon-SuccessfulPartial";
    private static final String BAMBOO_ICON_SUCCESSFUL = "bamboo-icon-Successful";
    private static final String BAMBOO_ICON_FAILED = "bamboo-icon-Failed";
    private static final String BAMBOO_ICON_UNKNOWN = "bamboo-icon-Unknown";
    private static final String BAMBOO_ICON_IN_PROGRESS = "bamboo-icon-InProgress";
    private static final String BAMBOO_ICON_QUEUED = "bamboo-icon-Queued";

    @Inject
    private Timeouts timeouts;

    @Inject
    private PageBinder pageBinder;

    @ElementBy(id = "runRelease")
    private PageElement runReleaseButton;

    @ElementBy(id = "build-details")
    private PageElement buildDetails;

    private PageElement buildStatusIcon;


    @Init
    private void init()
    {
        buildStatusIcon = buildDetails.find(By.className("bamboo-icon"));
    }

    public String linkId()
    {
        return "bamboo-release-tabpanel-panel";
    }

    public TimedCondition isOpen()
    {
        // this won't work in case app link to bamboo is missing. if you need to test for that case, add page element
        // for the button and corresponding or here
        return Conditions.or(runReleaseButton.timed().isPresent(), buildDetails.timed().isPresent());
    }

    public TimedCondition isVersionReleased()
    {
        return and(buildDetails.timed().isPresent(), not(runReleaseButton.timed().isPresent()));
    }


    public ReleaseDialog openReleaseDialog()
    {
        runReleaseButton.click();
        final ReleaseDialog dialog = pageBinder.bind(ReleaseDialog.class);
        Poller.waitUntilTrue(dialog.isOpen());
        return dialog;
    }

    public TimedCondition hasBuildStatus()
    {
        return and(isVersionReleased(), buildStatusIcon.timed().isPresent());
    }

    public TimedCondition isBuildRunning()
    {
        return and(hasBuildStatus(),
                or(buildStatusIcon.timed().hasClass(BAMBOO_ICON_IN_PROGRESS), buildStatusIcon.timed().hasClass(BAMBOO_ICON_QUEUED)));
    }

    public TimedCondition isBuildFinished()
    {
        return and(hasBuildStatus(), not(isBuildRunning()));
    }

    public ReleaseManagementTab waitForBuildToFinish(TimeoutType howLong)
    {
        Poller.waitUntil("Build has not finished by " + howLong, isBuildFinished(), is(true), by(timeouts.timeoutFor(howLong)));
        return this;
    }

    public BuildState getBuildStatus()
    {
        assertTrue("Build must be finished", isBuildFinished().now());
        if (!buildStatusIcon.hasClass(BAMBOO_ICON_UNKNOWN))
        {
            if (buildStatusIcon.hasClass(BAMBOO_ICON_SUCCESSFUL_PARTIAL) || buildStatusIcon.hasClass(BAMBOO_ICON_SUCCESSFUL))
            {
                return BuildState.SUCCESS;
            }
            else if (buildStatusIcon.hasClass(BAMBOO_ICON_FAILED))
            {
                return BuildState.FAILED;
            }
        }
        else
        {
            return BuildState.UNKNOWN;
        }
        throw new AssertionError("Unknown state: " + buildStatusIcon + " (css classes: " + buildStatusIcon.getAttribute("class") + ")");
    }


    public BuildState executeNewRelease(String planKey, String... stages)
    {
        openReleaseDialog().selectNewBuild(planKey).unselectAllStages().selectStages(stages).submit();
        return waitForBuildToFinish(TimeoutType.PAGE_LOAD).getBuildStatus();
    }

    public BuildState executeExistingBuildForRelease(String planKey, int buildNumber, String... stages)
    {
        openReleaseDialog().selectExistingBuild(planKey).selectBuildNumber(buildNumber).unselectAllStages()
                .selectStages(stages).submit();
        return waitForBuildToFinish(TimeoutType.PAGE_LOAD).getBuildStatus();
    }

}
