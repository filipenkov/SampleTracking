package com.atlassian.renderer.embedded;

/**
 * Created by IntelliJ IDEA.
 * User: Jeremy Higgs
 * Date: 23/09/2005
 * Time: 12:41:42
 */

public class EmbeddedWindowsMedia extends EmbeddedObject
{
    public static String RESOURCE_TYPE = "application/x-oleobject";

    public static String FILE_EXT_1 = ".wmv";
    public static String FILE_EXT_2 = ".wma";
    public static String FILE_EXT_3 = ".mpeg";

    public EmbeddedWindowsMedia(String string)
    {
        this(new EmbeddedResourceParser(string));
    }

    public EmbeddedWindowsMedia(EmbeddedResourceParser parser)
    {
        super(parser);

        // Add in the default properties if they haven't been defined
        if (!properties.containsKey("classid"))
            properties.put("classid", "CLSID:22d6f312-b0f6-11d0-94ab-0080c74c7e95");
        if (!properties.containsKey("codebase"))
            properties.put("codebase", "http://activex.microsoft.com/activex/controls/mplayer/en/nsmp2inf.cab#Version=5,1,52,701");
        if (!properties.containsKey("pluginspage"))
            properties.put("pluginspage", "http://microsoft.com/windows/mediaplayer/en/download/");
        if (!properties.containsKey("id"))
            properties.put("id", "mediaPlayer");
        if (!properties.containsKey("name"))
            properties.put("name", "mediaPlayer");
    }

    public static boolean matchesType(EmbeddedResourceParser parser)
    {
        // For some reason, WMV is recognised as "application/octet-stream". So we'll check here for the proper extensions
        return (parser.getType().startsWith(RESOURCE_TYPE)
                ||
                parser.getFilename().endsWith(FILE_EXT_1)
                ||
                parser.getFilename().endsWith(FILE_EXT_2)
                ||
                parser.getFilename().endsWith(FILE_EXT_3));
    }
}
