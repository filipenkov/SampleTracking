package com.atlassian.jira.webtest.framework.page.dashboard;

import com.atlassian.jira.webtest.framework.component.tab.NamedTab;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.gadget.Gadget;

/**
 * Tab containing contents of a single user dashboard on the {@link Dashboard} page.
 *
 * @since v4.3
 */
public interface DashboardTab extends NamedTab<DashboardTab>
{

    /**
     * <p>
     * Check if dashboard has a given gadget.
     *
     * <p>
     * NOTE: there may be more than one gadgets on a given Dashboard page and this method will not convey information
     * about how many gadgets are there. Use {@link #hasGadget(Class, int)} to check for a gadget with particular ID.
     *
     * @param gadgetType type of the gadget
     * @return timed condition checking, whether the given gadget exists on this dashboard
     * @see #hasGadget(Class, int)
     */
    TimedCondition hasGadget(Class<? extends Gadget> gadgetType);

    /**
     * <p>
     * Check if dashboard has gadget of given type and with given <tt>gadgetId</tt>.
     *
     * @param gadgetType type of the gadget
     * @param gadgetId ID of the gadget
     * @return timed condition checking, whether the given gadget exists on this dashboard
     */
    TimedCondition hasGadget(Class<? extends Gadget> gadgetType, int gadgetId);

    /**
     * <p>
     * Find gadget of given <tt>gadgetType</tt>.
     *
     * <p>
     * NOTE: if there are multiple gadget of <tt>gadgetType</tt> on the particular dashboard, the first found one will be
     * returned
     *
     * @param gadgetType gadget class
     * @param <T> type parameter of gadget
     * @return gadget instance. NOTE: this <i>will</i> return gadget instance (if given gadget type is supported), even if
     * it is not present on current dashboard. This may be checked by {@link com.atlassian.jira.webtest.framework.gadget.Gadget#isReady()},
     * or by {@link #hasGadget(Class)}.
     */
    <T extends Gadget> T gadget(Class<T> gadgetType);

    /**
     * <p>
     * Find gadget of given <tt>gadgetType</tt> and with given ID.
     *
     * @param gadgetType gadget class
     * @param gadgetId ID of the gadget
     * @param <T> type parameter of gadget
     * @return gadget instance. NOTE: this <i>will</i> return gadget instance (if given gadget type is supported), even if
     * it is not present on current dashboard. This may be checked by {@link com.atlassian.jira.webtest.framework.gadget.Gadget#isReady()},
     * or by {@link #hasGadget(Class, int)}.
     */
    <T extends Gadget> T gadget(Class<T> gadgetType, int gadgetId);
}
