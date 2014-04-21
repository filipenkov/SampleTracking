package com.atlassian.plugins.rest.module.servlet;

import static com.google.common.collect.Ordering.natural;

import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.servlet.DefaultServletModuleManager;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.plugin.servlet.descriptors.ServletFilterModuleDescriptor;
import com.atlassian.plugin.servlet.descriptors.ServletModuleDescriptor;
import com.atlassian.plugin.servlet.filter.FilterDispatcherCondition;
import com.atlassian.plugin.servlet.filter.FilterLocation;
import com.atlassian.plugin.servlet.util.DefaultPathMapper;
import com.atlassian.plugin.servlet.util.PathMapper;
import com.atlassian.plugins.rest.module.RestApiContext;
import com.atlassian.plugins.rest.module.RestServletFilterModuleDescriptor;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;

import org.apache.commons.lang.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.util.Comparator;
import java.util.SortedSet;

/**
 * <p>Servlet module manager to handle REST servlets.</p>
 */
public class DefaultRestServletModuleManager implements RestServletModuleManager
{
    private static final RestServletFilterModuleDescriptorComparator VALUE_COMPARATOR = new RestServletFilterModuleDescriptorComparator();

    /**
     * Multimap of filter module descriptors, the key is the "api path" of the REST module descriptor.
     */
    private final SortedSetMultimap<String, RestServletFilterModuleDescriptor> filterModuleDescriptors =
            Multimaps.synchronizedSortedSetMultimap(TreeMultimap.<String, RestServletFilterModuleDescriptor>create(natural(), VALUE_COMPARATOR));

    private final ServletModuleManager delegateModuleManager;
    private final PathMapper filterPathMapper;
    private final String path;

    public DefaultRestServletModuleManager(PluginEventManager pluginEventManager, String path)
    {
        this.filterPathMapper = new DefaultPathMapper();
        this.delegateModuleManager = new DefaultServletModuleManager(pluginEventManager, new DefaultPathMapper(), filterPathMapper);
        this.path = StringUtils.isNotBlank(path) ? path : "";
    }

    DefaultRestServletModuleManager(ServletModuleManager delegate, PathMapper filterPathMapper, String path)
    {
        this.filterPathMapper = filterPathMapper;
        this.delegateModuleManager = delegate;
        this.path = StringUtils.isNotBlank(path) ? path : "";
    }

    public void addServletModule(ServletModuleDescriptor descriptor)
    {
        delegateModuleManager.addServletModule(descriptor);
    }

    public HttpServlet getServlet(String path, ServletConfig servletConfig) throws ServletException
    {
        return delegateModuleManager.getServlet(path, servletConfig);
    }

    public void removeServletModule(ServletModuleDescriptor descriptor)
    {
        delegateModuleManager.removeServletModule(descriptor);
    }

    public void addFilterModule(ServletFilterModuleDescriptor descriptor)
    {
        if (descriptor instanceof RestServletFilterModuleDescriptor)
        {
            final RestServletFilterModuleDescriptor restServletFilterModuleDescriptor = (RestServletFilterModuleDescriptor) descriptor;
            final RestServletFilterModuleDescriptor latest = getRestServletFilterModuleDescriptorForLatest(restServletFilterModuleDescriptor.getBasePath());
            if (VALUE_COMPARATOR.compare(latest, restServletFilterModuleDescriptor) < 0)
            {
                if (latest != null)
                {
                    filterPathMapper.put(latest.getCompleteKey(), null);
                    for (String path : latest.getPaths())
                    {
                        filterPathMapper.put(latest.getCompleteKey(), path);
                    }
                }
                filterPathMapper.put(descriptor.getCompleteKey(), getPathPattern(restServletFilterModuleDescriptor.getBasePath()));
            }
            filterModuleDescriptors.put(restServletFilterModuleDescriptor.getBasePath(), restServletFilterModuleDescriptor);
        }
        delegateModuleManager.addFilterModule(descriptor);
    }

    private RestServletFilterModuleDescriptor getRestServletFilterModuleDescriptorForLatest(String path)
    {
        if (path == null)
        {
            return null;
        }

        final SortedSet<RestServletFilterModuleDescriptor> moduleDescriptors = filterModuleDescriptors.get(path);
        return moduleDescriptors.isEmpty() ? null : moduleDescriptors.last();
    }

    public Iterable<Filter> getFilters(FilterLocation location, String pathInfo, FilterConfig filterConfig) throws ServletException
    {
        return delegateModuleManager.getFilters(location, pathInfo, filterConfig);
    }

    public Iterable<Filter> getFilters(FilterLocation location, String pathInfo, FilterConfig filterConfig, FilterDispatcherCondition filterDispatcherCondition) throws ServletException
    {
        return delegateModuleManager.getFilters(location, StringUtils.removeStart(pathInfo, path), filterConfig, filterDispatcherCondition);
    }

    public void removeFilterModule(ServletFilterModuleDescriptor descriptor)
    {
        if (descriptor instanceof RestServletFilterModuleDescriptor)
        {
            final RestServletFilterModuleDescriptor restServletFilterModuleDescriptor = (RestServletFilterModuleDescriptor) descriptor;

            // check if it was the latest, before removing from the MultiMap
            RestServletFilterModuleDescriptor latest = getRestServletFilterModuleDescriptorForLatest(restServletFilterModuleDescriptor.getBasePath());
            filterModuleDescriptors.remove(restServletFilterModuleDescriptor.getBasePath(), restServletFilterModuleDescriptor);

            if (latest != null && latest.getCompleteKey().equals(descriptor.getCompleteKey()))
            {
                // latest has changed as we have removed an item from the multimap
                latest = getRestServletFilterModuleDescriptorForLatest(restServletFilterModuleDescriptor.getBasePath());
                if (latest != null)
                {
                    filterPathMapper.put(latest.getCompleteKey(), getPathPattern(latest.getBasePath()));
                }
            }
        }

        // remaing mapping of the descriptor will be removed by this call.
        delegateModuleManager.removeFilterModule(descriptor);
    }

    String getPathPattern(String basePath)
    {
        return basePath + RestApiContext.LATEST + RestApiContext.ANY_PATH_PATTERN;
    }

    private static final class RestServletFilterModuleDescriptorComparator implements Comparator<RestServletFilterModuleDescriptor>
    {
        public int compare(RestServletFilterModuleDescriptor descriptor1, RestServletFilterModuleDescriptor descriptor2)
        {
            if (descriptor1 == null)
            {
                return -1;
            }
            if (descriptor2 == null)
            {
                return +1;
            }
            return descriptor1.getVersion().compareTo(descriptor2.getVersion());
        }
    }
}
