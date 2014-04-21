package com.atlassian.administration.quicksearch.impl.spi;

import com.atlassian.administration.quicksearch.spi.AdminLink;
import com.atlassian.administration.quicksearch.spi.RenderingContext;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;

import javax.annotation.Nonnull;

/**
 * Default implementation of {@link AdminLink} based on the Atlassian web-fragments API.
 *
 * @since 1.0
 */
public class DefaultAdminLink extends AbstractDefaultAdminWebItem<WebItemModuleDescriptor> implements AdminLink
{
    public DefaultAdminLink(WebItemModuleDescriptor descriptor, RenderingContext context)
    {
        super(descriptor, context);
    }

    @Nonnull
    @Override
    public String getLinkUrl()
    {
        return descriptor.getLink().getDisplayableUrl(context.getRequest(), context.getContextMap());
    }
}
