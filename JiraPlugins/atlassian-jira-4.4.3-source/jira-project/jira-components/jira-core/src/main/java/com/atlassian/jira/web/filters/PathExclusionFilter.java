package com.atlassian.jira.web.filters;

import com.atlassian.core.filters.AbstractHttpFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.atlassian.jira.util.lang.JiraStringUtils.asString;
import static com.atlassian.jira.web.filters.InitParamSupport.required;

/**
 * <p>
 * A decorating filter that is configured with a set of excluded servlet paths, basing upon which it decides
 * to run the decorated filter over a particular request.
 *
 * <p>
 * This filter requires two initialization parameters:
 * <ul>
 * <li><tt>path.exclusion.filter.className</tt> - name of the wrapped filter class
 * <li><tt>path.exclusion.filter.paths</tt> - comma-separated list of excluded servlet paths
 * </ul>
 *
 * <p>
 * The decorated filter is instantiated from a class name read from an init parameter. If the instantiation fails,
 * an <tt>IllegalStateException</tt> is raised from within the {@link #init(javax.servlet.FilterConfig)} method.
 *
 * <p>
 * The excluded paths patterns in the list may come in two forms:
 * <ul>
 * <li>exact pattern - exact servlet path that will be matched by means of the {@link String#equals(Object)} method
 * <li>wildcard pattern - part of the path followed by the '*' (wildcard) character, will match against all
 * requests, whose servlet path starts with the pattern (by means of {@link String#startsWith(String)})
 * </ul>
 *
 * @since v4.2
 */
public final class PathExclusionFilter extends AbstractHttpFilter
{
    private static final String PATHS_SEPARATOR = ",";
    private static final String WILDCARD = "*";

    private static final String INIT_LOG_MSG = "PathExclusionFilter [%s] initialized, decorated filter class [%s]"
     + "\nExact exclude patterns: %s\nWildcard exclude patterns: %s";
    private static final String FILTER_LOG_MSG = "PathExclusionFilter[%s].filter decorated filter %s for servlet path [%s] ";

    private static final Logger log = Logger.getLogger(PathExclusionFilter.class);


    public static enum InitParams
    {
        FILTER_CLASS(required("path.exclusion.filter.className")),
        EXCLUDED_PATHS(required("path.exclusion.filter.paths"));

        private final InitParamSupport support;

        private InitParams(InitParamSupport support)
        {
            this.support = support;
        }

        public String key()
        {
            return support.key();
        }

        String get(FilterConfig config)
        {
            return support.get(config);
        }
    }

    Filter realFilter;
    private String name;
    private final Set<String> excludedExactPatterns = new LinkedHashSet<String>();
    private final Set<String> excludedWildcardPatterns = new LinkedHashSet<String>();


    /**
     * {@inheritDoc}
     *
     * Initializes the path instantiates the wrapped filter
     *
     */
    public void init(final FilterConfig filterConfig) throws ServletException
    {
        super.init(filterConfig);
        this.name = filterConfig.getFilterName();
        this.realFilter = instantiateFilter(InitParams.FILTER_CLASS.get(filterConfig));
        this.realFilter.init(filterConfig);
        parseExcludedPaths(InitParams.EXCLUDED_PATHS.get(filterConfig));
        logInit();
    }

    /**
     * {@inheritDoc}
     *
     */
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        final String path  = request.getServletPath();
        if (isExcluded(path))
        {
            logFilter(path, true);
            chain.doFilter(request, response);
        }
        else
        {
            logFilter(path, false);
            realFilter.doFilter(request, response, chain);
        }
    }

    /**
     * {@inheritDoc}
     *
     */
    public void destroy()
    {
        this.realFilter.destroy();
        super.destroy();
    }

    private boolean isExcluded(String servletPath)
    {
        return matchesExact(servletPath) || matchesWildcard(servletPath);
    }

    private boolean matchesExact(final String servletPath)
    {
        return excludedExactPatterns.contains(servletPath);
    }

    private boolean matchesWildcard(final String servletPath)
    {
        for (String wildcardPattern : excludedWildcardPatterns)
        {
            if (servletPath.startsWith(wildcardPattern))
            {
                return true;
            }
        }
        return false;
    }


    private Filter instantiateFilter(final String filterClassName)
    {
        try
        {
            Class<? extends Filter> filterClass = loadClass(filterClassName);
            return filterClass.getConstructor().newInstance();
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Cannot instantiate configured filter class <" + filterClassName + ">", e);
        }
    }

    @SuppressWarnings ("unchecked")
    private Class<? extends  Filter> loadClass(final String filterClassName) throws ClassNotFoundException
    {
        return (Class<? extends Filter>) Class.forName(filterClassName);
    }

    private void parseExcludedPaths(String pathsParamValue)
    {
        if (StringUtils.isBlank(pathsParamValue))
        {
            log.warn(asString("No excluded paths configured for filter '", name, "'"));
            return;
        }
        for (String path : pathsParamValue.split(PATHS_SEPARATOR))
        {
            addToPaths(path.trim());
        }
    }

    private void addToPaths(final String path)
    {
        if (hasWildcardPattern(path))
        {
            excludedWildcardPatterns.add(removeWildcard(path));
        }
        else
        {
            excludedExactPatterns.add(path);
        }
    }

    private boolean hasWildcardPattern(final String path)
    {
        return path.endsWith(WILDCARD);
    }
    
    private String removeWildcard(String pathPattern)
    {
        return pathPattern.substring(0, pathPattern.length() - WILDCARD.length());
    }


    private void logInit()
    {
        log.debug(String.format(INIT_LOG_MSG, name, realFilter.getClass().getName(), excludedExactPatterns, excludedWildcardPatterns));
    }

    private void logFilter(String servletPath, boolean excluded)
    {
        log.debug(String.format(FILTER_LOG_MSG, name, excludedMsg(excluded), servletPath));
    }
    private String excludedMsg(boolean excluded)
    {
        return excluded ? "EXCLUDED" : "ACCEPTED";
    }
}
