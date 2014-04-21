package com.atlassian.applinks.core.plugin.condition;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.core.InternalTypeAccessor;
import com.atlassian.applinks.core.webfragment.WebFragmentContext;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Condition that passes if the context {@link ApplicationLink} is of the specified type.
 * <p/>
 * param: type - (required) the fully qualified classname of a subclass of {@link ApplicationType}
 *
 * @since 3.0
 */
public class ApplicationLinkOfTypeCondition implements Condition
{
    private static final Logger log = LoggerFactory.getLogger(ApplicationLinkOfTypeCondition.class);

    private String typeClassName;
    private final InternalTypeAccessor typeAccessor;

    public ApplicationLinkOfTypeCondition(final InternalTypeAccessor typeAccessor)
    {
        this.typeAccessor = typeAccessor;
    }

    public void init(final Map<String, String> params) throws PluginParseException
    {
        typeClassName = params.get("type");
        if (typeClassName == null)
        {
            throw new PluginParseException("Must specify a type parameter for " + this.getClass().getSimpleName());
        }
    }

    public boolean shouldDisplay(final Map<String, Object> context)
    {
        final ApplicationLink applicationLink = (ApplicationLink) context.get(WebFragmentContext.APPLICATION_LINK);

        boolean shouldDisplay = true;
        if (applicationLink == null)
        {
            log.warn("This page has no applicationLink context. Ignoring " + this.getClass().getSimpleName());
        }
        else
        {
            final ApplicationType type = typeAccessor.loadApplicationType(typeClassName);
            if (type == null)
            {
                log.warn("type '" + typeClassName + "' specified in " + this.getClass().getSimpleName() +
                        " is not installed, condition evaluates to false.");
                shouldDisplay = false;
            }
            else
            {
                shouldDisplay = type.getClass().isAssignableFrom(applicationLink.getType().getClass());
            }
        }
        return shouldDisplay;
    }


}
