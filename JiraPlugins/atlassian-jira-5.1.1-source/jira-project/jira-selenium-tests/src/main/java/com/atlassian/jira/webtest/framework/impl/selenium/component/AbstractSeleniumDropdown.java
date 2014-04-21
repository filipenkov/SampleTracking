package com.atlassian.jira.webtest.framework.impl.selenium.component;

import com.atlassian.jira.webtest.framework.component.AjsDropdown;
import com.atlassian.jira.webtest.framework.core.Localizable;
import com.atlassian.jira.webtest.framework.core.PageObject;
import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.condition.Conditions;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.core.query.ExpirationHandler;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.ContainsTextCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.IsPresentCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.component.AbstractSeleniumComponent;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.query.AbstractSeleniumConditionBasedQuery;
import com.atlassian.jira.webtest.framework.impl.selenium.util.JqueryExecutor;
import com.atlassian.webtest.ui.keys.SpecialKeys;

import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.and;
import static com.atlassian.webtest.ui.keys.Sequences.keys;

/**
 * Abstract implementation of the {@link com.atlassian.jira.webtest.framework.component.AjsDropdown} interface.
 *
 * @since v4.3
 */
public abstract class AbstractSeleniumDropdown<P extends PageObject> extends AbstractSeleniumComponent<P>
        implements AjsDropdown<P>, Localizable
{
    private static final String DETECTOR_TEMPLATE = "div.ajs-layer.active div#%s.ajs-list";

    private final String id;
    private final SeleniumLocator openDropDownLocator;

    private final JqueryExecutor executor;
    private final CloseModeImpl closeMode;

    /**
     * Requires a unique id of the dropdown list used to evaluate its presence on the page.
     *
     * @param id unique id of this dropdown that is assigned to the container div
     * @param parent parent component of this dropdown
     * @param ctx Selenium context
     */
    protected AbstractSeleniumDropdown(String id, P parent, SeleniumContext ctx)
    {
        super(parent, ctx);
        this.id = notNull("id", id);
        this.openDropDownLocator = css(String.format(DETECTOR_TEMPLATE, id)).withDefaultTimeout(Timeouts.AJAX_ACTION);
        this.executor = new JqueryExecutor(context);
        this.closeMode = new CloseModeImpl();
    }


    /* ---------------------------------------------- LOCATORS ------------------------------------------------------ */

    @Override
    protected SeleniumLocator detector()
    {
        return openDropDownLocator;
    }

    @Override
    public Locator locator()
    {
        return openDropDownLocator;
    }

    /* ---------------------------------------------- QUERIES ------------------------------------------------------- */

    @Override
    public final TimedCondition isOpen()
    {
        return openDropDownLocator.element().isPresent();
    }


    @Override
    public TimedCondition isClosed()
    {
        return openDropDownLocator.element().isNotPresent();
    }

    @Override
    public final TimedCondition isOpenable()
    {
        return Conditions.and(isClosed(), isOpenableByContext());
    }

    /**
     * Whether there is appropriate context to open this drop-down (e.g. page/dialog/component that contains it has loaded)
     *
     * @return timed condition querying if a valid context for opening this drop-down is in place
     */
    protected abstract TimedCondition isOpenableByContext();


    @Override
    public TimedCondition hasItem(String itemText)
    {
        return ContainsTextCondition.forContext(context).locator(allItemsLocator()).expectedValue(itemText)
                .defaultTimeout(timeouts.timeoutFor(Timeouts.UI_ACTION)).build();
    }

    @Override
    public TimedCondition hasSection(String id)
    {
        return IsPresentCondition.forContext(context).locator(sectionWithinLocator(id)).build();
    }

    @Override
    public TimedQuery<Integer> itemCount()
    {
        return new AbstractSeleniumConditionBasedQuery<Integer>(and(isOpen(), executor.canExecute()),
                context, ExpirationHandler.RETURN_CURRENT)
        {
            @Override
            protected Integer evaluateNow()
            {
                return Integer.parseInt(executor.executeNow(positionCountScript()));
            }

            @Override
            protected Integer substituteValue()
            {
                return 0;
            }
        };
    }

    private String positionCountScript()
    {
        return String.format("return jQuery('%s').length;", allItemsLocator().bareLocator());
    }

    private SeleniumLocator allItemsLocator()
    {
        return openDropDownLocator.combine(css(SeleniumDDItem.LIST_ITEM_LOCATOR));
    }

    private SeleniumLocator sectionWithinLocator(String sectionId)
    {
        return openDropDownLocator.combine(id(sectionId));
    }

    /* ---------------------------------------------- COMPONENTS ---------------------------------------------------- */

    @Override
    public TimedQuery<List<Section<P>>> allSections()
    {
        return new SectionQuery();
    }

    @Override
    public TimedQuery<Item<P>> item(String text)
    {
        return new ItemQuery(itemWithTextLocator(text).bareLocator());
    }

    @Override
    public TimedQuery<Section<P>> section(String id)
    {
        return new SectionByIdQuery(id);
    }

    @Override
    public TimedQuery<Item<P>> selectedItem()
    {
        return new ItemQuery(selectedItemWithinLocator().bareLocator());
    }

    private SeleniumLocator selectedItemWithinLocator()
    {
        return openDropDownLocator.combine(css(SeleniumDDItem.ACTIVE_LIST_ITEM_LOCATOR));
    }

    private SeleniumLocator itemWithTextLocator(String text)
    {
        return openDropDownLocator.combine(jQuery(String.format(SeleniumDDItem.ITEM_WITH_TEXT_LOCATOR_TEMPLATE, text)));
    }

    /* ------------------------------------------------ ACTIONS ----------------------------------------------------- */

    @Override
    public CloseMode<P> close()
    {
        return closeMode;
    }

    private class CloseModeImpl implements CloseMode<P>
    {

        @Override
        public P byEnter()
        {
            locator().element().type(keys(SpecialKeys.ENTER));
            return parent();
        }

        @Override
        public P byEscape()
        {
            locator().element().type(keys(SpecialKeys.ESC));
            return parent();
        }

        @Override
        public P byClickIn(Item<P> item)
        {
            if (item.dropDown() != this)
            {
                throw new IllegalArgumentException("Item does not belong to this drop-down: " + item);
            }
            item.locator().element().click();
            return parent();
        }
    }


    private class SectionQuery extends AbstractSeleniumConditionBasedQuery<List<Section<P>>>
    {
        SectionQuery()
        {
            super(isOpen(), AbstractSeleniumDropdown.this.context, ExpirationHandler.RETURN_CURRENT);
        }
        @Override
        protected List<Section<P>> evaluateNow()
        {
            return new SectionListHandler<P>(AbstractSeleniumDropdown.this, context).sections();
        }

        @Override
        protected List<Section<P>> substituteValue()
        {
            return Collections.emptyList();
        }
    }

    private class SectionByIdQuery extends AbstractSeleniumConditionBasedQuery<Section<P>>
    {
        private final SectionQuery sections = new SectionQuery();
        private final String id;

        SectionByIdQuery(String id)
        {
            super(and(isOpen(), hasSection(id)), AbstractSeleniumDropdown.this.context, ExpirationHandler.RETURN_CURRENT);
            this.id = id;
        }

        @Override
        protected Section<P> evaluateNow()
        {
            for (Section<P> section : sections.now())
            {
                if (id.equals(section.id()))
                {
                    return section;
                }
            }
            return null;
        }
    }

    private class ItemQuery extends AbstractSeleniumConditionBasedQuery<Item<P>>
    {
        private final String locator;

        public ItemQuery(String locator)
        {
            super(and(isOpen(), executor.canExecute()), AbstractSeleniumDropdown.this.context, ExpirationHandler.RETURN_CURRENT);
            this.locator = locator;
        }

        @Override
        protected Item<P> evaluateNow()
        {
            return new ItemHandler<P>(AbstractSeleniumDropdown.this, locator, context).item(); 
        }
    }
}
