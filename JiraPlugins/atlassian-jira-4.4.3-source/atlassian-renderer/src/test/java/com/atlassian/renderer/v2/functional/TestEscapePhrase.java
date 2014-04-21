package com.atlassian.renderer.v2.functional;

import junit.framework.Test;

import java.io.IOException;

public class TestEscapePhrase extends BaseFunctionalTestRunner
{
    public static Test suite() throws IOException
    {
        return BaseFunctionalTestRunner.makeSuite(new TestEscapePhrase());
    }
    
    public TestEscapePhrase() throws IOException
    {
        super("escape-phrase");
    }
}
