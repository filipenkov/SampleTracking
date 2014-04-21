package com.atlassian.jira.webtest.framework.impl.selenium.page.issue.move;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.page.AbstractSeleniumFlowPage;
import com.atlassian.jira.webtest.framework.impl.selenium.page.ParentPage;
import com.atlassian.jira.webtest.framework.model.IssueAware;
import com.atlassian.jira.webtest.framework.model.IssueData;
import com.atlassian.jira.webtest.framework.model.SimpleIssueData;
import com.atlassian.jira.webtest.framework.page.FlowPage;
import com.atlassian.jira.webtest.framework.page.Page;
import org.apache.commons.lang.StringUtils;

import static com.atlassian.jira.util.lang.JiraStringUtils.asString;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.and;

/**
 * Abstract Move Sub-task class. It defines common next and cancel locators, as well as detector of the
 * flow step, based on the step number.
 *
 * @since v4.3
 */
public abstract class AbstractSeleniumMoveSubTask<P extends ParentPage, N extends Page> extends AbstractSeleniumFlowPage<P,N>
        implements FlowPage<P,N>, IssueAware
{
    private static final String HEADER_LOCATOR = ".content-body h3";
    private static final String HEADER_PREFIX = "Move Sub-Task:";
    private static final String MAIN_DETECTOR = HEADER_LOCATOR + ":contains('" + HEADER_PREFIX + "')";
    private static final String STEP_LOCATOR_TEMPLATE = ".descriptionrow b:contains('Step %d of 4')";

    private static final String ISSUE_ID_LOCATOR = "id";

    private static final String CANCEL_LOCATOR = "cancelButton";
    private static final String NEXT_LOCATOR = "next_submit";

    private final SeleniumLocator mainDetector;
    private final SeleniumLocator stepLocator;

    private final SeleniumLocator cancelLocator;
    private final SeleniumLocator nextLocator;

    private final SeleniumLocator headerLocator;
    private final SeleniumLocator issueIdLocator;

    protected AbstractSeleniumMoveSubTask(SeleniumContext ctx, P flowParent, int stepNo)
    {
        super(ctx, flowParent, stepNo);
        this.mainDetector = jQuery(MAIN_DETECTOR);
        this.stepLocator = jQuery(String.format(STEP_LOCATOR_TEMPLATE, stepNo));
        this.cancelLocator= id(CANCEL_LOCATOR);
        this.nextLocator = id(NEXT_LOCATOR);
        this.headerLocator = id(HEADER_LOCATOR);
        this.issueIdLocator = id(ISSUE_ID_LOCATOR);
    }

    public final IssueData issueData()
    {
        return new SimpleIssueData(retrieveId(), retrieveKey());
    }

    private long retrieveId()
    {
        checkOnPage();
        return Long.parseLong(issueIdLocator.element().text().now());
    }

    private String retrieveKey()
    {
        checkOnPage();
        final String title = headerLocator.element().text().now().trim();
        if (title.startsWith(HEADER_PREFIX))
        {
            return StringUtils.removeStart(title, HEADER_PREFIX).trim();
        }
        throw new IllegalStateException(asString("Title <", title, "> does not contain expected prefix <", HEADER_PREFIX, ">"));
    }


    /* ------------------------------------------------- QUERIES ---------------------------------------------------- */

    @Override
    public final TimedCondition isReady()
    {
        return and(super.isReady(), stepLocator.element().isPresent());
    }

    /* ------------------------------------------------- LOCATORS --------------------------------------------------- */

    @Override
    protected final SeleniumLocator detector()
    {
        return mainDetector;
    }

    @Override
    protected final Locator cancelLocator()
    {
        return cancelLocator;
    }

    @Override
    protected final Locator nextLocator()
    {
        return nextLocator;
    }

    private void checkOnPage()
    {
        if (!isAt().byDefaultTimeout())
        {
            throw new IllegalStateException("Not on page condition failed: " + isAt());
        }
    }
}
