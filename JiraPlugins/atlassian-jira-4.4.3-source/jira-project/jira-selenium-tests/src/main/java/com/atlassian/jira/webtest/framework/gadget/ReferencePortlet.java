package com.atlassian.jira.webtest.framework.gadget;

import com.atlassian.jira.webtest.framework.core.query.TimedQuery;

/**
 * <p>
 * Reference Portlet representation.
 *
 * <p>
 * Reference portlet does not do a lot except for displaying current user name.
 *
 * @since v4.3
 */
public interface ReferencePortlet extends Gadget
{

    /**
     * Return current user string as displayed by the portlet.
     *
     * @return current user name information as displayed by the reference portlet
     */
    TimedQuery<String> currentUserInfo();
}
