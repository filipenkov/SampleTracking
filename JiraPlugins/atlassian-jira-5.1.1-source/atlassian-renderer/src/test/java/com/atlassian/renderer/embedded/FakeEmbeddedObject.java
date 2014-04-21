package com.atlassian.renderer.embedded;

/**
 * Created by IntelliJ IDEA.
 * User: Jeremy Higgs
 * Date: 15/12/2005
 * Time: 12:24:11
 */

public class FakeEmbeddedObject extends EmbeddedObject
{
    public FakeEmbeddedObject(String string)
    {
        super(string);
    }

    public FakeEmbeddedObject(EmbeddedResourceParser parser)
    {
        super(parser);
    }
}
