package com.atlassian.renderer.v2.components;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.embedded.EmbeddedAudio;
import com.atlassian.renderer.embedded.EmbeddedFlash;
import com.atlassian.renderer.embedded.EmbeddedQuicktime;
import com.atlassian.renderer.embedded.EmbeddedRealMedia;
import com.atlassian.renderer.embedded.EmbeddedResource;
import com.atlassian.renderer.embedded.EmbeddedResourceParser;
import com.atlassian.renderer.embedded.EmbeddedWindowsMedia;
import com.atlassian.renderer.v2.RenderMode;

/**
 *
 *
 */
public class EmbeddedObjectRendererComponent extends AbstractEmbeddedRendererComponent
{

    public boolean shouldRender(RenderMode renderMode)
    {
        return renderMode.renderEmbeddedObjects();
    }

    protected EmbeddedResource findResource(final RenderContext context, final EmbeddedResourceParser parser, final String originalString)
    {

        if (EmbeddedResource.matchesType(parser))
            return new EmbeddedResource(parser);

        if (EmbeddedFlash.matchesType(parser))
            return new EmbeddedFlash(parser);

        if (EmbeddedQuicktime.matchesType(parser))
            return new EmbeddedQuicktime(parser);

        if (EmbeddedWindowsMedia.matchesType(parser))
            return new EmbeddedWindowsMedia(parser);

        if (EmbeddedAudio.matchesType(parser))
            return new EmbeddedAudio(parser);

        if (EmbeddedRealMedia.matchesType(parser))
            return new EmbeddedRealMedia(parser);

        // If nothing matches, return null
        return null;
    }
}
