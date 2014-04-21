package com.atlassian.renderer.embedded;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;

/**
 *
 *
 */
public class EmbeddedImage extends EmbeddedResource {

    private boolean isThumbnail;
    private static final Set/*<String>*/ VALID_PROPERTIES = createValidProperties();

    /**
     * Derived from http://www.w3.org/TR/html401/struct/objects.html#h-13.2
     */
    private static Set/*<String>*/ createValidProperties()
    {
        Set/*<String>*/ result = new HashSet();
        result.add("align");
        result.add("border");
        result.add("alt");
        result.add("title");
        result.add("longdesc");
        result.add("height");
        result.add("width");
        result.add("src");
        result.add("lang");
        result.add("dir");
        // result.add("style"); // EXCLUDE style because of CONF-9350
        result.add("hspace");
        result.add("vspace");
        result.add("ismap");
        result.add("usemap");
        result.add("id");
        result.add("class");
        return result;
    }

    public EmbeddedImage(String originalText)
    {
        this(new EmbeddedResourceParser(originalText));
    }

    public EmbeddedImage(EmbeddedResourceParser parser) {
        super(parser);

        // filter properties.
        properties = new Properties();
        properties.putAll(parser.getProperties());

        // default properties.
        if (!properties.containsKey("align"))
            properties.put("align", "absmiddle");
        if (!properties.containsKey("border"))
            properties.put("border", "0");

        // filter out 'thumbnail'
        isThumbnail = properties.containsKey("thumbnail");
        if (isThumbnail)
            properties.remove("thumbnail");

        // filter out javascript style properties.
        Enumeration e = properties.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            if (!VALID_PROPERTIES.contains(key.toLowerCase()))
            {
                properties.remove(key);
            }
        }
    }

    public static boolean matchesType(EmbeddedResourceParser parser)
    {
        return (parser.getType().startsWith("image"));
    }

    public boolean isThumbNail() {
        return isThumbnail;
    }

}
