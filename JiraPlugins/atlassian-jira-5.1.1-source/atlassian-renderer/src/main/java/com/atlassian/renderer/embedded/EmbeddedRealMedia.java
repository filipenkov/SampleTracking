package com.atlassian.renderer.embedded;

/**
 * Created by IntelliJ IDEA.
 * User: Jeremy Higgs
 * Date: 23/09/2005
 * Time: 14:31:50
 */

public class EmbeddedRealMedia extends EmbeddedObject
{
    public static String RESOURCE_TYPE = "application/vnd.rn-realmedia";

    public static String FILE_EXT_1 = ".rm";
    public static String FILE_EXT_2 = ".ram";

    public EmbeddedRealMedia(String string)
    {
        this(new EmbeddedResourceParser(string));
    }

    public EmbeddedRealMedia(EmbeddedResourceParser parser)
    {
        super(parser);

        // Add in the default properties if they haven't been defined
        if (!properties.containsKey("classid"))
            properties.put("classid", "clsid:CFCDAA03-8BE4-11cf-B84B-0020AFBBCCFA");
    }

    public static boolean matchesType(EmbeddedResourceParser parser)
    {
        // RealMedia files have some weird content-types, so also check for the file extension here
        return (parser.getType().startsWith(RESOURCE_TYPE)
                ||
                parser.getFilename().endsWith(FILE_EXT_1)
                ||
                parser.getFilename().endsWith(FILE_EXT_2));
    }
}
