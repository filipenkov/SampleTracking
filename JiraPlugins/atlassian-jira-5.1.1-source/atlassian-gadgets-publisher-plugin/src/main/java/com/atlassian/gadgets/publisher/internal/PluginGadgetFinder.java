package com.atlassian.gadgets.publisher.internal;

import com.atlassian.gadgets.plugins.PluginGadgetSpec;
import com.atlassian.gadgets.plugins.PluginGadgetSpecEventListener;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.tracker.DefaultPluginModuleTracker;
import com.atlassian.plugin.tracker.PluginModuleTracker;
import com.atlassian.sal.api.executor.ThreadLocalDelegateExecutorFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Notifies a series of {@link PluginGadgetSpecEventListener} implementations when gadget specs are enabled and disabled
 * by plugins.  When the constructor completes, all of the specified event listeners will have been notified of the
 * currently-enabled plugin gadget specs, by calling each listener's {@link PluginGadgetSpecEventListener#pluginGadgetSpecEnabled(PluginGadgetSpec)}
 * method for each enabled {@link PluginGadgetSpec} retrieved from {@code pluginAccessor}.  Subsequently, for the entire
 * lifespan of this object, each listener will be notified as plugin gadget specs are enabled by calling its {@code
 * pluginGadgetSpecEnabled(PluginGadgetSpec)} method for each enabled gadget spec, and as plugin gadget specs are
 * disabled by calling its {@link PluginGadgetSpecEventListener#pluginGadgetSpecDisabled(PluginGadgetSpec)} method for
 * each disabled gadget spec.  These calls may happen on multiple threads, so all {@code PluginGadgetSpecEventListener}
 * implementations <strong>must</strong> be thread-safe.
 * <p/>
 * This class does <em>not</em> attempt to guarantee that listener methods will only be called once per event, so
 * listener implementations should be prepared to handle multiple {@code pluginGadgetSpecEnabled} calls for the same
 * gadget spec without an intervening {@code pluginGadgetSpecDisabled} call, as well as {@code pluginGadgetSpecDisabled}
 * called with a gadget spec that was not previously passed to a {@code pluginGadgetSpecEnabled} call.
 * <p/>
 * This class will <em>never</em> pass a {@code null} argument to listener methods.
 * <p/>
 * The {@code eventListeners} iterable is not copied, and is considered "live" in the sense that listeners can be added
 * or removed from the underlying collection in order to register and unregister listeners to be notified.  The iterable
 * <strong>must</strong> be thread-safe and support concurrent iteration and modification without ever throwing {@link
 * java.util.ConcurrentModificationException}, and without requiring explicit synchronization or locking by the caller.
 * Any {@link java.util.Iterator} returned <strong>must</strong> support the contract of {@code Iterator}. Specifically,
 * if {@link java.util.Iterator#hasNext()} is called at any time and returns {@code true}, the next call to {@link
 * java.util.Iterator#next()}, whenever it occurs, <strong>must</strong> return a non-{@code null} {@code
 * PluginGadgetSpecEventListener}, and <strong>must not</strong> throw {@link java.util.NoSuchElementException} or any
 * other exception.  The returned {@code PluginGadgetSpecEventListener} <strong>may</strong>, however, refuse to handle
 * further events by throwing a {@link RuntimeException} implementation of its choosing.  This will not affect the
 * notification of other listeners.
 * <p/>
 * The intent is that {@code eventListeners} will be an OSGi service collection injected by Spring Dynamic Modules,
 * which guarantees the behavior documented above.
 *
 * @see <a href="http://static.springframework.org/osgi/docs/1.1.3/reference/html/service-registry.html#service-registry:refs:collection">Spring
 *      Dynamic Modules Reference Guide: Referencing A Collection Of Services</a>
 */
public class PluginGadgetFinder implements DisposableBean
{
    private final Log log = LogFactory.getLog(getClass());

    /*
     * This will be injected as a collection of OSGi service reference proxies, which means that elements could
     * potentially disappear at runtime. Ensure that anytime you dereference an element, you catch
     * ServiceUnavailableException and handle it appropriately.
     */
    private final Iterable<PluginGadgetSpecEventListener> eventListeners;
    private final PluginModuleTracker<PluginGadgetSpec, GadgetModuleDescriptor> gadgetTracker;
    private final ExecutorService executor;

    /**
     * Creates a new {@code PublishedGadgetFinder}.
     *
     * @param pluginAccessor the {@code PluginAccessor} for the host application, used to get the list of currently
     * enabled plugin modules.  Must not be {@code null}, or a {@code NullPointerException} will be thrown.
     * @param pluginEventManager the {@code PluginEventManager} of the host application, used to register this {@code
     * PublishedGadgetFinder} to receive events when plugin modules are enabled and disabled. Must not be {@code null},
     * or a {@code NullPointerException} will be thrown.
     * @param eventListeners the event listeners that will be notified when gadget specs are enabled or disabled. Must
     * not be {@code null}, or a {@code NullPointerException} will be thrown.  Must be safe for concurrent iteration and
     * modification, and all elements returned by iterating over it must be thread-safe.
     * @throws NullPointerException if any argument is {@code null}
     */
    public PluginGadgetFinder(PluginAccessor pluginAccessor, PluginEventManager pluginEventManager,
            ThreadLocalDelegateExecutorFactory executorFactory, Iterable<PluginGadgetSpecEventListener> eventListeners)
    {
        checkNotNull(pluginEventManager, "pluginEventManager");
        checkNotNull(pluginAccessor, "pluginAccessor");
        this.eventListeners = checkNotNull(eventListeners, "eventListener");
        this.executor = executorFactory.createExecutorService(Executors.newSingleThreadExecutor(new GadgetInstallationThreadFactory()));

        this.gadgetTracker = new DefaultPluginModuleTracker<PluginGadgetSpec, GadgetModuleDescriptor>(pluginAccessor,
                pluginEventManager, GadgetModuleDescriptor.class,
                new PluginModuleTracker.Customizer<PluginGadgetSpec, GadgetModuleDescriptor>()
                {
                    public GadgetModuleDescriptor adding(final GadgetModuleDescriptor gadgetModuleDescriptor)
                    {
                        executor.execute(new Runnable()
                        {
                            public void run()
                            {
                                installGadget(gadgetModuleDescriptor.getModule());
                            }
                        });
                        return gadgetModuleDescriptor;
                    }

                    public void removed(final GadgetModuleDescriptor gadgetModuleDescriptor)
                    {
                        removeGadget(gadgetModuleDescriptor.getModule());
                    }
                });
    }

    private void installGadget(PluginGadgetSpec gadgetSpec)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Publishing gadget spec " + gadgetSpec);
        }
        for (PluginGadgetSpecEventListener eventListener : eventListeners)
        {
            installGadget(eventListener, gadgetSpec);
        }
    }

    private void installGadget(PluginGadgetSpecEventListener eventListener, PluginGadgetSpec gadgetSpec)
    {
        try
        {
            eventListener.pluginGadgetSpecEnabled(gadgetSpec);
        }
        catch (RuntimeException e)
        {
            warn("Gadget spec " + gadgetSpec + " could not be added to " + eventListener + ", ignoring", e);
        }
    }

    private void removeGadget(PluginGadgetSpec gadgetSpec)
    {
        for (PluginGadgetSpecEventListener eventListener : eventListeners)
        {
            removeGadget(eventListener, gadgetSpec);
        }
    }

    private void removeGadget(PluginGadgetSpecEventListener eventListener, PluginGadgetSpec gadgetSpec)
    {
        try
        {
            eventListener.pluginGadgetSpecDisabled(gadgetSpec);
        }
        catch (RuntimeException e)
        {
            warn("Gadget spec " + gadgetSpec + " could not be removed from " + eventListener + ", ignoring", e);
        }
    }

    private void warn(String message, Throwable t)
    {
        if (log.isDebugEnabled())
        {
            log.warn(message, t);
        }
        else
        {
            log.warn(message);
        }
    }

    public void destroy() throws Exception
    {
        gadgetTracker.close();
        executor.shutdown();
    }

    private static class GadgetInstallationThreadFactory implements ThreadFactory
    {
        private final AtomicLong threadId = new AtomicLong(0);

        public Thread newThread(final Runnable runnable)
        {
            final Thread t = new Thread(runnable, "GadgetInstallationThread-" + threadId.incrementAndGet());
            t.setDaemon(true);
            return t;
        }
    }
}
