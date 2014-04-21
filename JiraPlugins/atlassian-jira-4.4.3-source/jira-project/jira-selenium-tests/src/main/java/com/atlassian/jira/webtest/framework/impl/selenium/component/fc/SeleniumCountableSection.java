package com.atlassian.jira.webtest.framework.impl.selenium.component.fc;

import com.atlassian.jira.webtest.framework.component.AjsDropdown;
import com.atlassian.jira.webtest.framework.component.fc.IssuePicker;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.impl.selenium.core.AbstractSeleniumPageObject;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Selenium implementation of {@link com.atlassian.jira.webtest.framework.component.fc.IssuePicker.CountableSection}.
 *
 * @since v4.3
 */
class SeleniumCountableSection extends AbstractSeleniumPageObject implements IssuePicker.CountableSection
{
    private static final String ISSUE_COUNT_REGEX = "\\(Showing (\\d+) of (\\d+) matching issues\\)";
    private static final Pattern ISSUE_COUNT_PATTERN = Pattern.compile(ISSUE_COUNT_REGEX);
    private static final int CURRENT_ISSUE_COUNT_GROUP_INDEX = 1;
    private static final int TOTAL_ISSUE_COUNT_GROUP_INDEX = 2;

    private final AjsDropdown.Section<IssuePicker> section;
    private final int currentCount;
    private final int totalCount;


    public SeleniumCountableSection(AjsDropdown.Section<IssuePicker> section, SeleniumContext ctx)
    {
        super(ctx);
        this.section = notNull("section", section);
        Matcher matcher = initMatcher();
        this.currentCount = initCurrentCount(matcher);
        this.totalCount = initTotalCount(matcher);
    }

    private Matcher initMatcher()
    {
        Matcher matcher = ISSUE_COUNT_PATTERN.matcher(section.header());
        if (!matcher.find())
        {
            throw new IllegalStateException("Header not matched: " + section.header());
        }
        return matcher;
    }

    private int initCurrentCount(Matcher matcher)
    {
        return Integer.parseInt(matcher.group(CURRENT_ISSUE_COUNT_GROUP_INDEX));
    }

    private int initTotalCount(Matcher matcher)
    {
        return Integer.parseInt(matcher.group(TOTAL_ISSUE_COUNT_GROUP_INDEX));
    }


    @Override
    public int currentCount()
    {
        return currentCount;
    }

    @Override
    public int totalCount()
    {
        return totalCount;
    }

    @Override
    public String id()
    {
        return section.id();
    }

    @Override
    public String header()
    {
        return section.header();
    }

    @Override
    public boolean hasHeader()
    {
        return section.hasHeader();
    }

    @Override
    public TimedQuery<List<AjsDropdown.Item<IssuePicker>>> items()
    {
        return section.items();
    }

    @Override
    public AjsDropdown<IssuePicker> parent()
    {
        return section.parent();
    }

    @Override
    public Locator locator()
    {
        return section.locator();
    }

    @Override
    public TimedCondition isReady()
    {
        return section.isReady();
    }


}
