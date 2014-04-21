package com.atlassian.jira.webtest.framework.impl.selenium.page.issue;

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
 * Abstract Convert to sub-task class. It defines common next and cancel locators, as well as detector of the
 * flow step, based on the step number.
 *
 * @since v4.3
 */
public abstract class AbstractSeleniumConvertToSubtask<P extends ParentPage, N extends Page> extends AbstractSeleniumFlowPage<P,N>
        implements FlowPage<P,N>, IssueAware
{
    private static final String TITLE_LOCATOR = "table.jiraform tr.titlerow h3.formtitle";
    private static final String TITLE_PREFIX = "Convert Issue to Sub-task:";
    private static final String MAIN_DETECTOR = TITLE_LOCATOR + ":contains('" + TITLE_PREFIX + "')";
    private static final String STEP_LOCATOR_TEMPLATE = "table.jiraform tr.descriptionrow div.desc-wrap:contains('Step %d of 4')";

    private static final String ISSUE_ID_LOCATOR = "id";

    private static final String CANCEL_LOCATOR = "cancelButton";
    private static final String NEXT_LOCATOR = "next_submit";

    private final SeleniumLocator mainDetector;
    private final SeleniumLocator stepLocator;

    private final SeleniumLocator cancelLocator;
    private final SeleniumLocator nextLocator;

    private final SeleniumLocator titleLocator;
    private final SeleniumLocator issueIdLocator;

    protected AbstractSeleniumConvertToSubtask(SeleniumContext ctx, P flowParent, int stepNo)
    {
        super(ctx, flowParent, stepNo);
        this.mainDetector = jQuery(MAIN_DETECTOR);
        this.stepLocator = jQuery(String.format(STEP_LOCATOR_TEMPLATE, stepNo));
        this.cancelLocator= id(CANCEL_LOCATOR);
        this.nextLocator = id(NEXT_LOCATOR);
        this.titleLocator = css(TITLE_LOCATOR);
        this.issueIdLocator = id(ISSUE_ID_LOCATOR);
    }

    public final IssueData issueData()
    {
        return new SimpleIssueData(retrieveId(), retrieveKey());
    }

    private long retrieveId()
    {
        return Long.parseLong(issueIdLocator.element().text().now());
    }

    private String retrieveKey()
    {
        checkOnPage();
        final String title = titleLocator.element().text().now().trim();
        if (title.startsWith(TITLE_PREFIX))
        {
            return StringUtils.removeStart(title, TITLE_PREFIX).trim();
        }
        throw new IllegalStateException(asString("Title <", title, "> does not contain expected prefix <", TITLE_PREFIX, ">"));
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
