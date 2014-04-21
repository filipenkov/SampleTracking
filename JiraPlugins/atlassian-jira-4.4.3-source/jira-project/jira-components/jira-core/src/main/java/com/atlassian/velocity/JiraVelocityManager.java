package com.atlassian.velocity;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.collect.CompositeMap;
import com.atlassian.util.concurrent.LazyReference;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class JiraVelocityManager extends DefaultVelocityManager
{
    private static final Logger log = Logger.getLogger(JiraVelocityManager.class);

    private final JiraAuthenticationContext authenticationContext;

    // do not use the superclass's engine as the accessor is synchronised due to lazy init.
    private final LazyReference<VelocityEngine> velocityEngine = new LazyReference<VelocityEngine>()
    {
        @Override
        protected VelocityEngine create() throws Exception
        {
            final VelocityEngine result = new VelocityEngine();
            initVe(result);
            return result;
        }
    };

    public JiraVelocityManager(final JiraAuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }

    @Override
    public DateFormat getDateFormat()
    {
        return authenticationContext.getOutlookDate().getCompleteDateFormat();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected VelocityContext createVelocityContext(final Map params)
    {
        // decorate the passed in map so we can modify it as the super call does...
        return super.createVelocityContext(CompositeMap.of(new HashMap<String, Object>(), (Map<String, Object>) params));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Map<String, ?> createContextParams(final String baseurl, final Map contextParameters)
    {
        final Map<String, Object> result = new HashMap<String, Object>();
        result.put(Key.BASE_URL, baseurl);
        result.put(Key.FORMATTER, new DelegateDateFormat(new DateFormatSupplier()));
        return CompositeMap.of((Map<String, Object>) contextParameters, result);
    }

    @Override
    protected VelocityEngine getVe()
    {
        return velocityEngine.get();
    }

    // todo: Until http://jira.atlassian.com/browse/VELOCITY-9 is implemented,
    // this needs to be kept in synch with the parents method.
    @Override
    protected void initVe(final VelocityEngine velocityEngine)
    {
        try
        {
            final Properties props = new Properties();

            try
            {
                props.load(ClassLoaderUtils.getResourceAsStream("velocity.properties", getClass()));
            }
            catch (final Exception e)
            {
                //log.warn("Could not configure DefaultVelocityManager from velocity.properties, manually configuring.");
                props.put("resource.loader", "class");
                props.put("class.resource.loader.description", "Velocity Classpath Resource Loader");
                props.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            }

            // override caching options if were in dev mode
            if (JiraSystemProperties.isDevMode())
            {
                // Turn off velocity caching
                props.put("class.resource.loader.cache", "false");
                props.put("velocimacro.library.autoreload", "true");
                props.put("plugin.resource.loader.cache", "false");
            }

            velocityEngine.init(props);
        }
        catch (final Exception e)
        {
            log.error("Exception initialising Velocity: " + e, e);
        }
    }

    static final class Key
    {
        static final String BASE_URL = "baseurl";
        static final String FORMATTER = "formatter";
    }

    class DateFormatSupplier extends LazyReference<DateFormat>
    {
        @Override
        protected DateFormat create()
        {
            return getDateFormat();
        }
    }
}
