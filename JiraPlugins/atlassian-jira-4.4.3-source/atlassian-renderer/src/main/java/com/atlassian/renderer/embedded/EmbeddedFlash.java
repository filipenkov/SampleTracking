package com.atlassian.renderer.embedded;

/**
 * Created by IntelliJ IDEA.
 * User: Jeremy Higgs
 * Date: 22/09/2005
 * Time: 11:43:59
 */

/**
 * Class to support the embedding of Macromedia Flash files into the wiki renderer
 */
public class EmbeddedFlash extends EmbeddedObject
{
    public static String RESOURCE_TYPE = "application/x-shockwave-flash";

    public EmbeddedFlash(String string)
    {
        this(new EmbeddedResourceParser(string));
    }

    public EmbeddedFlash(EmbeddedResourceParser parser)
    {
        super(parser);

        // Add in the default properties if they haven't been defined
        if (!properties.containsKey("classid"))
            properties.put("classid", "clsid:D27CDB6E-AE6D-11cf-96B8-444553540000");
        if (!properties.containsKey("codebase"))
            properties.put("codebase", "https://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0");
        if (!properties.containsKey("pluginspage"))
            properties.put("pluginspage", "https://www.macromedia.com/shockwave/download/index.cgi?P1_Prod_Version=ShockwaveFlash");
        if (!properties.containsKey("type"))
            properties.put("type", "application/x-shockwave-flash");
        if (!properties.containsKey("quality"))
            properties.put("quality", "high");
        if (!properties.containsKey("loop"))
            properties.put("loop", "false");
        if (!properties.containsKey("menu"))
            properties.put("menu", "false");
        if (!properties.containsKey("scale"))
            properties.put("scale", "exactfit");
        if (!properties.containsKey("wmode"))
            properties.put("wmode", "transparent");
    }

    public static boolean matchesType(EmbeddedResourceParser parser)
    {
        return (parser.getType().startsWith(RESOURCE_TYPE));
    }
}
