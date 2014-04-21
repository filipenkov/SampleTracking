package com.atlassian.jira.webtest.framework.impl.selenium.dialog.gadget;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.core.ui.Keys;
import com.atlassian.jira.webtest.framework.dialog.AddGadgetDialog;
import com.atlassian.jira.webtest.framework.gadget.Gadget;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.dialog.AbstractSeleniumAuiPageDialog;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.page.dashboard.GadgetInfo;
import com.atlassian.jira.webtest.framework.page.dashboard.Dashboard;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.byDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.and;

/**
 * Default implementation of {@link com.atlassian.jira.webtest.framework.dialog.AddGadgetDialog}.
 *
 * @since v4.3
 */
public class AddGadgetDialogImpl extends AbstractSeleniumAuiPageDialog<AddGadgetDialog, Dashboard> implements AddGadgetDialog
{
    private final Locator finishedButtonLocator;

    public AddGadgetDialogImpl(Dashboard page, SeleniumContext ctx)
    {
        super(page, ctx, "macro-browser-dialog");
        this.finishedButtonLocator = locator().combine(css(".button-panel-button.finish"));
    }

    @Override
    protected String getOpenDialogClass()
    {
        return "aui-dialog-open";
    }

    @Override
    public Locator locator()
    {
        return super.openDialogLocator();
    }

    @Override
    protected SeleniumLocator openDialogLocator()
    {
        return super.openDialogLocator().combine(forClass("dialog-page-body"))
                .withDefaultTimeout(Timeouts.PAGE_LOAD); // this dialog is freakin' slow
    }

    @Override
    public <T extends Gadget> AddGadgetDialog addGadget(Class<T> gadgetType)
    {
        assertThat(canAddGadget(gadgetType), byDefaultTimeout());
        addButtonLocator(gadgetType).element().click();
        return this;
    }

    @Override
    public <T extends Gadget> AddGadgetDialog addGadget(Class<T> gadgetType, String sourceUrl)
    {
        throw new UnsupportedOperationException("Cause it's freaking hard!");
    }

    @Override
    public TimedCondition canAddGadget(Class<? extends Gadget> gadgetType)
    {
        return and(isOpen(),
                isGadgetItemPresent(gadgetType),
                isGadgetAddButtonPresent(gadgetType),
                conditions().hasValue((SeleniumLocator)addButtonLocator(gadgetType),"Add it Now"));
    }

    @Override
    public TimedCondition canAddGadget(Class<? extends Gadget> gadgetType, String sourceUrl)
    {
        throw new UnsupportedOperationException("Cause it's freaking hard!");
    }

    private TimedCondition isGadgetItemPresent(Class<?> gadgetType)
    {
        return gadgetItemLocator(gadgetType).element().isPresent();
    }

    private TimedCondition isGadgetAddButtonPresent(Class<?> gadgetType)
    {
        return addButtonLocator(gadgetType).element().isPresent();
    }

    private Locator gadgetItemLocator(Class<?> gadgetClass)
    {
        final String id = GadgetInfo.gadgetId(gadgetClass);
         return locator().combine(id("macro-" + id));
    }

    private Locator addButtonLocator(Class<?> gadgetClass)
    {
        final String id = GadgetInfo.gadgetId(gadgetClass);
        return gadgetItemLocator(gadgetClass).combine(forClass("macro-button-add"));
    }

    @Override
    public AddGadgetDialog open()
    {
        return page().openGadgetDialog();
    }

    @Override
    public CloseMode close()
    {
        return new CloseMode()
        {
            @Override
            public Dashboard byClickInFinished()
            {
                finishedButtonLocator.element().click();
                return page();
            }

            @Override
            public Dashboard byEscape()
            {
                context().ui().pressInBody(Keys.ESCAPE);
                return page();
            }
        };
    }
}
