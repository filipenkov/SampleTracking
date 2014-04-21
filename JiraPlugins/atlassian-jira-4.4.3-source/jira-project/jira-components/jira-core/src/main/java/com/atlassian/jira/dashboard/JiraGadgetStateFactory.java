package com.atlassian.jira.dashboard;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.gadgets.dashboard.spi.GadgetStateFactory;
import com.atlassian.jira.portal.OfbizPortletConfigurationStore;
import com.atlassian.jira.util.dbc.Assertions;

import java.net.URI;

/**
 * Generates a gadgetState for gadgets to be added to the dashboard.  Legacy portlets will be a added
 * with a different color compared to normal gagdets.
 *
 * @since v4.0
 */
public class JiraGadgetStateFactory implements GadgetStateFactory
{
    public GadgetState createGadgetState(final URI uri)
    {
        Assertions.notNull("uri", uri);

        final Long seqId = CoreFactory.getGenericDelegator().getNextSeqId(OfbizPortletConfigurationStore.TABLE);
        final GadgetState.Builder builder = GadgetState.gadget(GadgetId.valueOf(seqId.toString())).specUri(uri);
        return builder.color(Color.color1).build();
    }
}
