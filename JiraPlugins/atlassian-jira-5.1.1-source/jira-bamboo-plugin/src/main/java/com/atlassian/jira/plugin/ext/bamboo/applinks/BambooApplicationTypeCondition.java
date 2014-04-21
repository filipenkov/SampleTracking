package com.atlassian.jira.plugin.ext.bamboo.applinks;

import java.util.Map;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.application.bamboo.BambooApplicationType;
import com.atlassian.applinks.spi.util.TypeAccessor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import org.apache.log4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Condition that passes if the context {@link com.atlassian.applinks.api.ApplicationLink} is BambooApplicationType.
 */
public class BambooApplicationTypeCondition implements Condition
{
    private static final Logger log = Logger.getLogger(BambooApplicationTypeCondition.class);
    private static final Class<BambooApplicationType> BAMBOO_TYPE = BambooApplicationType.class;

    private final TypeAccessor typeAccessor;

    public BambooApplicationTypeCondition(final TypeAccessor typeAccessor)
    {
        this.typeAccessor = checkNotNull(typeAccessor, "typeAccessor");
    }

    public void init(final Map<String, String> params) throws PluginParseException
    {
    }

    public boolean shouldDisplay(final Map<String, Object> context)
    {
        final ApplicationLink applicationLink = (ApplicationLink) context.get("applicationLink");

        boolean shouldDisplay = true;
        if (applicationLink == null)
        {
            log.warn("This page has no applicationLink context. Ignoring " + this.getClass().getSimpleName());
        }
        else
        {
            final ApplicationType type = typeAccessor.getApplicationType(BAMBOO_TYPE);
            if (type == null)
            {
                log.warn(new StringBuilder()
                         .append("Type '")
                         .append(BAMBOO_TYPE.getName())
                         .append("' specified in ")
                         .append(getClass())
                         .append(" is not installed, condition evaluates to false.")
                         .toString());
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
