package com.atlassian.jira.webtest.framework.impl.selenium.gadget;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.query.ExpirationHandler;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.gadget.ReferencePortlet;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.page.dashboard.GadgetInfo;
import com.atlassian.jira.webtest.framework.page.dashboard.Dashboard;

/**
 * Default implementation of {@link com.atlassian.jira.webtest.framework.gadget.ReferencePortlet}.
 *
 * @since v4.3
 */
public class ReferencePortletImpl extends AbstractGadget implements ReferencePortlet
{
    private static final String USER_INFO_CONTAINER_ID = "userinfo";

    private static final String NAME = GadgetInfo.gadgetName(ReferencePortlet.class);

    public ReferencePortletImpl(int id, Dashboard dashboard, SeleniumContext context)
    {
        super(id, NAME, dashboard, context);
    }

    public ReferencePortletImpl(Dashboard dashboard, SeleniumContext context)
    {
        super(NAME, dashboard, context);
    }

    @Override
    public TimedQuery<String> currentUserInfo()
    {
        return inGadget(queries().forTextBuilder(id(USER_INFO_CONTAINER_ID))
                .defaultTimeout(Timeouts.AJAX_ACTION).expirationHandler(ExpirationHandler.RETURN_NULL).build());
    }

}
