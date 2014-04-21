package com.atlassian.jira.webtest.framework.impl.selenium.page.issuenavigator;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.component.AbstractSeleniumComponent;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.model.SimpleSearchSection;
import com.atlassian.jira.webtest.framework.page.issuenavigator.SimpleSearchFilter;
import com.atlassian.jira.webtest.framework.page.issuenavigator.SimpleSearchFilterSection;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.and;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.not;

/**
 * Selenium implementation of {@link com.atlassian.jira.webtest.framework.page.issuenavigator.IssueAttributesSection}.
 *
 * @param <S> target section type 
 * @since v4.3
 */
public class AbstractSeleniumSimpleFilterSection<S extends SimpleSearchFilterSection<S>> extends
        AbstractSeleniumComponent<SimpleSearchFilter> implements SimpleSearchFilterSection<S>
{
    private static final String COLLAPSED_CLASS = "collapsed";

    protected final SimpleSearchSection type;
    private final Class<S> targetType;

    private final SeleniumLocator detector;
    private final SeleniumLocator expandLinkLocator;


    protected AbstractSeleniumSimpleFilterSection(SimpleSearchFilter parent, SeleniumContext context,
            SimpleSearchSection type, Class<S> targetType)
    {
        super(parent, context);
        this.type = notNull("type", type);
        this.targetType = notNull("targetType", targetType);
        this.detector = id(type.headerId());
        this.expandLinkLocator = detector.combine(css("span.toggle-title"));
    }

    @Override
    protected final SeleniumLocator detector()
    {
        return detector;
    }

    @Override
    public final SimpleSearchSection type()
    {
        return type;
    }

    @Override
    public final TimedCondition isExpanded()
    {
        return and(parent().isReady(), not(isCollapsedCondition()));
    }

    @Override
    public final TimedCondition isCollapsed()
    {
        return and(parent().isReady(), isCollapsedCondition());
    }

    private TimedCondition isCollapsedCondition()
    {
        return conditions().hasClass(detector, COLLAPSED_CLASS);
    }

    @Override
    public final S expand()
    {
        if (!isExpanded().now())
        {
            expandLinkLocator.element().click();
        }
        return asTargetType();
    }

    @Override
    public final S collapse()
    {
        if (!isCollapsed().now())
        {
            expandLinkLocator.element().click();
        }
        return asTargetType();
    }

    protected final S asTargetType()
    {
        return targetType.cast(this);
    }
}
