package com.atlassian.renderer.v2.functional;

import junit.framework.Test;

import java.io.IOException;

public class TestLine extends BaseFunctionalTestRunner
{
    public static Test suite() throws IOException
    {
        return BaseFunctionalTestRunner.makeSuite(new TestLine());
    }
    
    public TestLine() throws IOException
    {
        super("line");
    }
}
