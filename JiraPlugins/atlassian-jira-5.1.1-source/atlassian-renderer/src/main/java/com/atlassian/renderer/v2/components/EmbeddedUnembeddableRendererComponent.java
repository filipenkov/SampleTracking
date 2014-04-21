package com.atlassian.renderer.v2.components;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.embedded.EmbeddedResource;
import com.atlassian.renderer.embedded.EmbeddedResourceParser;
import com.atlassian.renderer.embedded.UnembeddableObject;
import com.atlassian.renderer.v2.RenderMode;

/**
 *
 *
 */
public class EmbeddedUnembeddableRendererComponent extends AbstractEmbeddedRendererComponent
{

    public boolean shouldRender(RenderMode renderMode)
    {
        return (renderMode.renderImages() || renderMode.renderEmbeddedObjects());
    }

    protected EmbeddedResource findResource(final RenderContext context, final EmbeddedResourceParser parser, final String originalString)
    {
           return new UnembeddableObject(parser);
    }
}