package it.com.atlassian.jira.plugin.ext.bamboo.pageobjects;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.*;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.base.Function;
import it.com.atlassian.jira.plugin.ext.bamboo.PageElementUtils;
import org.openqa.selenium.By;

import java.util.List;

import static com.atlassian.pageobjects.elements.query.Conditions.and;
import static org.junit.Assert.assertTrue;

/**
 * A dialog on the release management tab, to release versions in Bamboo.
 *
 */
public class ReleaseDialog
{
    // TODO better checking is it's open or not

    @ElementBy(id = "release-form")
    private PageElement releaseForm;

    private PageElement newBuildLink;
    private PageElement existingBuildLink;
    private SelectElement planSelect;
    private PageElement existingBuildsContainer;
    private PageElement stagesContainer;
    private PageElement releaseButton;

    @Init
    private void initialize()
    {
        newBuildLink = releaseForm.find(By.id("new-build"));
        existingBuildLink = releaseForm.find(By.id("existing-build"));
        planSelect = releaseForm.find(By.id("bamboo-plan"), SelectElement.class);
        existingBuildsContainer = releaseForm.find(By.id("bamboo-build-results"));
        stagesContainer = releaseForm.find(By.id("bamboo-plan-stages"));
        releaseButton = releaseForm.find(By.id("release"));
    }

    public TimedCondition isOpen()
    {
        return releaseForm.timed().isVisible();
    }

    public ReleaseDialog selectNewBuild(String planKey)
    {
        return selectBuild(newBuildLink, planKey);
    }

    public ReleaseDialog selectExistingBuild(String planKey)
    {
        return selectBuild(existingBuildLink, planKey);
    }

    private ReleaseDialog selectBuild(PageElement link, String planKey)
    {
        link.click();
        Poller.waitUntilTrue(planSelect.timed().isPresent());
        planSelect.select(Options.value(planKey));
        return this;
    }

    /**
     * Select build number to execute for release by an existing build. Applicable only when existing build is selected
     * for the release.
     *
     * @param number number of the build to select
     * @return this dialog instance
     * @see #selectExistingBuild(String)
     */
    public ReleaseDialog selectBuildNumber(int number)
    {
        Poller.waitUntilTrue("Only applicable when existing build is selected", isExistingBuildSelected());
        final PageElement buildToSelect = existingBuildsContainer.find(By.id("build-result-" + number));
        assertTrue("Build with number <" + number + "> not found", buildToSelect.isPresent());
        buildToSelect.select();
        return this;
    }

    public List<PageElement> allExistingBuilds()
    {
        Poller.waitUntilTrue("Only applicable when existing build is selected", isExistingBuildSelected());
        return existingBuildsContainer.findAll(By.tagName("input"));
    }

    public TimedCondition isExistingBuildSelected()
    {
        return and(existingBuildLink.timed().isSelected(), existingBuildsContainer.timed().isVisible());
    }


    public ReleaseDialog selectStages(String... stages)
    {
        return doWithStages(PageElementUtils.CHECK, stages);
    }

    public ReleaseDialog unselectStages(String... stages)
    {
        return doWithStages(PageElementUtils.UNCHECK, stages);
    }

    public ReleaseDialog doWithStages(Function<CheckboxElement, CheckboxElement> whatToDo, String... stages)
    {
        if (stages.length == 0)
        {
            return this;
        }
        for (String stage : stages)
        {
            CheckboxElement element = findStageCheckbox(stage);
            if (element.isVisible())
            {
                whatToDo.apply(element);
            }
        }
        return this;
    }

    public ReleaseDialog selectAllStages()
    {
        return doWithAllStages(PageElementUtils.CHECK);
    }

    public ReleaseDialog unselectAllStages()
    {
        return doWithAllStages(PageElementUtils.UNCHECK);
    }

    public ReleaseDialog doWithAllStages(Function<CheckboxElement, CheckboxElement> whatToDo)
    {
        for (CheckboxElement stageElement : allStages())
        {
            if (stageElement.isVisible())
            {
                whatToDo.apply(stageElement);
            }
        }
        return this;
    }

    public boolean hasStageCheckbox(String stage)
    {
        return findStageCheckboxNoException(stage) != null;
    }

    public CheckboxElement findStageCheckbox(String stage)
    {
        final CheckboxElement answer = findStageCheckboxNoException(stage);
        if (answer == null)
        {
            throw new IllegalArgumentException("No stage checkbox found for " + stage);
        }
        return answer;
    }

    private CheckboxElement findStageCheckboxNoException(String stage)
    {
        for (CheckboxElement stageElement : allStages())
        {
            if (stageElement.getValue().equals(stage))
            {
                return stageElement;
            }
        }
        return null;
    }

    private Iterable<CheckboxElement> allStages()
    {
        Poller.waitUntilTrue(stagesContainer.timed().isVisible());
        return stagesContainer.findAll(By.cssSelector("input[type=checkbox]"), CheckboxElement.class);
    }

    public ReleaseDialog submit()
    {
        releaseButton.click();
        Poller.waitUntilFalse(isOpen());
        return this;
    }

}
