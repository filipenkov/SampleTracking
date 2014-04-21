package com.atlassian.administration.quicksearch.impl.spi;

import com.atlassian.administration.quicksearch.spi.AdminWebItem;
import com.atlassian.administration.quicksearch.spi.RenderingContext;
import com.atlassian.plugin.web.descriptors.WebFragmentModuleDescriptor;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link com.atlassian.administration.quicksearch.spi.AdminWebItem} based
 * on the Atlassian web-fragments API.
 *
 * @since 1.0
 */
public abstract class AbstractDefaultAdminWebItem<D extends WebFragmentModuleDescriptor<Void>> implements AdminWebItem
{
    protected final D descriptor;
    protected final RenderingContext context;

    public AbstractDefaultAdminWebItem(D descriptor, RenderingContext context)
    {
        this.descriptor = checkNotNull(descriptor, "descriptor");
        this.context = checkNotNull(context, "context");
    }

    @Override
    public String getId()
    {
        return descriptor.getKey();
    }

    public String getCompleteKey()
    {
        return descriptor.getCompleteKey();
    }

    @Override
    public String getLabel()
    {
        return descriptor.getWebLabel().getDisplayableLabel(context.getRequest(), context.getContextMap());
    }

    @Override
    public Map<String, String> getParameters()
    {
        return descriptor.getParams();
    }
}
