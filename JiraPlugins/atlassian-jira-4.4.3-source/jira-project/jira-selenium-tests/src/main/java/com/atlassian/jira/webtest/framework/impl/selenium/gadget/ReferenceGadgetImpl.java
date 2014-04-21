package com.atlassian.jira.webtest.framework.impl.selenium.gadget;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.query.ExpirationHandler;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.gadget.ReferenceGadget;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.page.dashboard.GadgetInfo;
import com.atlassian.jira.webtest.framework.page.dashboard.Dashboard;

/**
 * Default implementation of {@link com.atlassian.jira.webtest.framework.gadget.ReferenceGadget}.
 *
 * @since v4.3
 */
public class ReferenceGadgetImpl extends AbstractGadget implements ReferenceGadget
{
    private static final String REFERENCE_RESPONSE_CONTAINER_ID = "reference-response";
    
    private static final String NAME = GadgetInfo.gadgetName(ReferenceGadget.class);

    public ReferenceGadgetImpl(int id, Dashboard dashboard, SeleniumContext context)
    {
        super(id, NAME, dashboard, context);
    }

    public ReferenceGadgetImpl(Dashboard dashboard, SeleniumContext context)
    {
        super(NAME, dashboard, context);
    }

    @Override
    public TimedQuery<String> referenceEndpointResponse()
    {
        return inGadget(queries().forTextBuilder(id(REFERENCE_RESPONSE_CONTAINER_ID))
                .defaultTimeout(Timeouts.AJAX_ACTION).expirationHandler(ExpirationHandler.RETURN_NULL).build());
    }

}
