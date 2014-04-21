package com.atlassian.applinks.core.webfragment;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.EntityLink;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides the rendering context for web fragments displayed by the applinks plugin
 *
 * @see WebFragmentHelper
 */
public class WebFragmentContext
{
    // keys for values in the render context Map
    public static final String APPLICATION_LINK = "applicationLink";
    public static final String ENTITY_LINK = "entityLink";

    private final Map<String, Object> contextMap;

    /* (non-Javadoc)
     * Use {@link Builder}
     */
    private WebFragmentContext(final Map<String, Object> contextMap) {
        this.contextMap = ImmutableMap.copyOf(contextMap);
    }

    public Map<String, Object> getContextMap() {
        return contextMap;
    }

    public static class Builder {

        private Map<String, Object> contextMap = new HashMap<String, Object>();

        public Builder applicationLink(final ApplicationLink applicationLink) {
            contextMap.put(APPLICATION_LINK, applicationLink);
            return this;
        }

        public Builder entityLink(final EntityLink entityLink) {
            contextMap.put(ENTITY_LINK, entityLink);
            return this;
        }

        public WebFragmentContext build() {
            final WebFragmentContext context = new WebFragmentContext(contextMap);
            contextMap = ImmutableMap.of(); // prevent further modification of the map via the builder
            return context;
        }

    }

}
