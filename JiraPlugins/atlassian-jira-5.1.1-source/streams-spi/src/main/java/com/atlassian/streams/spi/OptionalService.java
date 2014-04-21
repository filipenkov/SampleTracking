package com.atlassian.streams.spi;

import com.atlassian.streams.api.common.Option;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.osgi.context.BundleContextAware;

import static com.atlassian.streams.api.common.Option.option;
import static com.google.common.base.Preconditions.checkNotNull;

public abstract class OptionalService<T> implements BundleContextAware, InitializingBean, DisposableBean
{
    private final Class<T> type;

    private BundleContext bundleContext;
    private ServiceTracker tracker;

    public OptionalService(Class<T> type)
    {
        this.type = checkNotNull(type, "type");
    }

    protected final Option<T> getService()
    {
        return option(type.cast(tracker.getService()));
    }

    public final void afterPropertiesSet() throws Exception
    {
        tracker = new ServiceTracker(bundleContext, type.getName(), null);
        tracker.open();
    }

    public final void destroy() throws Exception
    {
        tracker.close();
    }

    public final void setBundleContext(BundleContext bundleContext)
    {
        this.bundleContext = bundleContext;
    }
}
