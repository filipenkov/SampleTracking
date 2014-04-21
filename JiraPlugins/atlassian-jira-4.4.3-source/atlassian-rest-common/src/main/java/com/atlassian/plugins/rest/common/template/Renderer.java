package com.atlassian.plugins.rest.common.template;

import java.io.IOException;

/**
 * Provides a means for REST plugins to use the REST renderer programmatically
 */
public interface Renderer
{
    void render(Object model, String template) throws IOException;
}
