package com.atlassian.jira.webtest.framework.impl.selenium.page.issuenavigator;

import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.webtest.framework.core.condition.Conditions;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.page.AbstractSeleniumPageSection;
import com.atlassian.jira.webtest.framework.page.issuenavigator.CustomFieldsSection;
import com.atlassian.jira.webtest.framework.page.issuenavigator.DatesAndTimesSection;
import com.atlassian.jira.webtest.framework.page.issuenavigator.IssueAttributesSection;
import com.atlassian.jira.webtest.framework.page.issuenavigator.IssueNavigator;
import com.atlassian.jira.webtest.framework.page.issuenavigator.SimpleSearchFilter;
import com.atlassian.jira.webtest.framework.page.issuenavigator.SimpleSearchFilterSection;
import com.atlassian.jira.webtest.framework.page.issuenavigator.WorkRatioSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.webtest.framework.core.condition.Conditions.and;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.or;

/**
 * Selenium implementation of {@link com.atlassian.jira.webtest.framework.page.issuenavigator.SimpleSearchFilter}.
 *
 * @since v4.3
 */
public class SeleniumSimpleSearchFilter extends AbstractSeleniumPageSection<IssueNavigator> implements SimpleSearchFilter
{
    private static final String SUBMIT_BUTTON_ID = "issue-filter-submit";
    private static final String COLLAPSE_LINK = "a.toggle-lhc";

    private final SeleniumLocator detector;
    private final SeleniumLocator collapseLinkLocator;

    private final Map<Class<SimpleSearchFilterSection<?>>, SimpleSearchFilterSection<?>> sectionMappings;
    private final List<SimpleSearchFilterSection<?>> allSections;

    public SeleniumSimpleSearchFilter(IssueNavigator page, SeleniumContext context)
    {
        super(page, context);
        this.detector = id(SUBMIT_BUTTON_ID);
        this.collapseLinkLocator = css(COLLAPSE_LINK);
        this.sectionMappings = initSectionMappings();
        this.allSections = initSectionList();
    }

    @SuppressWarnings ({ "unchecked" })
    private Map<Class<SimpleSearchFilterSection<?>>, SimpleSearchFilterSection<?>> initSectionMappings()
    {
        return MapBuilder.<Class<SimpleSearchFilterSection<?>>, SimpleSearchFilterSection<?>>newBuilder()
                .add((Class)IssueAttributesSection.class, new SeleniumIssueAttributesSection(this, context))
                .add((Class) DatesAndTimesSection.class, new SeleniumDatesAndTimesSection(this, context))
                .add((Class) WorkRatioSection.class, new SeleniumWorkRatioSection(this, context))
                .add((Class) CustomFieldsSection.class, new SeleniumCustomFieldsSection(this, context))
                .toMap();
    }

    private List<SimpleSearchFilterSection<?>> initSectionList()
    {
        return new ArrayList<SimpleSearchFilterSection<?>>(sectionMappings.values());
    }

    /* ---------------------------------------------- LOCATORS ------------------------------------------------------ */

    @Override
    protected SeleniumLocator detector()
    {
        return detector;
    }

    /* ----------------------------------------------- QUERIES ------------------------------------------------------ */

    @Override
    public TimedCondition isReady()
    {
        return and(page().isSimpleMode(), super.isReady());
    }

    @Override
    public TimedCondition allSectionsExpanded()
    {
        Conditions.CombinableCondition result = and(isExpanded());
        for (SimpleSearchFilterSection<?> section : allSections)
        {
            result = result.and(section.isExpanded());
        }
        return result;
    }

    @Override
    public TimedCondition allSectionsCollapsed()
    {
        List<TimedCondition> allCollapsed = CollectionUtil.transform(allSections, new Function<SimpleSearchFilterSection<?>, TimedCondition>()
        {
            @Override
            public TimedCondition get(SimpleSearchFilterSection<?> input)
            {
                return input.isCollapsed();
            }
        });
        return or(isCollapsed(), and(allCollapsed));
    }


    @Override
    public TimedCondition isExpanded()
    {
        return and(page().isAt(), detector.element().isVisible());
    }

      @Override
    public TimedCondition isCollapsed()
    {
        return and(page().isAt(), detector.element().isNotVisible());
    }


    /* -------------------------------------------- COMPONENTS ------------------------------------------------------ */

    @Override
    @SuppressWarnings ({ "unchecked" })
    public <S extends SimpleSearchFilterSection<S>> S section(Class<S> sectionType)
    {
        return (S) sectionMappings.get(sectionType);
    }


    /* --------------------------------------------- ACTIONS -------------------------------------------------------- */

    @Override
    public SimpleSearchFilter expandAllSections()
    {
        if (allSectionsExpanded().now())
        {
            return this;
        }
        for (SimpleSearchFilterSection<?> section : allSections)
        {
            if (section.isCollapsed().now())
            {
                section.expand();
            }
        }
        return this;
    }

    @Override
    public SimpleSearchFilter collapseAllSections()
    {
        if (allSectionsCollapsed().now())
        {
            return this;
        }
        for (SimpleSearchFilterSection<?> section : allSections)
        {
            if (section.isExpanded().now())
            {
                section.collapse();
            }
        }
        return this;
    }



    @Override
    public SimpleSearchFilter expand()
    {
        if (isCollapsed().now())
        {
            collapseLinkLocator.element().click();
        }
        return this;
    }

    @Override
    public SimpleSearchFilter collapse()
    {
        if (isExpanded().now())
        {
            collapseLinkLocator.element().click();
        }
        return this;
    }
}
