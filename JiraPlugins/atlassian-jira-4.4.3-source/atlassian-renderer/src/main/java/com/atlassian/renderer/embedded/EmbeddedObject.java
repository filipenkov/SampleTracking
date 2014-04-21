package com.atlassian.renderer.embedded;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Jeremy Higgs
 * Date: 21/09/2005
 * Time: 17:13:32
 */

/**
 * Class to support the embedding of generic content (such as Flash, Quicktime Video) into wiki markup
 */
public class EmbeddedObject extends EmbeddedResource
{
    public EmbeddedObject(String string)
    {
        this(new EmbeddedResourceParser(string));
    }

    public EmbeddedObject(EmbeddedResourceParser parser)
    {
        super(parser);

        properties = new Properties();
        properties.putAll(parser.getProperties());
    }
}