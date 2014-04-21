package it.com.atlassian.jira.plugin.ext.bamboo.pageobjects;

import com.atlassian.jira.plugin.ext.bamboo.model.BuildState;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import org.openqa.selenium.By;
import org.openqa.selenium.interactions.ElementNotDisplayedException;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class BambooRelease
{
    private static final String BAMBOO_ICON_SUCCESSFUL_PARTIAL = "bamboo-icon-SuccessfulPartial";
    private static final String BAMBOO_ICON_SUCCESSFUL = "bamboo-icon-Successful";
    private static final String BAMBOO_ICON_FAILED = "bamboo-icon-Failed";
    private static final String BAMBOO_ICON_UNKNOWN = "bamboo-icon-Unknown";
    private static final String BAMBOO_ICON = "bamboo-icon";
    private static final String BUILD_DETAILS = "build-details";
    private static final int ATTEMPTS = 25;

    private ReleaseManagementTabPanel releaseManagementTabPanel;

    @Inject
    PageElementFinder elementFinder;

    public BambooRelease()
    {
    }

    public BambooRelease(final ReleaseManagementTabPanel releaseManagementTabPanel)
    {
        this.releaseManagementTabPanel = releaseManagementTabPanel;
    }

    public BuildState getBuildState()
    {
        for (int i = 0; i < ATTEMPTS; i++)
        {
            //Suspect a timing issue here. whats the right way todo this in page objects?
            try
            {
                Thread.sleep(TimeUnit.SECONDS.toMillis(1));
            }
            catch (InterruptedException e)
            {
            }
            
            PageElement buildDetails = elementFinder.find(By.id(BUILD_DETAILS), TimeoutType.AJAX_ACTION);

            if (buildDetails != null)
            {
                PageElement buildStatusIcon = buildDetails.find(By.className(BAMBOO_ICON));
                
                if (buildStatusIcon != null)
                {
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
                }
            }
        }
        throw new ElementNotDisplayedException("Could not determine BuildState of Bamboo plan");
    }
}
