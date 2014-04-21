package com.atlassian.renderer.embedded;

public class UnembeddableObject extends EmbeddedObject
{
    public static String[] UNEMBEDDABLE_TYPES = new String[]
    {
        "application/octet-stream", "text/.*", "message/.*"
    };

    // You should always check isUnembeddable last, because some
    // other resources may be able to be rendered when they're
    // a bad mime-type but a known file extension
    public static boolean matchesType(EmbeddedResourceParser parser)
    {
        for (int i = 0; i < UNEMBEDDABLE_TYPES.length; i++)
        {
            String s = UNEMBEDDABLE_TYPES[i];
            if (parser.getType().matches(s))
                return true;
        }

        return false;
    }

    public UnembeddableObject(String string)
    {
        super(string);
    }

    public UnembeddableObject(EmbeddedResourceParser parser)
    {
        super(parser);
    }
}
