package com.atlassian.jira.webtest.framework.impl.selenium.component.fc;

import com.atlassian.jira.webtest.framework.component.fc.FcLozenge;
import com.atlassian.jira.webtest.framework.component.fc.FrotherControl;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.core.query.ExpirationHandler;
import com.atlassian.jira.webtest.framework.core.query.Queries;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.component.AbstractSeleniumComponent;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.not;

/**
 * Selenium implementation of {@link com.atlassian.jira.webtest.framework.component.fc.FcLozenge}.
 *
 * @author Dariusz Kordonski
 */
public class SeleniumLozenge extends AbstractSeleniumComponent<FrotherControl<?,?,?>> implements FcLozenge
{
    private static final String LOCATOR_TEMPLATE = "div.representation li.item-row[title='%s']";

    private final String label;
    private final SeleniumLocator mainLocator;
    private final SeleniumLocator xIconLocator;

    public SeleniumLozenge(String label, FrotherControl<?,?,?> parent, SeleniumContext context)
    {
        super(parent, context);
        this.label = notNull("label", label);
        this.mainLocator = initMainLocator(label);
        this.xIconLocator = mainLocator.combine(jQuery("em.item-delete"));
    }

    private SeleniumLocator initMainLocator(String label)
    {
        return (SeleniumLocator) parent().locator().combine(jQuery(String.format(LOCATOR_TEMPLATE, label)));
    }

    @Override
    public Locator locator()
    {
        return mainLocator;
    }

    @Override
    protected SeleniumLocator detector()
    {
        return mainLocator;
    }

    @Override
    public TimedQuery<String> value()
    {
        return Queries.conditionalQuery(label, mainLocator.element().isPresent())
                .expirationHandler(ExpirationHandler.RETURN_NULL).build();
    }

    @Override
    public TimedCondition isSelected()
    {
        return conditions().hasClass(mainLocator, "focused");
    }

    @Override
    public TimedCondition isNotSelected()
    {
        return not(isSelected());
    }

    @Override
    public void removeByClick()
    {
        xIconLocator.element().click();
    }
}
