package com.atlassian.renderer.embedded;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderUtils;

public class UnembeddableObjectRenderer implements EmbeddedResourceRenderer
{
    public String renderResource(EmbeddedResource resource, RenderContext context)
    {
        return RenderUtils.error("Unable to embed resource: " + resource.getFilename() + " of type " + resource.getType());
    }
}
