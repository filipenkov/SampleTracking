package com.atlassian.plugins.rest.common.security.jersey;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.tracker.DefaultPluginModuleTracker;
import com.atlassian.plugin.tracker.PluginModuleTracker;
import com.atlassian.plugins.rest.common.security.CorsAllowed;
import com.atlassian.plugins.rest.common.security.descriptor.CorsDefaults;
import com.atlassian.plugins.rest.common.security.descriptor.CorsDefaultsModuleDescriptor;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import org.springframework.beans.factory.DisposableBean;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

/**
 * Factory for the Cross-Origin Resource Sharing resource filter, triggering off {@link com.atlassian.plugins.rest.common.security.CorsAllowed}.
 *
 * @since 2.6
 */
@Provider
public class CorsResourceFilterFactory implements ResourceFilterFactory, DisposableBean
{
    private final PluginModuleTracker<CorsDefaults,CorsDefaultsModuleDescriptor> tracker;

    public CorsResourceFilterFactory(PluginAccessor pluginAccessor, PluginEventManager pluginEventManager)
    {
        tracker = new DefaultPluginModuleTracker<CorsDefaults, CorsDefaultsModuleDescriptor>(pluginAccessor, pluginEventManager, CorsDefaultsModuleDescriptor.class);
    }

    public List<ResourceFilter> create(final AbstractMethod method)
    {
        if (method.isAnnotationPresent(CorsAllowed.class)
                || method.getResource().isAnnotationPresent(CorsAllowed.class)
                || method.getResource().getResourceClass().getPackage().isAnnotationPresent(CorsAllowed.class))
        {
            String targetMethod = HttpMethod.GET;
            for (Annotation ann : method.getAnnotations())
            {
                HttpMethod m = ann.annotationType().getAnnotation(HttpMethod.class);
                if (m != null)
                {
                    targetMethod = m.value();
                    break;
                }
            }

            return Collections.<ResourceFilter>singletonList(new CorsResourceFilter(tracker, targetMethod));
        }
        return Collections.emptyList();
    }

    public void destroy() throws Exception
    {
        tracker.close();
    }
}
