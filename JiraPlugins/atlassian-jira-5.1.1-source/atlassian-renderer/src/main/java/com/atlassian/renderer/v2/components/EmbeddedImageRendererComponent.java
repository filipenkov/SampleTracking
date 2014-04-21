package com.atlassian.renderer.v2.components;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.embedded.EmbeddedImage;
import com.atlassian.renderer.embedded.EmbeddedResource;
import com.atlassian.renderer.embedded.EmbeddedResourceParser;
import com.atlassian.renderer.embedded.EmbeddedResourceRenderer;
import com.atlassian.renderer.v2.RenderMode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 *
 */
public class EmbeddedImageRendererComponent extends AbstractEmbeddedRendererComponent
{

    public boolean shouldRender(RenderMode renderMode)
    {
        return renderMode.renderImages();
    }
    
    protected EmbeddedResource findResource(final RenderContext context, final EmbeddedResourceParser parser, final String originalString)
    {
        if (EmbeddedImage.matchesType(parser))
        {
            return  new EmbeddedImage(parser);
        }
        else
        {
           return null;
        }
    }
}
