package com.atlassian.jira.webtest.selenium.harness.util;

/**
 * Contains utility methods for navigating and modifying the new dashboard in JIRA 4.0
 *
 * @since v4.0
 * @deprecated use {@link com.atlassian.jira.webtest.framework.page.dashboard.Dashboard} instead
 */
@Deprecated
public interface Dashboard
{
    /**
     * View this dashboard
     */
    void view();

    /**
     * Returns how many gadgets are being displayed on this dashboard
     *
     * @return how many gadgets are being displayed on this dashboard
     */
    int getGadgetCount();

    /**
     * Drags a gadget to the tab specified by name.
     * <p/>
     * The targetShimIndex is a bit of a workaround to fire the mouseup event when dropping the gadget on the right
     * element.
     * <p/>
     * Basically every writable tab will have a hotspot shim.  The index starts with 1 for the first tab, and is
     * incremented by 1 for every subsequent writable tab.
     * <p/>
     * E.g. if we have the following tabs: <ul>
     * <li>Tab 1 (current tab)</li>
     * <li>Tab 2</li>
     * <li>Tab 3 (read-only)</li>
     * <li>Tab 4</li>
     * <li>Tab 5</li>
     * <li>Tab 6</li>
     * <p/>
     * If we want to drag and drop the gadget with id 10010 to Tab 5, we'd have to call this method: {@code
     * dragGadgetToTab("10010", "Tab5",3);}
     *
     * @param gadgetId The id of the gadget to drag
     * @param targetTabName The name of the tab to drag to
     * @param targetShimIndex This is the id of the hotspot shim for the tab we're dragging to. See above for explanation
     */
    void dragGadgetToTab(String gadgetId, String targetTabName, int targetShimIndex);
}
