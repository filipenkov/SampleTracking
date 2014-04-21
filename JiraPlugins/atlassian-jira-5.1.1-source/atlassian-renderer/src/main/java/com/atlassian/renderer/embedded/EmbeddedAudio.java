package com.atlassian.renderer.embedded;

/**
 * Created by IntelliJ IDEA.
 * User: Jeremy Higgs
 * Date: 23/09/2005
 * Time: 16:12:13
 */

public class EmbeddedAudio extends EmbeddedObject
{
    public static String RESOURCE_TYPE = "audio/";

    public EmbeddedAudio(String string)
    {
        this(new EmbeddedResourceParser(string));
    }

    public EmbeddedAudio(EmbeddedResourceParser parser)
    {
        super(parser);

        // If we don't define the width and height, the player won't show up on the page!
        if (!properties.containsKey("width"))
            properties.put("width", "300");
        if (!properties.containsKey("height"))
            properties.put("height", "42");
    }

    public static boolean matchesType(EmbeddedResourceParser parser)
    {
        return (parser.getType().startsWith(RESOURCE_TYPE));
    }
}
