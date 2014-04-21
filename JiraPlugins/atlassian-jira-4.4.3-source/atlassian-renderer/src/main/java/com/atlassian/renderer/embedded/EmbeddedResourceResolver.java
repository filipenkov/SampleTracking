package com.atlassian.renderer.embedded;

/**
 *
 *
 */
public class EmbeddedResourceResolver
{
    /**
     * Factory method for creating EmbeddedResource instances from the specified string input.
     * <p/>Note: the input string must specify a valid embedded resource.
     *
     * @param str
     * @return
     * @see EmbeddedResourceParser
     */
    public static EmbeddedResource create(String str)
    {
        EmbeddedResourceParser parser = new EmbeddedResourceParser(str);

        if (EmbeddedResource.matchesType(parser))
            return new EmbeddedResource(parser);

        if (EmbeddedImage.matchesType(parser))
            return new EmbeddedImage(parser);

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

        if (UnembeddableObject.matchesType(parser))
            return new UnembeddableObject(parser);

        // If nothing matches, try using an EmbeddedObject
        return new EmbeddedObject(parser);
    }
}
