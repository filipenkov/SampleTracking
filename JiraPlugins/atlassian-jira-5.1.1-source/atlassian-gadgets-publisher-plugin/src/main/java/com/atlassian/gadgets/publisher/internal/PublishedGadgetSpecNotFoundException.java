package com.atlassian.gadgets.publisher.internal;

import com.atlassian.gadgets.GadgetSpecUriNotAllowedException;

public class PublishedGadgetSpecNotFoundException extends GadgetSpecUriNotAllowedException
{
    public PublishedGadgetSpecNotFoundException(Throwable e)
    {
        super(e);
    }

    public PublishedGadgetSpecNotFoundException(String message)
    {
        super(message);
    }
}
