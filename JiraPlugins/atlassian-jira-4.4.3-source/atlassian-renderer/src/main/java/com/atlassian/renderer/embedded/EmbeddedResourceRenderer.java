package com.atlassian.renderer.embedded;

import com.atlassian.renderer.RenderContext;

/**
 * Interface for embedded resource renderers.
 *
 */
public interface EmbeddedResourceRenderer {

    /**
     * Render the embedded resource to a string.
     * @param resource to be rendered
     * @param context of render
     */
    String renderResource(EmbeddedResource resource, RenderContext context);
}
