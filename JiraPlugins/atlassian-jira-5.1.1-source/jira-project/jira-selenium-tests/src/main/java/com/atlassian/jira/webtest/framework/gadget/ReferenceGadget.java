package com.atlassian.jira.webtest.framework.gadget;

import com.atlassian.jira.webtest.framework.core.query.TimedQuery;

/**
 * <p>
 * Reference gadget representation.
 *
 * <p>
 * Reference gadget does not do a lot except for calling the reference (non-)endpoint resource and display its
 * answer.
 *
 * @since v4.3
 */
public interface ReferenceGadget extends Gadget
{

    /**
     * Return reference endpoint response as visible by the user of this gadget.
     *
     * @return reference resource response displayed within this reference gadget
     */
    TimedQuery<String> referenceEndpointResponse();
}
