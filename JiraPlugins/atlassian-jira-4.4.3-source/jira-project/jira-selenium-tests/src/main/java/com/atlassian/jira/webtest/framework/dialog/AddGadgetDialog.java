package com.atlassian.jira.webtest.framework.dialog;

import com.atlassian.jira.webtest.framework.core.DefaultCloseMode;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.gadget.Gadget;
import com.atlassian.jira.webtest.framework.page.dashboard.Dashboard;

/**
 * The 'Add gadget dialog' on the JIRA dashboard.
 *
 * @since v4.3
 */
public interface AddGadgetDialog extends PageDialog<AddGadgetDialog, Dashboard>
{

    /**
     * <p>
     * Add gadget of given type to the dashboard. This will also close the dialog.
     *
     * <p>
     * NOTE: there may be more then one gadgets of given type to add. Use {@link #addGadget(Class, String)} to add a
     * gadget from particular source.
     *
     * @param gadgetType class of gadget to add
     * @param <T> type parameter
     * @return gadget instance
     */
    <T extends Gadget> AddGadgetDialog addGadget(Class<T> gadgetType);

    /**
     * <p>
     * Add gadget of given type and <tt>sourceUrl</tt> to the dashboard. This will also close the dialog.
     *
     * @param gadgetType class of gadget to add
     * @param sourceUrl URL of the gadget spec source
     * @param <T> type parameter
     * @return gadget instance
     */
    <T extends Gadget> AddGadgetDialog addGadget(Class<T> gadgetType, String sourceUrl);

    /**
     * Check if given gadget type can be added. This means that the dialog is open and the 'Add gadget' button
     * is available (i.e. not already waiting for adding a gadget).
     *
     * @param gadgetType type of the gadget to add.
     * @return condition checking, if gadget of given type can be added
     */
    TimedCondition canAddGadget(Class<? extends Gadget> gadgetType);

    /**
     * <p>
     * Check if given gadget type can be added. This means that the dialog is open and the 'Add gadget' button
     * is available (i.e. not already waiting for adding a gadget).
     *
     * <p>
     * Use this method if there are more than one gadgets of given type, coming from different applications.
     *
     * @param gadgetType type of the gadget to add.
     * @param sourceUrl URL of the gadget source
     * @return condition checking, if gadget of given type and from given source can be added
     */
    TimedCondition canAddGadget(Class<? extends Gadget> gadgetType, String sourceUrl);


    /**
     * Close the dialog.
     *
     * @return close mode
     */
    CloseMode close();


    public static interface CloseMode extends DefaultCloseMode<Dashboard>
    {
        /**
         * Close by clicking in the 'Finished' button.
         *
         * @return parent dashboard page 
         */
        Dashboard byClickInFinished();
    }

    // TODO other stuff
}
