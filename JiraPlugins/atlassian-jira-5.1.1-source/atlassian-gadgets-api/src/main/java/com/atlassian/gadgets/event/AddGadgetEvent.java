package com.atlassian.gadgets.event;

import java.net.URI;

/**
 * When a gadget is added to the directory, this event will be fired.  Host applications can listen to this event and
 * add an entry to their whitelist configuration for this gadgetUri to ensure that shindig can retrieve this gadget.
 * <p/>
 * Please note that this event will be fired <strong>before</strong> the gadget spec has been validated.  This is so
 * that the host container has a chance to whitelist the location first such that shindig is actually allowed to
 * retrieve the gadget spec so that it can be validated.
 *
 * @since v3.0
 */
public class AddGadgetEvent
{
    private final URI gadgetUri;

    public AddGadgetEvent(final URI gadgetUri)
    {
        this.gadgetUri = gadgetUri;
    }

    public URI getGadgetUri()
    {
        return gadgetUri;
    }
}
