package com.atlassian.jira.webtest.framework.impl.selenium.dialog;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.component.Option;
import com.atlassian.jira.webtest.framework.core.component.Select;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.query.ExpirationHandler;
import com.atlassian.jira.webtest.framework.core.query.Queries;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.dialog.IssuePickerPopup;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.component.ReloadingSelect;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.query.AbstractSeleniumTimedQuery;
import com.atlassian.jira.webtest.framework.model.IssueData;
import com.atlassian.jira.webtest.framework.page.Page;
import com.google.common.collect.Maps;

import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.and;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.not;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.or;

/**
 * Selenium implementation of {@link com.atlassian.jira.webtest.framework.dialog.IssuePickerPopup}.
 *
 * @since v4.3
 */
public class SeleniumIssuePickerPopup extends AbstractSeleniumPopupInPage<IssuePickerPopup>
        implements IssuePickerPopup
{
    private static final String WINDOW_ID = "IssueSelectorPopup";
    private static final String TABLE_ID = "issue-picker-popup-open";

    private static final String ISSUE_LINK_TEMPLATE = "a[rel='%s']";
    private static final String ISSUE_ROW_LOCATOR = "tr.issue-picker-row";

    private final SeleniumLocator recentIssueModeDetector;
    private final SeleniumLocator toggleRecentIssueModeLinkLocator;
    private final SeleniumLocator filterSelectLocator;

    private final SeleniumLocator recentIssuesSectionLocator;
    private final SeleniumLocator currentIssuesSectionLocator;
    private final SeleniumLocator filterSectionLocator;

    private final CloseModeImpl closeMode;
    private final Select filterSelect;

    private final Map<ResultSection, SeleniumLocator> sectionLocatorMappings = Maps.newHashMap();

    public SeleniumIssuePickerPopup(Page page, SeleniumLocator openLinkLocator, SeleniumContext ctx)
    {
        super(page, openLinkLocator, ctx, TABLE_ID, WINDOW_ID);
        this.recentIssueModeDetector = css("span.picker-label-recent-active");
        this.toggleRecentIssueModeLinkLocator = css("a.picker-recent-link");
        this.filterSelectLocator = name("searchRequestId");
        this.recentIssuesSectionLocator = openDialogLocator().combine(css("div#recent-issues"));
        this.currentIssuesSectionLocator = openDialogLocator().combine(css("div#current-issues"));
        this.filterSectionLocator = openDialogLocator().combine(css("div#filter-issues"));
        this.closeMode = new CloseModeImpl();
        this.filterSelect = new ReloadingSelect(filterSelectLocator, context);
        initMappings();
    }

    private void initMappings()
    {
        sectionLocatorMappings.put(ResultSection.RECENT_ISSUES, recentIssuesSectionLocator);
        sectionLocatorMappings.put(ResultSection.CURRENT_ISSUES, currentIssuesSectionLocator);
        sectionLocatorMappings.put(ResultSection.FILTER, filterSectionLocator);
    }

    private SeleniumLocator locatorOf(ResultSection section)
    {
        return notNull("locator", sectionLocatorMappings.get(section));
    }

    @Override
    @SuppressWarnings ({ "unchecked" })
    protected Class<IssuePickerPopup> dialogType()
    {
        return (Class) getClass();
    }

    /* ----------------------------------------------- COMPONENTS --------------------------------------------------- */

    @Override
    public Select filterSelect()
    {
        return filterSelect;
    }

    /* ------------------------------------------------ QUERIES ----------------------------------------------------- */

    @Override
    public TimedCondition isInMode(SearchMode mode)
    {
        return mode == SearchMode.RECENT_ISSUES ? isInRecentIssuesMode() : isInFilterMode();
    }

    private TimedCondition isInRecentIssuesMode()
    {
        return and(isOpen(), recentIssueModeDetector.element().isPresent());
    }

    private TimedCondition isInFilterMode()
    {
        return and(isOpen(), toggleRecentIssueModeLinkLocator.element().isPresent());
    }

    @Override
    public TimedQuery<SearchMode> searchMode()
    {
        return Queries.conditionalQuery(searchModeQuery(), isOpen()).build();
    }

    private TimedQuery<SearchMode> searchModeQuery()
    {
        return new AbstractSeleniumTimedQuery<SearchMode>(context, ExpirationHandler.RETURN_NULL, Timeouts.UI_ACTION)
        {
            @Override
            protected boolean shouldReturn(SearchMode currentEval)
            {
                return currentEval != null;
            }

            @Override
            protected SearchMode currentValue()
            {
                if (isInRecentIssuesMode().now())
                {
                    return SearchMode.RECENT_ISSUES;
                }
                else if (isInFilterMode().now())
                {
                    return SearchMode.FILTER;
                }
                else return null;
            }
        };
    }

    @Override
    public TimedCondition hasAnyIssues(ResultSection section)
    {
        return and(isOpen(), isInMode(section.mode()), isAnyIssueInSection(section));
    }

    @Override
    public TimedCondition hasIssue(ResultSection section, IssueData issueData)
    {
        return and(isOpen(), isInMode(section.mode()), issueInSectionLocator(section, issueData).element().isPresent());
    }

    @Override
    public TimedCondition hasIssue(IssueData issueData)
    {
        return and(isOpen(), issueInResultsLocator(issueData).element().isPresent());
    }

    private TimedCondition isAnyIssueInSection(ResultSection section)
    {
        return inSectionLocator(section).element().isPresent();
    }

    private SeleniumLocator inSectionLocator(ResultSection section)
    {
        return locatorOf(section).combine(css(ISSUE_ROW_LOCATOR));
    }

    private SeleniumLocator issueInSectionLocator(ResultSection section, IssueData data)
    {
        return inSectionLocator(section).combine(issueLinkLocator(data));
    }

    private SeleniumLocator issueInResultsLocator(IssueData issueData)
    {
        return openDialogLocator().combine(issueLinkLocator(issueData));
    }

    private SeleniumLocator issueLinkLocator(IssueData data)
    {
        return css(String.format(ISSUE_LINK_TEMPLATE, data.key()));
    }

    /* ------------------------------------------------ ACTIONS ----------------------------------------------------- */


    @Override
    public IssuePickerPopup switchToRecentIssues()
    {
        if (isInMode(SearchMode.RECENT_ISSUES).now())
        {
            return this;
        }
        toggleRecentIssueModeLinkLocator.element().click();
        waitFor().pageLoad();
        return this;
    }

    @Override
    public IssuePickerPopup switchToFilter(Option option)
    {
        filterSelect.select(option);
        return this;
    }

    @Override
    public CloseMode close()
    {
        return closeMode;
    }

    private class CloseModeImpl implements CloseMode
    {
        @Override
        public void byClosingWindow()
        {
            if (isClosed().now())
            {
                throw new IllegalStateException("Already closed");
            }
            client.getEval("selenium.browserbot.getCurrentWindow().close()");
            client.selectWindow(null);
        }

        @Override
        public void bySelectingIssue(IssueData issueData)
        {
            if (or(isClosed(), not(hasIssue(issueData))).now())
            {
                throw new IllegalStateException("Already closed or does not have <" + issueData + "> in results");
            }
            issueInResultsLocator(issueData).element().click();
            client.selectWindow(null);
        }
    }
}
