package com.atlassian.jira.webtest.framework.gadget;

import com.atlassian.jira.webtest.framework.core.Localizable;
import com.atlassian.jira.webtest.framework.core.component.Component;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.page.dashboard.Dashboard;

/**
 * A gadget is well... a gadget. Lots of meaningless JavaScript loaded into freaking
 * IFrames all over the Dashboard page. In other words: web testing HELL! 
 *
 * @since v4.3
 */
public interface Gadget extends Localizable, Component<Dashboard>
{

    /**
     * Unique ID of the gadget.
     *
     * @return gadget id
     */
    int id();


    /**
     * Return name of the gadget as visible by user on the Dashboard.
     *
     * @return name of the gadget
     */
    String name();

    /**
     * Each gadget on Dashboard is embedded withing a frame. This method returns this gadget's
     * frame locator.
     *
     * @return frame locator of this gadget's frame
     */
    Locator frameLocator();
}
