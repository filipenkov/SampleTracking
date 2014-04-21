package com.atlassian.renderer.embedded;

/**
 * Created by IntelliJ IDEA.
 * User: Jeremy Higgs
 * Date: 23/09/2005
 * Time: 12:20:37
 */

public class EmbeddedQuicktime extends EmbeddedObject
{
    public static String RESOURCE_TYPE = "video/quicktime";
    public static String FILE_EXT_1 = ".mp4";

    public EmbeddedQuicktime(String string)
    {
        this(new EmbeddedResourceParser(string));
    }

    public EmbeddedQuicktime(EmbeddedResourceParser parser)
    {
        super(parser);

        // Add in the default properties if they haven't been defined
        if (!properties.containsKey("classid"))
            properties.put("classid", "clsid:02BF25D5-8C17-4B23-BC80-D3488ABDDC6B");
        if (!properties.containsKey("codebase"))
            properties.put("codebase", "https://www.apple.com/qtactivex/qtplugin.cab");
        if (!properties.containsKey("pluginspage"))
            properties.put("pluginspage", "https://www.apple.com/quicktime/download/");

        // Quicktime movies won't play unless the dimensions are set... so let's set a reasonable one by default
        if (!properties.containsKey("width"))
            properties.put("width", "480");
        if (!properties.containsKey("height"))
            properties.put("height", "380");
    }

    public static boolean matchesType(EmbeddedResourceParser parser)
    {
        // For some reason, MP4 is recognised as "application/octet-stream". CONF-7034
        return (parser.getType().startsWith(RESOURCE_TYPE)) || parser.getFilename().endsWith(FILE_EXT_1);
    }
}
