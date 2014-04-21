package com.atlassian.applinks.core.plugin.condition;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.core.InternalTypeAccessor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/**
 * Condition that passes if at least one {@link ApplicationLink} (of the specified type, if supplied) is configured.
 *
 * param: type - (optional) the fully qualified classname of a subclass of {@link ApplicationType}
 *
 * @since 3.0
 */
public class ApplicationLinksExistCondition implements Condition
{
    private static final Logger log = LoggerFactory.getLogger(ApplicationLinksExistCondition.class);

    private final ApplicationLinkService applicationLinkService;
    private final InternalTypeAccessor typeAccessor;

    private String typeClassName;

    public ApplicationLinksExistCondition(final ApplicationLinkService applicationLinkService,
                                          final InternalTypeAccessor typeAccessor)
    {
        this.applicationLinkService = applicationLinkService;
        this.typeAccessor = typeAccessor;
    }

    public void init(final Map<String, String> params) throws PluginParseException
    {
        typeClassName = params.get("type");
    }

    public boolean shouldDisplay(final Map<String, Object> context)
    {
        final Iterable<ApplicationLink> links;
        if (typeClassName != null)
        {
            final ApplicationType type = typeAccessor.loadApplicationType(typeClassName);
            if (type == null)
            {
                log.warn("type specified for ApplicationLinksExistCondition " + typeClassName + " is not installed, condition evaluates to false.");
                return false;
            }
            links = applicationLinkService.getApplicationLinks(type.getClass());
        }
        else
        {
            links = applicationLinkService.getApplicationLinks();
        }
        return !Iterables.isEmpty(links);
    }
}
