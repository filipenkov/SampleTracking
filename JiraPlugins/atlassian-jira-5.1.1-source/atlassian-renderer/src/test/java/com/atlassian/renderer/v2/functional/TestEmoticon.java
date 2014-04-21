package com.atlassian.renderer.v2.functional;

import junit.framework.Test;

import java.io.IOException;

public class TestEmoticon extends BaseFunctionalTestRunner
{
    public static Test suite() throws IOException
    {
        return BaseFunctionalTestRunner.makeSuite(new TestEmoticon());
    }
    
    public TestEmoticon() throws IOException
    {
        super("emoticon");
    }
}
